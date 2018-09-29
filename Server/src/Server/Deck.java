
package Server;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author alfred
 */
public class Deck {
    private static String[] cards= {"A","K","Q","J","10","9","8","7","6","5","4","3","2"};
    private static String[] clubs = {"C","D","H","S"};
    private String[] deck;
    private int nextCard;
    /*
    Constructor.
    La baraja queda preparada para repartir.
    */
    public Deck() {
        int i = 0;
        this.deck = new String[52];
        for(String card: this.cards){
            for(String club: this.clubs){
                this.deck[i] = card+club;
                i++;
            }
        }
        this.shuffle(100);
        this.nextCard = -1;
    }
    /*
    Intercambia cartas en la baraja it veces.
    */
    private void shuffle(int it){
        int i,j;
        String tmp;
        Random rn = new Random();
        for (int ch = 0; ch < it;ch++){
            i = rn.nextInt(52);
            j = rn.nextInt(52);
            tmp = this.deck[i];
            this.deck[i] = this.deck[j];
            this.deck[j] = tmp;
        }
    }
    /*
    Retorna la siguiente carta a repartir.
    */
    public String getNextCard(){
        this.nextCard++;
        return this.deck[this.nextCard];
    }
    
    /**
     * Draws five cards using the getNextCard method
     * @return the new hand
     */
    public ArrayList<String> drawFiveCards() {
        ArrayList<String> new_hand = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            new_hand.add(this.getNextCard());
        }
        
        return new_hand;
    }
    /*
    probando contribution
    */
    /*
    Repone el estado de la baraja para volver a repartir.
    */
    public void reloadDeck() {
        this.nextCard = -1;
        this.shuffle(100);
    }

}
