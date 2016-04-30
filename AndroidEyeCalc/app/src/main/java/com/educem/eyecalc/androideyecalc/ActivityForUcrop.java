package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ActivityForUcrop extends AppCompatActivity {
    //ruta on es guarda el resultat del ucrop, en cachè.
    private Uri finalPhoto;
    //Resultat del UCrop invertit
    private Bitmap bmpInvertit;
    //resultat del UCrop en bytes per enviarlo
    private byte[] imgbyte;
    //id de transacció, per el servido
    private int ID=-1;
    //El resultat del Thread
    private String[] res = {""};
    //El contenidor de la app
    private LinearLayout ll;
    //boto per anar a la primera activity i torna a fer la foto
    private Button scanAgain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //agafo el intent que m'ha obert la activity
        Intent intentResult = getIntent();
        //agafo la uri(photo) del intent
        Uri OriginalImage = intentResult.getData();
        //obro el uCrop
        StartUcrop(OriginalImage);
        //new escoltaServerSocket(2010);

        //inicialitzo el boto per tornar a scanejar i li poso l'escoltador per quan el clickin
        scanAgain = (Button) findViewById(R.id.btScanAgain);
        scanAgain.setOnClickListener(new goInitial());
        this.ll = (LinearLayout) findViewById(R.id.llContenidor);

    }

    //classe a que sentra quan fas click a scanAgain
    public class goInitial implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            //creo un intent per tornar a la primera activity
            Intent intTofirstActivity = new Intent(ActivityForUcrop.this,InitialActivity.class);
            //vaig a ala primera activity per torna a scanejar
            startActivity(intTofirstActivity);
            //tanco la activity
            ActivityForUcrop.this.finish();
        }
    }

    //obre la activity del uCrop
    public void StartUcrop(Uri photo){
        finalPhoto = Uri.fromFile(new File(getCacheDir(), "takenPhoto"+SimpleDateFormat.getDateTimeInstance()+".bmp"));
        UCrop.Options opt = new UCrop.Options();
        opt.setFreeStyleCropEnabled(true);
        opt.setToolbarColor(Color.parseColor("#3a5795"));
        opt.setStatusBarColor(Color.parseColor("#3a5795"));
        UCrop.of(photo, finalPhoto).withAspectRatio(16, 9).withMaxResultSize(100, 100).withOptions(opt).start(ActivityForUcrop.this);
    }
    //agafa el resultat del UCrop
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);

        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP){
            //agafo el resultat del uCrop
            final Uri CropResult = UCrop.getOutput(Data);
            try {
                //converteixo el resultat a BMP
                InputStream is = getContentResolver().openInputStream(CropResult);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                //inverteixo la imatge
                bmpInvertit = invertBMP(bmp);
                //la paso a bytes
                imgbyte = getBytesFromBitmap(bmpInvertit);
                //comprobar que hi ha internet
                //si hi ha internet envio la foto al servidor
                new enviaServerSocket().execute();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(Data);
            if (cropError != null) {
                Log.e("ActivityForUcrop", "handleCropError: ", cropError);
                Toast.makeText(ActivityForUcrop.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ActivityForUcrop.this,"unexpected error", Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void acabarEspera(){
        //Eliminem els elements antics del contenidor
        TextView tv = (TextView) findViewById(R.id.tvMostrarEstat);
        ll.removeView(tv);
        ProgressBar pb = (ProgressBar) findViewById(R.id.progBar);
        ll.removeView(pb);
        Log.i("HOLA", "hem acabat la espera");
    }

    private void mostrarResultatCorrecte(String operacio, String resultat){
        Log.i("HOLA","entro a mostrar resultat correcte");
        //Creem els nous TextView
        TextView tvOperacio = new TextView(ActivityForUcrop.this);
        tvOperacio.setText(operacio);
        ll.addView(tvOperacio);
        TextView tvResultat = new TextView(ActivityForUcrop.this);
        tvOperacio.setText(resultat);
        ll.addView(tvResultat);
    }

    private void mostrarError(){
        Log.i("HOLA","entro a mostrar error");
        TextView tvError = new TextView(ActivityForUcrop.this);
        tvError.setText("Error reading operation, try again.");
        ll.addView(tvError);
    }
    //inverteixo la imatge
    public Bitmap invertBMP(Bitmap bmp){
        Matrix m = new Matrix();
        m.preScale(1, -1);
        bmpInvertit = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
        bmpInvertit.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return bmpInvertit;
    }
    //el crida la funció anterior rep un bitmap i el retorna en bytes i comprimit en JPEG.
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        /*ByteBuffer bb = ByteBuffer.allocate(bmpInvertit.getRowBytes() * bmpInvertit.getHeight());
        bmpInvertit.copyPixelsToBuffer(bb);*/
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 5, bitmap.getHeight() * 5,true);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
    //Thread per enviar info al server 0-> enviar token de validació 1-> enviar la imatge feta.
    public class enviaServerSocket extends AsyncTask<Void, Void, Void> {
        //canal de sortida per enviar el token.
        DataOutputStream out;
        //canal de sortida per enviar la imatge.
        OutputStream outImg;
        //Ip del servidor
        private static final String SERVER_ADRESS="172.20.10.4";
        //token identificatiu perque el servidor respongui
        private final String token= "Ccalc";
        //Socket (canal de comunicacio amb el servidor)
        private Socket s;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //obro el socket, envio el token i espero resposta
                s = new Socket(SERVER_ADRESS,2010);
                enviaMissatge(token);
                escoltaDades();
                //quan tinc la resposta envio la imatge
                enviarImatge();
                //obro un altre socket i envio la ID de transaccio perque m'envii el resultat
                s = new Socket(SERVER_ADRESS,2010);
                enviaMissatge(token + ":"+ID);
                //espero el resultat de la operacio
                escoltaDades();
                //tanco el socket
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Boolean seguir = false;
            while(!seguir){
                if(!res[0].equals(""))
                    seguir = true;
            }
            acabarEspera();
            if(res[1].equals("err")){
                mostrarError();
            }else{
                mostrarResultatCorrecte(res[0],res[1]);
            }
        }

        //enviar token al server
        private void enviaMissatge(String msg){
            try {
                Log.i("HOLA","enntro a enviar missatge");
                out = new DataOutputStream(s.getOutputStream());
                out.writeBytes(msg+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //enviar la imatge al server
        private void enviarImatge(){
            Log.i("HOLA", "entro a enviar imatge");
            //envio la imatge en bytes i tanco el socket, important perque rebi la imatge correctament.
            try {
                outImg = s.getOutputStream();
                outImg.write(imgbyte);
                outImg.flush();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //quan rep algo del servidor crida a tractarDades i li passa el missatge del servidor
        private void escoltaDades(){
            try {
                Log.i("HOLA","entro a escoltar dades");
                //obro canal de comunicació per rebre dades del servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String missatge = in.readLine();
                Log.i("HOLA","he llegit una linia");
                //quan es rep algo es tracta la informació rebuda amb la funcio tractar dades.
                tractaDades(missatge);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //actua en funció de les dades rebudes per el servidor.
        private void tractaDades(String msg){
            Log.i("HOLA", "entro a tractar dades");
            String[] dades = msg.trim().split(":");
            Log.i("HOLA", dades[0] + "-" + dades[1]);
            if(dades[0].trim().equals("OK")){
                Log.i("HOLA","man enviat ok:id");
                ID = Integer.valueOf(dades[1]);
            }else{
                Log.i("HOLA","man enviat resultat");
                ActivityForUcrop.this.res=dades;

            }
        }
    }
}