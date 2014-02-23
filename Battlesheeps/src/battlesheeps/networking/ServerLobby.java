package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import battlesheeps.accounts.Account;
import battlesheeps.networking.LobbyMessageToServer.LobbyNotification;
import battlesheeps.server.GameManager;
import battlesheeps.server.ServerGame;

public class ServerLobby
{
	private static final int PORT = 5003; /* port to listen on */
    private static ServerSocket SERVER;
    
//    public static void main (String[] args) {
//    	Account a1 = new Account("dave", "12345");
//		Account a2 = new Account("bob", "password");
//		Account a3 = new Account("dinkle", "IamAdinkle");
//		Account a4 = new Account("bobs", "password");
//		
//		// TODO Ettiene, creating the game should add to Accounts SavedGames
//		ServerGame g1 = new ServerGame(1, a1, a2);
//		ServerGame g2 = new ServerGame(2, a2, a3);
//		ServerGame g3 = new ServerGame(3, a3, a1);
//		
//		// shouldn't have to do this
//		a1.addNewGame(g1.getGameID());
//		a1.addNewGame(g3.getGameID());
//		a2.addNewGame(g1.getGameID());
//		a2.addNewGame(g2.getGameID());
//		a3.addNewGame(g2.getGameID());
//		a3.addNewGame(g3.getGameID());
//		
//		GameManager gm = GameManager.getInstance();
//		Hashtable<String,Account> accts = gm.getAccounts();
//		accts.put(a1.getUsername(), a1);
//		accts.put(a2.getUsername(), a2);
//		accts.put(a3.getUsername(), a3);
//		accts.put(a4.getUsername(), a4);
//		
//		gm.addGame(g1);
//		gm.addGame(g2);
//		gm.addGame(g3);
//		
//    	(new ServerLobby()).acceptClients();
//    }
    
    /**
     * Creates serverSocket on specified port.
     */
    public ServerLobby(){
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
            } catch (IOException e) {
                System.err.println("Accept failed.\n" + e);
                System.exit(1);
            }
            /* start a new thread to handle this client */
            (new Thread(new ClientConnLobby(client))).start();
        }
    }
}

class ClientConnLobby implements Runnable {
	
	private static final ArrayList<ClientConnLobby> aClientList = new ArrayList<ClientConnLobby>();
	private static final ArrayList<Account> aOnlineAccounts = new ArrayList<Account>();
	
    private ObjectInputStream aInput;
    private ObjectOutputStream aOutput;
    private String aUsername;
    private Account aAccount;

    /**
     * Client connections on the server side will receive ChatMessage objects and
     * will send out only Strings.
     * @param pClient The client socket accepted by the server.
     */
    ClientConnLobby(Socket pClient) {
        try {
        	aOutput = new ObjectOutputStream(pClient.getOutputStream());
            aInput = new ObjectInputStream(pClient.getInputStream());
        } catch (IOException e) {
            System.err.println(e);
        }
    }
     
    /**
     * Continuously tries to read in LobbyMessages and does some error handling.
     */
    public void run() {
        try {
        	LobbyMessageToServer msg;
            while ((msg = (LobbyMessageToServer) aInput.readObject()) != null) 
            {
            	if (msg.getEnterOrExit() == LobbyNotification.ENTERING){
            		aUsername = msg.getUsername();
            		aAccount = GameManager.getInstance().getAccount(aUsername);
            		addConnection(this);
            		addAccount(aAccount);
            		// TODO set user to online? EXECUTIVE DECISION, I'm not using player availability.
            		
            		// update users of new online user
            		LobbyMessageToClient lobbyMsg = new LobbyMessageToClient(aOnlineAccounts);
            		updateAllClients(lobbyMsg);
                	
            		// create saved game list to send to this user.
                	Iterator<Integer> currentGames = aAccount.getCurrentGames();
                	ArrayList<LobbyMessageGameSummary> games = new ArrayList<LobbyMessageGameSummary>();
                	
                	while (currentGames.hasNext()){
                		ServerGame sg = GameManager.getInstance().getGame(currentGames.next());
                		Account p1 = GameManager.getInstance().getAccount(sg.getP1Username());
                		Account p2 = GameManager.getInstance().getAccount(sg.getP2Username());
                		
                		games.add(new LobbyMessageGameSummary(sg.getGameID(), p1, p2, sg.getTurnNum(), sg.getDateLastPlayed()));
                	}
                	lobbyMsg.setGames(games);
            		
                	this.updateClient(lobbyMsg);
                	// System.out.println(aUsername + " has connected");
            	}
            	else { // exiting
            		removeConnection(this);
            		removeAccount(aAccount);
            		
            		// update users of new online user
            		LobbyMessageToClient lobbyMsg = new LobbyMessageToClient(aOnlineAccounts);
            		updateAllClients(lobbyMsg);
            		// System.out.println(aUsername + " has disconnected");
            	}
            	
            }
        } 
        catch (IOException e) 
        {
        	close();
        }
		catch (ClassNotFoundException e)
		{
			System.err.println("Error reading in LobbyMessage, or casting it." + e);
			close();
		}
    }
    
    private synchronized void updateAllClients(LobbyMessageToClient pMsg){
    	for (ClientConnLobby aClient : aClientList){
    		if (!aClient.equals(this))
    		{
    			aClient.updateClient(pMsg);
    		}
    	}
    }
    private void updateClient(LobbyMessageToClient pMsg){
    	try	{
			this.aOutput.writeObject(pMsg);
			this.aOutput.reset();
		}
		catch (IOException e) {
			System.err.println("Error sending Lobby update to Client: " + this.getName() + " " + e);
		}
    }
    
    private static synchronized void addConnection(ClientConnLobby aConnection){
    	aClientList.add(aConnection);
    }
    
    private static synchronized void removeConnection(ClientConnLobby aConnection){
    	aClientList.remove(aConnection);
    }
    
    private static synchronized void addAccount(Account pAccount){
    	aOnlineAccounts.add(pAccount);
    }
    
    private static synchronized void removeAccount(Account pAccount){
    	aOnlineAccounts.remove(pAccount);
    }

	private void close(){
		try {
			aInput.close();
			aOutput.close();
		}
		catch (IOException e1) { /* do nothing, resource already closed. */ }
	}
	
	private String getName(){
		return aUsername;
	}
	
	public boolean equals(Object p1){
		if( p1 == null )
		{
			return false;
		}
		if( p1 == this )
		{
			return true;
		}
		if( p1.getClass() != getClass() )
		{
			return false;
		}
		return this.aUsername.equals(((ClientConnLobby)p1).getName());
	}
	
	public int hashCode(){
		return aUsername.hashCode();
	}
}

