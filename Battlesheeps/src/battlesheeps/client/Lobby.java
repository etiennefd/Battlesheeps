package battlesheeps.client;

//make pretty

import java.awt.Dialog.ModalityType;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import battlesheeps.accounts.Account;
import battlesheeps.networking.ClientGamesAndMoves;
import battlesheeps.networking.ClientLobby;
import battlesheeps.networking.LobbyMessageGameSummary;
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
class UserListMouseAdapter extends MouseAdapter
{
	private JList aList;
	private String requester;
	private ClientLobby aClient;
	private JFrame aFrame;
	
	public UserListMouseAdapter(JList pList, String pRequester, ClientLobby pClient, JFrame pFrame)
	{
		this.aFrame = pFrame;
		this.aClient = pClient;
		this.aList = pList;
		this.requester = pRequester;
	}
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = aList.locationToIndex(e.getPoint());
            System.out.println("Double clicked on Item " + index +" "+aList.getSelectedIndex());
            if(index != -1)
            {
	            SpringLayout layout = new SpringLayout();
	            //user name player requested info to label
	            String userRequested = (String) aList.getSelectedValue();
	            int endIndex = userRequested.indexOf(" ");
	            userRequested = userRequested.substring(0, endIndex);
	            //System.out.println("User requested"+userRequested);
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
				dialog.setLocationRelativeTo(aFrame);
				dialog.pack();
				
				aClient.sendRequest(new Request(LobbyRequest.REQUEST,requester,userRequested));
				
				dialog.setVisible(true);
				
            }
         }
    }
}
//listener for games list
class GamesListMouseAdapter extends MouseAdapter
{
	private JList aList;
	private String requester;
	private ClientLobby aClient;
	public GamesListMouseAdapter(JList pList, String pRequester, ClientLobby pClient)
	{
		this.aClient = pClient;
		this.aList = pList;
		this.requester = pRequester;
	}
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = aList.locationToIndex(e.getPoint());
            System.out.println("Double clicked on Item " + index);
            if(index != -1)
            {
            	//get user to request
            	String userRequested = (String) aList.getSelectedValue().toString();
            	int startIndex = userRequested.indexOf(">");
            	int endIndex = userRequested.indexOf("(");
	            userRequested = userRequested.substring(startIndex+1, endIndex-1);
	            System.out.println(userRequested);

	            SpringLayout layout = new SpringLayout();
	            
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
				dialog.setLocationRelativeTo(aClient.getLobby().getFrame());
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
	private boolean aNewGame;
	
	public AcceptListener(JDialog pWaitingRequest, ClientLobby pClient,
			String pRequester, String pRequestee, boolean isNewGame)
	{
		this.aClient = pClient;
		this.requester = pRequester;
		this.requestee = pRequestee;
		this.aRequestDialog = pWaitingRequest;
		this.aNewGame = isNewGame;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.aRequestDialog.setVisible(false);
		this.aRequestDialog.dispose();
		aClient.sendRequest(new Request(LobbyRequest.ACCEPT, requester, requestee));
		aClient.getLobby().getFrame().setVisible(false);
		aClient.getLobby().getFrame().dispose();
		//Open game board
		
		if(aNewGame)	//open new game
		{
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ClientGame cg = new ClientGame(requestee);
					new ClientGamesAndMoves(requestee, null, 0, cg);
				}
				
			});
			
		}
		//open game with old game id
		else 
		{
			for(LobbyMessageGameSummary game : aClient.getSavedGames())
			{
				if(game.getPlayer1().getUsername().equals(requester) ||
						game.getPlayer2().getUsername().equals(requester))
				{
					final int gameID = game.getGameID();
					System.out.println("Requestee gameID: "+gameID);
					
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							ClientGame cg = new ClientGame(requestee);
							new ClientGamesAndMoves(requestee, null, gameID, cg);
						}
						
					});
					
				}
			}
			
		}
	}
}

class DeclineListener implements ActionListener
{
	private JDialog aRequest;
	private ClientLobby aClient;
	private String requester;
	private String requestee;
	//private boolean aNewGame;
	
	public DeclineListener(JDialog pWaitingRequest, ClientLobby pClient, 
			String pRequester, String pRequestee, boolean isNewGame)
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
		System.out.println("Window closed.");
		aLobby.close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		System.out.println("Window closed.");
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
	
	private JList userList = new JList();
	private DefaultListModel userData = new DefaultListModel();
	
	private JList gamesList = new JList();
	private DefaultListModel gamesData = new DefaultListModel();

