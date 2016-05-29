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
            int filesize=300000;
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
                    //Fusionem els punts pròxims per evitar errors d'autotrace
                    fusionarPunts(idThread);
                    String strokes = llegirFitxerSeshat(idThread);
                    //Creo el Thread que farà la petició al server
                    if(idThread>14) {
                        sendPost sp = new sendPost(strokes, idThread);
                        Thread sendp = new Thread(sp);
                        sendp.start();
                    }
                    creat = false;
                    while(!creat){
                        fitxerSortida = new File("expresions/exp" + idThread + ".txt");
                        if (fitxerSortida.exists()) {
                            //Ja tenim resposta del server
                            System.out.println("Ja tenim resposta del server, engeuem les llibreries matemàtiques");
                            creat = true;
                        }
                    }
                    String operacio = "";
                    try (BufferedReader br = new BufferedReader(new FileReader("expresions/exp" + idThread + ".txt"))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            operacio += line;
                        }
                    }
                    if(idThread > 14){
                        sendGet s = new sendGet(operacio,idThread);
                        Thread t1 = new Thread(s);
                        t1.start();
                    }

                    //Aqui ja ha acabat el seshat, ja podem posar en marxa les llibreries de calcul matemàtic.
                    engegarLibMath();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Aquesta funció fusiona els punts que son molt pròxims i estan dividits degut a errors d'autotrace
    private void fusionarPunts(int id){
        Point p1;
        Point p2;
        String aux;
        String [] auxSplitat;
        String strokesFinal = "";
        String ruta = "/Ccalc/ServidorCcalc/ServidorCcalc/seshat/SampleMathExps/exp" + id + ".scgink";
        String strokes = "";
        File f = new File(ruta);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                strokes = line.trim();
            }
            System.out.println(strokes);
            //un cop tenim els strokes en un string ja podem passar a la modificació
            String [] strokesSplitats = strokes.split(":");
            for (int i = 0; i < strokesSplitats.length; i++){
                if(!strokesSplitats[i].contains("]]]") && strokesSplitats[i].contains("]]")){
                    aux = strokesSplitats[i].replace('[',' ').trim();
                    aux = aux.replace(']',' ').trim();
                    auxSplitat = aux.split(",");
                    p1 = new Point(Integer.parseInt(auxSplitat[0]),Integer.parseInt(auxSplitat[1]));
                    aux = strokesSplitats[i+1].replace('[',' ').trim();
                    aux = aux.replace(']',' ').trim();
                    auxSplitat = aux.split(",");
                    p2 = new Point(Integer.parseInt(auxSplitat[0]),Integer.parseInt(auxSplitat[1]));
                    //Es pot anar jugant amb el parametre de la distància de la següent funció per anar corregint errors
                    if(distanciaEntreDosPuntsInferiorA(p1,p2,(float)7)){
                        strokesSplitats[i] = strokesSplitats[i].substring(0,strokesSplitats[i].length()-1);
                        if(strokesSplitats[i+1].contains("[[")){
                            strokesSplitats[i+1] = strokesSplitats[i+1].substring(1,strokesSplitats[i+1].length());
                        }
                    }
                }
                if(i<strokesSplitats.length-1)
                    strokesFinal += strokesSplitats[i] + ",";
                else
                    strokesFinal += strokesSplitats[i];
            }

            PrintWriter writer = new PrintWriter(ruta, "UTF-8");
            writer.println(strokesFinal);
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //A aquesta funció sa li passen dos punts i una distancia y retorna cert si hi ha mes distancia i fals si ni ha menys
    private boolean distanciaEntreDosPuntsInferiorA(Point p1, Point p2, float dist){
        float resultat = (float) Math.sqrt(Math.pow((p2.x - p1.x),2) + Math.pow((p2.y-p1.y),2));
        return (dist > resultat);
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
            ProcessBuilder pb;
            pb = new ProcessBuilder("python2.7","/Ccalc/PythonLibs/mathlibs/main.py",String.valueOf(idThread));
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
            ProcessBuilder pb = new ProcessBuilder("python2.7","../../PythonLibs/SimpleCv/filtradorImatges.py",fitxerConvertitBmp);
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

    class Point{
        int x;
        int y;

        public Point(int x ,int y) {
            this.x = x;
            this.y = y;
        }
    }

}
