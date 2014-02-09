package battlesheeps.networking;

/* Need a port for
 * 1 Game
 * 2 game Request
 * 3 Move
 * 4 Chat
 */
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientChat {
	/* Host to connect to. This can be "localhost" if running both client/server 
	 * on your computer, or the IP address of the host. 
	 */
	private static final String HOST = "localhost"; 
	private static final String INIT = "INIT"; // This requires that no username be INIT
    private static final int PORT = 5000; /* port to connect to */
    
    private ObjectOutputStream aOutput = null;
    private String aOpponentUsername = null;
    /**
     * To test the chat on your computer:
     * 	1. Uncomment main methods in ServerChat and ClientChat.
     * 	2. Run ServerChat
     * 	3. Run ClientChat. Enter, into the console, a name on each line eg: "Alice\n" "Bob\n"
     * 	4. Run ClientChat again, entering the names in opposite order eg: "Bob\n" "Alice\n"
     * 	5. In the Frames that open up, type into the bottom one.
     */
    
//    public static void main (String[] args) {
//    	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//    	String you, oppo;
//    	try
//    	{
//    		you = stdIn.readLine();
//    		oppo = stdIn.readLine();
//    	}
//    	catch (IOException e1)
//    	{
//    		you = "p1";
//    		oppo = "p2";
//    		e1.printStackTrace();
//    	}
//    	
//    	JFrame t = new JFrame();
//    	t.setLayout(new BorderLayout());
//    	
//    	/* Example creation of ClientChat */
//    	OutputBox b = new OutputBox();
//    	t.add(b, BorderLayout.CENTER);
//    	ClientChat c = new ClientChat(you, oppo, b); 
//    	t.add(new InputBox(c, b), BorderLayout.SOUTH);
//    	
//    	t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // In the real application, call c.close() to safely close the ClientChat
//		t.pack(); 						
//		t.setLocationRelativeTo(null);	
//		t.setVisible(true);
//    }
    
    public ClientChat(String pUsername, String pOpponentUsername, OutputBox pOutput) {
    	aOpponentUsername = pOpponentUsername;
        try {
            Socket server = new Socket(HOST, PORT);
            
            // Create a thread to asynchronously read messages from the server
            try {
            	(new Thread(new ServerConn(server, pOutput))).start();
            }
            catch (IOException e){
            	System.err.println("Error creating server input thread: " + e);
            	System.exit(1);
            }
            
    		try	{
    			aOutput = new ObjectOutputStream(server.getOutputStream());
    			aOutput.writeObject(new ChatMessage(pUsername, INIT)); // Initialize connection with the username.
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
    public boolean sendMessage(String msg){
    	try	{
    		aOutput.writeObject(new ChatMessage(msg, aOpponentUsername));
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
 
class ServerConn implements Runnable {
    private BufferedReader aInput = null;
    private OutputBox aOutput = null;
 
    public ServerConn(Socket pServer, OutputBox pOutput) throws IOException {
        aInput = new BufferedReader(new InputStreamReader(pServer.getInputStream()));
        aOutput = pOutput;
    }
 
    public void run() {
		try {
			String msg;
			while ((msg = aInput.readLine()) != null) { /* loop reading messages from the server and show them on stdout */
				aOutput.append(msg);
			}
		} 
		catch (IOException e) { /* executed when StdIn reads QUIT message and closes the socket. */
			try {
				aInput.close();
			}
			catch (IOException e1) { 
				System.err.println("Error closing ServerIn: " + e1);
			}
		}
	}
}

@SuppressWarnings("serial")
class InputBox extends JPanel implements ActionListener
{
	private static final String YOU = "You: ";
	private JTextField aText = new JTextField(30);
	private ClientChat aChatInput = null;
	private OutputBox aOutput = null;
	
	public InputBox(ClientChat pChatInput, OutputBox pOutput){
		aChatInput = pChatInput;
		aOutput = pOutput;
		aText.setText("");
		aText.addActionListener(this);
		add(aText);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		aChatInput.sendMessage(aText.getText());
		aOutput.append(YOU + aText.getText());
		aText.setText("");
	}
}

@SuppressWarnings("serial")
class OutputBox extends JPanel
{
	private JTextArea aText= new JTextArea(6, 30);
	
	public OutputBox(){
		aText.setLineWrap(true);
		aText.setEditable(false);
		add(new JScrollPane(aText));
	}
	
	public void append(String pAppendedText){
		aText.append("\n" + pAppendedText);
		aText.setCaretPosition(aText.getDocument().getLength()); // moves the pane to the bottom.
	}
}