/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorccalc;


import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;
import sun.font.Script;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;

/**
 *
 * @author francesc
 */
public class ServidorCcalc {

    private static int id;
    
    public static void main(String[] args) {
        // TODO code application logic here
        id = 0;
        Monitor m = new Monitor();
        EscoltaConnexio ec = new EscoltaConnexio(m);
        Thread t = new Thread(ec);
        t.start();
    }
    
    private static class EscoltaConnexio implements Runnable{
        Monitor m;
        public EscoltaConnexio(Monitor m){
            this.m = m;
        }
        
        @Override
        public synchronized void run() {
            try {
                ServerSocket ss = new ServerSocket(2010);
                while(true){
                    Socket s = ss.accept(); 
                    System.out.println("Ha arribat una connexió\n");
                    //Un cop ha arribat la connexió, vaig a mirar que la cadena siqui correcta
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String token = entrada.readLine().trim();
                    if(token.equals("Ccalc")){
                        //Es una connexió vàlida i podem crear el thread
                            m.setConnexio(s);
                            ClientConnectat client = new ClientConnectat(s,id,m);
                            Thread t = new Thread(client);
                            t.start();
                            System.out.println("S'ha creat el thread");
                            m.enviarMissatge("OK:"+ String.valueOf(id) + "\n",id);
                            m.augmentarConnexio();
                    }else if(token.startsWith("Ccalc:")){
                        //Es una connexió vàlida i espera una respota
                            int idTrans = Integer.valueOf(token.substring(6));
                            m.setConnexio(s);
                            RespostaClient client = new RespostaClient(s,id,idTrans,m);
                            Thread t = new Thread(client);
                            t.start();
                            System.out.println("S'ha creat el thread de resposta");
                            m.augmentarConnexio();
                    }
                }   
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }

    private static class RespostaClient implements Runnable{
        Socket connexio;
        Monitor m;
        Boolean creat;
        File fitxerSortida;
        int id;
        int idTransaccio;

        public RespostaClient(Socket s, int id, int idTrans, Monitor m){
            this.connexio = s;
            this.id = id;
            this.idTransaccio = idTrans;
            this.m = m;
            creat = false;
        }

        @Override
        public void run(){
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

            m.enviarMissatge(resultat,id);
        }
    }


    private static class ClientConnectat implements Runnable{
        Socket connexio;
        String encodetImage;
        Monitor m;
        Boolean seguirConnectat;
        Boolean creat;
        File fitxerSortida;
        String fitxerRebutMobil = "";
        String fitxerConvertitBmp = "";
        int id;

        public ClientConnectat(Socket s, int id, Monitor m){
            this.connexio = s;
            this.id = id;
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
                        System.out.println(bytesRead);
                    } while((bytesRead > -1));
                    System.out.println("sortim del bucle");
                    
                    bos.write(mybytearray2);
                    System.out.println("hem acabat l'escriptura");
                    bos.flush();
                    bos.close();
                    //converteixo la imatge de jpg a bmp
                    convertirImatgeJPGaBMP();
                    //Natejo la imatge, li trec sombres i defectes
                    if(filtrarImatge()==1){
                        System.out.println("Imatge filtrada");
                        if(existeixFitxer("/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out" + String.valueOf(id) + ".inkml")){
                            File f = new File("/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out" + String.valueOf(id) + ".inkml");
                            f.delete();
                        }
                        //Llenço els scripts que executen PoinTransform,autrase i seshat
                        llencarScripts();
                        System.out.println("Scripts llençats");
                        //Un cop llençats els scripts, he de veure si han acabat i ho miraré comprovant
                        //si s'ha creat el fitxer de sortida

                        while(!creat){
                            fitxerSortida = new File("/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out" + String.valueOf(id) + ".inkml");
                            if (fitxerSortida.exists()) {
                                //Els scripts han acabat
                                System.out.println("Els scripts han finalitzat, engegant llibreries matemàtiques");
                                creat = true;
                            }
                        }
                        //Aqui ja ha acabat el seshat, ja podem posar en marxa les llibreries de calcul matemàtic.
                        engegarLibMath();
                        if(existeixFitxer("/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/"+ id + ".txt")) {
                            File f = new File("/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida" + String.valueOf(id) + ".txt");
                            f.delete();
                        }
                    }

                }
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
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
                ProcessBuilder pb = new ProcessBuilder("python","/Ccalc/PythonLibs/mathlibs/main.py",String.valueOf(id));
                Process p = null;
                p = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void llencarScripts(){
            try {
                ProcessBuilder pb = new ProcessBuilder("/Ccalc/PoinTransform/PoinTransform/bin/Debug/PoinTransform", String.valueOf(id));
                Process p = null;
                p = pb.start();
                /*ProcessBuilder pb = new ProcessBuilder("PoinTransform");
                Map<String, String> env = pb.environment();
                env.put("VAR1", String.valueOf(id));
                //env.remove("OTHERVAR");
                pb.directory(new File("../../PoinTransform/PoinTransform/bin/Debug/"));
                Process p = pb.start();*/
                //Process process = Runtime.getRuntime().exec("/Ccalc/PoinTransform/PoinTransform/bin/Debug/PoinTransform " + String.valueOf(id));
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
                ret = new Integer(in.readLine()).intValue();

                long end = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
        
    }
    
    private static class Monitor{
        ArrayList<Socket> connexio;
        ArrayList<String> usuaris;
        public Monitor() {
            connexio = new ArrayList();
            usuaris = new ArrayList();
        }
        
        public synchronized void setConnexio(Socket s){
            this.connexio.add(s);
        }
        
        private void enviarMissatge(String missatge, int id){
            DataOutputStream sortida;
            try {
                sortida = new DataOutputStream(connexio.get(id).getOutputStream());
                sortida.writeBytes(missatge);
                System.out.println("S'ha enviat un missatge al client num "+ id);
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
      
        private synchronized void eliminarUsuari(int id){
            usuaris.remove(id-1);
            connexio.remove(id-1);
        }
        
        private synchronized void augmentarConnexio(){
            //hi ha hagut una nova connexió, augmento la variable global id i aviso tothom
            id++;
        }

    }
}
