package battlesheeps.client;

//TODO fix withdraw, make OPEN GAME WINDOW work, fix games list, make pretty

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import battlesheeps.accounts.Account;
import battlesheeps.networking.ClientLobby;
import battlesheeps.networking.Request;
import battlesheeps.networking.Request.LobbyRequest;

class ExitActionListener implements ActionListener
{
	private JFrame aFrame;
	private ClientLobby aClient;
	
	ExitActionListener(JFrame pFrame, ClientLobby pClient)
	{
		this.aFrame = pFrame;
		this.aClient = pClient;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//close socket
		aClient.close();
		aFrame.setVisible(false);
		aFrame.dispose();
		System.out.println("exit!");
	}
}

class LogoutActionListener implements ActionListener
{
	private JFrame aFrame;
	private ClientLobby aClient;
	
	LogoutActionListener(JFrame pFrame, ClientLobby pClient)
	{
		this.aFrame = pFrame;
		this.aClient = pClient;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		aFrame.setVisible(false);
		aFrame.dispose();
		System.out.println("logout!");
		//close socket
		aClient.close();
		new LoginScreen();
	}
}

/*
 * Handles user game request
 */
class ListMouseAdapter extends MouseAdapter
{
	private JList<Account> aList;
	private String requester;
	private ClientLobby aClient;
	
	public ListMouseAdapter(JList<Account> pList, String pRequester, ClientLobby pClient)
	{
		this.aClient = pClient;
		this.aList = pList;
		this.requester = pRequester;
	}
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = aList.locationToIndex(e.getPoint());
            System.out.println("Double clicked on Item " + index);
            if(index != -1 && !aList.getSelectedValue().equals("No other players online :("))
            {
	            SpringLayout layout = new SpringLayout();
	            //user name player requested info to label
	            String userRequested = aList.getSelectedValue().getUsername();
	            final JPanel requestPane = new JPanel();
	            JLabel waitLabel = new JLabel("Waiting for response from: ");
	            JLabel userLabel = new JLabel(userRequested);
	            requestPane.add(waitLabel);
	            requestPane.add(userLabel);
	            JButton withdrawButton = new JButton("Withdraw Request");
	            requestPane.add(withdrawButton);
	            
	            layout.putConstraint(SpringLayout.NORTH, waitLabel, 10, SpringLayout.NORTH, requestPane);
	            layout.putConstraint(SpringLayout.WEST, waitLabel, 10, SpringLayout.WEST, requestPane);
	
	            layout.putConstraint(SpringLayout.NORTH, userLabel, 10, SpringLayout.SOUTH, waitLabel);
	            layout.putConstraint(SpringLayout.WEST, userLabel, 10, SpringLayout.WEST, requestPane);
	            layout.putConstraint(SpringLayout.NORTH, withdrawButton, 10, SpringLayout.SOUTH, userLabel);
	            
	            layout.putConstraint(SpringLayout.WEST, withdrawButton, 10, SpringLayout.WEST, requestPane);
	            layout.putConstraint(SpringLayout.SOUTH, withdrawButton, -10, SpringLayout.SOUTH, requestPane);
	            
	            requestPane.setLayout(layout);
	            
				final JDialog dialog = new JDialog();
				dialog.setTitle("");
				dialog.setModal(true);
				dialog.setMinimumSize(new Dimension(225,125));
				dialog.setMaximumSize(new Dimension(225,125));
				dialog.setResizable(false);
		
				dialog.setContentPane(requestPane);
				aClient.getLobby().setRequesterDialog(dialog);
				
				//add action listener to withdraw
				withdrawButton.addActionListener(new WithdrawListener(dialog, aClient, requester, userRequested));
					
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.pack();
				
				aClient.sendRequest(new Request(LobbyRequest.REQUEST,requester,userRequested));
				
				dialog.setVisible(true);
            }
         }
    }
}

class WithdrawListener implements ActionListener
{
	private JDialog aWaitingRequest;
	private ClientLobby aClient;
	private String aRequester;
	private String aRequestee;
	
