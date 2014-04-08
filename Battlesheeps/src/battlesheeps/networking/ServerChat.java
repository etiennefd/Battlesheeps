package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

 
public class ServerChat implements Runnable
{
    private static final int PORT = 5000; /* port to listen on */
    private static ServerSocket SERVER = null;
    
    /**
     * To test the chat on your computer:
     * 	1. Uncomment main methods in ServerChat and ClientChat.
     * 	2. Run ServerChat
     * 	3. Run ClientChat. Enter, into the console, a name on each line eg: "Alice\n" "Bob\n"
     * 	4. Run ClientChat again, entering the names in opposite order eg: "Bob\n" "Alice\n"
     * 	5. In the Frames that open up, type into the bottom one.
     */
    
    public static void main (String[] args) {
    	(new ServerChat()).acceptClients(); 	/* Example creation of ServerChat */
    }
    
    /**
     * Creates serverSocket on specified port.
     */
    public ServerChat(){
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
            (new Thread(new ClientConn(client))).start();
        }
    }

	@Override
	public void run()
	{
		this.acceptClients();
	}
}
 
class ClientConn implements Runnable {
	private static Hashtable<String, ClientConn> aUsernames = new Hashtable<String, ClientConn>();
	private static final String INIT = "INIT";
	
    private ObjectInputStream aInput = null;
    private PrintWriter aOutput = null;
    private String aUser;

    /**
     * Client connections on the server side will receive ChatMessage objects and
     * will send out only Strings.
     * @param pClient The client socket accepted by the server.
     */
    ClientConn(Socket pClient) {
        try {
            aOutput = new PrintWriter(pClient.getOutputStream(), true);
            aInput = new ObjectInputStream(pClient.getInputStream());
        } catch (IOException e) {
            System.err.println(e);
        }
    }
 
    public void sendMsg(String msg) {
    	aOutput.println(msg);
    }
    
    /**
     * Continuously tries to read in ChatMessages and does some error handling.
     */
    public void run() {
        try {
        	ChatMessage msg;
            while ((msg = (ChatMessage) aInput.readObject()) != null) 
            {
            	// Name connections by putting INIT as recipient, username as message.
            	if (msg.getRecipient().equals(INIT))
            	{
            		aUsernames.put(msg.getMessage(), this);
            		aUser = msg.getMessage();
            		System.out.println(aUser + " has connected to CHAT.");
            	}
            	else 
            	{
            		if (aUsernames.containsKey(msg.getRecipient())) 
            		{
            			ClientConn c = aUsernames.get(msg.getRecipient());
            			c.sendMsg(aUser + ": " + msg.getMessage());
            		} 
            		else {
            			aOutput.println("SERVER: Error, cannot find recipient: \"" + msg.getRecipient() + "\"");
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
			System.err.println("Error reading in ChatMessage, or casting it.\n" + e);
			close();
		}
    }

	private void close()
	{
		aUsernames.remove(aUser);
		aOutput.close();
		
		try {aInput.close();}
		catch (IOException e1) { /* do nothing, resource already closed. */ }
		
		System.out.println(aUser + " has disconnected from CHAT.");
	}
}
