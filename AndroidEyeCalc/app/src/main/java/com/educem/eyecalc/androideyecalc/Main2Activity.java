package com.educem.eyecalc.androideyecalc;

import android.app.Dialog;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
//imports per comunicarse amb el servidor
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    //elements per la camara
    Button tkfoto;   //boto que obra la camara
    ImageView takenfoto; //image view que mostra la foto feta
    Bitmap bmpInvertit;//imatge que senviara al server
    private static final int CAM_REQUEST = 1313;
    //adreça del nostre server
    private static final String SERVER_ADRESS="http://192.168.0.158/ApiCcalc/";
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
                //enviar la imatge a la api, cridem a la clase que hem fet i li pasem la imatge i un nom de la imatge.
                new uploadFile().execute();
            } else if (resultCode == RESULT_CANCELED) Toast.makeText(getApplicationContext(), "el usuari ha cancelat la captura de la imatge", Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(), "error en captura la imatge", Toast.LENGTH_LONG).show();
        }
    }
    // 3 classe para subir la foto al server
    public class uploadFile extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //intanciem un byteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //comprimim la imatge en format JPEG amb cualitat 100 i la guardem al byteArrayOutputStream anterior
            bmpInvertit.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            //codifiquem la imatge
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
            //creem un arraylist i li afegim les dades que volem enviar al server.
                /*ArrayList<NameValuePair> dataToSend = new ArrayList<>();
                dataToSend.add(new BasicNameValuePair("image",encodedImage));
                dataToSend.add(new BasicNameValuePair("name","primeraImgPalserver"));*/
            //instanciem la conexio aki perque el finally la pugui desconectar
            try {
                URL url = new URL(SERVER_ADRESS+"index.php?img="+encodedImage);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    //obrim connexio amb el servidor
                    urlConnection.setDoOutput(true);
                    urlConnection.setChunkedStreamingMode(0);
                    //aqui auriam de posar la imatge i despres enviarla al servidor
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    //writeStream(out);

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    //readStream(in);

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
    /* INTENTONAS VARIAS SIN EXITO ALGUNO JODERRRR
    //classe per enviar la imatge al servidor 2
    class ImageUploadTask extends AsyncTask <Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... unused) {

            Toast.makeText(getApplicationContext(), "entro al doinbackground", Toast.LENGTH_LONG).show();
            HttpClient hc = new DefaultHttpClient();
            String message;
            HttpPut p = new HttpPut(SERVER_ADRESS+"index.php");
            JSONObject Jobject = new JSONObject();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmpInvertit.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            byte[] data = bos.toByteArray();

            try {
                Jobject.put("Image", data);
                Toast.makeText(getApplicationContext(), "Jobject preparat correctament", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                message = Jobject.toString();
                Toast.makeText(getApplicationContext(), "preparo be el message"+message, Toast.LENGTH_LONG).show();
                p.setEntity(new StringEntity(message, "UTF8"));
                p.setHeader("Content-type", "application/json");
                Toast.makeText(getApplicationContext(), "just abans del response", Toast.LENGTH_LONG).show();
                HttpResponse resp = hc.execute(p);
                Toast.makeText(getApplicationContext(), "he rebut response", Toast.LENGTH_LONG).show();
                if (resp != null) {
                    if (resp.getStatusLine().getStatusCode() == 204)
                    {
                        Toast.makeText(getApplicationContext(), "entro al response != null", Toast.LENGTH_LONG).show();
                    }
                }
                Log.d("Status line", "" + resp.getStatusLine().getStatusCode());
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "entro al onpst execute", Toast.LENGTH_LONG).show();
            try {
                Dialog dialog = new Dialog(getApplicationContext());
                if (dialog.isShowing())dialog.dismiss();

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"Error"+e,
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }
    classe per enviar la imatge al servidor cridada per el onActivityResult
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
                HttpResponse response = client.execute(post);
                if (response != null) {
                    if (response.getStatusLine().getStatusCode() == 204)
                    {
                        //Toast.makeText(getApplicationContext(), "Response - imatge pujada al Servidor ;D", Toast.LENGTH_LONG).show();
                    }
                }
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
    }*/
}