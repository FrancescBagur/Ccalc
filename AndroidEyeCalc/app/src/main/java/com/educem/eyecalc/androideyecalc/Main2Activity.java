package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;

//imports per comunicarse amb el servidor

public class Main2Activity extends AppCompatActivity {
    //elements per la camara
    Button tkfoto;   //boto que obra la camara
    ImageView takenfoto; //image view que mostra la foto feta
    Bitmap bmpInvertit;//imatge que senviara al server
    private static final int CAM_REQUEST = 1313;
    //adreça del nostre server
    private static final String SERVER_ADRESS="http://172.20.10.4/ApiCcalc/";
    private File file;
    private String foto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //inicialitza i associa elements visuals amb els programatics
        tkfoto = (Button) findViewById(R.id.btTakePhoto);
        takenfoto = (ImageView) findViewById(R.id.imgTaken);
        //escoltador del boto tkfoto que crida a una clase
        tkfoto.setOnClickListener(new takenfotoClicker());
    }
    //classe a que sentra quan fas click a tkfoto
    public class takenfotoClicker implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            //intent que obrira la camara i guardara la foto que es faci
            if(!isDeviceSupporCamera()) {
                Toast.makeText(getApplicationContext(), "el mobil no te camera", Toast.LENGTH_LONG).show();
                finish();
            }
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //envia l'intent amb la foto feta
            startActivityForResult(cameraIntent, CAM_REQUEST);
        }
        private boolean isDeviceSupporCamera(){
            //si el mobil te camera retorna true si no false
            if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) return true;
            else return false;
        }

    }
    //el crida la clase anterior, inverteix la imatge i l'envia la clase upload filetoserver que s'encarregara d'enviarla a la api
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);

        if (requestCode == CAM_REQUEST) {
            if(resultCode == RESULT_OK) {
                Bitmap bmp = (Bitmap) Data.getExtras().get("data");

                //invertir la imatge
                Matrix m = new Matrix();
                m.preScale(1, -1);
                bmpInvertit = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
                bmpInvertit.setDensity(DisplayMetrics.DENSITY_DEFAULT);
                //mostra la imatge
                takenfoto.setImageBitmap(bmpInvertit);
                //enviar la imatge a la api AQUI ES CRIDA LA CLASE PER ENVIAR LA IMATGE "BMPINVERTIT" AL SERVIDOR.
                new uploadFile().execute();
            } else if (resultCode == RESULT_CANCELED) Toast.makeText(getApplicationContext(), "el usuari ha cancelat la captura de la imatge", Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(), "error en captura la imatge", Toast.LENGTH_LONG).show();
        }
    }
    //classe para subir la foto al server
    public class uploadFile extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //intanciem un byteArrayOutputStream
            //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //comprimim la imatge en format JPEG amb cualitat 100 i la guardem al byteArrayOutputStream anterior
            //bmpInvertit.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            //-------------------------------------------------------------------------------------------------
            ByteBuffer bb = ByteBuffer.allocate(bmpInvertit.getRowBytes()*bmpInvertit.getHeight());
            bmpInvertit.copyPixelsToBuffer(bb);
            byte[] imgbytes = bb.array();
            String encodedImage = Base64.encodeToString(imgbytes,Base64.DEFAULT);
            //------------------------------------------------------------------------------------------------------
            //codifiquem la imatge
            //String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            //creem un arraylist i li afegim les dades que volem enviar al server.
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            //dataToSend.add(new BasicNameValuePair("name", "primeraImgPalserver"));
            //instanciem la conexio aki perque el finally la pugui desconectar
            try {
                URL url = new URL(SERVER_ADRESS + "index.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    //obrim connexio amb el servidor
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    //aqui auriam de posar la imatge i despres enviarla al servidor
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                    out.write(dataToSend.get(0).getValue().getBytes());
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}