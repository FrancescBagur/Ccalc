package com.educem.eyecalc.androideyecalc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class InitialActivity extends AppCompatActivity {

    //camera
    private static final int CAM_REQUEST = 1313;

    //buttons
    private Button scanButton;
    private Button writeButton;

    //intents
    private Intent intentToUcrop;
    private Intent intentToDrawingActivity;
    private Intent intentToCamera;

    //flags
    private Boolean con = true;
    private Boolean pressed = false;

    //logo image view
    ImageView logo;

    //shared preferences instance
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_activity_no_touch);

        sharedPref = getSharedPreferences("IP_CONFIG", MODE_PRIVATE);
        intentToUcrop = new Intent(this,ActivityForUcrop.class);

        setDefaultIpAddresToSharedPreferences("palmendr.sytes.net");

        getViewsById();

        addClickListeners();

        if(!isNetworkAvailable(getApplicationContext())){
            writeButton.setText("No internet connection, Reload");
            con=false;
        }
    }

    private void addClickListeners(){
        logo.setOnClickListener(new logoClick(sharedPref));
        writeButton.setOnClickListener(new listenClick());
        scanButton.setOnClickListener(new listenClick());
    }

    private void getViewsById(){
        logo = (ImageView) findViewById(R.id.imageLogo);
        writeButton = (Button) findViewById(R.id.btWrite);
        scanButton = (Button) findViewById(R.id.btScan);
    }

    private void setDefaultIpAddresToSharedPreferences(String ipAddr){
        if (sharedPref.getString("IP","").equals("")) {
            SharedPreferences.Editor editsp = sharedPref.edit();
            editsp.putString("IP", ipAddr);
            editsp.apply();
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

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public class listenClick implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!con){

                InitialActivity.this.recreate();

            } else {
                //si m'apreten el botó scan...
                if (v.getId() == R.id.btScan) {

                    if(!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

                        Toast.makeText(getApplicationContext(), "The mobile has no camera", Toast.LENGTH_LONG).show();

                    }else {

                        intentToCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intentToCamera, CAM_REQUEST);
                    }
                }
                //si m'apreten el boto write...
                else {

                    intentToDrawingActivity = new Intent(InitialActivity.this,drawingActivity.class);
                    startActivity(intentToDrawingActivity);
                    InitialActivity.this.finish();

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);

           if (requestCode == CAM_REQUEST) {

               if (resultCode == RESULT_OK) {

                   intentToUcrop.setData(Data.getData()); //Data.getData() == photo
                   startActivity(intentToUcrop);
                   InitialActivity.this.finish();

               }

            } else{

               Toast.makeText(getApplicationContext(), "Error taking picture please try again", Toast.LENGTH_LONG).show();

           }
    }
}