package servidorccalc;

import java.io.*;
import java.net.*;

/**
 * Created by francesc on 02/05/16.
 */

//HTTP Post request
class sendPost implements Runnable{
    private static final String USER_AGENT = "Mozilla/5.0";
    String url = "http://cat.prhlt.upv.es/mer/eq.php";
    String strokes;
    int id;
    public sendPost(String strokes, int id) {
        this.strokes = strokes;
        this.id = id;
    }

    @Override
    public void run() {
        URL obj = null;
        try {
            obj = new URL(url);

            HttpURLConnection con;
            con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            String encodedStrokes = URLEncoder.encode(strokes, "UTF-8");
            String urlParameters = "strokes="+encodedStrokes;
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
            PrintWriter writer = new PrintWriter("expresions/"+id+"exp.txt", "UTF-8");
            writer.println(response);
            writer.close();
            File oldfile =new File("expresions/"+id+"exp.txt");
            File newfile =new File("expresions/exp"+id+".txt");

            if(oldfile.renameTo(newfile)){
                System.out.println("Fitxer renombrat");
            }else{
                System.out.println("Ha fallat al renombrar el fitxer");
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
