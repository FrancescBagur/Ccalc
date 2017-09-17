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

    //views
    private LinearLayout llres;
    private ImageView scanAgain;

    //flags
    private Boolean con=true;

    //intents
    private Intent intToInitialActivity;

    //other data
    private static String SERVER_ADRESS="";
    private Uri finalPhoto;
    private Bitmap bmpInvertit;
    private String ruta;
    private byte[] imgbyte;
    private int ID=-1;
    private String[] res = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_for_ucrop_no_touch);

        SERVER_ADRESS = getSharedPreferences("IP_CONFIG",MODE_PRIVATE).getString("IP","");
        intToInitialActivity = new Intent(ActivityForUcrop.this, InitialActivity.class);

        //obro el uCrop
        StartUcrop(getIntent().getData());

        llres = (LinearLayout) findViewById(R.id.llResultat2);
        scanAgain = (ImageView) findViewById(R.id.ivfletcha);

        scanAgain.setOnClickListener(new goInitial());
    }

    public class goInitial implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(con) {
                startActivity(intToInitialActivity);
                ActivityForUcrop.this.finish();
            } else {
                ActivityForUcrop.this.recreate();
            }
        }
    }

    public void StartUcrop(Uri photo){

        finalPhoto = Uri.fromFile(new File(getCacheDir(), "takenPhoto"+SimpleDateFormat.getDateTimeInstance()+".bmp"));
        UCrop.Options opt = new UCrop.Options();
        opt.setFreeStyleCropEnabled(true);
        opt.setToolbarColor(Color.parseColor("#3a5795"));
        opt.setStatusBarColor(Color.BLACK);
        UCrop.of(photo, finalPhoto).withAspectRatio(16, 9).withMaxResultSize(100, 100).withOptions(opt).start(ActivityForUcrop.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);

        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP){

            final Uri CropResult = UCrop.getOutput(Data);

            try {

                //converteixo el resultat a BMP
                InputStream is = getContentResolver().openInputStream(CropResult);
                Bitmap bmp = BitmapFactory.decodeStream(is);

                //si hi ha internet envio la foto al servidor
                bmpInvertit = invertBMP(bmp);
                imgbyte = getBytesFromBitmap(bmpInvertit);

                if(isNetworkAvailable(getApplicationContext())){

                    new enviaServerSocket().execute();

                }
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

            startActivity(intToInitialActivity);
            ActivityForUcrop.this.finish();

        }
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void acabarEspera(){
        //Eliminem els elements antics del contenidor
        ProgressBar pb1 = (ProgressBar) findViewById(R.id.pbOpE);
        ProgressBar pb2 = (ProgressBar) findViewById(R.id.pbResE);
        llres.removeView(pb1);
        llres.removeView(pb2);
    }

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

    public Bitmap invertBMP(Bitmap bmp){
        Matrix m = new Matrix();
        m.preScale(1, -1);
        bmpInvertit = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
        bmpInvertit.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return bmpInvertit;
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * 5, bitmap.getHeight() * 5,true);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public class enviaServerSocket extends AsyncTask<Void, Void, Void> {
        DataOutputStream out;
        OutputStream outImg;
        private final String token= "Ccalc";
        private Socket s;
        private Boolean serverOFF=false;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //obro el socket, envio el token i espero resposta
                s = new Socket(SERVER_ADRESS,2010);
                s.setSoTimeout(20000);
                enviaMissatge(token);
                escoltaDades();

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

        private void enviaMissatge(String msg){
            try {
                out = new DataOutputStream(s.getOutputStream());
                out.writeBytes(msg+"\n");
            } catch (IOException e) {
                e.printStackTrace();
                serverOFF=true;
            }
        }

        private void enviarImatge(){
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

        private void escoltaDades(){
            try {

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String missatge = in.readLine();

                if(missatge == null ){
                    serverOFF=true;
                }
                else{
                    tractaDades(missatge);
                }
            } catch (IOException e) {

                e.printStackTrace();
                serverOFF=true;

            }
        }

        private void tractaDades(String msg){
            String[] dades = msg.trim().split(":");
            if (dades[0].trim().equals("OK")) ID = Integer.valueOf(dades[1]);
            else ActivityForUcrop.this.res = dades;
        }

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

        startActivity(intToInitialActivity);
        ActivityForUcrop.this.finish();
    }
}