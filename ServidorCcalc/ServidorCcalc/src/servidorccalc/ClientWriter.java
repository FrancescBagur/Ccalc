package servidorccalc;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by root on 03/05/16.
 */
class ClientWriter implements Runnable{
    Socket connexio;
    ServidorCcalc.Monitor m;
    int idThread;
    Boolean creat;
    File fitxerSortida;

    public ClientWriter(Socket s, int id, ServidorCcalc.Monitor m){
        this.connexio = s;
        this.idThread = id;
        this.m = m;
    }

    @Override
    public void run(){
        try {
            BufferedReader bf =new  BufferedReader(new InputStreamReader(connexio.getInputStream()));
            String strokes = bf.readLine();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
