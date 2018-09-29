/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import Utils.Protocol;



/**
 *
 * @author joseluis
 */
public class ServerThread extends Thread{
    private Socket socket;
    private Protocol protocol;
    private  Table table;
    private int status;

    public ServerThread(int mode ,String name, Socket s) throws IOException{
        super(name);
        this.socket = s;
        s.setSoTimeout(60000);
        this.protocol = new Protocol(this.socket, "Server-"+this.getName()+".log");
        this.table = new Table(mode);
    }
  
    /*
     main() thread
    */
    @Override
    public void run(){
        boolean end = true;
        ArrayList<String> params;
        String str = "void";
        String str2 = "";
        String [] st;
        int id;
        while(end){
            try {
                /*if (this.protocol.avaliable()){ Thread.sleep(2000);}*/
               str = this.protocol.readServer();
               if(str.length() > 4){
                   this.protocol.writeServer("erro "+str);
               }
            } catch (IOException ex) {
                System.out.println("Error de lectura en thread "+this.getName());
            }
            /*if(!str.equals("void")){
                System.out.println("instr readed ");
                System.out.println(this.table.toString());
                System.out.println();
            }*/
            try{
                switch(str){
                    
                    case "strt":
                        if (this.status == 0) { // lista players
                            params = this.protocol.getParams();
                            id = Integer.parseInt(params.get(0));
                            this.protocol.writeServer("ante "+ this.table.getMin_bet()); //chips?
                            this.protocol.writeServer(this.table.stakes());
                            this.status = -1;
                        }
                        else{
                            this.protocol.writeServer("erro error, incorrect flux");
                        }
                        break;
                    case "anok":
                        if (this.status == -1){
                            if(this.table.anok()){
                                this.protocol.writeServer("deal "+this.table.getDealer());
                                str2 = "HAND";
                                st = this.table.getPHand();
                                for(String s: st){
                                    str2 += " "+s;
                                }
                                this.protocol.writeServer(str2);
                                if (this.table.isPdealer()){
                                    //System.out.println("Doing action");
                                    //System.out.println(this.table.toString());
                                    str2 = this.table.getBetAction();
                                    this.protocol.writeServer(str2);
                                    //System.out.println(this.table.toString());
                                }
                                this.status = 2;
                            }
                            else{ this.protocol.writeServer("erro error, no money to play b**ch");}
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        break;
                    case "quit":
                        if (this.status == -1){
                            end = false;
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        break;
                    case "bet_":
                        if(this.status == 2){
                            params = this.protocol.getParams();
                            if(this.table.addPlayerBet(Integer.parseInt(params.get(0)))){
                               // System.out.println("Doing action");
                               // System.out.println(this.table.toString());
                                str2  = this.table.getBetAction();
                                this.protocol.writeServer(str2);
                                this.status = this.table.state();
                                //System.out.println(this.table.toString());
                                if(this.status == -1){this.protocol.writeServer(this.table.stakes());}
                            }
                            else{
                                this.protocol.writeServer("erro error, incorrect amount of betting money");
                            }
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        this.table.toString();
                        break;
                    case "pass":
                        if(this.status == 2){
                            if(this.table.passPlayer()) {
                                if(!this.table.isPdealer()){
                                       // System.out.println("Doing action");
                                       // System.out.println(this.table.toString());
                                        str2  = this.table.getBetAction();
                                        this.protocol.writeServer(str2);
                                       // System.out.println(this.table.toString());
                                }
                            }
                            else{
                                this.protocol.writeServer("erro error, pass is not a valid action"); 
                            }
                        this.status = this.table.state();
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        this.table.toString();
                        break;
                    case "call":
                        if(this.status == 2){ //showdown -stakes?
                                if(!this.table.callPlayer()){this.protocol.writeServer("erro error, incorrect call");}
                                this.status = this.table.state();
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        this.table.toString();
                        break;
                    case "rise":
                        if (this.status == 2){
                            params = this.protocol.getParams();
                            if(this.table.raisePlayer(Integer.parseInt(params.get(0)))){
                               // System.out.println("Doing action");
                               // System.out.println(this.table.toString());
                                str2 = this.table.getBetAction();
                                this.protocol.writeServer(str2);
                                this.status = this.table.state();
                               // System.out.println(this.table.toString());
                            }
                            else{this.protocol.writeServer("erro error, incorrect rise");}
                            if(this.status == -1){this.protocol.writeServer(this.table.stakes());}
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        break;
                    case "draw":
                        if(this.status == 3){
                            params = this.protocol.getParams(); // puede estar vacio si no se desea cambiar
                            st = new String[params.size()-1];
                            for(int i = 1; i < params.size();i++){
                                st[i-1] = params.get(i);
                            }
                            if(!this.table.validCards(st)){this.protocol.writeServer("erro error, not valid cards as a parameters");}
                            else{
                                this.table.drawPlayer(params.size() -1 ,st);
                                str2  = this.table.getDrawAction();
                                this.protocol.writeServer(str2);
                                if(this.table.isPdealer()){
                                    //System.out.println("Doing action");
                                   // System.out.println(this.table.toString());
                                    str2  = this.table.getBetAction();
                                    this.protocol.writeServer(str2);
                                    //System.out.println(this.table.toString());
                                }
                                this.status = 2;
                            }
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        break;
                    case "fold":
                        if(this.status == 2){
                            if(!this.table.playerFold()) {
                                this.protocol.writeServer("erro error, invalid fold");
                            }
                            str2  = this.table.stakes();
                            this.protocol.writeServer(str2);
                            this.status =-1;
                        }
                        else{this.protocol.writeServer("erro error, incorrect flux");}
                        break;
                    case "void":
                        if(this.protocol.checkClientIsOut()){ end = false;}
                        break;
                }
        }
        
        catch (IOException e){
                try {
                    this.protocol.writeServer("erro error, Server cant write in socket and he will maybe disconect ");
                } catch (IOException ex) {
                    end = false;      
                }    
        }
        if(this.status == 4){
                try {
                    this.protocol.writeServer(this.table.showdown());
                    this.protocol.writeServer(this.table.stakes());
                    this.status = -1;
                } catch (IOException ex) {
                    end =true;
                }
        }
     }
        try {
            this.socket.close();
            System.out.println("finished thread");
        } catch (IOException ex) {
            System.out.println("Error Closing Socket");
        }
    }
}
