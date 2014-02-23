package battlesheeps.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import battlesheeps.client.Lobby;
import battlesheeps.networking.LoginMessage.LoginType;

public class ClientLogin
{
    /* Host to connect to. This can be "localhost" if running both client/server 
     * on your computer, or the IP address of the host. 
     */
    private static final String HOST = "localhost"; 
    private static final int PORT = 5002; /* port to connect to */
    private ObjectOutputStream aOutput = null;
    
    public ClientLogin(JDialog pLoginDialog, JFrame pLoginFrame) {
        try {
            Socket server = new Socket(HOST, PORT);
            
            // Create a thread to asynchronously read messages from the server
            try {
            	(new Thread(new ServerConnLogin(server, pLoginDialog, pLoginFrame))).start();
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
    private JFrame aLoginFrame;
 
    public ServerConnLogin(Socket pServer, JDialog pDialog, JFrame pLoginFrame) throws IOException {
        aInput = new ObjectInputStream(pServer.getInputStream());
        aDialog = pDialog;
        aLoginFrame = pLoginFrame;
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
						// Display failure message, go back to Create Account screen 
						aDialog.setVisible(false);
						aDialog.dispose();
						JOptionPane.showMessageDialog(null, "Account creation failed.", 
								"Invalid Input", JOptionPane.ERROR_MESSAGE);
					} 
					else {
						//Successful creation, go back to login screen.
						aDialog.setVisible(false);
						aDialog.dispose();
						JOptionPane.showMessageDialog(null, "Account creation successful! " +
								"Please login. :)", 
								"Account Creation Success", JOptionPane.INFORMATION_MESSAGE);
					}
				} 
				else // Logging in
				{
					if (msg.getLogin().equals(FAILURE)){
						// display failure message, either username or password failure.
						msg.getPassword(); //failure message is here.
						aDialog.setVisible(false);
						aDialog.dispose();
						JOptionPane.showMessageDialog(null, msg.getPassword(), 
								"Login Failed", JOptionPane.ERROR_MESSAGE);
					} 
					else {
						// Successful login, go to lobby.
						aDialog.setVisible(false);
						aDialog.dispose();
						aLoginFrame.setVisible(false);
						aLoginFrame.dispose();
						new Lobby();
						
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
