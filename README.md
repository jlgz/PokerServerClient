# PokerServerClient

# Protocol
Once the transmission channel is established, the player sends a START command. The server send the STACK command with the amount in coins of the player table stakes.Next the player sends an ANTE_OK message for accepting the cards. The server reply with a DEALER message telling whether the player is the dealer or not of the game, following a HAND command with the players 5 cards.Then a betting phase starts followed by a drawing phase (if noone has fold) and a second betting phase. If noone folds a showdowns occurs.

START: With this command the client can start a game with the server, An ID is needed. After this message an ANTE and STACKS message from the server are expected. 

STACKS   This command informs to the player how many stakes there are in the   table. The first number indicates client chips while the second   indicates server clients. After this message, an ANTEOK or QUIT message is expected.   

ANTE_OK: This command is used for accepting a STACKS message coming from the server. After this command the client expects a DEALER and a HAND message.

QUIT: After this command the communication with the server is closed.

DEALER: This command informs to the player whether the client is the dealer (1) or not (0). The dealer changes in every round. The first round the dealer is randomly chosen. After this message, the server sends a HAND message.

HAND   This command sends 5 cards to the player. After this message, if the client player is the dealer, the server sends a PASS or BET message. Otherwise, a PASS or BET message is expected from the client.

BET:   This command is used for betting an amount of money. After this command a CALL, FOLD or RISE message is expected.

RISE:   This command is used for raising the bet by an amount of money. After this command a CALL or RISE message is expected.

PASS:   This command is used for letting the other player to bet or not.   After this command a BET or a PASS command is expected.   

CALL:   This command is used when the other player has betted and you want equals his bet. After this message, if the betting phase is over,  SHOWNDOWN and a STAKES message is expected. Otherwise a DRAW message is expected from the client.

FOLD:   This command is used when the other player has betted an you don't want to follow him. In this case your round is over and a STAKS message is expected.
 
DRAW: this command is used to swapping cards from client's hand for new ones. After this command a DRAW_SERVER message is expected.

DRAW_SERVER: this command sends the new cards that client asked for.

SHOWNDOWN:

 
START:        STRT<SP><ID>
STAKES:        STKS<SP><CHIPS><SP><CHIPS>
ANTE_OK:        ANOK
QUIT:        QUIT
DEALER:        DEAL<SP>'0'|'1'        
CARD: CARD<SP><CARD>        
CHECK:        CHCK        
BET:        BET_        
CALL:        CALL        
FOLD:        FOLD        
SHOWNDOWN:        SHOW<SP><CARD>        
<SP> ::= ' ' Space character  
<ID> ::= Over network integer positive number. 
<CARD> ::= 'K' | 'Q' | 'J' |  
<CHIPS> ::= Over network integer positive number.
 
