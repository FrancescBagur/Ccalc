package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

//imports per comunicarse amb el servidor

public class Main2Activity extends AppCompatActivity {
    //elements per la camara
    Button tkfoto;   //boto que obra la camara
    ImageView takenfoto; //image view que mostra la foto feta
    Bitmap bmpInvertit;//imatge que senviara al server
    private static final int CAM_REQUEST = 1313;
    private final String token= "CcAlC4Kl1w40m5tAnC978olspA";
    //adreça del nostre server
    //private static final String SERVER_ADRESS="http://192.168.0.162/ApiCcalc/";
    private static final String SERVER_ADRESS="192.168.0.162";
    private Socket s;
    private String encodedImage;
    private File file;
    private String foto;
    private byte[] imgbyte;
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
                //bmpInvertit = Bitmap.createBitmap(bmpInvertit);
                //enviar la imatge AQUI ES CRIDA LA CLASE PER ENVIAR LA IMATGE "BMPINVERTIT" AL SERVIDOR via API.
                //new uploadFile().execute();
                //preparem la imatge per enviarla com a string
                    //ByteBuffer bb = ByteBuffer.allocate(bmpInvertit.getRowBytes() * bmpInvertit.getHeight());
                    //bmpInvertit.copyPixelsToBuffer(bb);
                    //byte[] imgBytes = bb.array();
                    //encodedImage = Base64.encodeToString(imgBytes,Base64.DEFAULT);
                    //enviar la imatge AQUI ES CRIDA LA CLASE PER ENVIAR LA IMATGE "BMPINVERTIT" AL SERVIDOR via SOCKET.
                /*File imagefile = new File(bmpInvertit);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(imagefile);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Bitmap bm = BitmapFactory.decodeStream(fis);*/
                imgbyte = getBytesFromBitmap(bmpInvertit);
                try {
                    s = new Socket(SERVER_ADRESS,2010); //obro el socket.
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "error new socket"+e.getMessage(), Toast.LENGTH_LONG).show();
                }
                new escoltaServerSocket().execute(); //executo un thread per escoltar dades que envii el servidor
                new enviaServerSocket(1).execute(); //executo un thread per enviar el token al servidor

            } else if (resultCode == RESULT_CANCELED) Toast.makeText(getApplicationContext(), "captura cancelada", Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(), "error en capturar la imatge", Toast.LENGTH_LONG).show();
        }
    }
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }
    //Thread per enviar info al server
    public class enviaServerSocket extends AsyncTask <Void, Void, Void> {
        int operacio;
        DataOutputStream out;
        OutputStream outImg;
        public enviaServerSocket(int operacio) {
            this.operacio = operacio;
            try {
                out = new DataOutputStream(s.getOutputStream()); //obro el canal d'enviament de dades.
                outImg = s.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "error en obrir output enviaServerSocket"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        protected Void doInBackground(Void... params) {
            Boolean ok=false;
            switch (operacio){
                case 0:
                    ok=this.enviarToken();
                    break;
                case 1:
                    ok=this.enviarImatge();
                    break;
            }
            return null;
        }
        private boolean enviarImatge(){
            Boolean ok=false;
            try {
                 //obro el canal d'enviament
                outImg.write(imgbyte);
                ok=true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "error en enviar la imatge - "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return ok;
        }
        /*OutputStream out = s.getOutputStream();    //agafo el canal per enviar dades
                PrintWriter output = new PrintWriter(out); //instancio print writer per enviar informacio
                output.println(token); //envio el token per valida la peticio
                out.flush(); //confirmo l'enviament.
                out.close();
                output.close();
        */
        private Boolean enviarToken(){
            Boolean ok=false;
            try {
                out.writeBytes(token); //envio el token per validar la conexió
                ok=true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "error en enviar el token - "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return ok;
        }
    }
    //thread per rebre info del server
    public class escoltaServerSocket extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String missatge;
                //tractarDades rebudes.
                while((missatge = in.readLine()) != null) {
                    tractaDades(missatge);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "error al rebre info del server -- "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }
        private void tractaDades(String msg){
            Toast.makeText(getApplicationContext(), "el servidor ha respos -- "+msg, Toast.LENGTH_LONG).show();
            if(msg.trim().equals("OK")) new enviaServerSocket(2).execute();
        }
    }
    /*try {
                    Socket s = new Socket(SERVER_ADRESS,2010);
                    OutputStream out = s.getOutputStream();
                    PrintWriter output = new PrintWriter(out);
                    //envio token per saber que la peticio ve de la nostra aplicació
                    output.println("CcAlC4Kl1w40m5tAnC978olspA");
                    out.flush();
                    //escoltar resposta del servidor
                    InputStream in = s.getInputStream();
                    byte[] respostaServer = new byte[0];
                    in.read(respostaServer);
                    //elaborarResposta(String resp = respostaServer.toString());
                    //envio la imatge
                    output.println(encodedImage);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

    //classe para subir la foto al server
    /*public class uploadFile extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //intanciem un byteArrayOutputStream
                //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //comprimim la imatge en format JPEG amb cualitat 100 i la guardem al byteArrayOutputStream anterior
                //bmpInvertit.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            //codifiquem la imatge en base64
                //String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            //Intent de pasar la imatge a bytes sense fer el compress
                ByteBuffer bb = ByteBuffer.allocate(bmpInvertit.getRowBytes() * bmpInvertit.getHeight());
                bmpInvertit.copyPixelsToBuffer(bb);
                byte[] imgBytes = bb.array();
                String encodedImage = Base64.encodeToString(imgBytes,Base64.DEFAULT);
            //creem un arraylist i li afegim les dades que volem enviar al server.
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            dataToSend.add(new BasicNameValuePair("name", "primeraImgPalserver"));
            //instanciem la conexio aki perque el finally la pugui desconectar
            try {
                URL url = new URL(SERVER_ADRESS + "index.php");  //inicialitzem una conexió amb el servidor
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(); //obrim la conexió amb el servidor
                try {
                    urlConnection.setDoOutput(true);    //obrim connexio amb el servidor
                    urlConnection.setChunkedStreamingMode(0); //serveix per evitar alguns errors
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream()); //crea un outputStream per enviar dades al server amb la seguent instruccio
                    out.write(dataToSend.get(0).getValue().getBytes());
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream()); //agafa les possibles dades que pugui retornar el servidor

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
    }*/
}