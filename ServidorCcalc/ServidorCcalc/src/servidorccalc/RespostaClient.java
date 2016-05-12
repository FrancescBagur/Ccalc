package servidorccalc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by francesc on 02/05/16.
 */

class RespostaClient implements Runnable{
    Socket connexio;
    ServidorCcalc.Monitor m;
    Boolean creat;
    File fitxerSortida;
    File imatgeLatex;
    int idThread;
    int idTransaccio;

    public RespostaClient(Socket s, int id, int idTrans, ServidorCcalc.Monitor m){
        this.connexio = s;
        this.idThread = id;
        this.idTransaccio = idTrans;
        this.m = m;
        creat = false;
    }

    @Override
    public void run(){
        System.out.println("La id de transaccio val " + idTransaccio);
        while(!creat){
            fitxerSortida = new File("/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/" + String.valueOf(idTransaccio) + ".txt");
            if (fitxerSortida.exists()) {
                //Els scripts han acabat
                System.out.println("Ja hi ha el fitxer de sortida");
                creat = true;
            }
        }
        String resultat = "";
        try (BufferedReader br = new BufferedReader(new FileReader(fitxerSortida))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                resultat += line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("La id del thread es " + idThread);
        System.out.println("El resultat es: " + resultat);

        //Envio el missatge amb el resultat
        m.enviarMissatge(resultat+"\n",idThread);
        //Espero rebre el missatge de ok
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(connexio.getInputStream()));
            String missatge = in.readLine();
            if(missatge.trim().equals("OK")){
                //El client ja ha rebut el resultat, passo a enviar-li la imatge
                try {
                    enviarImatge();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        creat = false;
        while(!creat) {
            imatgeLatex = new File("/Ccalc/ServidorCcalc/ServidorCcalc/latexImages/latexImage" + String.valueOf(idTransaccio) + ".gif");
            if (imatgeLatex.exists()) {
                //Els scripts han acabat
                System.out.println("Ja hi ha la imatge latex guardada i la podem enviar");
                creat = true;
            }
        }

        //borrarFitxersAntics(idTransaccio);
    }

    private void borrarFitxersAntics(int idTran){
        String[] fitxerBorrar = {"/imatges/rebut"+idTran+".jpg",
                "/Ccalc/ServidorCcalc/ServidorCcalc/imatges/render"+idTran+".bmp",
                "/Ccalc/ServidorCcalc/ServidorCcalc/fitxerSortida"+idTran+".txt",
                "/Ccalc/ServidorCcalc/ServidorCcalc/seshat/SampleMathExps/exp"+idTran+".scgink",
                "/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out"+idTran+".inkml",
                "/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out"+idTran+".dot",
                "/Ccalc/ServidorCcalc/ServidorCcalc/seshat/render"+idTran+".pmg",
                "/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/"+idTran+".txt",
                "/Ccalc/ServidorCcalc/ServidorCcalc/expresions/exp"+idTran+".txt"
        };
        for (int i=0; i<fitxerBorrar.length; i++){
            if(existeixFitxer(fitxerBorrar[i])){
                File f = new File(fitxerBorrar[i]);
                f.delete();
            }
        }
        System.out.printf("Fitxers anitics Eliminats");
    }

    private Boolean existeixFitxer(String path){
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }else
            return false;
    }

    private void enviarImatge() throws IOException, InterruptedException {
        OutputStream outputStream = connexio.getOutputStream();

        BufferedImage image = ImageIO.read(new File("/Ccalc/ServidorCcalc/ServidorCcalc/latexImages/latexImage" + String.valueOf(idTransaccio) + ".gif"));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "gif", byteArrayOutputStream);

        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
        outputStream.write(size);
        outputStream.write(byteArrayOutputStream.toByteArray());
        outputStream.flush();
        System.out.println("Flushed: " + System.currentTimeMillis());

        Thread.sleep(120000);
        System.out.println("Closing: " + System.currentTimeMillis());
        connexio.close();
    }
}