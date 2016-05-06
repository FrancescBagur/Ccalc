package servidorccalc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by francesc on 02/05/16.
 */

class ClientConnectat implements Runnable{
    Socket connexio;
    String encodetImage;
    ServidorCcalc.Monitor m;
    Boolean seguirConnectat;
    Boolean creat;
    File fitxerSortida;
    String fitxerRebutMobil = "";
    String fitxerConvertitBmp = "";
    int idThread;

    public ClientConnectat(Socket s, int id, ServidorCcalc.Monitor m){
        this.connexio = s;
        this.idThread = id;
        this.m = m;
        seguirConnectat = true;
        creat = false;
        fitxerRebutMobil = "rebut" + String.valueOf(id) + ".jpg";
        fitxerConvertitBmp = "render" + String.valueOf(id) + ".bmp";
    }

    @Override
    public void run(){
        try {
            int bytesRead;
            int current;
            int filesize=65383;
            byte [] mybytearray2  = new byte [filesize];
            InputStream is = connexio.getInputStream();
            try (FileOutputStream fos = new FileOutputStream("/imatges/"+fitxerRebutMobil); // destination path and name of file
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bytesRead = is.read(mybytearray2,0,mybytearray2.length);
                current = bytesRead;
                do {
                    bytesRead = is.read(mybytearray2, current, mybytearray2.length-current);
                    if(bytesRead >= 0) current += bytesRead;
                    //System.out.println(bytesRead);
                } while((bytesRead > -1));
                System.out.println("Imatge rebuda");

                bos.write(mybytearray2);
                System.out.println("hem acabat l'escriptura");
                bos.flush();
                bos.close();
                //converteixo la imatge de jpg a bmp
                convertirImatgeJPGaBMP();
                //Natejo la imatge, li trec sombres i defectes
                if(filtrarImatge()==1){
                    System.out.println("Imatge filtrada");
                    if(existeixFitxer("/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out" + String.valueOf(idThread) + ".inkml")){
                        File f = new File("/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out" + String.valueOf(idThread) + ".inkml");
                        f.delete();
                        System.out.println("S'ha eliminat un fitxer de sortida antic de seshat\n");
                    }
                    //Llenço els scripts que executen PoinTransform i autotrace, que generaran l'entrada de seshat
                    llencarScripts();
                    System.out.println("Scripts llençats");
                    //Un cop llençats els scripts, he de veure si han acabat i ho miraré comprovant
                    //si s'ha creat el fitxer de sortida

                    while(!creat){
                        fitxerSortida = new File("/Ccalc/ServidorCcalc/ServidorCcalc/seshat/SampleMathExps/exp" + String.valueOf(idThread) + ".scgink");
                        if (fitxerSortida.exists()) {
                            //Els scripts han acabat
                            System.out.println("Els scripts han finalitzat, llencem la petició  a seshat");
                            creat = true;
                        }
                    }
                    String strokes = llegirFitxerSeshat(idThread);
                    //Creo el Thread que farà la petició al server
                    sendPost sp = new sendPost(strokes,idThread);
                    Thread sendp = new Thread(sp);
                    sendp.start();
                    creat = false;
                    while(!creat){
                        fitxerSortida = new File("expresions/exp" + idThread + ".txt");
                        if (fitxerSortida.exists()) {
                            //Ja tenim resposta del server
                            System.out.println("Ja tenim resposta del server, engeuem les llibreries matemàtiques");
                            creat = true;
                        }
                    }
                    //Aqui ja ha acabat el seshat, ja podem posar en marxa les llibreries de calcul matemàtic.
                    engegarLibMath();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String llegirFitxerSeshat(int id){
        String ruta = "/Ccalc/ServidorCcalc/ServidorCcalc/seshat/SampleMathExps/exp" + id + ".scgink";
        String strokes = "";
        File f = new File(ruta);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                strokes = line.trim();
            }
            return strokes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Boolean existeixFitxer(String path){
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {
            return true;
        }else
            return false;
    }

    private void engegarLibMath(){
        try {
            ProcessBuilder pb = new ProcessBuilder("python","/Ccalc/PythonLibs/mathlibs/main.py",String.valueOf(idThread));
            Process p = null;
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void llencarScripts(){
        try {
            ProcessBuilder pb = new ProcessBuilder("/Ccalc/PoinTransform/PoinTransform/bin/Debug/PoinTransform", String.valueOf(idThread));
            Process p = null;
            p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String missatge;
            while((missatge = in.readLine() )!= null){
                System.out.println(missatge + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertirImatgeJPGaBMP(){
        try {
            File input = new File("/imatges/" + fitxerRebutMobil);
            //Llegeixo el fitxer a un buffered image
            BufferedImage image = null;
            image = ImageIO.read(input);
            //Creo el fitxer de sortida segons la id de la transaccio
            File output = new File("/Ccalc/ServidorCcalc/ServidorCcalc/imatges/"+ fitxerConvertitBmp);
            //Escric el jpg amb bmp
            ImageIO.write(image, "bmp", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int filtrarImatge(){
        //Executo un programa Python que s'encarrega de passar un filtre a la imatge de tal manera
        //que eliminem sombres i defectes
        int ret = 0;
        try {
            ProcessBuilder pb = new ProcessBuilder("python","../../PythonLibs/SimpleCv/filtradorImatges.py",fitxerConvertitBmp);
            Process p = null;
            p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            ret = new Integer(in.readLine());
            long end = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
