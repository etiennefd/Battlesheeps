package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import battlesheeps.accounts.Account;
import battlesheeps.client.Lobby;
import battlesheeps.networking.LobbyMessageToServer.LobbyNotification;
import battlesheeps.networking.Request.LobbyRequest;

//TODO disable multiple sign in for single user
public class ClientLobby
{
    /* Host to connect to. This can be "localhost" if running both client/server 
     * on your computer, or the IP address of the host. 
     */
    private static final String HOST = "142.157.113.139"; 
    private static final int PORT = 5003; /* port to connect to */
    
    private String aUsername;
    private ObjectOutputStream aOutput = null;
    private Lobby aLobby;
    private ArrayList<Account> aAccounts;
	private ArrayList<LobbyMessageGameSummary> aSavedGames;
	private Account aAccount;
    
//    public static void main(String[] args)
//	{
//		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//    	String you, foo;
//    	try{
//    		you = stdIn.readLine();f
//    	}
//    	catch (IOException e1) {
//    		you = "p1";
//    		e1.printStackTrace();
//    	}
//    	ClientLobby p = new ClientLobby(you);
//    	while (true){
//    		try{
//    			foo = stdIn.readLine();
//    			if (foo.equals("close")){
//    				p.close();
//    				break;
//    			}
//    			else if (foo.startsWith("a ")){
//    				String[] arr = foo.split(" ");
//    				p.sendRequest(new Request(LobbyRequest.ACCEPT, arr[1], you));
//    			}
//    			else if (foo.startsWith("d ")){
//    				String[] arr = foo.split(" ");
//    				p.sendRequest(new Request(LobbyRequest.DECLINE, arr[1], you));
//    			}
//    			else if (foo.startsWith("w ")){
//    				String[] arr = foo.split(" ");
//    				p.sendRequest(new Request(LobbyRequest.REQUEST_WITHDRAW, you, arr[1]));
//    			}
//    			else { // should be a name to send a request to.
//    				p.sendRequest(new Request(LobbyRequest.REQUEST, you, foo));
//    			}
//    		}
//    		catch (IOException e1) {
//    			e1.printStackTrace();
//    		}
//    	}
//	}
    
