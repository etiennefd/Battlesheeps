package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JDialog;

import battlesheeps.networking.LoginMessage.LoginType;

public class ClientLogin
{
    /* Host to connect to. This can be "localhost" if running both client/server 
     * on your computer, or the IP address of the host. 
     */
    private static final String HOST = "localhost"; 
    private static final int PORT = 5002; /* port to connect to */
    private ObjectOutputStream aOutput = null;
    
    public ClientLogin(JDialog pDialog) {
        try {
            Socket server = new Socket(HOST, PORT);
            
            // Create a thread to asynchronously read messages from the server
            try {
            	(new Thread(new ServerConnLogin(server, pDialog))).start();
            }
            catch (IOException e){
            	System.err.println("Error creating server input thread: " + e);
            	System.exit(1);
            }
            
    		try	{
    			aOutput = new ObjectOutputStream(server.getOutputStream());
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
    public boolean sendMessage(LoginMessage pLoginMessage){
    	try	{
    		aOutput.writeObject(pLoginMessage);
    		return true;
    	}
    	catch (IOException e) {
    		System.err.println("Error outputting message: " + e);
    		return false;
    	}
    }
    /**
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

class ServerConnLogin implements Runnable {
	private static final String FAILURE = "Failure";
	
    private ObjectInputStream aInput;
    private JDialog aDialog;
 
    public ServerConnLogin(Socket pServer, JDialog pDialog) throws IOException {
        aInput = new ObjectInputStream(pServer.getInputStream());
        aDialog = pDialog;
    }
 
    public void run() {
		try {
			LoginMessage msg;
			while ((msg = (LoginMessage)aInput.readObject()) != null) 
			{ 	
				if (msg.getType() == LoginType.CREATE)
				{
					// Creation of account
					if (msg.getLogin().equals(FAILURE)){
						// TODO Display failure message, go back to Create Account screen 
					} 
					else {
						// TODO Successful creation, go back to login screen.
					}
				} 
				else // Logging in
				{
					if (msg.getLogin().equals(FAILURE)){
						// TODO display failure message, either username or password failure.
						msg.getPassword(); //failure message is here.
					} 
					else {
						// TODO Successful login, go to lobby.
					}
				}
			}
		} 
		catch (IOException e) { /* executed when StdIn reads QUIT message and closes the socket. */
			close();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Error reading in ChatMessage, or casting it.\n" + e);
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
