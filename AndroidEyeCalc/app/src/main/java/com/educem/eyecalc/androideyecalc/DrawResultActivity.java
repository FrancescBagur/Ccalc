package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class DrawResultActivity extends Activity {
    //aqui es guardara la operacio escrita per lusuari en el format correcte.
    String operacio = "";
    //identificador per el servidor
    private int ID=0;
    //aqui es guardara el resultat que envii el servidor.
    private String[] res = new String[5];
    //layout on es mostrara el resultat.
    private RelativeLayout rl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_result);
        //inicialitzo el layout pare i agafo les dades amb que m'han obert
        rl = (RelativeLayout) findViewById(R.id.RlBoss);
        Intent res = getIntent();
        String[] strokes = res.getExtras().getStringArray("strokes");
        preparaDades(strokes);
        /*TextView tvRes = (TextView) findViewById(R.id.textView);
        String str="";
        for(int i=0; i<strokes.length; i++) {
           str +=strokes[i];
        }
        tvRes.setText(str);*/
        new enviaServerSocket().execute();
    }
    private void preparaDades(String[] strokes){
        operacio = "[";
        for (int i=0; i<strokes.length; i++){
            operacio += strokes[i];
        }
        operacio += "]";
    }
    //cancela la progress bar i activa el boto
    private void acabarEspera(){
        //Eliminem els elements antics del contenidor
        ProgressBar pb = (ProgressBar) findViewById(R.id.progBar);
        rl.removeView(pb);
    }
    //mostra per pantalla el resultat correcte
    private void mostrarResultatCorrecte(String operacio, String resultat){
        //operacio
        TextView tvOperacio = new TextView(DrawResultActivity.this);
        tvOperacio.setText("Operation   \n"+operacio);
        tvOperacio.setGravity(Gravity.CENTER_HORIZONTAL);
        tvOperacio.setTextSize(50);
        tvOperacio.setTextColor(Color.WHITE);
        tvOperacio.setBackgroundResource(R.drawable.text_views);
        tvOperacio.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        rl.addView(tvOperacio);
        //resultat
        TextView tvResultat = new TextView(DrawResultActivity.this);
        tvResultat.setText("Result   \n"+resultat);
        tvResultat.setGravity(Gravity.CENTER_HORIZONTAL);
        tvResultat.setTextSize(50);
        tvResultat.setTextColor(Color.WHITE);
        tvResultat.setBackgroundResource(R.drawable.text_views);
        tvResultat.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        rl.addView(tvResultat);
    }
    //mostra error al calcular
    private void mostrarError() {
        TextView tvError = new TextView(DrawResultActivity.this);
        tvError.setBackgroundResource(R.drawable.text_views);
        tvError.setGravity(Gravity.CENTER_HORIZONTAL);
        if(res[0].equals("ers"))tvError.setText("Error while connecting to the server, try again later.");
        else tvError.setText("Error reading operation, try again.");
        tvError.setTextSize(50);
        tvError.setTextColor(Color.WHITE);
        tvError.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        rl.addView(tvError);
    }
    public class enviaServerSocket extends AsyncTask<Void, Void, Void> {
        //canal de sortida per enviar strings
        DataOutputStream out;
        //Ip del servidor
        private static final String SERVER_ADRESS="192.168.0.160";
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
                enviaMissatge(token);
                escoltaDades();
                //quan tinc la resposta envio la imatge
                enviaMissatge(operacio);
                //espero resultat
                escoltaDades();
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
            if(!serverOFF) {
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
            if (dades[0].trim().equals("OK")) ID = Integer.valueOf(dades[1]);
            else DrawResultActivity.this.res = dades;
        }
    }
}
