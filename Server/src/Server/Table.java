/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author alfred
 */
public class Table {
    private static final int max_money = 500;
    private int min_bet;            // can be 20, 30, 40 or 50
    private int betRound;           // 0 --> 0 bet rounds done, 1 --> first bet round done, 2 --> second bet round done
    private int id_player;
    private int money_player;
    private int money_server;
    private int dealer;             // 0 -> player NOT dealer, 1 -> player dealer
    private Deck deck;
    private Hand playerH;
    private Hand serverH;
    private int playerBet;
    private int serverBet;
    private IA ia;
    private int state;              // 4 --> showdown, 3 --> draw, 2 --> player, 1 --> server
    private String[] player_draws;
    
    
    public Table (int mode) {
        Random rn = new Random();
        this.min_bet = (rn.nextInt(4) + 2) * 10; 
        this.money_player = 500;
        this.money_server = 500;
        this.deck = new Deck();
        this.playerH = new Hand();
        this.serverH = new Hand();
        this.ia = new IA(mode);
        this.betRound = 0;
        this.dealer = rn.nextInt(2);
        if (dealer == 1){
            this.state = 1;
        }
        else {
            this.state = 2;
        }
    }
    
    public Table (int id, int money) {
        // minimum bet randomly generated (20, 30, 40 or 50)
        Random rn = new Random();
        this.min_bet = (rn.nextInt(4) + 2) * 10; 
        
        // table occupied
        
        // player id
        this.id_player = id;
        
        // money for the player and the server
        if (money <= 500) {
            this.money_player = money;
            this.money_server = money;
        } else {
            this.money_player = 500;
            this.money_server = 500;
        }
        
        // dealer randomly generated
        this.dealer = rn.nextInt(2);
    }
    
    
    
    // GETTERS
    public int getMin_bet() {
        return this.min_bet;
    }
    

