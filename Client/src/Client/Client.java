/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Client;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import Utils.Protocol;
import java.util.ArrayList;

/**
 *
 * @author alfred
 */
public class Client {
    
    private static String host = "161.116.52.112";
    private int port= 1212;
    private Socket socket;
    private Protocol protocol;
    private String dealer;
    private Table_client table;
    private int mode;
    public static void main(String[] args) throws IOException {
        String host ="127.0.0.1";
        int puerto=1212,mode=0;
        for (int i = 0; i< args.length;i++ ){
            if(args[i].equals("-s")){
                i++;
                host = args[i];
            }
            if(args[i].equals("-p")){
                i++;
                puerto =Integer.parseInt(args[i]);
            }
            if(args[i].equals("-i")){
                i++;
                mode =Integer.parseInt(args[i]);
            }
        }
        Client c = new Client(host,puerto,mode);
        c.iniciated();
    }
    
    public Client() {
        try {
            this.socket = new Socket(this.host, this.port);
            this.socket.setSoTimeout(1000);
            this.protocol = new Protocol(this.socket);
        } catch (IOException ex) {
            System.out.println("Error en los datos de conexion");
            System.exit(0);
        }
    }
    
    /*
    llamar con host h y puerto p.
    */
    public Client(String h, int p, int mode) {
        try {
            this.mode = mode;
            this.socket = new Socket(h, p);
            this.socket.setSoTimeout(1000);
            this.protocol = new Protocol(this.socket);
            this.table = new Table_client(mode);
        } catch (IOException ex) {
            System.out.println("Error en los datos de conexion");
            System.exit(0);
        }
    }
    /*
    A partir de un String realiza el call asociado al server
    */
    private void call(String s) throws IOException{
        try {
            //System.out.println("CALL "+s);
            //aqui distinguir entre ints y char
            this.protocol.writeInstruction(s);
        } 
        catch (IOException ex) {
            System.out.println("Error al enviar");
        }
        /*
        String resp, last;
        resp = this.response();
        last = resp;
        if (last.equals("ante") || last.equals("deal") || (last.equals("hand")&& "1".equals(this.dealer)) || last.equals("fold")) {
            last = this.response();
            resp += " " + last;
        }*/
                
        //return this.response();
    }
    /*
    inicia la interfaz por teclado del cliente
    */
    public void iniciated() throws IOException{
        if(this.mode == 0){
            this.manual();
        }
        else{
            this.Auto();
        }
    }
    public void manual() throws IOException {
        String r, s = "";
        Scanner sc = new Scanner(System.in);
        while(!s.equals("end")){
            System.out.print("//>> ");
            s = sc.nextLine();
            this.call(s);
            r = this.response();
            while(!r.equals("void")){
                //System.out.println(r);
                r = this.response();
            }
        }
    }
    /*
    Analiza la respuesta del servidor
    */
    private String response() throws IOException {
        String s = "void";
        String aux="";
        ArrayList<String> params;
        try {
            s= this.protocol.getInstruction();
            if(s.equals("void")){return s;}
            if (s.length()>4) {
                s = "erro "+s;
                this.protocol.writeInstruction(s);
            }
            else{
                //aux = s;
                params = this.protocol.getParams();
                for(int i = 0;i<params.size();i++){
                    s +=" "+params.get(i);
                }
                /*if ("deal".equals(aux)){ this.dealer=params.get(0);}
                if (aux.equals("ante") || 
                        aux.equals("deal") || 
                        (aux.equals("hand")&& "'1'".equals(this.dealer)) || 
                        aux.equals("fold") ||
                        (aux.equals("drws") && "'1'".equals(this.dealer))){
                    aux = this.response();
                    s += '\n'+ aux;*
                } */
            }
        } catch (IOException ex) {
            s = "erro Error de lectura";
            this.protocol.writeInstruction(s);
            s= "Error";
        }
        return s;
    }
    public void Auto() throws IOException{
        boolean end = true;
        int rounds = 0;
        ArrayList<String> params;
        String str = "void";
        String str2 = "";
        String [] st;
        int id;
        this.protocol.writeInstruction("strt 100");
        while(end){
            try {
                /*if (this.protocol.avaliable()){ Thread.sleep(2000);}*/
               
               if(this.protocol.avaliable()){
                    str = this.protocol.getInstruction();
               }
               else{str = "void";}
               if(str.length() > 4){
                   this.protocol.writeInstruction("erro "+str);
               }
            } catch (IOException ex) {
                //System.out.println("Error de lectura en cliente");
            }
            if(!str.equals("void")){
                //System.out.println("instr readed ");
                //System.out.println("table"+ this.table.state());
            }
            try{
                switch(str){
                    case "ante":
                        params = this.protocol.getParams();
                        this.table.setMin_bet(Integer.parseInt(params.get(0)));
                        break;
                    case "stks":
                        params = this.protocol.getParams();
                        this.table.setMoney_player(Integer.parseInt(params.get(0)));
                        this.table.setMoney_server(Integer.parseInt(params.get(1)));
                        rounds++;
                        if (rounds == 100 || this.table.getMoney_player() < this.table.getMin_bet()) {
                            this.protocol.writeInstruction("quit");
                            end = false;
                        }
                        else{
                            this.protocol.writeInstruction("anok");
                            this.table.anok();
                            this.table.reload();
                        }
                        break;
                    case "deal":
                        params = this.protocol.getParams();
                        this.table.setDealer(Integer.parseInt(""+params.get(0).charAt(1)));
                        break;
                    case "hand":
                        params = this.protocol.getParams();
                        this.table.setHand(params);
                        if(!this.table.isPdealer()){
                            str2  = this.table.getBetAction();
                            this.protocol.writeInstruction(str2);
                        }
                        break;
                    case "drws":
                        params = this.protocol.getParams();
                        params.remove(params.size()-1);
                        String [] swap = new String[params.size()];
                        for(int i = 0;i<params.size();i++){swap[i] = params.get(i);}
                        this.table.drawServer(params.size(),swap);
                        if(!this.table.isPdealer()){
                            str2  = this.table.getBetAction();
                            this.protocol.writeInstruction(str2);
                        }
                        break;
                    case "bet_":
                        params = this.protocol.getParams();
                        this.table.addPlayerBet(Integer.parseInt(params.get(0)));
                        //System.out.println("Doing action");
                        //System.out.println(this.table.toString());
                        str2  = this.table.getBetAction();
                        this.protocol.writeInstruction(str2);
                        break;
                    case "pass":
                        this.table.passPlayer();
                        if(this.table.isPdealer()){
                                str2  = this.table.getBetAction();
                                this.protocol.writeInstruction(str2);
                        }
                        break;
                    case "call":
                        this.table.callPlayer();
                        break;
                    case "rise":
                        params = this.protocol.getParams();
                        this.table.raisePlayer(Integer.parseInt(params.get(0)));
                        str2 = this.table.getBetAction();
                        this.protocol.writeInstruction(str2);
                        break;
                    case "fold":
                        this.table.playerFold();
                        break;
                    case "void":
                        break;
                }
                if(this.table.state() == 3){
                     this.protocol.writeInstruction(this.table.getDrawAction());
                }
        }
        
                catch (IOException e){
                    try {
                        this.protocol.writeInstruction("erro error, Server cant write in socket and he will maybe disconect ");
                    } catch (IOException ex) {
                        end = false;      
                }    
       
        }
      
        
        }
        try {
            this.socket.close();
        } catch (IOException ex) {
            System.out.println("Error Closing Socket");
        }
    }
}
