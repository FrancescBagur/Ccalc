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
//imports per comunicarse amb el servidor
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    //elements per la camara
    Button tkfoto;   //boto que obra la camara
    ImageView takenfoto; //image view que mostra la foto feta
    private static final int CAM_REQUEST = 1313;
    //adreça del nostre server
    private static final String SERVER_ADRESS="80.1.1.2/ApiCcalc/";
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
                Bitmap bmpInvertit = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
                bmpInvertit.setDensity(DisplayMetrics.DENSITY_DEFAULT);
                //mostra la imatge
                takenfoto.setImageBitmap(bmpInvertit);
                //enviar la imatge a la api, cridem a la clase que hem fet i li pasem la imatge i un nom de la imatge.
                new UploadFileToServer(bmp,"PrimeraFotoPalServer");
            } else if (resultCode == RESULT_CANCELED) Toast.makeText(getApplicationContext(), "el usuari ha cancelat la captura de la imatge", Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(), "error en captura la imatge", Toast.LENGTH_LONG).show();
        }
    }
    //classe per enviar la imatge al servidor cridada per el onActivityResult
    private class UploadFileToServer extends AsyncTask<Void, Void, Void> {

        Bitmap image;
        String name;

        public UploadFileToServer(Bitmap img, String name){
            this.image = img;
            this.name = name;

        }
        @Override
        protected Void doInBackground(Void... params) {
            //intanciem un byteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //comprimim la imatge en format JPEG amb cualitat 100 i la guardem al byteArrayOutputStream anterior
            this.image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            //codifiquem la imatge
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
            //creem un arraylist i li afegim les dades que volem enviar al server.
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image",encodedImage));
            dataToSend.add(new BasicNameValuePair("name",this.name));
            //d'aqui cap abaix no tinc molt clar que es exactament el que fa....
            HttpParams HttpRequestParams = getHttpRequestParams();

            //serveix per enviar dades al servidor per post
            HttpClient client = new DefaultHttpClient(HttpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADRESS+"index.php");

            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "caca", Toast.LENGTH_LONG).show();
            }
            return null;
        }
        private HttpParams getHttpRequestParams(){
            HttpParams HttpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(HttpRequestParams, 1000 * 30);
            HttpConnectionParams.setSoTimeout(HttpRequestParams,1000*30);
            return HttpRequestParams;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "imatge pujada al Servidor ;D", Toast.LENGTH_LONG).show();
        }
    }
















}