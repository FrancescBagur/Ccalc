package servidorccalc;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by francesc on 03/05/16.
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
        String operacio = "";
        try {
            BufferedReader bf =new  BufferedReader(new InputStreamReader(connexio.getInputStream()));
            String strokes = bf.readLine();
            //if(idThread > 14) {
            sendPost sp = new sendPost(strokes, idThread);
            Thread sendp = new Thread(sp);
            sendp.start();
            //}
            creat = false;
            while(!creat){
                fitxerSortida = new File("expresions/exp" + idThread + ".txt");
                if (fitxerSortida.exists()) {
                    //Ja tenim resposta del server
                    System.out.println("Ja tenim resposta del server, engeuem les llibreries matemàtiques i fem la petició de l'imatge latex");
                    creat = true;
                }
            }
            try (BufferedReader br = new BufferedReader(new FileReader("expresions/exp" + idThread + ".txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    operacio += line;
                }
            }
            if(!operacio.isEmpty()) {
                //if(idThread > 14) {
                sendGet s = new sendGet(operacio, idThread);
                Thread t1 = new Thread(s);
                t1.start();
                //}
                //Aqui ja ha acabat el seshat, ja podem posar en marxa les llibreries de calcul matemàtic.
                engegarLibMath();
            } else {

                //PrintWriter writer = new PrintWriter("/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/temp" + idThread + ".txt", "UTF-8");
                PrintWriter writer = new PrintWriter("fitxersSortida/temp" + idThread + ".txt", "UTF-8");
                writer.println("postbuit:err");
                writer.close();
                //File oldfile = new File("/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/temp" + idThread + ".txt");
                //File newfile = new File("/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/" + idThread + ".txt");
                File oldfile = new File("fitxersSortida/temp" + idThread + ".txt");
                File newfile = new File("fitxersSortida/" + idThread + ".txt");
                if (oldfile.renameTo(newfile)) {
                    System.out.println("Fitxer renombrat");
                } else {
                    System.out.println("Ha fallat al renombrar el fitxer");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void engegarLibMath(){
        try {
            ProcessBuilder pb;
            pb = new ProcessBuilder("python2.7","/home/palmendr/Documentos/PerePersonal/Ccalc/PythonLibs/mathlibs/main.py",String.valueOf(idThread));
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
