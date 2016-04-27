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
    //id de transacció, per el servidor
    private int ID=-1;
    //Socket (canal de comunicacio amb el servidor)
    private Socket s;
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
    }
    //obre la activity del uCrop
    public void StartUcrop(Uri photo){
        String timeStamp=generaTMS();
        finalPhoto = Uri.fromFile(new File(getCacheDir(), "takenPhoto"+timeStamp+".bmp"));
        UCrop.Options opt = new UCrop.Options();
        opt.setFreeStyleCropEnabled(true);
        opt.setToolbarColor(Color.parseColor("#3a5795"));
        opt.setStatusBarColor(Color.parseColor("#3a5795"));
        UCrop.of(photo, finalPhoto).withAspectRatio(16, 9).withMaxResultSize(100, 100).withOptions(opt).start(ActivityForUcrop.this);
    }
    //genera un timeStamp per posar al nom de la foto i evitar conflites de fitxers repetits
    public String generaTMS(){
        String tms;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tms = df.format(new Date());
        return tms;
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

                /*//la mostro per pantalla (per comprobar que es la imatge correcte, aquest codi es borrarà)
                ImageView iv = (ImageView) findViewById(R.id.ivMostraRes);
                iv.setImageBitmap(bmpInvertit);
                iv.setVisibility(View.VISIBLE);*/

                //comprobar que hi ha internet
                //si hi ha internet envio la foto al servidor
                    new enviaServerSocket(0,s).execute();     //----------------------------Descomentar Prque envii a servidor. y posar be la IP!!!!
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
    //Thread per enviar info al server 0-> enviar token de validació 1-> enviar la imatge feta.
    public class enviaServerSocket extends AsyncTask<Void, Void, Void> {
        //m'indica l'operacio que tinc de fer.
        int operacio;
        //canal de sortida per enviar el token.
        DataOutputStream out;
        //canal de sortida per enviar la imatge.
        OutputStream outImg;
        //Ip del servidor
        private static final String SERVER_ADRESS="172.20.10.4";
        //token identificatiu perque el servidor respongui
        private final String token= "Ccalc\n";
        //Socket (canal de comunicacio amb el servidor)
        private Socket s;
        //boolea per executar o no el thread que escoltarà el que envii el servidor
        private Boolean executeListener;
        //en el constructor inicialitzo les variables.
        public enviaServerSocket(int operacio, Socket s) {
            this.operacio = operacio;
            this.s = s;
            Log.i("HOLA", "operacio??----------   " + this.operacio + "   ---------------");
        }
        //en funcio del parametre rebut faig una cosa o un altra.
        @Override
        protected Void doInBackground(Void... params) {
            Log.i("HOLA", "operacio??----------   "+this.operacio+"   ---------------");
            switch (this.operacio){
                case 0:
                    try {
                        //obro el socket, envio el token i poso a true el executeListener.
                        s = new Socket(SERVER_ADRESS,2010);
                        out = new DataOutputStream(s.getOutputStream());
                        out.writeBytes(token);
                        executeListener=true;
                        Log.i("HOLA", "token enviat??-------------------------");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    Log.i("HOLA", "entra al case enviar imatge??-------------------------");
                    try {
                        Log.i("HOLA", "comensa enviar imatge??-------------------------");
                        //envio la imatge en bytes i tanco el socket, important perque rebi la imatge correctament.
                        outImg = s.getOutputStream();
                        outImg.write(imgbyte);
                        outImg.flush();
                        s.close();
                        executeListener=false;
                        //un cop s'ha enviat la imatge anem a la activity per mostrar el resultat.
                        Intent inToResult = new Intent(ActivityForUcrop.this, ResultActivity.class);
                        inToResult.putExtra("ID", ID);
                        startActivity(inToResult);
                        ActivityForUcrop.this.finish();
                        Log.i("HOLA", "acaba envia imatge??-------------------------");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //executo un thread per escoltar al servidor per si m'envia informació
            escoltaServerSocket escolta = new escoltaServerSocket(s);
            if(executeListener) escolta.execute();
            else escolta.cancel(true);
            Log.i("HOLA", "muerete enviaserverSocket");
        }
    }
    //thread per rebre info del server
    public class escoltaServerSocket extends AsyncTask <Void, Void, Void> {
        private Socket s;
        public escoltaServerSocket(Socket s) {
            this.s=s;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                //obro canal de comunicació per rebre dades del servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String missatge;
                //quan es rep algo s'entra al while i es tracta la informació rebuda amb la funcio tractar dades.
                while((missatge = in.readLine()) != null) {
                    tractaDades(missatge);
                }
                Log.i("HOLA","te mueres o no cabron??-------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        //actua en funció de les dades rebudes per el servidor.
        private void tractaDades(String msg){
            //si m'ha enviat un OK vol dir que el token es correcte i per tant espera a que li envii la imatge
            //si ok crido a un thread passantlli 1 de parametre perque envii la foto al servidor i mato aquest thread.
            String[] dades = msg.trim().split(":");
            if(dades[0].equals("OK")){
                //guardem la id de la trnasaccio
                ID = Integer.valueOf(dades[1]);
                Log.i("HOLA", "executa enviar imatge??-------------------------");
                new enviaServerSocket(1,s).execute();
            }


        }
    }
}