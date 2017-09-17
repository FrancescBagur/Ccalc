package com.educem.eyecalc.androideyecalc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class DrawResultActivity extends Activity {

    //views
    private LinearLayout llres;
    private ImageView scanAgain;

    //flags
    private Boolean con = true;

    //intents
    Intent intToInitialActivity;

    //other data
    private static String SERVER_ADRESS="";
    String operacio = "";
    private int ID=0;
    private String[] res = {""};
    private String ruta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_result);

        //agafo la ip de les preferencies
        SERVER_ADRESS = getSharedPreferences("IP_CONFIG",MODE_PRIVATE).getString("IP","");
        intToInitialActivity = new Intent(DrawResultActivity.this, InitialActivity.class);

        //inicialitzo el boto per tornar al principi
        llres = (LinearLayout) findViewById(R.id.llResultat);
        scanAgain = (ImageView) findViewById(R.id.ivReturn);

        scanAgain.setOnClickListener(new goInitial());

        String[] strokes = getIntent().getExtras().getStringArray("strokes");
        preparaDades(strokes);

        if (isNetworkAvailable(this)){
            new enviaServerSocket().execute();
        }
        else {
            acabarEspera();
            this.res[0]="ers";
            mostrarError();
            con =false;
        }
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public class goInitial implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(con) {
                startActivity(intToInitialActivity);
                DrawResultActivity.this.finish();
            } else {
                DrawResultActivity.this.recreate();
            }
        }
    }

    private void preparaDades(String[] strokes){
        operacio = "[";
        for (int i=0; i<strokes.length; i++){
            if(i==strokes.length-1) operacio += strokes[i];
            else operacio += strokes[i]+",";
        }
        operacio += "]";
    }

    private void acabarEspera(){
        ProgressBar pb1 = (ProgressBar) findViewById(R.id.progBarOp);
        ProgressBar pb2 = (ProgressBar) findViewById(R.id.progressBarRes);
        llres.removeView(pb1);
        llres.removeView(pb2);
    }

    private void mostrarResultatCorrecte(String operacio, String resultat){

        //operacio
        ImageView ivO = (ImageView) findViewById(R.id.ivOpe);
        Bitmap res = BitmapFactory.decodeFile(ruta);
        res.getScaledWidth(new DisplayMetrics().densityDpi);
        res.getScaledHeight(new DisplayMetrics().densityDpi);
        ivO.setImageBitmap(res);
        ivO.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        //resultat
        TextView tvResultat = (TextView) findViewById(R.id.tvRes);
        tvResultat.setText(resultat.replace(";","\n"));
        tvResultat.setGravity(Gravity.CENTER_HORIZONTAL);
        tvResultat.setTextSize(30);
        tvResultat.setTextColor(Color.BLACK);
    }

    private void mostrarError() {
        //operacio
        Bitmap resp = BitmapFactory.decodeFile(ruta);
        if (resp != null) {
            ImageView ivO = (ImageView) findViewById(R.id.ivOpe);
            resp.getScaledWidth(new DisplayMetrics().densityDpi);
            resp.getScaledHeight(new DisplayMetrics().densityDpi);
            ivO.setImageBitmap(resp);
        } else {
            TextView op = (TextView) findViewById(R.id.tvopE);
            llres.removeView(op);
        }

        //resultat
        TextView tvError = (TextView) findViewById(R.id.tvRes);
        tvError.setGravity(Gravity.CENTER_HORIZONTAL);
        if(res[0].equals("ers"))tvError.setText("Error while connecting to the server, try again later.");
        else tvError.setText("Error reading operation, try again.");
        tvError.setTextSize(50);
        tvError.setTextColor(Color.BLACK);
    }

    public class enviaServerSocket extends AsyncTask<Void, Void, Void> {

        DataOutputStream out;
        private final String token= "CcalcWriter";
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

                //quan tinc la resposta envio els strokes de la operacio
                enviaMissatge(operacio);
                s.close();

                //obro un altre socket i envio la ID de transaccio perque m'envii el resultat
                s = new Socket(SERVER_ADRESS,2010);
                s.setSoTimeout(20000);
                enviaMissatge("Ccalc" + ":" + ID);

                //espero el resultat de la operacio
                escoltaDades();

                if (!res[0].equals("postbuit")) {
                    //envio ok conforme he rebut un resultat
                    enviaMissatge("OK");
                    escoltaImatge();
                }
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
                out.writeBytes(msg + "\n");
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
            else DrawResultActivity.this.res = dades;
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
        DrawResultActivity.this.finish();
    }
}