	private JPanel listsPanel = new JPanel();
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
    	if(!requester.equals(aClient.getUsername()))
    	{
    		//System.out.println("Requestee exists: "+aRequesteeDialog==null);
	    	aRequesteeDialog.setVisible(false);
	    	aRequesteeDialog.dispose();
	    	JOptionPane.showMessageDialog(null, requester + " has withdrawn their request.",
	    			"Request Withdrawn", JOptionPane.INFORMATION_MESSAGE);
    	}
    }
    
    public void requestAccepted(final String requestee,boolean isNewGame)
    {
    	aRequesterDialog.setVisible(false);
    	aRequesterDialog.dispose();
    	aFrame.setVisible(false);
    	aFrame.dispose();
    	System.out.println("lobby closed.");
    	//Open game board
    	
		if(isNewGame)
		{
			System.out.println("Requester " +aClient.getUsername()+ " new game.");
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					ClientGame cg = new ClientGame(aClient.getUsername());
					new ClientGamesAndMoves(aClient.getUsername(), requestee, 0, cg);
				}
				
			});
			
		}
		//open game with old game id
		else 
		{
			System.out.println("opening old game.");
			for(LobbyMessageGameSummary game : aClient.getSavedGames())
			{
				if(game.getPlayer1().getUsername().equals(requestee) ||
						game.getPlayer2().getUsername().equals(requestee))
				{
					final int gameID = game.getGameID();
					
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							ClientGame cg = new ClientGame(aClient.getUsername());
							new ClientGamesAndMoves(aClient.getUsername(), null, gameID, cg);
						}
					
					});
					
				}
			}
			
		}
    	
    }
    public void requestDeclined(String requestee)
    {
    	aRequesterDialog.setVisible(false);
    	aRequesterDialog.dispose();
    	JOptionPane.showMessageDialog(null, requestee + " has declined your request.",
    			"Request Withdrawn", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void requestPopup(String requester, String requestee, boolean isNewGame)
    {
    	SpringLayout layout = new SpringLayout();

    	final JPanel requestPane = new JPanel();
        
    	JLabel waitLabel;
    	if(isNewGame)
    	{
    		waitLabel = new JLabel("New Game request from: ");
    	}
    	else
    	{
    		waitLabel = new JLabel("Continue Game request from: ");
    	}
        
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
        layout.putConstraint(SpringLayout.WEST, declineButton, 10, SpringLayout.EAST, acceptButton);
        layout.putConstraint(SpringLayout.SOUTH, declineButton, -10, SpringLayout.SOUTH, requestPane);
        layout.putConstraint(SpringLayout.EAST, declineButton, -10, SpringLayout.EAST, requestPane);

        requestPane.setLayout(layout);
        
		final JDialog dialog = new JDialog();
		dialog.setTitle("");
		dialog.setModal(true);
		dialog.setMinimumSize(new Dimension(225,125));
		dialog.setMaximumSize(new Dimension(225,125));
		dialog.setResizable(false);

		dialog.setContentPane(requestPane);
		
		//add action listeners
		acceptButton.addActionListener(new AcceptListener(dialog, aClient, requester, requestee,isNewGame));
		declineButton.addActionListener(new DeclineListener(dialog,aClient, requester, requestee,isNewGame));
		
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setLocationRelativeTo(aFrame);
		aRequesteeDialog = dialog;
		aRequesteeDialog.setModalityType(ModalityType.MODELESS);
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
   	        	userData.addElement(acct.toString());
   	        }
    	}
    }
    
    public void  updateSavedGames()
    {
    	gamesData.clear();
    	
    	System.out.println("updating saved games");
    	if(aClient.getSavedGames() != null)
    	{
    		System.out.println("user has saved games");
    		for (LobbyMessageGameSummary game : aClient.getSavedGames())
   	        {	
    			
    			//below this user is player one in this game
    			if(game.getPlayer1().equals(aClient.getAccount()))
    			{

    				System.out.println("user is player 1, player 2 is "+game.getPlayer2().getUsername());
    				
    				//remove this if
    		    	if(aClient.getAccounts() != null && (aClient.getAccounts().size() !=0))
    		    	{
    		    		System.out.println("first element " +aClient.getAccounts().get(0).getUsername());
    		    		for (Account acct : aClient.getAccounts())
    		   	        {
    		   	        	System.out.println(acct.getUsername());
    		   	        	
    		   	       //below this checks if player 2 of this game is online
    	    				if(acct.getUsername().equals(game.getPlayer2().getUsername()))
    	    				{
    	    					System.out.println("user is player 1 and player 2 is online");
    	    					System.out.println("adding game: "+game.getGameID());
    	    					gamesData.addElement(game);
    	    					
    	    					//remove opponent from available players list
    	    					userData.removeElement(acct.toString());
    	    				}
    		   	        }
    		    	}
    			}
    			else //this user is player two in this game
				{
    				System.out.println("user is player 2, player 1 is "+game.getPlayer1().getUsername());
    				
    				//remove this if
    		    	if(aClient.getAccounts() != null && (aClient.getAccounts().size() !=0))
    		    	{
    		    		for (Account acct : aClient.getAccounts())
    		   	        {
    		   	        	System.out.println(acct.getUsername());
    		   	       //below checks if player 1 of this game is online
    	    				if(acct.getUsername().equals(game.getPlayer1().getUsername()))
    	    				{
    	    					System.out.println("user is player 2 and player 1 is online");
    	    					System.out.println("adding game: "+game.getGameID());
    	    					gamesData.addElement(game);
    	    					
    	    					//remove opponent from available players lisr
    	    					userData.removeElement(acct.toString());
    	    				}
    		   	        }
    		    	}
    				
				}
   	        }
    	}
    }
    
    public void createAndShowLobby()
	{
		aFrame = new JFrame("Battlesheeps Lobby");
        aFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aFrame.addWindowListener(new FrameListener(aClient));
        
        aFrame.setMinimumSize(new Dimension(640,480));
       
        SpringLayout layout = new SpringLayout();
        JLabel listTitle = new JLabel("AVAILABLE PLAYERS");
        listsPanel.add(listTitle);
        
        userList.setModel(userData);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
               
        //ArrayList<String> userData = new ArrayList<String>();
        updateOnlineUsers();
        MouseListener userListListener = new UserListMouseAdapter(userList, aClient.getUsername(), aClient, aFrame);
        userList.addMouseListener(userListListener);
        JScrollPane userPane = new JScrollPane(userList);
        listsPanel.add(userPane);

        layout.putConstraint(SpringLayout.NORTH, listTitle, 5, SpringLayout.NORTH, listsPanel);
        layout.putConstraint(SpringLayout.WEST, listTitle, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, listTitle, -10, SpringLayout.EAST, listsPanel);
        //layout.putConstraint(SpringLayout.SOUTH, listTitle, 10, SpringLayout.NORTH, userPane);
        
        layout.putConstraint(SpringLayout.NORTH, userPane, 5, SpringLayout.SOUTH, listTitle);
        layout.putConstraint(SpringLayout.WEST, userPane, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, userPane, -10, SpringLayout.EAST, listsPanel);
                
        //populate save games list
        JLabel gamesTitle = new JLabel("AVAILABLE GAMES TO CONTINUE");
        listsPanel.add(gamesTitle);
        
        gamesList.setModel(gamesData);
        gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        updateSavedGames();
        MouseListener gamesListListener = new GamesListMouseAdapter(gamesList,aClient.getUsername(), aClient);
        gamesList.addMouseListener(gamesListListener);
        
        JScrollPane gamesPane = new JScrollPane(gamesList);
        listsPanel.add(gamesPane);
        
        layout.putConstraint(SpringLayout.NORTH, gamesTitle, 5, SpringLayout.SOUTH, userPane);
        layout.putConstraint(SpringLayout.WEST, gamesTitle, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, gamesTitle, -10, SpringLayout.EAST, listsPanel);
        
        layout.putConstraint(SpringLayout.NORTH, gamesPane, 5, SpringLayout.SOUTH, gamesTitle);
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
        //Menu bar
        JMenu battlesheepMenu = new JMenu("Battlesheeps");
        JMenuItem logoutButton = new JMenuItem("Logout");
        logoutButton.addActionListener(new LogoutActionListener(aFrame, aClient));
        battlesheepMenu.add(logoutButton);

        JMenuItem exitButton = new JMenuItem("Exit");
        
        exitButton.addActionListener(new ExitActionListener(aFrame, aClient));
        battlesheepMenu.add(exitButton);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem instructionsButton = new JMenuItem("Game Instructions");
        instructionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(aFrame, "Double click on a player to send a game request. If a player with whom\n" +
						"you have already started a game is available, you can continue that game.\n" +
						"Once a game starts, both players must accept the obstacle configuration. \n" +
						"Then they can place their ships, and then play. \n\nThe goal is to sink every enemy ship.", 
						"Help", JOptionPane.PLAIN_MESSAGE);
			}		
		});
        helpMenu.add(instructionsButton);
        JMenuItem creditsButton = new JMenuItem("Credits");
        creditsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(aFrame, "Battlesheeps game by Stefan Battiston, Etienne Fortier-Dubois, Lei Lopez and Kate Sprung\n\n" +
						"Designed by Joerg Kienzle.\n" +
						"Images from Pokemon Crystal version (Copyright Game Freak Inc. 2001)\n" +
						"Sounds?\n\n" +
						"Montreal, 2014", "About", JOptionPane.PLAIN_MESSAGE);
			}		
		});
        helpMenu.add(creditsButton);
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(battlesheepMenu);
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
        aFrame.setLocationRelativeTo(null);
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