    public int getId_player() {
        return this.id_player;
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
    
    
    public void setId_player(int id) {
        this.id_player = id;
    }
    
    public void setMoney_player(int money) {
        this.money_player = money;
    }
    
    public void setMoney_server(int money) {
        this.money_server = money;
    }
    
    public void setDealer(int dealer) {
        this.dealer = dealer;
    }
    
    public boolean addPlayerBet(int bet) {
        // player move
        if (this.state == 2) {
            if (this.money_player < bet || this.money_server == 0) {
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
            if(this.playerBet == this.serverBet || 
                    (this.playerBet > this.serverBet && this.money_server == 0)){
                return false;
            }
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
            if (rest + r > this.money_player || rest == 0 || this.money_server == 0) {
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
             cards[i] = this.deck.getNextCard();
        }
        this.playerH.swap(cards,swap);
        this.player_draws = cards;
        // if player dealer, server plays (state = 1)
        if (this.dealer==1){
            this.state = 1;
        }
        else{
            this.state = 2;
        }
    }
    
    public void drawServer(int d, String[] swap) {
        String [] cards = new String[d];
        for(int i = 0; i<d; i++){
             cards[i] = this.deck.getNextCard();
        }
        this.serverH.swap(cards,swap);
        if (this.dealer==1){
            this.state = 1;
        }
        else{
            this.state =2;
        }
    }

    public boolean playerFold() {
        if(this.money_player == 0 || this.playerBet ==this.serverBet){return false;}
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
        // money bet is equal between player and server, now check state
        if (this.state == 2) {
            if (this.playerBet != this.serverBet && this.money_server != 0 && this.money_player !=0) {
                return false;
            }
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

    /**
     * initialize player and server hands
     * reset bet round
     * swap dealer
     * modify state
     * 
     * @return player hand
     */
    public String[] reload() {
        this.betRound = 0;
        this.deck.reloadDeck();
        this.playerH = new Hand(this.deck.drawFiveCards());
        this.serverH = new Hand(this.deck.drawFiveCards());
        // if player is dealer, set player not dealer and state on player starts
        if(this.dealer == 1){
            this.dealer = 0;
            this.state = 2;
        // if player is dealer, server starts   
        } else{
            this.dealer = 1;
            this.state = 1;
        }
        return this.playerH.getHandAsArray();
    }

    public boolean isPdealer() {
        return this.dealer == 1;
    }

    public String showdown() {
        /*System.out.println("Server hand: "+ this.serverH.toString());
        System.out.println("Player hand: "+ this.playerH.toString());
        System.out.println("Server rank: "+ this.serverH.Rank()
                + " second rank: " + this.serverH.getSecondaryRank()
                + " aux rank: " + this.serverH.getAuxRank());
        System.out.println("Player rank: "+ this.playerH.Rank()
                + " second rank: " + this.playerH.getSecondaryRank()
                + " aux rank: " + this.playerH.getAuxRank());*/
        if(this.playerH.Rank() < this.serverH.Rank()|| 
                (this.playerH.Rank() == this.serverH.Rank() && this.playerH.getSecondaryRank() < this.serverH.getSecondaryRank()) ||
                (this.playerH.Rank() == this.serverH.Rank() && this.playerH.getSecondaryRank() == this.serverH.getSecondaryRank() && this.playerH.getAuxRank() < this.serverH.getAuxRank())){
            if(this.playerBet <= this.serverBet){
                this.money_server += this.playerBet + this.serverBet;
            }
            else{
                this.money_server+= this.serverBet + this.serverBet; 
                this.money_player += this.playerBet - this.serverBet;
            }
        }
        else if(this.playerH.Rank() > this.serverH.Rank()|| 
                (this.playerH.Rank() == this.serverH.Rank() && this.playerH.getSecondaryRank() > this.serverH.getSecondaryRank()) ||
                (this.playerH.Rank() == this.serverH.Rank() && this.playerH.getSecondaryRank() == this.serverH.getSecondaryRank() && this.playerH.getAuxRank() > this.serverH.getAuxRank())){
            if(this.playerBet >= this.serverBet){
                this.money_player += this.playerBet + this.serverBet;
            }
            else{
                this.money_player += 2*this.playerBet;
                this.money_server += this.serverBet-this.playerBet;
            }
        }
        else{
            
            this.money_player += this.playerBet;
            this.money_server += this.serverBet;
        }
        this.playerBet = 0;
        this.serverBet = 0;
        this.state = -1;
        return "show"+this.serverH.toString();
    }

    public String getBetAction() {
        
        // CHANGE STATE
        if(this.state != 1){return "NOP";}
        ArrayList<String> info = this.ia.getSmartBetAction(this.serverH.Rank(), this.serverBet, this.playerBet, this.money_server, this.money_player);
        
        if (info.size() == 1) {
            // call, fold, pass
            switch (info.get(0)) {
                case "call":
                    this.callPlayer();
                    break;
                case "fold":
                    this.serverFold();
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
        String s = "drws";
        String[] new_cards;
        ArrayList<String> draw_cards = this.ia.getRandomDrawAction(this.serverH.Rank(), this.serverH.getHand());
        new_cards = new String[draw_cards.size()];
        for(int i = 0; i < draw_cards.size(); i++){
            new_cards[i] = draw_cards.get(i);
        }
        
        this.drawServer(draw_cards.size(), new_cards);
        
        for (String card: this.player_draws) {
            s += " " + card;
        }
        
        s += " " +draw_cards.size();
        
        return s;
    }

    public String stakes() {
        if(this.money_server< this.min_bet){
            this.money_server = this.money_player;
        }
        this.reload();
        return "stks "+this.money_player+" "+this.money_server;
    }


    public int state() {
        return this.state;
    }

    public boolean validCards(String[] st) {
        return this.playerH.validCards(st);
    }

    public boolean anok() {
        if(this.money_player<this.min_bet){return false;}
        this.playerBet += this.min_bet;
        this.serverBet += this.min_bet;
        
        this.money_player -= this.min_bet;
        this.money_server -= this.min_bet;
        
        return true;
    }
    @Override
    public String toString(){
        return "state: "+this.state + " betRound :"+this.betRound+" playeBet: "+this.playerBet+" serverBet: "+ this.serverBet+" hand server: "+this.serverH.toString();
    }

    String[] getPHand() {
        return this.playerH.getHandAsArray();
    }
    

  
}
