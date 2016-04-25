package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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


public class Main2Activity extends AppCompatActivity {
    private Uri finalPhoto; //ruta on es guarda el resultat del ucrop, en cachè.
    //variables per la imatge, tractarla i enviarla.
    Bitmap bmpInvertit; //aqui es guardar la imatge invertida en format BMP.
    private byte[] imgbyte; //aqui es guardarà la imatge en bytes.
    private final String token= "Ccalc\n"; //token per enviar al servidor perque validi la conexió.
    private static final String SERVER_ADRESS="172.20.10.9"; //ip del servidor (SOCKETS).
    private Socket s;
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
        //inicialitzo el boto per tornar a scanejar i li poso l'escoltador per quan el clickin
        scanAgain = (Button) findViewById(R.id.btScanAgain);
        scanAgain.setOnClickListener(new takenfotoClicker());
    }
    //classe a que sentra quan fas click a tkfoto
    public class takenfotoClicker implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            //creo un intent per tornar a la primera activity
            Intent intTofirstActivity = new Intent(Main2Activity.this,MainActivity.class);
            //vaig a ala primera activity per torna a scanejar
            startActivity(intTofirstActivity);
            //tanco la activity
            Main2Activity.this.finish();
        }
    }
    //obre la activity del uCrop
    public void StartUcrop(Uri photo){
        finalPhoto = Uri.fromFile(new File(getCacheDir(), "takenPhoto.bmp"));
        UCrop.Options opt = new UCrop.Options();
        opt.setFreeStyleCropEnabled(true);
        opt.setToolbarColor(Color.parseColor("#3a5795"));
        opt.setStatusBarColor(Color.parseColor("#3a5795"));
        UCrop.of(photo, finalPhoto).withAspectRatio(16, 9).withMaxResultSize(100, 100).withOptions(opt).start(Main2Activity.this);
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

                //la mostro per pantalla (per comprobar que es la imatge correcte, aquest codi es borrarà)
                ImageView iv = (ImageView) findViewById(R.id.ivMostraRes);
                iv.setImageBitmap(bmp);
                iv.setVisibility(View.VISIBLE);

                //comprobar que hi ha internet
                //si hi ha internet envio la foto al servidor
                    //new enviaServerSocket(0).execute();     //----------------------------Descomentar Prque envii a servidor. y posar be la IP!!!!
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(Data);
            if (cropError != null) {
                Log.e("Main2Activity", "handleCropError: ", cropError);
                Toast.makeText(Main2Activity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(Main2Activity.this,"unexpected error", Toast.LENGTH_SHORT).show();
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
        int operacio; //m'indica l'operacio que tinc de fer.
        DataOutputStream out; //canal de sortida per enviar el token.
        OutputStream outImg; //canal de sortida per enviar la imatge.
        //en el constructor inicialitzo les variables.
        public enviaServerSocket(int operacio) {
            this.operacio = operacio;
        }
        //en funcio del parametre rebut faig una cosa o un altra.
        protected Void doInBackground(Void... params) {
            switch (operacio){
                case 0:
                    try {
                        //obro el socket
                        s = new Socket(SERVER_ADRESS,2010);
                        out = new DataOutputStream(s.getOutputStream());
                        out.writeBytes(token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        //envio la imatge en bytes i tanco el socket, important perque rebi la imatge correctament.
                        outImg = s.getOutputStream();
                        outImg.write(imgbyte);
                        outImg.flush();
                        s.close();
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
            new escoltaServerSocket(s).execute();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        //actua en funció de les dades rebudes per el servidor.
        private void tractaDades(String msg){
            //si m'ha enviat un OK vol dir que el token es correcte i per tant espera a que li envii la imatge
            //si ok crido a un thread passantlli 1 de parametre perque envii la foto al servidor.
            if(msg.trim().equals("OK")) new enviaServerSocket(1).execute();
        }
    }
}