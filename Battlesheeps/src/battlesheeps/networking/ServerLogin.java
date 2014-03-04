package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import battlesheeps.accounts.Account;
import battlesheeps.accounts.Account.Status;
import battlesheeps.networking.LoginMessage.LoginType;
import battlesheeps.server.GameManager;

public class ServerLogin
{
	private static final int PORT = 5002; /* port to listen on */
    private static ServerSocket SERVER;
    
    public static void main (String[] args) {
    	(new ServerLogin()).acceptClients();
    }
    
    /**
     * Creates serverSocket on specified port.
     */
    public ServerLogin(){
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
                System.out.println("new client accepted");
            } catch (IOException e) {
                System.err.println("Accept failed.\n" + e);
                System.exit(1);
            }
            /* start a new thread to handle this client */
            (new Thread(new ClientConnLogin(client))).start();
        }
    }
}

class ClientConnLogin implements Runnable {
	private static final String FAILURE = "Failure";
	private static final String INIT = "INIT"; // Reserved for use in chat feature, no player can have this as username.
	private static Hashtable<String, Account> aAccounts = GameManager.getInstance().getAccounts();

    private ObjectInputStream aInput;
    private ObjectOutputStream aOutput;

    /**
     * Client connections on the server side will receive ChatMessage objects and
     * will send out only Strings.
     * @param pClient The client socket accepted by the server.
     */
    ClientConnLogin(Socket pClient) {
    	try {
    		aOutput = new ObjectOutputStream(pClient.getOutputStream());
            aInput = new ObjectInputStream(pClient.getInputStream());
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    private synchronized void addAccount(LoginMessage pMsg){
		aAccounts.put(pMsg.getLogin(), new Account(pMsg.getLogin(), pMsg.getPassword()));
    }
    
    private synchronized boolean containsKey(String pLogin){
    	return aAccounts.containsKey(pLogin);
    }
    
    private synchronized Account getAccount(String pLogin){
    	return aAccounts.get(pLogin);
    }
     
    /**
     * Continuously tries to read in LoginMessages and does some error handling.
     */
    public void run() {
        try {
        	LoginMessage msg;
            while ((msg = (LoginMessage) aInput.readObject()) != null) 
            {
            	if (msg.getType() == LoginType.CREATE)
            	{
            		// Creating account
            		if (this.containsKey(msg.getLogin()) || msg.getLogin().equals(INIT)) { 
            			// Username in use
            			aOutput.writeObject(new LoginMessage(LoginType.CREATE, FAILURE, ""));
            		}
            		else { 
            			// Create account
            			addAccount(msg);
            			aOutput.writeObject(new LoginMessage(LoginType.CREATE, "", ""));
            		}
            	}
            	else 
            	{
            		// Logging in
            		if (this.containsKey(msg.getLogin()))
            		{ 	
            			// Account exists
            			Account thisAcct = getAccount(msg.getLogin());
            			if (thisAcct.getPassword().equals(msg.getPassword()))
            			{
            				// Password correct
            				thisAcct.setAvailability(Status.AVAILABLE);
            				aOutput.writeObject(new LoginMessage(LoginType.LOGIN, "", ""));
            				// Send lobby?
            			}
            			else {
            				// Password incorrect
            				aOutput.writeObject(new LoginMessage(LoginType.LOGIN, FAILURE, "Incorrect password."));
            			}
            		}
            		else { 
            			// Account not found
            			aOutput.writeObject(new LoginMessage(LoginType.LOGIN, FAILURE, "Username not found."));
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
			System.err.println("Error reading in LoginMessage, or casting it.\n" + e);
			close();
		}
    }

	private void close()
	{
		try {
			aInput.close();
			aOutput.close();
		}
		catch (IOException e1) { /* do nothing, resource already closed. */ }
	}
}
