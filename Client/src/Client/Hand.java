/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import static java.lang.Integer.max;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author alfred
 */
public class Hand {
    private ArrayList<String> hand;
    private int rank;           // determine the rank of the hand
    private int secondary_rank; // in case of a draw, we use a secondary rank
    private int aux_rank;       // in case of a 2nd draw, we use an auxiliar rank
    private static String[] suits_values = {"C","D","H","S"};
    private static String[] numbers_values = {"A","K","Q","J","10","9","8","7","6","5","4","3","2"};
    
    public Hand() {
        this.hand = new ArrayList<>();
        this.rank = 1;
        this.secondary_rank = 0;
        this.aux_rank = 0;
    }
    
    public Hand(ArrayList<String> cards) {
        this.hand = cards;
        this.rank = 0;
        this.secondary_rank = 0;
        this.aux_rank = 0;
        
    }
    
    /**
     * Ranks:
     * 9 - Straight flush
     * 8 - Four of a kind
     * 7 - Full house
     * 6 - Flush
     * 5 - Straight
     * 4 - Three of a kind
     * 3 - Two pair
     * 2 - One pair
     * 1 - High card
     * 
     * @return 
     */
    public int Rank() {
        ArrayList<String> numbers = getNumbers();
        ArrayList<String> suits = getSuits();
        
        // if 5 of the same suit and we have a straight --> 9 (straight flush)
        if (this.getMaxSuit(suits) == 5 && this.straight(numbers)) {
            this.rank = 9;
        // if 4 of the same number --> 8 (four of a kind)
        } else if (this.getMaxNumber(numbers) == 4) {
            this.rank = 8;
        // if three and pair --> 7 (full house)
        } else if (this.getMaxNumber(numbers) == 5) {
            this.rank = 7;
        // if 5 of the same suit --> 6 (flush)
        } else if (this.getMaxSuit(suits) == 5) {
            this.rank = 6;
        // if 5 straight --> 5 (straight)
        } else if (this.straight(numbers)) {
            this.rank = 5;
        // if 3 same numbers --> 4 (three of a kind)
        } else if (this.getMaxNumber(numbers) == 3) {
            this.rank = 4;
        // if 2 pairs (using aux_rank to check it) --> 3 (two pair)
        } else if (this.getMaxNumber(numbers) == 2 && this.aux_rank != 0) {
            this.rank = 3;
        // if one pair --> 2 (one pair)
        } else if (this.getMaxNumber(numbers) == 2) {
            this.rank = 2;
        // else, we have high card
        } else {
            this.checkHighCard(numbers);
            this.rank = 1;
        }
        return this.rank;
    }
    
    public int getSecondaryRank() {
        return this.secondary_rank;
    }
    
    public int getAuxRank() {
        return this.aux_rank;
    }

    public ArrayList<String> getHand() {
        return this.hand;
    }
    
    // get hand as an array (instead of arraylist)
    public String[] getHandAsArray() {
        String[] h = new String[this.hand.size()];
        
        for (int i = 0; i < this.hand.size(); i++) {
            h[i] = this.hand.get(i);
        }
        
        return h;
    }
    
    // ************ AUX METHODS TO CHECK THE RANK ************ //
    
    private ArrayList<String> getNumbers() {
        ArrayList<String> number = new ArrayList<>();
        String aux;
        
        for (String c: hand) {
            // /CASE WITH 10
            if (c.length() == 3) {
                aux = c.substring(0,2);
            } else {
                aux = c.substring(0,1); // getting first character
            }
            number.add(aux);
        }
        
        return number;
    }
    
    private ArrayList<String> getSuits() {
        ArrayList<String> suit = new ArrayList<>();
        String aux;
        
        for (String c: hand) {
            if (c.length() == 3) {
                aux = c.substring(2);
            } else {
                aux = c.substring(1); // getting second character
            }
            suit.add(aux);
        }
        
        return suit;
    }
    
    private int getNumberAsInteger(String n) {
        int value;
        n = n.toUpperCase();
        if ("J".equals(n)) {
            value = 11;
        } else if ("Q".equals(n)) {
            value = 12;
        } else if ("K".equals(n)) {
            value = 13;
        } else if ("A".equals(n)) {
            value = 14;
        } else {
            try{value = Integer.parseInt(n);}
            catch(NumberFormatException e){
                //System.out.println("ERROR RANK INT"+n);
                value = 14;
            }
        }
        
        return value;
    }
    
