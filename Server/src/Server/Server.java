/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 *
 * @author alfred
 */

public class Server {
    private final int threads =2;
    private ServerSocket socket;
    private ArrayList <ServerThread> ths;
    private int mode;
    /*
    constructor indicando modo 
    */
    public static void main(String[] args) throws IOException {
        int puerto=1212,mode=1;
        for (int i = 0; i< args.length;i++ ){
            if(args[i].equals("-p")){
                i++;
                puerto =Integer.parseInt(args[i]);
            }
            if(args[i].equals("-i")){
                i++;
                mode =Integer.parseInt(args[i]);
            }
        }
        Server s = new Server(puerto,mode);
        s.StartThreads();
    }
    public Server(int puerto, int mode) throws IOException{
        this.socket = new ServerSocket(puerto);
        this.socket.setSoTimeout(60000);
        this.mode = mode;
        this.ths = new ArrayList();
    }
    /*
    Inicia threads
    */
    public void StartThreads() throws IOException{
        boolean  end = true;
        int i = -1;
        while(end){
           try{
               if(this.ths.size()< this.threads){
                    i++;
                    ServerThread t = new ServerThread(this.mode,Integer.toString(i),this.socket.accept());
                    t.start();
                    this.ths.add(t);
                }
           }
           catch(SocketTimeoutException e){
                for(int j= 0; j<this.ths.size();j++){
                   if(!this.ths.get(j).isAlive()){
                       j--;
                   }
                }
               if(this.ths.isEmpty()){
                    System.out.println("Timeout conection");
                    end = false;
               }
           }
        }
        this.socket.close();
    }
   

}
