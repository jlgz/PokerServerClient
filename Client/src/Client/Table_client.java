/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.util.ArrayList;


/**
 *
 * @author alfred
 */
public class Table_client {
    private static final int max_money = 500;
    private int min_bet;            // can be 20, 30, 40 or 50
    private int betRound;           // 0 --> 0 bet rounds done, 1 --> first bet round done, 2 --> second bet round done
    private int money_player;
    private int money_server;
    private int dealer;             // 0 -> player NOT dealer, 1 -> player dealer
 
    private Hand playerH;
    private int playerBet;
    private int serverBet;
    private IA ia;
    private int state;   
    
    private ArrayList <String> draw_cards;
    
    public Table_client(int mode){
        this.ia = new IA(mode);
    }
    // GETTERS
    public int getMin_bet() {
        return this.min_bet;
    }
    
    
    public int getMoney_player() {
        return this.money_player;
    }
    
    public int getMoney_server() {
        return this.money_server;
    }
    
    public int getDealer() {
        return this.dealer;
    }
    
    // SETTERS
    public void setMin_bet(int bet) {
        this.min_bet = bet;
    }
    
   
    
    public void setMoney_player(int money) {
        this.money_player = money;
    }
    
    public void setMoney_server(int money) {
        this.money_server = money;
    }
    
    public void setDealer(int dealer) {
        if(dealer == 1){
            this.state = 1;
        }
        else{
            this.state = 2;
        }
        this.dealer = dealer;
    }
    
    public boolean addPlayerBet(int bet) {
        // player move
        if (this.state == 2) {
            if (this.money_player < bet) {
                return false;
            }
            this.playerBet += bet;
            this.money_player -=bet;
            // server has to make action
            this.state = 1;
        } else {
            if (this.money_server < bet) {
                return false;
            }
            this.serverBet += bet;
            this.money_server -=bet;
            // server has to make action
            this.state = 2;
        }
        return true;
    }

    public boolean callPlayer() {
        if (this.state == 2) {
            if(this.playerBet == this.serverBet){return false;}
            int rest = this.serverBet - this.playerBet;
            if ( rest >= this.money_player){
                this.playerBet += this.money_player;
                this.money_player = 0;
            }
            else{
                this.playerBet += rest;
                this.money_player -=rest;
            }
            
        } else {
            int rest = this.playerBet - this.serverBet;
            if ( rest >= this.money_server){
                this.serverBet += this.money_server;
                this.money_server = 0;
            }
            else{
                this.serverBet += rest;
                this.money_server -=rest;
            }
        }
        
        this.betRound +=1;
        if(this.betRound ==2){
            this.state = 4;
        }
        else{
            this.state=3;
        }
        return true;
    }

    public boolean raisePlayer(int r) {
        if (this.state == 2) {
            int rest = this.serverBet - this.playerBet;
            if (rest + r > this.money_player || rest == 0) {
                return false;
            }
            this.playerBet += rest +r;
            this.money_player -=rest + r;
            this.state = 1;
        } else {
            int rest = this.playerBet - this.serverBet;
            if (rest + r > this.money_server || rest == 0) {
                return false;
            }
            this.serverBet += rest +r;
            this.money_server -=rest + r;
            this.state = 2;
        }
        
        return true;
    }

    /**
     * 
     * @param d
     * @param swap cards to change
     */
    public void drawPlayer(int d, String[] swap) {
        String [] cards = new String[d];
        for(int i = 0; i<d; i++){
             cards[i] = this.draw_cards.get(i);
        }
        this.playerH.swap(swap,cards);
        // if player dealer, server plays (state = 1)
        if (this.dealer==1){
            this.state = 1;
        }
        else{
            this.state = 2;
        }
    }
     public void drawServer(int d,String[] swap) {
        this.drawPlayer(d, swap);
        if (this.dealer==1){
            this.state = 1;
        }
        else{
            this.state =2;
        }
    }
    public boolean playerFold() {
        if(this.dealer == 0 && this.playerBet ==this.serverBet){return false;}
        this.money_server += this.playerBet + this.serverBet;
        this.playerBet = 0;
        this.serverBet = 0;
        this.state = -1;
        return true;
    }
    
    public void serverFold() {
        this.money_player += this.playerBet + this.serverBet;
        this.playerBet = 0;
        this.serverBet = 0;
        this.state = -1;
    }
    
    public boolean passPlayer() {
        if (this.playerBet != this.serverBet) {
            return false;
        }
        
        // money bet is equal between player and server, now check state
        if (this.state == 2) {
            // player moves and is dealer
            if (this.dealer == 1) {
                this.betRound += 1;
                if(this.betRound == 1){
                    this.state = 3;
                }
                else{
                    this.state = 4;
                }
                return true;
            } else {
                this.state = 1;
                return true;
            }
        } else {
            // server moves and is dealer
            if (this.dealer == 0) {
                this.betRound += 1;
                if(this.betRound == 1){
                    this.state = 3;
                }
                else{
                    this.state = 4;
                }
                return true;
            } else {
                this.state = 2;
                return true;
            }
        }
    }

    public boolean isPdealer() {
        return this.dealer == 1;
    }

    public String getBetAction() {
        
        // CHANGE STATE
        if(this.state != 2){return "NOP";}
        ArrayList<String> info = this.ia.getSmartBetAction(this.playerH.Rank(), this.playerBet, this.serverBet, this.money_player);
        
        if (info.size() == 1) {
            // call, fold, pass
            switch (info.get(0)) {
                case "call":
                    this.callPlayer();
                    break;
                case "fold":
                    this.playerFold();
                    break;
                case "pass":
                    this.passPlayer();
                    break;
            }
            
            return info.get(0);
        } else {
            // bet_ or rise
            switch (info.get(0)) {
                case "bet_":
                    this.addPlayerBet(Integer.parseInt(info.get(1)));
                    break;
                case "rise":
                    this.raisePlayer(Integer.parseInt(info.get(1)));
                    break;
            }
            
            return info.get(0) + " " + info.get(1);
        }
        
    }

    public String getDrawAction() {
        String s = "draw";
        this.draw_cards = this.ia.getRandomDrawAction(this.playerH.Rank(), this.playerH.getHand());
        s += " "+this.draw_cards.size();
        for (String card: this.draw_cards) {
            s += " " + card;
        }
        if (this.dealer==1){
            this.state = 1;
        }
        else{
            this.state = 2;
        }
        return s;
    }


    public int state() {
        return this.state;
    }
    public boolean anok() {
        if(this.money_player<this.min_bet){return false;}
        this.playerBet += this.min_bet;
        this.serverBet += this.min_bet;
        
        this.money_player -= this.min_bet;
        this.money_server -= this.min_bet;
        
        return true;
    }
  
    public void setHand(ArrayList<String> params) {
        for (int i = 0; i< params.size();i++){params.set(i,params.get(i).toUpperCase());}
        this.playerH = new Hand(params);
    }
    public void reload(){
        this.betRound = 0;
    }
}
