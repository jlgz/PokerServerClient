/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 *
 * @author joseluis
 */
public class Protocol {
    private ComUtils data;
    private ComUtils log;
    private int cardsExpected;
    private ArrayList<String> params;
    private int voids;
    private char checkChar= ' ';
    private boolean check = false;
    
    public Protocol(Socket s) throws IOException{
        this.params =new ArrayList();
        this.data = new ComUtils(s);
        this.cardsExpected = 0;
    }
    public Protocol(Socket s,String log) throws IOException{
        this.params =new ArrayList();
        this.cardsExpected = 0;
        this.data = new ComUtils(s);
        this.log = new ComUtils(log);
        this.voids = 0;
        
    }
    public ArrayList <String> getParams(){
        return this.params;
    }
    public boolean checkClientIsOut(){
        if(this.voids <0){
            try{
                this.checkChar =(char) this.data.readByte();
                this.check = true;
            } catch (IOException ex) {
                
                return true;
            }
        }
        return false;
    }
    public void writeInstruction(String s) throws IOException{
        s = s.toLowerCase();
        String [] str = s.split(" "); 
        String str2= str[0].toUpperCase();
        s = "";
        if (str[0].equals("strt") || str[0].equals("ante")|| str[0].equals("stks")||str[0].equals("bet_") || str[0].equals("rise")){
            //System.out.println(str[0]);
            this.data.write_string(str2, str2.length());
            for (int i = 1;i<str.length;i++){
                  try{
                      this.data.write_string(" ", 1);
                      this.data.write_int32(Integer.parseInt(str[i]));
                  }
                  catch (Exception e){
                      this.data.write_string(" " +str[i],str[i].length()+1);
                  }
            }
        }
        else if(str[0].equals("erro")){
            this.data.write_string(str2+" ", str2.length()+1);
            for (int i = 2;i<str.length;i++){
                    s +=str[i]+" ";
            }
            this.data.write_string_variable(2, s);
        }
        else{
            if(str[0].equals("draw")){
                try{
                    this.cardsExpected = Integer.parseInt(""+str[1].charAt(1));
                }
                catch(Exception e){this.cardsExpected = 0;}
            }
            //System.out.println(str[0]);
            this.data.write_string(str2, str2.length());
            for (int i = 1;i<str.length;i++){
                    s +=" "+str[i];
            }
            if (s.length() != 0){  this.data.write_string(s,s.length());}
        }
    }
    public void writeTest(String s) throws IOException{
        String str = s.toLowerCase();
        String aux = "";
        int k,h=0;
        int i = str.indexOf("draw");
        if (i != -1){
            try{
              this.cardsExpected = Integer.parseInt(""+str.charAt(i + 5));
            }
            catch(Exception e){this.cardsExpected = 0;}
        }
        i = str.indexOf("erro");
        if (i != -1){
            this.data.write_string_variable(2, s.substring(i+5));
            return;
        }
        i = str.indexOf("(int)");
        int j = 0;
        while ( i != -1){
            this.data.write_string(s.substring(j, i),i - j);
            j = i +6;
            k = Integer.parseInt(""+s.charAt(j-1));
            for (h = 0;h<k;h++){
                aux +=s.charAt(j+h);
            }
            this.data.write_int32(Integer.parseInt(aux));
            if (j+h == str.length()){break;}
            i = str.indexOf("(int)",j+h);
        }
        if (j+h< str.length()){this.data.write_string(s.substring(j+h),s.length()-j-h);}
    }
    
