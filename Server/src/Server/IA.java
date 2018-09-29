/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import static java.lang.Integer.max;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author joseluis
 */
public class IA {
    private int mode;
    
    public IA(int mode){
        this.mode = mode;
    }
    
    // get a random draw action
    /**
     * 
     * @param rank hand rank
     * @param hand hand cards
     * @return ArrayList<String> of the cards to swap (randomly) from the hand with the deck
     */
    public ArrayList<String> getRandomDrawAction(int rank, ArrayList<String> hand){
        // random number generator
        Random rn = new Random();
        // Array of cards to be swapped
        ArrayList<String> swap_hand = new ArrayList<>();
        
        for (String card: hand) {
            int num = rn.nextInt(2);
            // if num is 1, add it to the swap_hand
            if (num == 1) {
                swap_hand.add(card);
            }
        }
        
        return swap_hand;
    }
    
    /**
     * 
     * @param rank player hand rank
     * @param p1_bet player actual bet (the one that is calling the method)
     * @param p2_bet player_2 actual bet (the other player)
     * @param p1_money player money left
     * @param p2_money player_2 money left
     * @return Command of the action to make (bet_, rise, call, pass, fold)
     */
    public ArrayList<String> getSmartBetAction(int rank, int p1_bet, int p2_bet, int p1_money, int p2_money) {
        ArrayList<String> bet_action = new ArrayList<>();
        
        // random number generator
        Random rn = new Random();
        
        // bet amount
        int bet;
        int bet_diff;
        // "win probability"
        double winProb = this.standardWinProbability(rank);
        
        if (p2_bet > p1_bet && p2_money == 0) {
            bet_action.add("call");
            return bet_action;
        }
        
        // check if a player (client or server) can't keep betting, if so both will "pass"
        if (p2_bet < p1_bet || p1_money == 0) {
            bet_action.add("pass");
            return bet_action;
        }
        
        // we have a good hand! --> "bet_" or "rise" if possible, if not "call"
        if (rank == 9 || rank == 8 || rank == 7 || rank == 6) {
            int ratio;
            
            // determine bet ratio according to our money
            if (rank >= 8) {
                // if rank 9 or 8, ratio = 3
                ratio = 3;
            } else {
                // if rank 7 or 6, ratio = 2
                ratio = 2;
            }
            
            // if p1 is starting the bet round (or following a "call" or "pass"), we "bet_"
            if (p1_bet == p2_bet) {
                // bet between (money*ratio/4 - money/4) = money*(ratio-1)/4 and money*ratio/4 
                // if ratio 3 --> money*( 2/4, 3/4 ]
                // if ratio 2 --> money*( 1/4, 2/4 ]
                bet = (int) ((int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4)));
                bet_action.add("bet_");
                bet_action.add(Integer.toString(bet));
                return bet_action;
                
            // else, p2 has made a bet, we "rise" the bet if we can, else we "bet_" or "call"
            } else {
                bet_diff = (p2_bet - p1_bet);
                
                // if we can't "raise" neither "call", we "bet_" all the money (ALL IN)
                if (bet_diff > p1_money) {
                    bet_action.add("call");
                    return bet_action;
                    
                // if we have only enough to "call", we "call"
                } else if (bet_diff == p1_money) {
                    bet_action.add("call");
                    return bet_action;
                    
                // else (bet < money), we "rise"
                } else {
                    // minimum bet + random amount of the money left
                    // if ratio 3 --> minimum bet + money*( 2/4, 3/4 ]
                    // if ratio 2 --> minimum bet + money*( 1/4, 2/4 ]
                    p1_money = p1_money - bet_diff;
                    bet = bet_diff + ((int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4)));
                    bet_action.add("rise");
                    bet_action.add(Integer.toString(bet - bet_diff));
                    return bet_action;
                }
            }
            
