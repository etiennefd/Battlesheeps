package battlesheeps.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import battlesheeps.accounts.Account;
import battlesheeps.networking.LobbyMessageToServer.LobbyNotification;

public class ClientLobby
{
    /* Host to connect to. This can be "localhost" if running both client/server 
     * on your computer, or the IP address of the host. 
     */
    private static final String HOST = "localhost"; 
    private static final int PORT = 5003; /* port to connect to */
    
    private String aUsername;
    private ObjectOutputStream aOutput = null;
    
    public static void main(String[] args)
	{
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    	String you, foo;
    	try{
    		you = stdIn.readLine();
    	}
    	catch (IOException e1) {
    		you = "p1";
    		e1.printStackTrace();
    	}
    	ClientLobby p = new ClientLobby(you);
    	try{
    		foo = stdIn.readLine();
    		if (foo.equals("close")){
    			p.close();
    		}
    	}
    	catch (IOException e1) {
    		e1.printStackTrace();
    	}
	}
    
    public ClientLobby(String pUsername) {
    	aUsername = pUsername;
        try {
            Socket server = new Socket(HOST, PORT);
            
            // Create a thread to asynchronously read messages from the server
            try {
            	(new Thread(new ServerConnLobby(server))).start();
            }
            catch (IOException e){
            	System.err.println("Error creating server input thread: " + e);
            	System.exit(1);
            }
            
    		try	{
    			aOutput = new ObjectOutputStream(server.getOutputStream());
    			aOutput.writeObject(new LobbyMessageToServer(LobbyNotification.ENTERING, aUsername));
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
     * Closing pOut will also close the socket. This will cause the ServerConn thread
     * to throw an error when it tries to access the socket. Then the ServerConn exits
     * as planned.
     */
    public void close(){
    	try {
    		aOutput.writeObject(new LobbyMessageToServer(LobbyNotification.EXITING, aUsername));
    		aOutput.close();
    	} catch (IOException e)	{ 
            System.err.println("Error closing socket: " + e);
		}
    }
}

class ServerConnLobby implements Runnable {
	
    private ObjectInputStream aInput;
 
    public ServerConnLobby(Socket pServer) throws IOException {
        aInput = new ObjectInputStream(pServer.getInputStream());
    }
 
    public void run() {
		try {
			LobbyMessageToClient msg;
			while ((msg = (LobbyMessageToClient)aInput.readObject()) != null) 
			{ 	
				if (msg.getGames() == null){
					// CASE 1: A different user has entered or exited the lobby and this user must be updated
					System.out.print("Update online users: ");
					for (Account acct : msg.getOnlineAccounts()){
						System.out.print(acct.getUsername() + " ");
	            		// TODO Populate online user list
						// list will contain this user, which should be filtered out
	            	}
					System.out.println();
					// TODO Re-match online users with saved games
				}
				else {
					// CASE 2: This user has just entered the lobby.
					System.out.print("Initial online users: ");
					for (Account acct : msg.getOnlineAccounts()){
						System.out.print(acct.getUsername() + " ");
						// TODO Populate online user list
						// list will contain this user, which should be filtered out
					}
					System.out.println();

					System.out.print("Saved games: ");
					for (LobbyMessageGameSummary game : msg.getGames()){
						System.out.print(game.getGameID() + " ");
						// TODO Populate saved game list and match any games with online users.
					}
					System.out.println();
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
		try {
			aInput.close();
		}
		catch (IOException e1) { 
			System.err.println("Error closing ServerIn: " + e1);
		}
	}
}

