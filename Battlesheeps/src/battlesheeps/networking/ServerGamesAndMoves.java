package battlesheeps.networking;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import battlesheeps.server.GameManager;
import battlesheeps.server.Move;
import battlesheeps.server.Move.ServerInfo;
import battlesheeps.server.ServerGame;
import battlesheeps.server.ServerGame.ClientInfo;
import battlesheeps.ships.Ship;

public class ServerGamesAndMoves implements Runnable 
{
	private static final int PORT = 5001; /* port to listen on */
    private static ServerSocket SERVER;
    
//    public static void main (String[] args) {
//		
//		Account a1 = new Account("dave", "12345");
//		Account a2 = new Account("bob", "password");
//		Account a3 = new Account("dinkle", "IamAdinkle");
//		Account a4 = new Account("bobs", "password");
//		
//		GameManager gm = GameManager.getInstance();
//		Hashtable<String,Account> accts = gm.getAccounts();
//		accts.put(a1.getUsername(), a1);
//		accts.put(a2.getUsername(), a2);
//		accts.put(a3.getUsername(), a3);
//		accts.put(a4.getUsername(), a4);
//
//		new Thread(new ServerGamesAndMoves()).start();
//    }
    /**
     * Creates serverSocket on specified port.
     */
    public ServerGamesAndMoves(){
    	try {
            SERVER = new ServerSocket(PORT); /* start listening on the port */
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + PORT + "\n" + e);
            System.exit(1);
        }
    }
    
    /**
     * Continuously accepts new clientConnections and spawns new threads to listen to them.
     */
    public void acceptClients()
    {
    	Socket client = null;
        while (true) 
        {
            try {
                client = SERVER.accept();
                System.out.println("game client accepted!");
            } catch (IOException e) {
                System.err.println("Accept failed.\n" + e);
                System.exit(1);
            }
            /* start a new thread to handle this client */
            (new Thread(new ClientConnGame(client))).start();
        }
    }
	@Override
	public void run() {
		this.acceptClients();
	}
}

class ClientConnGame implements Runnable {
	private static final Hashtable<String, ClientConnGame> aClientList = new Hashtable<String, ClientConnGame>();
	
    private ObjectInputStream aInput;
    private ObjectOutputStream aOutput;
    private String aUsername;
    private ClientConnGame aOpponent;
    private int aGameID;
    private boolean opponentRespondedToCoral = false;
    private boolean opponentAcceptedCoral = false;
    private boolean isP1 = false;
    