	public WithdrawListener(JDialog pWaitingRequest, ClientLobby pClient, String pRequester, String pRequestee)
	{
		this.aClient = pClient;
		this.aRequestee = pRequestee;
		this.aRequester = pRequester;
		this.aWaitingRequest = pWaitingRequest;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.aWaitingRequest.setVisible(false);
		this.aWaitingRequest.dispose();
		aClient.sendRequest(new Request(LobbyRequest.REQUEST_WITHDRAW,aRequester, aRequestee));
	}
}

class AcceptListener implements ActionListener
{
	private JDialog aRequestDialog;
	private ClientLobby aClient;
	private String requester;
	private String requestee;
	
	public AcceptListener(JDialog pWaitingRequest, ClientLobby pClient,
			String pRequester, String pRequestee)
	{
		this.aClient = pClient;
		this.requester = pRequester;
		this.requestee = pRequestee;
		this.aRequestDialog = pWaitingRequest;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.aRequestDialog.setVisible(false);
		this.aRequestDialog.dispose();
		aClient.sendRequest(new Request(LobbyRequest.ACCEPT, requester, requestee));
		aClient.getLobby().getFrame().setVisible(false);
		aClient.getLobby().getFrame().dispose();
		//TODO Open game
		
	}
}

class DeclineListener implements ActionListener
{
	private JDialog aRequest;
	private ClientLobby aClient;
	private String requester;
	private String requestee;
	
	public DeclineListener(JDialog pWaitingRequest, ClientLobby pClient, 
			String pRequester, String pRequestee)
	{
		this.aRequest = pWaitingRequest;
		this.aClient = pClient;
		this.requester = pRequester;
		this.requestee = pRequestee;

	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.aRequest.setVisible(false);
		this.aRequest.dispose();
		aClient.sendRequest(new Request(LobbyRequest.DECLINE, requester, requestee));
	}
}

class FrameListener implements WindowListener
{
	private ClientLobby aLobby;
	
	public FrameListener(ClientLobby pLobby)
	{
		this.aLobby = pLobby;
	}
	
	@Override
	public void windowActivated(WindowEvent e) {
		// don't care
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		aLobby.close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		aLobby.close();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// don't care		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// don't care		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// don't care		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// don't care		
	}
}

//Prettify: MAKE GAMES LIST BIGGER
public class Lobby 
{
	private ClientLobby aClient;
	private JList<Account> userList = new JList<Account>();
	private JPanel listsPanel = new JPanel();
	private DefaultListModel<Account> userData = new DefaultListModel<Account>();
	private JDialog aRequesterDialog;
	private JDialog aRequesteeDialog;
	private JFrame aFrame;
	
	//params: dialog box to close
    public Lobby(String pUsername) {
    	
    	aClient = new ClientLobby(pUsername, this);
    	//assuming request can be null
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        /*javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowLobby();
            }
        });*/
    }
    
    public void requestWithdrawn(String requester)
    {
    	if(requester.equals(aClient.getUsername()))
    	{
    		
    	}
    	else
    	{
    		System.out.println(aRequesteeDialog==null);
	    	aRequesteeDialog.setVisible(false);
	    	aRequesteeDialog.dispose();
	    	JOptionPane.showMessageDialog(null, requester + " has withdrawn their request.",
	    			"Request Withdrawn", JOptionPane.INFORMATION_MESSAGE);
    	}
    }
    
