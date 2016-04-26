package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ResultActivity extends Activity {
    //Barra de progres mentre s'espera el resultat del servidor
    private ProgressBar pb;
    //boto per anar a la primera activity i torna a fer la foto
    private Button scanAgain;
    //id de transacció, perque el servidor sepiga qui soc quan li demani el resultat.
    private int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //agafo la id que em servire per que el servidor sepiga qui soc.
        Intent in = getIntent();
        ID = in.getExtras().getInt("id");
        //Mostro una animacio (processant)
        //Associo la progres Bar visual amb la programatica per poderla treure.
        pb = (ProgressBar) findViewById(R.id.ProgBar);
        //si el ID es -1 significa que algo no ha anat be en la activity anterior (la id no sa inicialitzat), mostro missatge i trec el progress
        if(ID==-1){
            finalizeProgress();
            Toast.makeText(this,"Please try again, an error occurred in the server",Toast.LENGTH_LONG).show();
        } else { //si el id no es -1 demo al servidor el resultat i el mostro per pantalla.
            //obro threads d'escolta al servidor
            new escoltaServerSocket(2010);
        }
        //inicialitzo el boto per tornar a scanejar i li poso l'escoltador per quan el clickin
        scanAgain = (Button) findViewById(R.id.btScanAgain);
        scanAgain.setOnClickListener(new takenfotoClicker());
    }
    //classe a que sentra quan fas click a scanAgain
    public class takenfotoClicker implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            //creo un intent per tornar a la primera activity
            Intent intTofirstActivity = new Intent(ResultActivity.this,InitialActivity.class);
            //vaig a ala primera activity per torna a scanejar
            startActivity(intTofirstActivity);
            //tanco la activity
            ResultActivity.this.finish();
        }
    }
    //aquesta funcio activa els elements de la activity i treu la progress bar
    public void finalizeProgress(){
        pb.setVisibility(View.GONE);
        scanAgain.setEnabled(true);
    }
    //thread per rebre info del server
    public class escoltaServerSocket extends AsyncTask<Void, Void, Void> {
        private Socket s;
        private String SERVER_ADRESS = "";
        public escoltaServerSocket(int port) {
            try {
                s = new Socket(SERVER_ADRESS,port);
                enviarID();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                Log.i("HOLA", "te mueres o no cabron??-------------------------");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        //actua en funció de les dades rebudes per el servidor.
        private void tractaDades(String msg){
            //aqui es es mostraran a la pantalla les dades que envii el servidor.
        }
        private void enviarID(){
            try {
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeBytes(String.valueOf(ID));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