    public ClientLobby(String pUsername, Lobby pLobby) {
    	aUsername = pUsername;
    	aLobby = pLobby;
    	aAccounts = new ArrayList<Account>();
    	aSavedGames = new ArrayList<LobbyMessageGameSummary>();
    	
    	System.out.println("Name going into client lobby: " + aUsername);
        try {
            Socket server = new Socket(HOST, PORT);
            
            // Create a thread to asynchronously read messages from the server
            try {
            	(new Thread(new ServerConnLobby(server,this))).start();
            }
            catch (IOException e){
            	System.err.println("Error creating server input thread: " + e);
            	System.exit(1);
            }
            
    		try	{
    			aOutput = new ObjectOutputStream(server.getOutputStream());
    			aOutput.writeObject(new LobbyMessageToServer(LobbyNotification.ENTERING, aUsername, null));
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
    public boolean sendRequest(Request pRequest){
    	try	{
    		aOutput.writeObject(new LobbyMessageToServer(LobbyNotification.GAME_REQUEST, aUsername, pRequest));
    		return true;
    	}
    	catch (IOException e) {
    		System.err.println("Error outputting request message: " + e);
    		return false;
    	}
    }
    
    /**
     * Closing pOut will also close the socket. This will cause the ServerConn thread
     * to throw an error when it tries to access the socket. Then the ServerConn exits
     * as planned.
     */
    public void close(){
    	try {
    		aOutput.writeObject(new LobbyMessageToServer(LobbyNotification.EXITING, aUsername, null));
    		aOutput.close();
    	} catch (IOException e)	{ 
            System.err.println("Error closing socket: " + e);
		}
    }
    //Getters
    public Lobby getLobby()
    {
    	return this.aLobby;
    }
    public String getUsername()
    {
    	return this.aUsername;
    }
	public ArrayList<Account> getAccounts() {
		return this.aAccounts;
	}
	public void setAccounts(ArrayList<Account> aAccounts) {
		aAccounts = this.aAccounts;
	}
	public ArrayList<LobbyMessageGameSummary> getSavedGames() {
		return aSavedGames;
	}
	public void setSavedGames(ArrayList<LobbyMessageGameSummary> aSavedGames) {
		this.aSavedGames = aSavedGames;
	}
	public Account getAccount() {
		return aAccount;
	}
	public void setAccount(Account aAccount) {
		this.aAccount = aAccount;
	}
}

class ServerConnLobby implements Runnable {
	
    private ObjectInputStream aInput;
    private ClientLobby aOutput;
 
    public ServerConnLobby(Socket pServer, ClientLobby pClientLobby) 
    		throws IOException {
        aInput = new ObjectInputStream(pServer.getInputStream());
        aOutput = pClientLobby;
    }
 
    public void run() {
		try {
			LobbyMessageToClient msg;
			while ((msg = (LobbyMessageToClient)aInput.readObject()) != null) 
			{ 	
				// A LobbyMessageToClient is either a request or a lobby update
				if (msg.getRequest() == null){
					// Lobby update
					if (msg.getGames() == null){
						// CASE 1: A different user has entered or exited the lobby and this user must be updated
						System.out.print("Update online users: ");
						aOutput.getAccounts().clear();
						for (Account acct : msg.getOnlineAccounts()){
							System.out.print(acct.getUsername() + " ");
							// User list will contain this user, which should be filtered out
							if (!acct.getUsername().equals(aOutput.getUsername()))
							{
								// Populate online user list
								aOutput.getAccounts().add(acct);
							}
						}
						System.out.println();
						aOutput.getLobby().updateOnlineUsers();
						//Re-match online users with saved games
						aOutput.getLobby().updateSavedGames();
					}
					else {
						// CASE 2: This user has just entered the lobby.
						System.out.print("Initial online users: ");
						if(!msg.getOnlineAccounts().isEmpty()){
							System.out.println(msg.getOnlineAccounts());
							for (Account acct : msg.getOnlineAccounts()){
								
								if(acct!=null)
								{
									System.out.println(acct.getUsername());
									// User list will contain this user, which should be filtered out
									if (acct.getUsername().equals(aOutput.getUsername()))
									{
										aOutput.setAccount(acct);
									}
									// Populate online user list
									else
									{
										aOutput.getAccounts().add(acct);
									}
								}
							}
						}
						System.out.println();
	
						System.out.print("Saved games: ");
						for (LobbyMessageGameSummary game : msg.getGames()){
							System.out.print(game.getGameID() + " ");
							// Populate saved game list with list already filtered for this user
							//DONE IN GUI match any games with online users.
							aOutput.getSavedGames().add(game);
							if(game.hasPlayer(aOutput.getAccount())==1)
			    			{
			    				game.setMyGame(1);
			    			}
			    			else
			    			{
			    				game.setMyGame(2);
			    			}
							
						}
						System.out.println();
						javax.swing.SwingUtilities.invokeLater(new Runnable() {
				            public void run() {
				                aOutput.getLobby().createAndShowLobby();
				            }
				        });
					}
				}
				else {
					// Request
					Request r = msg.getRequest();
					
					if (r.getType() == LobbyRequest.REQUEST){
						// Display requester info in pop-up with accept or decline buttons 
						//Lei, you'll need to check if a saved game exists with the user
						
						boolean isNewGame = true;
						for(LobbyMessageGameSummary game : aOutput.getSavedGames())
						{
							String P1 = game.getPlayer1().getUsername();
							String P2 = game.getPlayer2().getUsername();
							if(msg.getRequest().getRequesterName().equals(P1)||
									msg.getRequest().getRequesterName().equals(P2))
							{
								isNewGame = false;
							}
						}
						
						System.out.println("Game request from: " + r.getRequesterName());
						System.out.println("Accept or Decline? (Input either a or d)");
						
						aOutput.getLobby().requestPopup(r.getRequesterName(), r.getRequestee(), isNewGame);
					
						// Accept will trigger: DONE IN LOBBY CLASS
						// aOutput.sendRequest(new Request(LobbyRequest.ACCEPT, r.getRequesterName(), r.getRequestee()));

						// Decline will trigger:
						// aOutput.sendRequest(new Request(LobbyRequest.DECLINE, r.getRequesterName(), r.getRequestee()));
					}
					else if (r.getType() == LobbyRequest.REQUEST_WITHDRAW){
						// Close the existing request pop-up
						aOutput.getLobby().requestWithdrawn(r.getRequesterName());
						System.out.println("Request withdrawn by user: " + r.getRequesterName());
					}
					else if (r.getType() == LobbyRequest.ACCEPT){
						// Opponent accepted invitation, move to game screen
						boolean isNewGame = true;
						for(LobbyMessageGameSummary game : aOutput.getSavedGames())
						{
							String P1 = game.getPlayer1().getUsername();
							String P2 = game.getPlayer2().getUsername();
							if(msg.getRequest().getRequestee().equals(P1)||
									msg.getRequest().getRequestee().equals(P2))
							{
								isNewGame = false;
							}
						}
						aOutput.getLobby().requestAccepted(r.getRequestee(),isNewGame);
						System.out.println("Now beginning game with " + r.getRequestee() + "...");
					}
					else { 
						// Opponent declined invitation, notify user that he/she should get on with their life.
						aOutput.getLobby().requestDeclined(r.getRequestee());
						System.out.println(r.getRequestee() + " has declined your invitation. Move on.");
					}
				}
			}
		} 
		catch (IOException e) { /* executed when StdIn reads QUIT message and closes the socket. */
			close();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Error reading in LobbyMessage, or casting it.\n" + e);
			close();
		}
	}

	private void close()
	{
		System.out.println("closing client");
		new LobbyMessageToServer(LobbyNotification.EXITING,"",null);
		try {
			aInput.close();
		}
		catch (IOException e1) { 
			System.err.println("Error closing ServerIn: " + e1);
		}
	}
}

