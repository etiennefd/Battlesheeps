package battlesheeps.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import battlesheeps.networking.ClientChat;

public class ChatPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 7338671942848528879L;
	
	private static final String YOU = "You: ";
	private JTextField aTextIn = new JTextField(0);
	private JTextArea aTextOut = new JTextArea(0, 0);
	private ClientChat aOpponent = null;
	private String aUsername = null;
	
	public ChatPanel(String pUsername){
		super();
		this.setLayout(new BorderLayout());

		aUsername = pUsername;
		
		aTextOut.setLineWrap(true);
		aTextOut.setEditable(false);
		this.add(new JScrollPane(aTextOut), BorderLayout.CENTER);
		
		aTextIn.setText("");
		aTextIn.addActionListener(this);
    	this.add(aTextIn, BorderLayout.SOUTH);
	}
	
	public void setOpponent(String pOpponent){
		if (aOpponent == null){
			aOpponent = new ClientChat(aUsername, pOpponent, aTextOut);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		aOpponent.sendMessage(aTextIn.getText());
		aTextOut.append("\n" + YOU + aTextIn.getText());
		aTextOut.setCaretPosition(aTextOut.getDocument().getLength()); // moves the pane to the bottom.
		aTextIn.setText("");
	}
	
//	public static void main(String[] args)
//	{
//		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
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
//		JFrame t = new JFrame();
//		ChatPanel p = new ChatPanel(you);
//		t.add(p);
//		
//		t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // In the real application, call c.close() to safely close the ClientChat
//		t.pack(); 						
//		t.setLocationRelativeTo(null);	
//		t.setVisible(true);
//
//		p.setOpponent(oppo);
//	}

}