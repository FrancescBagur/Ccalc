/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorccalc;
import org.apache.commons.codec.binary.Base64;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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
                    if(entrada.readLine().trim().equals("CcAlC4Kl1w40m5tAnC978olspA")){
                        //Es una connexió vàlida i podem crear el thread
                        synchronized(this){
                            m.setConnexio(s);
                            ClientConnectat client = new ClientConnectat(s,id,m);
                            Thread t = new Thread(client);
                            t.start();
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
                BufferedReader entrada = new BufferedReader(new InputStreamReader(connexio.getInputStream()));
                while((encodetImage=entrada.readLine()) != null){
                    seguirConnectat = m.tractarMissatge(encodetImage, id);
                    //InetAddress a = this.connexio.getInetAddress();
                }
                m.eliminarUsuari(id);
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
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
                sortida = new DataOutputStream(connexio.get(id-1).getOutputStream());
                sortida.writeBytes(missatge);
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private boolean tractarMissatge(String encodetImage, int id) {
            boolean seguirConectat = false;
            try {
                byte[] b = Base64.decode(encodetImage.getBytes());
                BufferedImage imag=ImageIO.read(new ByteArrayInputStream(b));
		ImageIO.write(imag, "jpg", new File("/var/www/html/ApiCcalc/imatges/","snap.jpg"));
                seguirConectat = true;
            } catch (Base64DecodingException | IOException ex) {
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

        private synchronized boolean comprovarNickNameRepetit(String nick){
            boolean trobat = false;
            int i=0;
            while(i<usuaris.size() && !trobat){
                if(usuaris.get(i).equals(nick.trim()))trobat=true;
                i++;
            }
            return trobat;
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