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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;

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
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(2010);
                while(true){
                    Socket s = ss.accept(); 
                    System.out.println("Ha arribat una connexió\n");
                    //Un cop ha arribat la connexió, vaig a mirar que la cadena siqui correcta
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    if(entrada.readLine().trim().equals("Ccalc")){
                        //Es una connexió vàlida i podem crear el thread
                        synchronized(this){
                            m.setConnexio(s);
                            ClientConnectat client = new ClientConnectat(s,id,m);
                            Thread t = new Thread(client);
                            t.start();
                            System.out.println("S'ha creat el thread");
                            m.enviarMissatge("OK\n", id);
                            m.augmentarConnexio();
                        }
                    }
                }   
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    private static class ClientConnectat implements Runnable{
        Socket connexio;
        String encodetImage;
        Monitor m;
        Boolean seguirConnectat;
        int id;
        public ClientConnectat(Socket s, int id, Monitor m){
            this.connexio = s;
            this.id = id;
            this.m = m;
            seguirConnectat = true;
        }
        
        @Override
        public void run(){
            try {
                /*InputStream in = client.getInputStream();
                line = null;
                in.read(line);

                    Log.d("ServerActivity", line.toString());
                    bitmap = BitmapFactory.decodeByteArray(line , 0, line.length);*/
                int bytesRead;
                int current;
                int filesize=65383; 
                byte [] mybytearray2  = new byte [filesize];
                InputStream is = connexio.getInputStream();
                //FileOutputStream fos = new FileOutputStream("/storage/sdcard0/Pictures/Screenshots/");
                try (FileOutputStream fos = new FileOutputStream("/imatges/prova.jpg"); // destination path and name of file
                 //FileOutputStream fos = new FileOutputStream("/storage/sdcard0/Pictures/Screenshots/");
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
                    //Executo un programa Python que s'encarrega de passar un filtre a la imatge de tal manera
                    //que eliminem sombres i defectes

                    ProcessBuilder pb = new ProcessBuilder("python","../../PythonLibstest1.py");
                    Process p = pb.start();

                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    int ret = new Integer(in.readLine()).intValue();
                    System.out.println(ret);
                    long end = System.currentTimeMillis();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }          
        }

        private void convertirImatgeJPGaBMP(){
            try {
                File input = new File("/imatges/prova.jpg");
                //Llegeixo el fitxer a un buffered image
                BufferedImage image = null;
                image = ImageIO.read(input);
                //Creo el fitxer de sortida
                File output = new File("/Ccalc/PoinTransform/PoinTransform/bin/Debug/autotrace-0.31.1/render.bmp");
                //Escric el jpg amb bmp
                ImageIO.write(image, "bmp", output);
            } catch (IOException e) {
                e.printStackTrace();
            }

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

        private boolean tractarMissatge(String encodetImage, int id) {
            boolean seguirConectat = false;
            try {           
                System.out.println(encodetImage);
                System.out.println("guardant imatge");
                byte[] b = Base64.decodeBase64(encodetImage);
                InputStream in = new ByteArrayInputStream(b);
                BufferedImage imag=ImageIO.read(in);
                System.out.println(imag);
		            ImageIO.write(imag, "bmp", new File("/imatges/","snap.bmp"));
                seguirConectat = true;
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }      
            return seguirConectat;
        }
        
        private synchronized void enviarLlista(int id){
            DataOutputStream sortida;
            try {
            sortida = new DataOutputStream(connexio.get(id-1).getOutputStream());
                sortida.writeBytes("Llista d'usuaris:\n");
                for (int i = 0; i < connexio.size(); i++) { 
                    sortida.writeBytes(usuaris.get(i)+"\n");
                }
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
        
        private synchronized void enviarMissatgeATothom(String missatge){
            DataOutputStream sortida;
            try {
                for (int i = 0; i < connexio.size(); i++) {
                    sortida = new DataOutputStream(connexio.get(i).getOutputStream());
                    sortida.writeBytes(missatge);
                }
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
