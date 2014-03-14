package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;

import battlesheeps.accounts.Account;
import battlesheeps.networking.LobbyMessageToServer.LobbyNotification;
import battlesheeps.networking.Request.LobbyRequest;
import battlesheeps.server.GameManager;
import battlesheeps.server.ServerGame;

public class ServerLobby implements Runnable
{
	private static final int PORT = 5003; /* port to listen on */
    private static ServerSocket SERVER;
    private static final Hashtable<String, ClientConnLobby> aClientList = new Hashtable<String, ClientConnLobby>();
	private static final ArrayList<Account> aOnlineAccounts = new ArrayList<Account>();
    
//    public static void main (String[] args) {
//    	Account a1 = new Account("dave", "12345");
//		Account a2 = new Account("bob", "password");
//		Account a3 = new Account("dinkle", "IamAdinkle");
//		Account a4 = new Account("bobs", "password");
//		
//		ServerGame g1 = new ServerGame(1, a1, a2);
//		ServerGame g2 = new ServerGame(2, a2, a3);
//		ServerGame g3 = new ServerGame(3, a3, a1);
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
            (new Thread(new ClientConnLobby(client, this))).start();
        }
    }

	@Override
	public void run()
	{
		this.acceptClients();
	}

	public Hashtable<String, ClientConnLobby> getClientlist() {
		return aClientList;
	}

	public ArrayList<Account> getOnlineaccounts() {
		return aOnlineAccounts;
	}
}

class ClientConnLobby implements Runnable {
	
