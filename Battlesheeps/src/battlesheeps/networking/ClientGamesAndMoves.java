package battlesheeps.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import battlesheeps.client.ClientGame;
import battlesheeps.networking.Request.LobbyRequest;
import battlesheeps.server.Move;
import battlesheeps.server.Move.ServerInfo;
import battlesheeps.server.ServerGame;

public class ClientGamesAndMoves
{
    private static final int PORT = 5001; /* port to connect to */
    
    /* Host to connect to. This can be "localhost" if running both client/server 
     * on your computer, or the IP address of the host. 
     */
    private static final String HOST = "142.157.113.61"; 
    private ObjectOutputStream aOutput = null;

//    public static void main(String[] args)
//	{
//    	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//      	String you, foo;
//      	try{
//      		you = stdIn.readLine();
//      		foo = stdIn.readLine();
//      	}
//      	catch (IOException e1) {
//      		you = "p1";
//      		foo = "p2";
//      		e1.printStackTrace();
//      	}
//      	if (foo.equals("")){
//      		System.out.println("foo empty");
//      		foo = null;
//      	}
//      	ClientGamesAndMoves p = new ClientGamesAndMoves(you, foo, 0);
//      	
//      	while (true){
//      		try{
//      			foo = stdIn.readLine();
//      			if (foo.equals("close")){
//      				p.close();
//      				break;
//      			}
//      			else if (foo.equals("a")){
//      				p.sendMove(new Move(null, null, null, ServerInfo.CORAL_REEF_ACCEPT));
//      			}
//      			else if (foo.equals("d")){
//      				p.sendMove(new Move(null, null, null, ServerInfo.CORAL_REEF_DECLINE));
//      			}
//      		}
//      		catch (IOException e1) {
//      			e1.printStackTrace();
//      		}
//      	}
//	}
    
    /**
     * @param pUsername The username of the active user
     * @param pOpponent The username of opponent, SHOULD BE NULL iff sent by requestee
     * @param pGameID If requesting a new game, send ID '0'
     */
    public ClientGamesAndMoves(String pUsername, String pOpponent, int pGameID, ClientGame pClient) {
        
    	pClient.setManager(this);
    	
    	try {
            Socket server = new Socket(HOST, PORT);
            
            // Create a thread to asynchronously read messages from the server
            try {
            	(new Thread(new ServerConnGame(server, pClient))).start();
            }
            catch (IOException e){
            	System.err.println("Error creating server input thread: " + e);
            	System.exit(1);
            }
            
    		try	{
    			aOutput = new ObjectOutputStream(server.getOutputStream());
    			aOutput.writeObject(new GameInit(pUsername, pOpponent, pGameID));
    		}
    		catch (IOException e){
    			System.err.println("Error in outputStream: " + e);
                System.exit(1);
    		}
            
        } catch (UnknownHostException e) {
            System.err.println("Error finding host: " + e);
            System.exit(1);
        }
		catch (IOException e){
			System.err.println("Error creating socket: " + e);
            System.exit(1);
		}
    }
    /**
     * Sends a move to the server.
     * @param pMove
     * @return false iff there's an error.
     */
    public boolean sendMove(Move pMove){
    	try	{
    		aOutput.writeObject(pMove);
    		return true;
    	}
    	catch (IOException e) {
    		System.err.println("Error outputting message: " + e);
    		return false;
    	}
    }
    /**
     * MUST BE CLOSED WHEN DONE WITH.
     * Closing pOut will also close the socket. This will cause the ServerConn thread
     * to throw an error when it tries to access the socket. Then the ServerConn exits
     * as planned.
     * @param pStdIn In.
     * @param pOut Out.
     */
    public void close(){
    	try {
    		aOutput.close();
    	} catch (IOException e)	{ 
            System.err.println("Error closing socket: " + e);
		}
    }
}

class ServerConnGame implements Runnable {
    private ObjectInputStream aInput = null;
    private ClientGame aMyClient; 
    
    public ServerConnGame(Socket pServer, ClientGame pClient) throws IOException {
        aInput = new ObjectInputStream(pServer.getInputStream());
        aMyClient = pClient;
    }
 
    public void run() {
		try {
			ServerGame newGame;
			while ((newGame = (ServerGame) aInput.readObject()) != null) { 
				// TODO update with new ServerGame
				switch (newGame.getClientInfo()){
					case NEW_GAME:
						aMyClient.updateGame(newGame);
						break;
					case NEW_CORAL:
						break;
					case FINAL_CORAL:
						break;
					case SHIP_INIT:
						break;
					case GAME_UPDATE:
						aMyClient.updateGame(newGame);
						break;
				}
			}
		} 
		catch (IOException e) { 
			close();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Error reading in LobbyMessage, or casting it.\n" + e);
			close();
		}
	}
    private void close(){
		try {
			aInput.close();
		}
		catch (IOException e1) { 
			System.err.println("Error closing ServerIn: " + e1);
		}
	}
}
