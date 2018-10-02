# PokerServerClient

# Protocol
Once the transmission channel is established, the player sends a START command. The server send ANTE and STACK commands with the amount in coins of the player table stakes and the amount of initial bet.Next the player sends an ANTE_OK message for accepting the cards. The server reply with a DEALER message telling whether the player is the dealer or not of the game, followed by a HAND command with the players 5 cards. Then a betting phase starts followed by a drawing phase (if noone has folded). After a another betting phase, if noone folds a showdowns occurs.

START: With this command the client can start a game with the server, An ID is required. After this message an ANTE and STACKS message from the server are expected. 

ANTE: this commands communicates the initial bet's amount of money.

STACKS   This command informs to the player how many stakes there are in the   table. The first number indicates client chips while the second   indicates server clients. After this message, an ANTEOK or QUIT message is expected.   

ANTE_OK: This command is used for accepting a STACKS message coming from the server. After this command the client expects a DEALER and a HAND message.

QUIT: After this command the communication with the server is closed.

DEALER: This command informs to the player whether the client is the dealer (1) or not (0). The dealer changes in every round. The first round the dealer is randomly chosen. After this message, the server sends a HAND message.

HAND   This command sends 5 cards to the client. After this message, if the client player is the dealer, the server sends a PASS or BET message. Otherwise, a PASS or BET message is expected from the client.

BET:   This command is used for betting an amount of money. After this command a CALL, FOLD or RISE message is expected.

RISE:   This command is used for raising the bet by an amount of money. After this command a CALL or RISE message is expected.

PASS:   This command is used for letting the other player to bet or not.   After this command a BET or a PASS command is expected.   

CALL:   This command is used when the other player has betted and you want equals his bet. After this message, if the betting phase is over,  SHOWNDOWN and a STAKES message is expected. Otherwise a DRAW message is expected from the client.

FOLD:   This command is used when the other player has betted an you don't want to follow him. In this case your round is over and a STAKS message is expected.
 
DRAW: this command is used to swapping cards from client's hand for new ones. After this command a DRAW_SERVER message is expected.

DRAW_SERVER: this command sends the new cards that client asked for.

SHOWNDOWN: after this command both hands are shown and bet money is distributed.

Command Syntax

   The commands consist of a command code, sometimes followed by a space
   and argument fields. Command codes are four alphabetic characters.
   Upper and lower case alphabetic characters are to be treated
   identically. Thus, any of the following may represent the START
   command:

   STRT    Strt    strt    StRt    StRT

   This also applies to any symbols representing parameter values.
   Argument field is composed for one or several arguments separated by
   a space character. 
   
   START:        STRT<SP><INT>
   STAKES:       STKS<SP><INT><SP><INT>
   ANTE:         ANTE<SP><INT>
   ANTE_OK:      ANOK
   QUIT:         QUIT
   DEALER:       DEAL<SP>'0'|'1'        
   HAND:         HAND<SP><CARD><SP><CARD><SP><CARD><SP><CARD><SP><CARD>        
   PASS:         PASS        
   BET:          BET_<SP><INT>
   RISE:         RISE<SP><INT>
   CALL:         CALL        
   FOLD:         FOLD
   DRAW:         DRAW<SP><INT><SP><CARD><SP><CARD>...
   DRAW_SERVER:  DRWS<SP><CARD><SP>CARD><SP>...<INT>
   SHOWNDOWN:    SHOW<SP><CARD><SP><CARD><SP><CARD><SP><CARD><SP><CARD>
   
   <SP> ::= ' ' Space character
   <INT> ::= integer positive number.
   <CARD> ::= <RANK><SUIT>
   RANKS: 'A' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | 10' | 'J' | 'Q' | 'K'
   SUITS: spades: 'S' | hearths: 'H' | clubs: 'C' | diamonds: 'D'
   
   FLUX EXAMPLE:
   
   C: strt 234
   S: ante 30
   S: stks 500 500
   C: anok
   S: deal 1
   S: HAND 3D 2S JH 7H QD
   S: pass
   C: bet_ 40
   S: fold
   S: stks 530 470
   C: anok
   S: deal 0
   S: HAND 2C AC 5D KC 6C
   C: pass
   S: pass
   C: draw '2' 2c 5d
   S: drws 9C JD 4
   C: pass
   S: pass
   S: show 4C 3H 4S 4H 9H
   S: stks 500 500
   C: anok
   S: deal 1
   S: HAND 3S 9C JC 10H 3C
   S: bet_ 74
   C: call
   C: draw '2' 3s jc
   S: drws 4H QH 3
   S: pass
   C: pass
   S: show JD AD 10C QD AC
   S: stks 396 604
   C: anok
   S: deal 0
   S: HAND 3C 8D QC 7D 4D
   C: bet_ 40
   S: rise 89
   C: call
   C: draw '2' 3c qc
   S: drws AC QS 2
   C: bet_ 40
   S: call
   S: show 5H 10C 2S JD 3D
   S: stks 595 405
   C: anok
   ...