    private ObjectInputStream aInput;
    private ObjectOutputStream aOutput;
    private String aUsername;
    private Account aAccount;
    private Queue<Request> aRequestQueue; // for requests received, as only one request can be sent at a time.
    private static ServerLobby aServer;
    /**
     * @param pClient The client socket accepted by the server.
     */
    ClientConnLobby(Socket pClient, ServerLobby pServer) {
    	ClientConnLobby.aServer = pServer;
    	aRequestQueue = new ArrayDeque<Request>();
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
    			System.out.println("Message received on server.");
    			System.out.println(msg.getUsername());
    			System.out.println(msg.getEnterOrExit() == LobbyNotification.GAME_REQUEST);

            	if (msg.getEnterOrExit() == LobbyNotification.GAME_REQUEST){
            		
            		Request r = msg.getRequest();
            		
            		if (r.getType() == LobbyRequest.REQUEST)
            		{
            			System.out.println("Request received on server. " +r.getRequestee());
            			// send request to requestee
            			aServer.getClientlist().get(r.getRequestee()).respondToRequest(r);
            		}
            		else if (r.getType() == LobbyRequest.REQUEST_WITHDRAW)
            		{
            			System.out.println(aServer.getClientlist()==null);
            			System.out.println(r.getRequestee()==null);
            			// withdraw request from requestee
            			aServer.getClientlist().get(r.getRequestee()).respondToRequestWithdraw(r);
            		}
            		else if (r.getType() == LobbyRequest.ACCEPT)
            		{
            			// send acceptance to requester
            			this.respondToAccept(r);
            		}
            		else 
            		{ 
            			// send decline to requester
            			this.respondToDecline(r);
            		}
            	}
            	else {
            		if (msg.getEnterOrExit() == LobbyNotification.ENTERING){
            			aUsername = msg.getUsername();
            			aAccount = GameManager.getInstance().getAccount(aUsername);
            			addConnection(aUsername, this);
            			addAccount(aAccount);
            			// TODO set user to online? EXECUTIVE DECISION, I'm not using player availability.

            			// update users of new online user
            			LobbyMessageToClient lobbyMsg = new LobbyMessageToClient(aServer.getOnlineaccounts(), null);
            			updateAllClients(lobbyMsg);

            			// create saved game list to send to this user.
            			ArrayList<LobbyMessageGameSummary> games = new ArrayList<LobbyMessageGameSummary>();
            			if(aAccount.getCurrentGames()!=null){
	            			Iterator<Integer> currentGames = aAccount.getCurrentGames();
	
	            			while (currentGames.hasNext()){
	            				ServerGame sg = GameManager.getInstance().getGame(currentGames.next());
	            				Account p1 = GameManager.getInstance().getAccount(sg.getP1Username());
	            				Account p2 = GameManager.getInstance().getAccount(sg.getP2Username());
	
	            				games.add(new LobbyMessageGameSummary(sg.getGameID(), p1, p2, sg.getTurnNum(), sg.getDateLastPlayed()));
	            			}
            			}
            			lobbyMsg.setGames(games);
            			
            			this.updateClient(lobbyMsg);
//            			System.out.println(aUsername + " has connected");
            		}
            		else { // exiting
            			removeConnection(aUsername);
            			removeAccount(aAccount);

            			// update users of new online user
            			LobbyMessageToClient lobbyMsg = new LobbyMessageToClient(aServer.getOnlineaccounts(), null);
            			updateAllClients(lobbyMsg);
            			System.out.println(aUsername + " has disconnected");
            			// closed socket execute close()
            		}
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
    	for (ClientConnLobby aClient : aServer.getClientlist().values()){
    		System.out.println("client list: "+aClient.aUsername);
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
    // TODO If there's time, create separate locks for RequestQueue, ClientList and OnlineAccounts
    /**
     * Add request to queue, and if it's first in queue, message requestee.
     * @param pRequest
     */
    private synchronized void respondToRequest(Request pRequest){
    	this.aRequestQueue.add(pRequest);
		if (this.aRequestQueue.peek().equals(pRequest))
		{
			this.updateClient(new LobbyMessageToClient(null, pRequest));
		}
    }
    /**
     * If this request is first in queue, remove it and message requestee.
     * Does nothing if request is not first in queue
     * @param pRequest
     */
    private synchronized void respondToRequestWithdraw(Request pRequest){
    	if (this.aRequestQueue.peek().equals(pRequest))
    	{
			this.aRequestQueue.poll();
			this.updateClient(new LobbyMessageToClient(null, pRequest));
		}
    }
    /**
     * If request is STILL first in queue (may have been withdrawn), removes it
     * and messages requester.
     * @param pRequest
     */
    private synchronized void respondToAccept(Request pRequest){
    	if (this.aRequestQueue.peek().equals(pRequest))
    	{
			this.aRequestQueue.poll();
			ClientConnLobby requester = aServer.getClientlist().get(pRequest.getRequesterName());
			requester.updateClient(new LobbyMessageToClient(null, pRequest));
		}
    }
    /**
     * If request is STILL first in queue (may have been withdrawn), removes it,
     * messages requester, and if there is another request in the queue, will send it.
     * @param pRequest
     */
    private synchronized void respondToDecline(Request pRequest){
    	if (this.aRequestQueue.peek().equals(pRequest))
    	{
			this.aRequestQueue.poll();
			ClientConnLobby requester = aServer.getClientlist().get(pRequest.getRequesterName());
			requester.updateClient(new LobbyMessageToClient(null, pRequest));
			
			if (this.aRequestQueue.peek() != null){
				this.updateClient(new LobbyMessageToClient(null, this.aRequestQueue.peek()));
			}
		}
    }
    
    private static synchronized void addConnection(String pUsername, ClientConnLobby aConnection){
    	aServer.getClientlist().put(pUsername, aConnection);
    }
    
    private static synchronized void removeConnection(String pUsername){
    	aServer.getClientlist().remove(pUsername);
    }
    
    private static synchronized void addAccount(Account pAccount){
    	aServer.getOnlineaccounts().add(pAccount);
    }
    
    private static synchronized void removeAccount(Account pAccount){
    	aServer.getOnlineaccounts().remove(pAccount);
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

