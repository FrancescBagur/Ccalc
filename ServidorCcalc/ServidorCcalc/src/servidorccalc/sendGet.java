package servidorccalc;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by francesc on 10/05/16.
 */

class sendGet implements Runnable{
    private static final String USER_AGENT = "Mozilla/5.0";
    private String latexEquation;
    private int id;
    private String url = "https://latex.codecogs.com/gif.latex?\\dpi{300}&space;\\huge&space;";
    private String fitxerRebutWeb = "latexImg.gif";

    public sendGet(String latexEquation, int id) {
        this.latexEquation = latexEquation;
        this.id = id;
        this.url += this.latexEquation.replace(" ","%20");
        this.fitxerRebutWeb = String.valueOf(this.id) + this.fitxerRebutWeb;
    }

    @Override
    public void run() {

        try {
            URL obj = null;
            obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            //aqui crec que caldira fer if responseCode == 200, continuar, per evitar que exploti i am seshat nose si ho fas pero igual
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            int bytesRead;
            int current;
            int filesize=300000;
            byte [] mybytearray2  = new byte [filesize];
            InputStream is = con.getInputStream();
            FileOutputStream fos = new FileOutputStream("/Ccalc/ServidorCcalc/ServidorCcalc/latexImages/"+fitxerRebutWeb); // destination path and name of file
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray2,0,mybytearray2.length);
            current = bytesRead;
            do {
                bytesRead = is.read(mybytearray2, current, mybytearray2.length-current);
                if(bytesRead >= 0) current += bytesRead;
                //System.out.println(bytesRead);
            } while((bytesRead > -1));
            System.out.println("Imatge latex rebuda");

            bos.write(mybytearray2);
            System.out.println("Imatge latex guardada");
            bos.flush();
            bos.close();

            File oldName = new File("/Ccalc/ServidorCcalc/ServidorCcalc/latexImages/"+fitxerRebutWeb);
            File newName = new File("/Ccalc/ServidorCcalc/ServidorCcalc/latexImages/latexImage" + String.valueOf(id) + ".gif");
            if(oldName.renameTo(newName)) {
                System.out.println("Imatge renombrada");
            } else {
                System.out.println("Error al renombrar l'imatge latex");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
