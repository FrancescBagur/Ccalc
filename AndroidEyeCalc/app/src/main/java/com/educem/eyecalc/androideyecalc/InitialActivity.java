package com.educem.eyecalc.androideyecalc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class InitialActivity extends AppCompatActivity {
    private static final int CAM_REQUEST = 1313; //codi de peticio per la camara
    private Button SCAN;    //boto que obrira la camara per fer la foto.
    private Button TRACE; //boto que obrira la pagina per escriure amb el dit
    private Intent intentCrop;  //intent que obrira la activity per mostra el resultat.
    private Boolean con = true;
    private Boolean pressed = false; //chivato de si s'ha apretat el logo o no
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //estableix la part visual feta graficament, codi autogenerated.
        setContentView(R.layout.initial_activity_no_touch);
        //estableixo la variable IP de manera que es veu entre activities
        SharedPreferences sp = getSharedPreferences("IP_CONFIG", MODE_PRIVATE);
        if (sp.getString("IP","").equals("")) {
            SharedPreferences.Editor editsp = sp.edit();
            editsp.putString("IP", "172.20.10.2");
            editsp.apply();
        }
        //poso un click listener al logo per poder canviar la ip
        ImageView logo = (ImageView) findViewById(R.id.imageLogo);
        logo.setOnClickListener(new logoClick(sp));
        //inicialitzo el intent que em portara a la pantalla del resultat.
        intentCrop = new Intent(this,ActivityForUcrop.class);
        //associo els botons programatic amb el boto creat visualment per obrir la camara
        TRACE = (Button) findViewById(R.id.btWrite);
        SCAN = (Button) findViewById(R.id.btScan);
        //poso un listener al botons
        TRACE.setOnClickListener(new listenClick());
        SCAN.setOnClickListener(new listenClick());
        if(!isNetworkAvailable(getApplicationContext())){
            SCAN.setText("No internet connection, Reload");
            con=false;
        }
    }
    public class logoClick implements Button.OnClickListener {
        private SharedPreferences.Editor editsp;
        public logoClick (SharedPreferences s){
            editsp = s.edit();
        }
        @Override
        public void onClick(View v) {
            EditText et = (EditText) findViewById(R.id.editIP);
            if (pressed){
                String novaIP = String.valueOf(et.getText()).trim();
                if (!novaIP.isEmpty()) {
                    editsp.putString("IP", novaIP);
                    editsp.apply();
                }
                et.setVisibility(View.INVISIBLE);
                pressed = false;
            } else {
                et.setVisibility(View.VISIBLE);
                pressed = true;
            }
        }
    }
    //Comprobo si hi ha internet
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    //classe a que sentra quan fas click a tkfoto
    public class listenClick implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!con){
                InitialActivity.this.recreate();
            } else {
                if (v.getId() == R.id.btScan) {
                    if(!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        Toast.makeText(getApplicationContext(), "The mobile has no camera", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    //si s'ha superat el if anterior vol dir que el mòvil te càmara i per tant la cridem.
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //envia l'intent amb la foto feta al acticity result. la seguent funcio del codi \/ despres d'aquesta classe
                    startActivityForResult(cameraIntent, CAM_REQUEST);
                }
                else {
                    Intent DrawIntent = new Intent(InitialActivity.this,drawingActivity.class);
                    startActivity(DrawIntent);
                    InitialActivity.this.finish();
                }
            }
        }
    }
    //aqui obrire un altra activity que s'encarregarà de tractar la imatge que s'ha fet.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);
           if (requestCode == CAM_REQUEST) {
               if (resultCode == RESULT_OK) {
                   //agafo la foto que sa fet.
                   Uri photo = Data.getData();
                   //li paso la foto a la segona activity a traves del intent
                   intentCrop.setData(photo);
                   //obro la segona activity
                   startActivity(intentCrop);
                   //tanco aquesta activity
                   InitialActivity.this.finish();
               } else if (resultCode == RESULT_CANCELED) Toast.makeText(getApplicationContext(), "Canceled by User", Toast.LENGTH_LONG).show();
            } else Toast.makeText(getApplicationContext(), "Error taking picture please try again", Toast.LENGTH_LONG).show();
    }
}