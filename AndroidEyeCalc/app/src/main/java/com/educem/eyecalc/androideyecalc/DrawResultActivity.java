package com.educem.eyecalc.androideyecalc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DrawResultActivity extends Activity {
    //aqui es guardara la operacio escrita per lusuari en el format correcte.
    String operacio = "";
    //identificador per el servidor
    private int ID=0;
    //aqui es guardara el resultat que envii el servidor.
    private String[] res = {""};
    //layout gefe
    private LinearLayout ll;
    //layout on es mostrara el resultat
    private LinearLayout llres;
    //torna a la plana inicial
    private ImageView scanAgain;
    //boolean per saber si hi ha o no conexio
    private Boolean con = true;
    //intent per obrir la primera activity
    Intent intTofirstActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_result);
        //creo un intent per tornar a la primera activity
        intTofirstActivity = new Intent(DrawResultActivity.this, InitialActivity.class);
        //inicialitzo el boto per tornar al principi
        scanAgain = (ImageView) findViewById(R.id.ivReturn);
        scanAgain.setOnClickListener(new goInitial());
        //inicialitzo el layout pare i la taula i agafo les dades amb que m'han obert
        ll = (LinearLayout) findViewById(R.id.LlBoss);
        llres = (LinearLayout) findViewById(R.id.llResultat);
        Intent res = getIntent();
        String[] strokes = res.getExtras().getStringArray("strokes");
        preparaDades(strokes);
        if (isNetworkAvailable(this))new enviaServerSocket().execute();
        else {
            acabarEspera();
            this.res[0]="ers";
            mostrarError();
            con =false;
        }
    }
    //Comprobo si hi ha internet
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    //classe a que sentra quan fas click a scanAgain
    public class goInitial implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(con) {
                //vaig a ala primera activity per torna a scanejar
                startActivity(intTofirstActivity);
                //tanco la activity
                DrawResultActivity.this.finish();
            } else DrawResultActivity.this.recreate();
        }
    }
    //prepara les dades per enviarles al servidor
    private void preparaDades(String[] strokes){
        operacio = "[";
        for (int i=0; i<strokes.length; i++){
            if(i==strokes.length-1) operacio += strokes[i];
            else operacio += strokes[i]+",";
        }
        operacio += "]";
    }
    //cancela la progress bar i activa el boto
    private void acabarEspera(){
        ProgressBar pb1 = (ProgressBar) findViewById(R.id.progBarOp);
        ProgressBar pb2 = (ProgressBar) findViewById(R.id.progressBarRes);
        llres.removeView(pb1);
        llres.removeView(pb2);
    }
    //mostra per pantalla el resultat correcte
    private void mostrarResultatCorrecte(String operacio, String resultat){
        //operacio
        TextView tvOperacio = (TextView) findViewById(R.id.tvOp);
        tvOperacio.setText(operacio);
        tvOperacio.setGravity(Gravity.CENTER_HORIZONTAL);
        tvOperacio.setTextSize(30);
        tvOperacio.setTextColor(Color.BLACK);
        //resultat
        TextView tvResultat = (TextView) findViewById(R.id.tvRes);
        tvResultat.setText(resultat);
        tvResultat.setGravity(Gravity.CENTER_HORIZONTAL);
        tvResultat.setTextSize(30);
        tvResultat.setTextColor(Color.BLACK);
    }
    //mostra error al calcular
    private void mostrarError() {
        TextView op = (TextView) findViewById(R.id.tvopE);
        TextView re = (TextView) findViewById(R.id.tvresE);
        llres.removeView(op);
        llres.removeView(re);
        TextView tvError = (TextView) findViewById(R.id.tvOp);
        tvError.setGravity(Gravity.CENTER_HORIZONTAL);
        if(res[0].equals("ers"))tvError.setText("Error while connecting to the server, try again later.");
        else tvError.setText("Error reading operation, try again.");
        tvError.setTextSize(50);
        tvError.setTextColor(Color.BLACK);
    }
    //thread per comunicarse amb el servidor
    public class enviaServerSocket extends AsyncTask<Void, Void, Void> {
        //canal de sortida per enviar strings
        DataOutputStream out;
        //Ip del servidor
        private static final String SERVER_ADRESS="172.20.10.12";
        //token identificatiu perque el servidor respongui
        private final String token= "CcalcWriter";
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
                //quan tinc la resposta envio els strokes de la operacio
                enviaMissatge(operacio);
                //tanco el socket
                s.close();
                //obro un altre socket i envio la ID de transaccio perque m'envii el resultat
                s = new Socket(SERVER_ADRESS,2010);
                s.setSoTimeout(20000);
                enviaMissatge("Ccalc" + ":" + ID);
                //espero el resultat de la operacio
                escoltaDades();
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
                out.writeBytes(msg + "\n");
            } catch (IOException e) {
                e.printStackTrace();
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
            Log.i("hola","--"+dades[0]+"--"+dades[1]+"--");
            if (dades[0].trim().equals("OK")) ID = Integer.valueOf(dades[1]);
            else DrawResultActivity.this.res = dades;
        }
    }
    //torna a la activity inicial
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //vaig a ala primera activity per torna a scanejar
        startActivity(intTofirstActivity);
        //tanco la activity
        DrawResultActivity.this.finish();
    }
}