    public void writeServer(String string) throws IOException {
        this.writeInstruction(string);
        string = "S: "+string+'\n';
        this.log.write_string(string, string.length());
    }
    public String readServer() throws IOException {
        String s = "void";
        if (this.avaliable()){s=this.getInstruction();}
        else{
            this.voids += 1;
            return "void";
        }
        String str = s;
        if(s.length()>4){
            str = "garbage instruction";
            str = "C: "+str+'\n';
            this.log.write_string(str, str.length());
            this.voids = 0;
        }
        else if(!s.equals("void")){
            
            for(int i = 0 ; i< this.params.size();i++){
                str += " "+this.params.get(i);
            }
            str = "C: "+str+'\n';
            this.log.write_string(str, str.length());
            this.voids = 0;
        }
        return s.toLowerCase();
    }
    public boolean avaliable() throws IOException{
        if(check){return this.data.available()  >=3;}
        else{return this.data.available()  >=4;}
    }
     public String getInstruction() throws IOException {
        char s = ' ';
        int entero;
        this.params = new ArrayList();
        String str;
        try{
            if(this.check){
                str = this.checkChar+this.data.readString(3);
                this.voids = 0;
                this.check = false;
            }
            else{str = this.data.readString(4);}
        }
        catch(SocketTimeoutException e){
            return "Error, TimeOut while the command was readed";
        }     
        str = str.toLowerCase();
        try{
            switch(str){
                case "strt":
                    if((char)this.data.readByte() != ' '){return "Error, expected space char after strt";}
                    entero = this.data.read_int32();
                    s = this.data.SpaceFilter();
                    if(s == ' '){
                        this.params.add(Integer.toString(entero));
                        return "strt";
                    }
                    else{return "Error, a non-space char was found after id";}
                case "ante":
                    if((char)this.data.readByte() != ' '){return "Error, expected space char after ante";}
                    entero = this.data.read_int32();
                    this.params.add(Integer.toString(entero));
                    return "ante";
                case "stks":
                    if((char)this.data.readByte() != ' '){return "Error, expected space char after stks";}
                    entero = this.data.read_int32();
                    if((char)this.data.readByte() != ' '){return "Error, expected space char after first integer";}
                    this.params.add(Integer.toString(entero));
                    entero = this.data.read_int32();
                    s = this.data.SpaceFilter();
                    if(s == ' '){
                        this.params.add(Integer.toString(entero));
                        return "stks";
                    }
                    else{return "Error, a non-space char was found after second integer";}
                case "anok":
                    s = this.data.SpaceFilter();
                    if (s == ' '){return "anok";}
                    else{return "Error, didnt expect any parameter in ANTEOK instruction";}
                case "quit":
                    s = this.data.SpaceFilter();
                    if (s == ' '){return "quit";}
                    else{return "Error, didnt expect any parameter in QUIT instruction";}
                case "deal":
                    if((char)this.data.readByte() != ' '){return "Error, expected space char after deal";}
                    //if((char)this.data.readByte() != '\''){ return "Error, expected \'";}
                    s = (char)this.data.readByte();
                    if(s!= '0' && s!= '1'){ return "Error, expected '0' | '1'";}
                    str = "'"+s;
                    //if((char)this.data.readByte() != '\''){ return "Error, expected \'";}
                    this.params.add(str+"'");
                    return "deal";
                case "hand":
                    entero = this.data.available();
                    for(int i=0; i< 5; i++){
                        if ((char)this.data.readByte() != ' '){ return "Error, expected space char before any card parameter" + " " + entero;}
                        str = this.data.readString(2);
                        // CASE OF 10
                        if (str.equals("10")) {
                            str += (char) this.data.readByte();
                        }
                        this.params.add(str);//tratarlo superiormente
                    }
                    return "hand";
                case "pass":
                    return "pass";
                case "bet_":
                    if((char)this.data.readByte() != ' '){return "Error, expected space char after bet_";}
                    entero = this.data.read_int32();
                    this.params.add(Integer.toString(entero));
                    return "bet_";
                case "call":
                    return "call";
                case "fold":
                    return "fold";
                case "rise":
                    if((char) this.data.readByte() != ' '){return "Error, expected space char after rise";}
                    entero = this.data.read_int32();
                    this.params.add(Integer.toString(entero));
                    return "rise";
                case "draw":
                    if ((char) this.data.readByte() != ' '){return "Error, a non-space char was found before a card parameter";}
                    //if((char) this.data.readByte() != '\''){return "Error, expected \' ex: '0'";}
                    s = (char) this.data.readByte();
                    try{
                        entero = Integer.parseInt(""+s);
                        //if((char) this.data.readByte() != '\''){return "Error, expected \' ex:'0'";}
                        if(entero <= 5 && entero >= 0 ){
                                this.params.add("'"+s+"'");
                                for (int i = 0; i<entero; i++){
                                    if(this.data.readByte() != ' ' ){ return "Error, a non-space char was found before a card parameter";}
                                    str = this.data.readString(2);
                                    // CASE OF 10
                                    if (str.equals("10")) {
                                        str += (char) this.data.readByte();
                                    }
                                    this.params.add(str); //tratar superiormente
                                }
                                return "draw";
                        }
                        else{throw new NumberFormatException();}        
                    }
                    catch (NumberFormatException e){return "Error, char '0'|'1'|'2'|'3'|'4'|'5' expected after after draw<sp> "+s+" was found";}
                case "drws":
                    for (int i = 0; i<this.cardsExpected; i++){
                        if(this.data.readByte() != ' ' ){ return "Error, a non-space char was found before a card parameter";}
                        str = this.data.readString(2);
                        // CASE OF 10
                        if (str.equals("10")) {
                            str += (char) this.data.readByte();
                        }
                        this.params.add(str); //tratar superiormente
                    }
                    try{
                       if((char) this.data.readByte()!= ' '){return "Error, space char expected before server card param";} 
                       //if((char) this.data.readByte() != '\''){return "Error, expected \' ex:'0'";}
                       s = (char)this.data.readByte();
                       entero = Integer.parseInt(""+s);
                       //if((char) this.data.readByte() != '\''){return "Error, expected \' ex:'0'";}
                       if(entero <= 5 && entero >= 0 ){
                               this.params.add("'"+s+"'");
                               return "drws";
                       }
                       else{throw new NumberFormatException();}
                    }
                    catch (NumberFormatException e){return "Error, char '0'|'1'|'2'|'3'|'4'|'5' expected "+s+" was found";}
                case "show":
                    for (int i = 0; i<5; i++){
                            if(this.data.readByte() != ' ' ){ return "Error, a non-space char was found before a card parameter";}
                            str = this.data.readString(2);
                            if(str.equals("10")){
                                str += (char) this.data.readByte();
                            }
                            this.params.add(str); //tratar superiormente
                    }
                    return "show";
                case "erro":
                    if ((char) this.data.readByte() != ' ') { return "CACA";}
                    this.params.add(this.data.read_string_variable(2));
                    return "erro";
                default: return "Error, unspecified command " + str;
            }
       }
       catch(SocketTimeoutException e) {return "Error,  TimeOut while parameter was readed";}
       catch(IOException e){return "Error,  IO error, maybe not enough-long arguments for that command";}
    }
     
}