    public void requestAccepted()
    {
    	aRequesterDialog.setVisible(false);
    	aRequesterDialog.dispose();
    	aFrame.setVisible(false);
    	aFrame.dispose();
    	
    	//TODO: open game
    	
    }
    public void requestDeclined(String requestee)
    {
    	aRequesterDialog.setVisible(false);
    	aRequesterDialog.dispose();
    	JOptionPane.showMessageDialog(null, requestee + " has declined your request.",
    			"Request Withdrawn", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void requestPopup(String requester, String requestee)
    {
    	SpringLayout layout = new SpringLayout();

    	final JPanel requestPane = new JPanel();
        JLabel waitLabel = new JLabel("Game request from: ");
        JLabel userLabel = new JLabel(requester);
        requestPane.add(waitLabel);
        requestPane.add(userLabel);
        JButton acceptButton = new JButton("Accept");
        JButton declineButton = new JButton("Decline");
        requestPane.add(acceptButton);
        requestPane.add(declineButton);
        
        layout.putConstraint(SpringLayout.NORTH, waitLabel, 10, SpringLayout.NORTH, requestPane);
        layout.putConstraint(SpringLayout.WEST, waitLabel, 10, SpringLayout.WEST, requestPane);

        layout.putConstraint(SpringLayout.NORTH, userLabel, 10, SpringLayout.SOUTH, waitLabel);
        layout.putConstraint(SpringLayout.WEST, userLabel, 10, SpringLayout.WEST, requestPane);
        layout.putConstraint(SpringLayout.NORTH, acceptButton, 10, SpringLayout.SOUTH, userLabel);
        
        layout.putConstraint(SpringLayout.WEST, acceptButton, 10, SpringLayout.WEST, requestPane);
        layout.putConstraint(SpringLayout.SOUTH, acceptButton, -10, SpringLayout.SOUTH, requestPane);
        
        layout.putConstraint(SpringLayout.NORTH, declineButton, 10, SpringLayout.SOUTH, userLabel);
        layout.putConstraint(SpringLayout.WEST, declineButton, 10, SpringLayout.WEST, acceptButton);
        layout.putConstraint(SpringLayout.SOUTH, declineButton, -10, SpringLayout.SOUTH, requestPane);
        
        requestPane.setLayout(layout);
        
		final JDialog dialog = new JDialog();
		dialog.setTitle("");
		dialog.setModal(true);
		dialog.setMinimumSize(new Dimension(225,125));
		dialog.setMaximumSize(new Dimension(225,125));
		dialog.setResizable(false);

		dialog.setContentPane(requestPane);
		
		//add action listeners
		acceptButton.addActionListener(new AcceptListener(dialog, aClient, requester, requestee));
		declineButton.addActionListener(new DeclineListener(dialog,aClient, requester, requestee));
		
		//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		aRequesteeDialog = dialog;
		dialog.pack();
		dialog.setVisible(true);
    }
    
    public void updateOnlineUsers()
    {
    	userData.clear();
    	
    	if(aClient.getAccounts() != null && (aClient.getAccounts().size() !=0))
    	{
    		for (Account acct : aClient.getAccounts())
   	        {
   	        	userData.addElement(acct);
   	        }
    	}
    	
    	 /*if(aClient.getAccounts() == null || (aClient.getAccounts().size() ==0))
         {
         	userData.addElement("No other players online :(");
         }   
     	else
     	{
 	        for (Account acct : aClient.getAccounts())
 	        {
 	        	userData.addElement(acct.userString());
 	        }
         }*/ 
    }
    
    public void createAndShowLobby()
	{
		aFrame = new JFrame("Battlesheeps Lobby");
        aFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aFrame.addWindowListener(new FrameListener(aClient));
        
        aFrame.setMinimumSize(new Dimension(640,480));
       
        SpringLayout layout = new SpringLayout();
        userList.setModel(userData);
        
        //ArrayList<String> userData = new ArrayList<String>();
        updateOnlineUsers();
        MouseListener userListListener = new ListMouseAdapter(userList, aClient.getUsername(), aClient);
        userList.addMouseListener(userListListener);
        JScrollPane userPane = new JScrollPane(userList);
        listsPanel.add(userPane);

        layout.putConstraint(SpringLayout.NORTH, userPane, 10, SpringLayout.NORTH, listsPanel);
        layout.putConstraint(SpringLayout.WEST, userPane, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, userPane, -10, SpringLayout.EAST, listsPanel);
        
        //TODO populate save games list
        String[] testGamesList = {"Game1","Game2","Game3"};
        JList<String> gamesList = new JList<String>(testGamesList);
        //TODO write listener for games list
        //MouseListener gamesListListener = new ListMouseAdapter(gamesList,aClient.getUsername(), aClient);
        //gamesList.addMouseListener(gamesListListener);
        
        JScrollPane gamesPane = new JScrollPane(gamesList);
        listsPanel.add(gamesPane);
        
        layout.putConstraint(SpringLayout.NORTH, gamesPane, 10, SpringLayout.SOUTH, userPane);
        layout.putConstraint(SpringLayout.WEST, gamesPane, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, gamesPane, -10, SpringLayout.EAST, listsPanel);
        layout.putConstraint(SpringLayout.SOUTH, gamesPane, -10, SpringLayout.SOUTH, listsPanel);

        listsPanel.setLayout(layout);
        listsPanel.setPreferredSize(new Dimension(200,200));
        //end listsPanel editing
        
        //set to account info from client
        JTextArea accountPanel = new JTextArea("Your Account\n"+aClient.getUsername()
        		+"\nNumber of games won: "+aClient.getAccount().getNumGamesWon() 
        		+"\nNumber of games lost: "+aClient.getAccount().getNumGamesLost());
        accountPanel.setBackground(aFrame.getBackground());
        //Prettify: Change Font
         	
        aFrame.getContentPane().add(accountPanel);
        aFrame.getContentPane().add(listsPanel);

        //Prettify: Make menus same size as menu title
        JMenu battlesheepMenu = new JMenu("Battlesheeps");
        JMenuItem logoutButton = new JMenuItem("Logout");
        logoutButton.addActionListener(new LogoutActionListener(aFrame, aClient));
        battlesheepMenu.add(logoutButton);

        JMenuItem exitButton = new JMenuItem("Exit");
        
        exitButton.addActionListener(new ExitActionListener(aFrame, aClient));
        battlesheepMenu.add(exitButton);
        
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem soundButton = new JMenuItem("Sound");
        settingsMenu.add(soundButton);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem instructionsButton = new JMenuItem("Game Instructions");
        helpMenu.add(instructionsButton);
        JMenuItem creditsButton = new JMenuItem("Credits");
        helpMenu.add(creditsButton);
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(battlesheepMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        aFrame.setJMenuBar(menuBar);

        //set layout for main Panel
        layout.putConstraint(SpringLayout.NORTH, listsPanel, 10, SpringLayout.NORTH, aFrame.getContentPane());
        layout.putConstraint(SpringLayout.WEST, listsPanel, 10, SpringLayout.WEST, aFrame.getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, listsPanel, -10, SpringLayout.SOUTH, aFrame.getContentPane());
        layout.putConstraint(SpringLayout.EAST, listsPanel, -2, SpringLayout.WEST, accountPanel);
        
        layout.putConstraint(SpringLayout.NORTH, accountPanel, 10, SpringLayout.NORTH, aFrame.getContentPane());
        layout.putConstraint(SpringLayout.EAST, accountPanel, -10, SpringLayout.EAST, aFrame.getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, accountPanel, -10, SpringLayout.SOUTH, aFrame.getContentPane());
        
        aFrame.getContentPane().setLayout(layout);
        aFrame.pack();
        aFrame.setVisible(true);
	}
	 
	public JDialog getRequesteeDialog() {
		return aRequesteeDialog;
	}

	public void setRequesteeDialog(JDialog aRequestDialog) {
		this.aRequesteeDialog = aRequestDialog;
	}

	public JDialog getRequesterDialog() {
		return aRequesterDialog;
	}

	public void setRequesterDialog(JDialog aRequestedDialog) {
		this.aRequesterDialog = aRequestedDialog;
	}

	public JFrame getFrame() {
		return aFrame;
	}

/*    public static void main(String[] args) {
    	
    	new Lobby("sheep");
    	
    }*/
}

