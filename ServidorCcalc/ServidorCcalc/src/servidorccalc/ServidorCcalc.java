/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorccalc;


import com.sun.deploy.util.SessionState;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;


/**
 *
 * @author francesc
 */

public class ServidorCcalc {

    private static int id;

    public static void main(String[] args) throws Exception {
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
                        //Es una connexió vàlida que ve d'una camara i podem crear el thread
                        m.setConnexio(s);
                        ClientConnectat client = new ClientConnectat(s,id,m);
                        Thread t = new Thread(client);
                        t.start();
                        System.out.println("S'ha creat el thread de camara");
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
                    }else if(token.equals("CcalcWriter")){
                        //Es una connexió valida que ve d'una tauleta
                        m.setConnexio(s);
                        ClientWriter cw = new ClientWriter(s,id,m);
                        Thread t = new Thread(cw);
                        t.start();
                        System.out.println("S'ha creat un thread de tauleta");
                        m.enviarMissatge("OK:"+ String.valueOf(id) + "\n",id);
                        m.augmentarConnexio();
                    }
                }   
            } catch (IOException ex) {
                Logger.getLogger(ServidorCcalc.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    static class Monitor{
        ArrayList<Socket> connexio;
        ArrayList<String> usuaris;
        public Monitor() {
            connexio = new ArrayList();
            usuaris = new ArrayList();
        }

        public synchronized void setConnexio(Socket s){
            this.connexio.add(s);
        }

        void enviarMissatge(String missatge, int id){
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
