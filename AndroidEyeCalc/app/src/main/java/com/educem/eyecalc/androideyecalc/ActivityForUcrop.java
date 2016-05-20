package com.educem.eyecalc.androideyecalc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;


public class ActivityForUcrop extends AppCompatActivity {
    //Ip del servidor
    private static String SERVER_ADRESS="";
    //ruta on es guarda el resultat del ucrop, en cachè.
    private Uri finalPhoto;
    //Resultat del UCrop invertit
    private Bitmap bmpInvertit;
    //imatge rebuda del servidor (operacio)
    private Bitmap imgRes;
    //imatge rebuda del servidor (operacio)
    private String ruta;
    //resultat del UCrop en bytes per enviarlo
    private byte[] imgbyte;
    //id de transacció, per el servido
    private int ID=-1;
    //El resultat del Thread
    private String[] res = {""};
    //layout on es mostrara el resultat
    private LinearLayout llres;
    //boto per anar a la primera activity i torna a fer la foto
    private ImageView scanAgain;
    //bolea per saber si hi ha conexio o no
    private Boolean con=true;
    //intent per anar a la activity inicial
    private Intent intTofirstActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_ucrop_no_touch);
        //agafo la ip de les preferencies
        SERVER_ADRESS = getSharedPreferences("IP_CONFIG",MODE_PRIVATE).getString("IP","");
        Log.i("IP", SERVER_ADRESS);
        //creo un intent per tornar a la primera activity
        intTofirstActivity = new Intent(ActivityForUcrop.this, InitialActivity.class);
        //agafo el intent que m'ha obert la activity
        Intent intentResult = getIntent();
        //agafo la uri(photo) del intent
        Uri OriginalImage = intentResult.getData();
        //obro el uCrop
        StartUcrop(OriginalImage);
        //inicialitzo el boto i el layout per tornar a scanejar i li poso l'escoltador per quan el clickin
        llres = (LinearLayout) findViewById(R.id.llResultat2);
        scanAgain = (ImageView) findViewById(R.id.ivfletcha);
        scanAgain.setOnClickListener(new goInitial());
    }
    //classe a que sentra quan fas click a scanAgain
    public class goInitial implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(con) {
                //vaig a ala primera activity per torna a scanejar
                startActivity(intTofirstActivity);
                //tanco la activity
                ActivityForUcrop.this.finish();
            } else ActivityForUcrop.this.recreate();
        }
    }
    //obre la activity del uCrop
    public void StartUcrop(Uri photo){
        finalPhoto = Uri.fromFile(new File(getCacheDir(), "takenPhoto"+SimpleDateFormat.getDateTimeInstance()+".bmp"));
        UCrop.Options opt = new UCrop.Options();
        opt.setFreeStyleCropEnabled(true);
        opt.setToolbarColor(Color.parseColor("#3a5795"));
        opt.setStatusBarColor(Color.BLACK);
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
                //si hi ha internet envio la foto al servidor
                if(isNetworkAvailable(getApplicationContext()))new enviaServerSocket().execute();
                else {
                    acabarEspera();
                    con = false;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(Data);
            if (cropError != null) {
                Toast.makeText(ActivityForUcrop.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ActivityForUcrop.this, "unexpected error", Toast.LENGTH_SHORT).show();
            }
        } else {
            //vaig a ala primera activity per torna a scanejar
            startActivity(intTofirstActivity);
            //tanco la activity
            ActivityForUcrop.this.finish();
        }

    }
    //comproba si hi ha internet
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    //cancela la progress bar i activa el boto
    private void acabarEspera(){
        //Eliminem els elements antics del contenidor
        ProgressBar pb1 = (ProgressBar) findViewById(R.id.pbOpE);
        ProgressBar pb2 = (ProgressBar) findViewById(R.id.pbResE);
        llres.removeView(pb1);
        llres.removeView(pb2);
    }
    //mostra per pantalla el resultat correcte
    private void mostrarResultatCorrecte(String operacio, String resultat){
        //operacio
        ImageView ivOperacio = (ImageView) findViewById(R.id.ivOp);
        ivOperacio.setContentDescription(operacio);
        Bitmap res = BitmapFactory.decodeFile(ruta);
        res.getScaledWidth(new DisplayMetrics().densityDpi);
        res.getScaledHeight(new DisplayMetrics().densityDpi);
        ivOperacio.setImageBitmap(res);
        //ivOperacio.setImageURI(Uri.fromFile(new File(ruta)));
        ivOperacio.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //resultat
        TextView tvResultat = (TextView) findViewById(R.id.tvResultat);
        tvResultat.setText(resultat.replace(";","\n"));
        tvResultat.setGravity(Gravity.CENTER_HORIZONTAL);
        tvResultat.setTextSize(30);
        tvResultat.setTextColor(Color.BLACK);
    }
    //mostra error al calcular
    private void mostrarError() {
        //operacio
        Bitmap resp = BitmapFactory.decodeFile(ruta);
        if(resp!=null) {
            ImageView ivOperacio = (ImageView) findViewById(R.id.ivOp);
            resp.getScaledWidth(new DisplayMetrics().densityDpi);
            resp.getScaledHeight(new DisplayMetrics().densityDpi);
            ivOperacio.setImageBitmap(resp);
        } else {
            ImageView ivOperacio = (ImageView) findViewById(R.id.ivOp);
            llres.removeView(ivOperacio);
        }
        //resultat
        TextView tvError = (TextView) findViewById(R.id.tvResultat);
        tvError.setGravity(Gravity.CENTER_HORIZONTAL);
        if(res[0].equals("ers"))tvError.setText("Error while connecting to the server, try again later.");
        else tvError.setText("Error reading operation, try again.");
        tvError.setTextSize(50);
        tvError.setTextColor(Color.BLACK);
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
        //token identificatiu perque el servidor respongui
        private final String token= "Ccalc";
        //Socket (canal de comunicacio amb el servidor)
        private Socket s;
        //per que laplicacio no es quedi penjada si cau el servidor
        private Boolean serverOFF=false;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //obro el socket, envio el token i espero resposta
                s = new Socket(SERVER_ADRESS,2010);
                s.setSoTimeout(20000);
                enviaMissatge(token);
                escoltaDades();
                //quan tinc la resposta envio la imatge
                enviarImatge();
                //obro un altre socket i envio la ID de transaccio perque m'envii el resultat
                s = new Socket(SERVER_ADRESS,2010);
                s.setSoTimeout(20000);
                enviaMissatge(token + ":" + ID);
                //espero el resultat de la operacio
                escoltaDades();
                if(!res[0].equals("postbuit")) {
                    //envio ok conforme he rebut un resultat
                    enviaMissatge("OK");
                    //espero a rebre una imatge
                    escoltaImatge();
                }
                //tanco el socket
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
                serverOFF=true;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Boolean seguir = false;
            acabarEspera();
            if (!serverOFF) {
                while (!seguir) {
                    if (!res[0].equals(""))
                        seguir = true;
                }
                if (res[1].equals("err")) {
                    mostrarError();
                } else {
                    mostrarResultatCorrecte(res[0], res[1]);
                }
            } else {
                res[0] = "ers";
                mostrarError();
            }
        }
        //enviar token al server
        private void enviaMissatge(String msg){
            try {
                out = new DataOutputStream(s.getOutputStream());
                out.writeBytes(msg+"\n");
            } catch (IOException e) {
                e.printStackTrace();
                serverOFF=true;
            }
        }
        //enviar la imatge al server
        private void enviarImatge(){
            //envio la imatge en bytes i tanco el socket, important perque rebi la imatge correctament.
            try {
                outImg = s.getOutputStream();
                outImg.write(imgbyte);
                outImg.flush();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
                serverOFF=true;
            }
        }
        //quan rep algo del servidor crida a tractarDades i li passa el missatge del servidor
        private void escoltaDades(){
            try {
                //obro canal de comunicació per rebre dades del servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String missatge = in.readLine();
                //si el missatge es null error de conexio amb servidor
                if(missatge == null ) serverOFF=true;
                //si no, quan es rep algo es tracta la informació rebuda amb la funcio tractar dades.
                else tractaDades(missatge);
            } catch (IOException e) {
                e.printStackTrace();
                serverOFF=true;
            }
        }
        //actua en funció de les dades rebudes per el servidor.
        private void tractaDades(String msg){
            String[] dades = msg.trim().split(":");
            if (dades[0].trim().equals("OK")) ID = Integer.valueOf(dades[1]);
            else ActivityForUcrop.this.res = dades;
        }
        //rep una imatge.
        private void escoltaImatge(){
            try {
                int bytesRead;
                int current;
                int filesize=300000;
                ruta = getCacheDir()+"imgserver"+SimpleDateFormat.getDateTimeInstance()+".jpg";
                byte [] mybytearray2  = new byte [filesize];
                InputStream is = s.getInputStream();
                FileOutputStream fos = new FileOutputStream(ruta); // destination path and name of file
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bytesRead = is.read(mybytearray2,0,mybytearray2.length);
                current = bytesRead;
                do {
                    bytesRead = is.read(mybytearray2, current, mybytearray2.length-current);
                    if(bytesRead >= 0) current += bytesRead;
                } while((bytesRead > -1));
                bos.write(mybytearray2);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
                serverOFF=true;
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //vaig a ala primera activity per torna a scanejar
        startActivity(intTofirstActivity);
        //tanco la activity
        ActivityForUcrop.this.finish();
    }
}