    /**
     * 
     * @param suit
     * @return number of occurrences of the same suit in the hand
     */
    private int getMaxSuit(ArrayList<String> suit) {
        int occ = 0;
        
        // count the occurrences of each suit in the hand given and return the maximum
        for (String v: Hand.suits_values) {
            occ = max(occ, Collections.frequency(suit, v));
        }
        
        return occ;
    }

    private boolean straight(ArrayList<String> number) {
        ArrayList<Integer> aux = new ArrayList<>();
        boolean straight = false;
        
        for (int i = 0; i<number.size(); i++) {
            int value;
            
            // value stores the numeric value of the card
            value = this.getNumberAsInteger(number.get(i));
            
            // add it to the arraylist
            aux.add(value);
        }
        
        // sort in ascending order
        Collections.sort(aux);
        
        // check if there is a straight of 5 cards
        if (    (aux.get(0) == aux.get(1) - 1) &&
                (aux.get(1) == aux.get(2) - 1) &&
                (aux.get(2) == aux.get(3) - 1) &&
                (aux.get(3) == aux.get(4) - 1) ) {
            
            straight = true;
            
            // set the secondary value to the highest value of the straight
            this.secondary_rank = aux.get(4);
        }
        
        return straight;
    }

    /**
     * 
     * @param number
     * @return maximum number of occurrences of a number (card) in the hand
     *          5 -> full house
     *          4 -> four of a kind
     *          3 -> three of a kind
     *          2 -> one pair or two pair
     *          1 -> nothing
     */
    private int getMaxNumber(ArrayList<String> number) {
        int occ = 0;
        
        // count the occurrences of each number in the hand given
        for (String v: number) {
            int n = Collections.frequency(number, v);
            int aux_n = this.getNumberAsInteger(v);
            
            // four of a kind case --> there are four of a kind
            if (n == 4) {
                occ = 4;
                this.secondary_rank = aux_n;
                
            // full house case (1) --> if there is a three kind and a pair
            } else if (occ == 3 && n == 2) {
                occ = 5;
                this.aux_rank = aux_n;
                
            // full house case (2) --> if there is a three kind and a pair
            } else if (n == 3 && occ == 2) {
                occ = 5;
                
                // aux_rank as the numeric value of the "two pair"
                this.aux_rank = this.secondary_rank;
                // secondary_rank as the numeric value of the "three of a kind"
                this.secondary_rank = aux_n;
            
            // two pair case --> if there is a secondary pair (not the same pair)
            } else if (occ == 2 && n == 2 && this.secondary_rank != aux_n) {
                // set the aux_rank
                if (aux_n > this.secondary_rank) {
                    // set the secondary rank as the higher value between
                    // the two pair
                    this.aux_rank = this.secondary_rank;
                    this.secondary_rank = aux_n;
                } else {
                    this.aux_rank = aux_n;
                }
            } else if (n == 2) {
                occ = n;
                this.secondary_rank = aux_n;
                
            // else, we have nothing yet
            } else {
                occ = n;
                this.secondary_rank = aux_n;
            }
        }
        
        return occ;
    }
    
    /**
     * sets secondary_rank with the higher value card of the hand
     * @param numbers 
     */
    private void checkHighCard(ArrayList<String> numbers) {
        int high_card = 0;
        
        for (String n : numbers) {
            int value;
            
            // value stores the numeric value of the card
            value = this.getNumberAsInteger(n);
            
            // high_card equals the maximum between the old high_card and
            // the new card checked
            high_card = max(high_card, value);
        }
        
        this.secondary_rank = high_card;
    }

    
    // ************ OTHER METHODS ************ //
    
    /**
     * This method swaps the cards we want to remove from our hand
     * with the new cards we have drawn
     * 
     * @param cards new cards we have to include on the hand
     * @param swap old cards we have to remove from the hand
     */
    public void swap(String[] cards, String[] swap) {
        // count for the "cards" index to add
        int j=0;
        int index;
        
        for (int i = 0; i < swap.length; i++) {
            index = this.hand.indexOf(swap[i].toUpperCase());
            // removing the old card ("swap")
            this.hand.remove(index);
            // adding the new card ("cards")
            this.hand.add(cards[j]);
            j++;
        }
    }

    public boolean validCards(String[] st) {
     
        for (String card: st) {
            if (!this.hand.contains(card.toUpperCase())) {
                return false;
            }
        }
        return true;
    }
    public String toString() {
        if(this.hand.size()== 0){return "";}
        String str = "";
        for(String s: this.hand){
            str+= " "+s;
        }
        return str;
    }
    
}