    /**
     * Client connections on the server side will receive ChatMessage objects and
     * will send out only Strings.
     * @param pClient The client socket accepted by the server.
     */
    ClientConnGame(Socket pClient) {
        try {
        	aOutput = new ObjectOutputStream(pClient.getOutputStream());
            aInput = new ObjectInputStream(pClient.getInputStream());
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    /**
     * Continuously tries to read in Moves and does some error handling.
     */
    public void run() {
        try {
        	// accept init data
        	GameInit init = (GameInit) aInput.readObject();
        	aUsername = init.getUsername();
        	aGameID = init.getGameID(); // if gameID is zero, new game requested
        	addConnection(aUsername, this);
        	System.out.println(aUsername + " has connected to GAME.");
        	
        	boolean isNewGame = false;
        	if (init.getGameID() == 0) isNewGame = true;
        	
        	ServerGame aGame = null;
        	GameManager gm = GameManager.getInstance();
        	
        	// Requestee will only send their own username, opponent should be null
        	if (aGameID == 0 && init.getOpponent() != null)
        	{
        		// The requester is always the one who makes the game.
        		aGameID = gm.generateGameID();
        		aGame = new ServerGame(aGameID, gm.getAccount(aUsername), gm.getAccount(init.getOpponent()));
        		gm.addGame(aGame);
        		System.out.println(aUsername + " has created game " + aGameID);
        		
        		while (!aClientList.containsKey(init.getOpponent())) {/*WAIT for requestee to connect*/}
        		aClientList.get(init.getOpponent()).aGameID = this.aGameID;
        		System.out.println(aUsername + " has informed opponent of GameID");
        	} 
        	else {
        		System.out.println(aUsername + " is finding game");
        		while (aGame == null){
        			aGame = gm.getGame(aGameID);
        		}
        		System.out.println(aUsername + " has joined game " + aGameID);
        	}
        	aOutput.writeObject(aGame);
        	aOutput.reset();
        	
        	// Set aOpponent
        	if (aGame.getP1Username().equals(aUsername)){
        		while (!aClientList.containsKey(aGame.getP2Username())) {/*WAIT for requestee to connect*/}
        		aOpponent = aClientList.get(aGame.getP2Username());
        		isP1 = true;
        	}
        	else {
        		while (!aClientList.containsKey(aGame.getP1Username())) {/*WAIT for requestee to connect*/}
        		aOpponent = aClientList.get(aGame.getP1Username());
        	}
        	if (aOpponent == null) System.out.println(aUsername + " opponent connection null.");
        	
        	Move msg;
        	if (isNewGame){
        		// Coral reef config.
        		System.out.println(aUsername + " CORAL REEF CONFIG");
        		if (isP1)
        		{
        			// P1 is the controller. It waits for p1 Move, then waits for p2 move.
        			// After that it either generates new coral or exits loop.
        			boolean p1acceptedGame = false;
        			
        			while ((msg = (Move) aInput.readObject()) != null)
        			{
        				if (msg.getServerInfo() == ServerInfo.CORAL_REEF_ACCEPT){
        					p1acceptedGame = true;
        				}
        				
        				while (!opponentRespondedToCoral) {/*WAIT*/}
        				
        				if (!opponentAcceptedCoral || !p1acceptedGame){
        					aGame.generateCoralReefs();
        					aGame.setClientInfo(ClientInfo.NEW_CORAL);
        					aOutput.writeObject(aGame);
        					aOutput.reset();
        					
        					System.out.println("p1: " + p1acceptedGame + "   p2: "+ opponentAcceptedCoral);
        					p1acceptedGame = false;
        					opponentAcceptedCoral = false;
        					opponentRespondedToCoral = false;
        					
        					aOpponent.opponentRespondedToCoral = true;
        				}
        				else {
        					aGame.setClientInfo(ClientInfo.FINAL_CORAL);
        					aOpponent.opponentAcceptedCoral = true;
        					aOpponent.opponentRespondedToCoral = true;
        					aOutput.writeObject(aGame);
        					aOutput.reset();
        					
        					System.out.println("p1: " + p1acceptedGame + "   p2: "+ opponentAcceptedCoral);
        					break;
        				}
        			}
        		}
        		else {
        			// P2 will tell p1 accept/decline, then wait for p1 to respond.
        			while ((msg = (Move) aInput.readObject()) != null)
        			{
        				if (msg.getServerInfo() == ServerInfo.CORAL_REEF_ACCEPT){
        					aOpponent.opponentAcceptedCoral = true;
        				}
        				aOpponent.opponentRespondedToCoral = true;
        				
        				while (!opponentRespondedToCoral) {/*WAIT*/}
        				
        				if (opponentAcceptedCoral){
        					aOutput.writeObject(aGame);
        					aOutput.reset();
        					break;
        				}
        				else {
        					aOutput.writeObject(aGame);
        					aOutput.reset();
        					opponentRespondedToCoral = false;
        				}
        			}
        		}
        		
        		System.out.println("SHIP SETUP for user: " + aUsername);
        		// Ship setup, moves ships into place until it is told player is done.
        		aGame.setClientInfo(ClientInfo.SHIP_INIT);
        		while ((msg = (Move) aInput.readObject()) != null){
        			if (msg.getServerInfo() == ServerInfo.SHIP_INIT_COMPLETE){
        				break;
        			}
        			else if (msg.getServerInfo() == ServerInfo.SHIP_INIT) {
        				// TODO is this correct? probably not.
        				aGame.computeMoveResult(aGame.matchWithShip(msg.getaShip()), msg.getMoveType(), msg.getCoord(), msg.getSecondaryCoord());
        				aOutput.writeObject(aGame);
        				aOutput.reset();
        			}
        		}
        	}
        	
        	aGame.setClientInfo(ClientInfo.GAME_UPDATE);
            while ((msg = (Move) aInput.readObject()) != null) 
            {
            	aGame.computeMoveResult(aGame.matchWithShip(msg.getaShip()), msg.getMoveType(), msg.getCoord(), msg.getSecondaryCoord());
            	// System.out.println(aGame.printBoard());
            	
            	
            	aOutput.writeObject(aGame);
            	aOpponent.aOutput.writeObject(aGame);
            	aOutput.reset();
            	aOpponent.aOutput.reset();
            	
            	if (aGame.isGameComplete()){
            		gm.endGame(aGameID);
            		close();
            		break;
            	}
            }
        }
        catch (EOFException e) {
        	close();
        }
        catch (IOException e) 
        {
        	close();
        	e.printStackTrace();
        }
		catch (ClassNotFoundException e)
		{
			close();
			System.err.println("Error reading in Move, or casting it." + e);
		}
    }
    
    private static synchronized void addConnection(String pUsername, ClientConnGame aConnection){
    	aClientList.put(pUsername, aConnection);
    }
    
    private static synchronized void removeConnection(String pUsername){
    	aClientList.remove(pUsername);
    }
    
    private void close(){
    	System.out.println(aUsername + " has disconnected from GAME.");
    	removeConnection(aUsername);
		try {
			aInput.close();
			aOutput.close();
		}
		catch (IOException e1) { /* do nothing, resource already closed. */ }
	}
}