        // we have a normal hand! --> "bet_" or "rise" if possible, if not "call"
        } else if (rank == 5) {
            // if p1 is starting the bet round (or following a "call" or "pass"), we "bet_"
            if (p1_bet == p2_bet) {
                int pass = rn.nextInt(100);
                
                // 20% probability to "pass"
                if (pass < 20) {
                    bet_action.add("pass");
                    return bet_action;
                    
                // else, we "bet_"
                } else {
                    // bet = money*( 1/10, 2/10]
                    bet = (int) ((int) p1_money/5 - rn.nextInt(max(1,(int) p1_money/10)));
                    bet_action.add("bet_");
                    bet_action.add(Integer.toString(bet));
                    return bet_action;
                }
                
            // else, p2 has made a bet, we may "rise" the bet or "call"
            } else {
                bet_diff = (p2_bet - p1_bet);
                
                // if we can't "raise", we may "call" or "fold"
                if (bet_diff > p1_money) {
                    // if the bet we have already made is high compared to what
                    // we have left, we do a "call"
                    if (p1_bet/2 > p1_money) {
                        bet_action.add("call");
                    // if the bet is low compared to our money, we may "call" or "fold"
                    } else if (p1_bet*2 < p1_money) {
                        int fold = rn.nextInt(100);
                
                        // 60% probability to "fold"
                        if (fold < 60) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    // else, we probably "fold", if not we "call"
                    } else {
                        int fold = rn.nextInt(100);
                        
                        // 80% probability to "fold"
                        if (fold < 80) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    }
                    bet_action.add("fold");
                    return bet_action;
                    
                // if we have only enough to "call", we may "call"
                } else if (bet_diff == p1_money) {
                    // random number to "call"
                    int call = rn.nextInt(100);
                    
                    // if minimum bet is low compared to our previous bet
                    if (bet_diff < p1_bet) {
                        // 90% probability to "call"
                        if (call < 90) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is as high as our previous bet
                    } else if (bet_diff >= p1_bet) {
                        // 50% probability to "call"
                        if (call < 50) {
                            bet_action.add("call");
                            return bet_action;
                        }
                    }
                    // if we have not "call" yet, then we "fold"
                    bet_action.add("fold");
                    return bet_action;
                    
                // else (bet < money), we may "rise" or "call", low probability to "fold"
                } else {
                    // random number to "call"
                    int call = rn.nextInt(100);
                    
                    p1_money = p1_money - bet_diff;
                    bet = bet_diff + ((int) p1_money/5 - rn.nextInt(max(1,(int) p1_money/10)));
                    
                    // if minimum bet is low compared to our previous bet
                    if (bet_diff < p1_bet) {
                        // 95% probability to "call"
                        if (call < 95) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is as HIGH as our previous bet, but LOW compared to our money
                    } else if (bet_diff >= p1_bet && p1_money/3 > bet_diff) {
                        // random number to "rise"
                        int rise = rn.nextInt(100);
                        
                        // 70% probability to "rise"
                        if (rise < 70) {
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                            
                        // else, "call"
                        } else {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is as HIGH as our previous bet, and HIGH compared to our money 
                    } else {
                        // random number to "rise"
                        int rise = rn.nextInt(100);
                        
                        // 40% probability to "rise"
                        if (rise < 40) {
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                            
                        // else, 90% of the 60% to "call"
                        } else if (call < 90) {
                            bet_action.add("call");
                            return bet_action;
                            
                        // else, 10% of the 60% to "fold"
                        } else {
                            bet_action.add("fold");
                            return bet_action;
                        }
                    }
                    // if we have not "call" yet, then we "fold"
                    bet_action.add("fold");
                    return bet_action;
                }
            }
            
        // we have a "bad-ish" hand! --> may "bet_" or "rise", if not "call" or "fold"
        } else if (rank == 4 || rank == 3) {
            // if p1 is starting the bet round (or following a "call" or "pass"), we "bet_" or "pass"
            if (p1_bet == p2_bet) {
                int pass = rn.nextInt(100);
                
                // 10% probability to bluff
                if (pass < 10) {
                    // ratio = 2 or 3 (same as rank 9, 8, 7 or 6)
                    int ratio = rn.nextInt(2) + 2;
                    // higher bet to buff (same as rank 9, 8, 7 or 6)
                    bet = (int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4));
                    bet_action.add("bet_");
                    bet_action.add(Integer.toString(bet));
                    return bet_action;
                }
                
                pass = rn.nextInt(100);
                // 70% probability to "pass"
                if (pass < 70) {
                    bet_action.add("pass");
                    return bet_action;
                    
                // else, we "bet_"
                } else {
                    // bet = money*( 1/18, 3/18]
                    bet = (int) ((int) p1_money/6 - rn.nextInt(max(1,(int) p1_money/9)));
                    bet_action.add("bet_");
                    bet_action.add(Integer.toString(bet));
                    return bet_action;
                }
                
            // else, p2 has made a bet, we may "rise" the bet or "call"
            } else {
                bet_diff = (p2_bet - p1_bet);
                
                // if we can't "raise", we may "call" or "fold"
                if (bet_diff > p1_money) {
                    // if the bet we have already made is high compared to what
                    // we have left, we do a "call" or less probable a "fold"
                    if (p1_bet/2 > p1_money) {
                        int fold = rn.nextInt(100);
                
                        // 20% probability to "fold"
                        if (fold < 20) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    // if the bet is low compared to our money, we may "call" or "fold"
                    } else if (p1_bet*2 < p1_money) {
                        int fold = rn.nextInt(100);
                
                        // 50% probability to "fold"
                        if (fold < 50) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    // else, we probably "fold", if not we "call"
                    } else {
                        int fold = rn.nextInt(100);
                        
                        // 80% probability to "fold"
                        if (fold < 80) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    }
                    
                    return bet_action;
                    
                // if we have only enough to "call", we may "call"
                } else if (bet_diff == p1_money) {
                    // random number to "call"
                    int call = rn.nextInt(100);
                    
                    // if minimum bet is low compared to our previous bet
                    if (bet_diff < p1_bet) {
                        // 80% probability to "call"
                        if (call < 80) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is as high as our previous bet
                    } else if (bet_diff >= p1_bet) {
                        // 50% probability to "call"
                        if (call < 50) {
                            bet_action.add("call");
                            return bet_action;
                        }
                    }
                    // if we have not "call" yet, then we "fold"
                    bet_action.add("fold");
                    return bet_action;
                    
                // else (bet < money), we may "rise" or "call", low probability to "fold"
                } else {
                    // random number to "call"
                    int call = rn.nextInt(100);
                    
                    p1_money = p1_money - bet_diff;
                    bet = bet_diff + ((int) p1_money/6 - rn.nextInt(max(1,(int) p1_money/9)));
                    
                    // if minimum bet is low compared to our previous bet
                    if (bet_diff < p1_bet/2) {
                        // 90% probability to "call"
                        if (call < 90) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is HIGH compared to our previous bet, but LOW compared to our money
                    } else if (bet_diff >= p1_bet/2 && p1_money/3 > bet_diff) {
                        // random number to "rise"
                        int rise = rn.nextInt(100);
                        
                        // 10% probability to bluff
                        if (rise < 10) {
                            // ratio = 2 or 3 (same as rank 9, 8, 7 or 6)
                            int ratio = rn.nextInt(2) + 2;
                            // higher bet to buff (same as rank 9, 8, 7 or 6)
                            bet = bet_diff + ((int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4)));
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        }
                        
                        rise = rn.nextInt(100);
                        // 50% probability to "rise"
                        if (rise < 50) {
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        // else, "call"
                        } else {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is HIGH compared to our previous bet, and HIGH compared to our money 
                    } else {
                        // random number to "rise"
                        int rise = rn.nextInt(100);
                        
                        // 10% probability to bluff
                        if (rise < 10) {
                            // ratio = 2 or 3 (same as rank 9, 8, 7 or 6)
                            int ratio = rn.nextInt(2) + 2;
                            // higher bet to buff (same as rank 9, 8, 7 or 6)
                            bet = bet_diff + ((int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4)));
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        }
                        
                        rise = rn.nextInt(100);
                        // 25% probability to "rise"
                        if (rise < 25) {
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        // else, 30% of the 75% to "call"
                        } else if (call < 30) {
                            bet_action.add("call");
                            return bet_action;
                        // else, 70% of the 75% to "fold"
                        } else {
                            bet_action.add("fold");
                            return bet_action;
                        }
                    }
                    // if we have not "call" yet, then we "fold"
                    bet_action.add("fold");
                    return bet_action;
                }
            }
            
        // we have a "bad" hand! --> may "bet_" or "rise", if not "call" or "fold"
        } else if (rank == 2 || rank == 1) {
            // if p1 is starting the bet round (or following a "call" or "pass")
            if (p1_bet == p2_bet) {
                int pass = rn.nextInt(100);
                
                // 5% probability to bluff
                if (pass < 5) {
                    // ratio = 2 or 3 (same as rank 9, 8, 7 or 6)
                    int ratio = rn.nextInt(2) + 2;
                    // higher bet to buff (same as rank 9, 8, 7 or 6)
                    bet = (int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4));
                    bet_action.add("bet_");
                    bet_action.add(Integer.toString(bet));
                    return bet_action;
                }
                
                rn.nextInt(100);
                // 80% probability to "pass"
                if (pass < 80) {
                    bet_action.add("pass");
                    return bet_action;
                    
                // else, 20% we "bet_"
                } else {
                    // bet = money*( 1/18, 3/18]
                    bet = (int) ((int) p1_money/6 - rn.nextInt(max(1,(int) p1_money/9)));
                    bet_action.add("bet_");
                    bet_action.add(Integer.toString(bet));
                    return bet_action;
                }
                
            // else, p2 has made a bet, we may "rise" the bet or "call"
            } else {
                bet_diff = (p2_bet - p1_bet);
                
                // if we can't "raise", we may "call" or "fold"
                if (bet_diff > p1_money) {
                    // if the bet we have already made is high compared to what
                    // we have left, we do a "call" or "fold"
                    if (p1_bet/2 > p1_money) {
                        int fold = rn.nextInt(100);
                
                        // 20% probability to "fold"
                        if (fold < 20) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    // if the bet is low compared to our money, we may "call" or "fold"
                    } else if (p1_bet*2 < p1_money) {
                        int fold = rn.nextInt(100);
                
                        // 70% probability to "fold"
                        if (fold < 50) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    // else, we probably "fold", if not we "call"
                    } else {
                        int fold = rn.nextInt(100);
                        
                        // 90% probability to "fold"
                        if (fold < 90) {
                            bet_action.add("fold");
                        } else {
                            bet_action.add("call");
                        }
                    }
                    
                    return bet_action;
                    
                // if we have only enough to "call", we may "call"
                } else if (bet_diff == p1_money) {
                    // random number to "call"
                    int call = rn.nextInt(100);
                    
                    // if minimum bet is low compared to our previous bet
                    if (bet_diff < p1_bet) {
                        // 30% probability to "call"
                        if (call < 30) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is as high as our previous bet
                    } else if (bet_diff >= p1_bet) {
                        // 10% probability to "call"
                        if (call < 10) {
                            bet_action.add("call");
                            return bet_action;
                        }
                    }
                    // if we have not "call" yet, then we "fold"
                    bet_action.add("fold");
                    return bet_action;
                    
                // else (bet < money), we may "rise" or "call", low probability to "fold"
                } else {
                    // random number to "call"
                    int call = rn.nextInt(100);
                    
                    p1_money = p1_money - bet_diff;
                    bet = bet_diff + ((int) p1_money/6 - rn.nextInt(max(1,(int) p1_money/9)));
                    
                    // if minimum bet is low compared to our previous bet
                    if (bet_diff < p1_bet/2) {
                        // 30% probability to "call"
                        if (call < 30) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is HIGH compared to our previous bet, but LOW compared to our money
                    } else if (bet_diff >= p1_bet/2 && p1_money/3 > bet_diff) {
                        // random number to "rise"
                        int rise = rn.nextInt(100);
                        
                        // 5% probability to bluff
                        if (rise < 5) {
                            // ratio = 2 or 3 (same as rank 9, 8, 7 or 6)
                            int ratio = rn.nextInt(2) + 2;
                            // higher bet to buff (same as rank 9, 8, 7 or 6)
                            bet = bet_diff + ((int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4)));
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        }
                        
                        rise = rn.nextInt(100);
                        // 5% probability to "rise"
                        if (rise < 5) {
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        // else, 10% "call"
                        } else if (rise < 10) {
                            bet_action.add("call");
                            return bet_action;
                        }
                        
                    // else if new bet is HIGH compared to our previous bet, and HIGH compared to our money 
                    } else {
                        // random number to "rise"
                        int rise = rn.nextInt(100);
                        
                        // 5% probability to bluff
                        if (rise < 5) {
                            // ratio = 2 or 3 (same as rank 9, 8, 7 or 6)
                            int ratio = rn.nextInt(2) + 2;
                            // higher bet to buff (same as rank 9, 8, 7 or 6)
                            bet = bet_diff + ((int) p1_money*ratio/4 - rn.nextInt(max(1,(int) p1_money/4)));
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        }
                        
                        rn.nextInt(100);
                        // 5% probability to "rise"
                        if (rise < 5) {
                            bet_action.add("rise");
                            bet_action.add(Integer.toString(bet - bet_diff));
                            return bet_action;
                        // else, 1% of the 95% to "call"
                        } else if (call < 1) {
                            bet_action.add("call");
                            return bet_action;
                        // else, 99% of the 95% to "fold"
                        } else {
                            bet_action.add("fold");
                            return bet_action;
                        }
                    }
                    // if we have not "call" yet, then we "fold"
                    bet_action.add("fold");
                    return bet_action;
                }
            }
        }
        bet_action.add("fold");
        return bet_action;
    }
    
    public double standardWinProbability(int rank) {
        // cards rank odds
        double total = 2598960;
        double one = 1303560;
        double two = 1098240;
        double three = 123552;
        double four = 54912;
        double five = 9180;
        double six = 5112;
        double seven = 3744;
        double eight = 624;
        double nine = 36;
        
        double value = 50; // probability of having a better hand rank
        
        // calculate the probability of having a better hand by using
        // the rank of your hand and the odds of having other ranks
        switch(rank) {
            case 1:
                // 0 = 0%
                value = 1 - (one+two+three+four+five+six+seven+eight+nine)/total; 
                break;
            case 2:
                value = 1 - (two+three+four+five+six+seven+eight+nine)/total;
                break;
            case 3:
                value = 1 - (three+four+five+six+seven+eight+nine)/total;
                break;
            case 4:
                value = 1 - (four+five+six+seven+eight+nine)/total;
                break;
            case 5:
                value = 1 - (five+six+seven+eight+nine)/total;
                break;
            case 6:
                value = 1 - (six+seven+eight+nine)/total;
                break;
            case 7:
                value = 1 - (seven+eight+nine)/total;
                break;
            case 8:
                value = 1 - (eight+nine)/total;
                break;
            case 9:
                // 1 = 100%
                value = 1 - (nine)/total;
                break;
        }
        
        return value;
    }
    
}
