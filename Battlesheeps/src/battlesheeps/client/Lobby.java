package battlesheeps.client;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

class ExitActionListener implements ActionListener
{
	private JFrame aFrame;
	
	ExitActionListener(JFrame pFrame)
	{
		this.aFrame = pFrame;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		aFrame.setVisible(false);
		aFrame.dispose();
		System.out.println("exit!");
		//TODO: close socket
	}
}

class LogoutActionListener implements ActionListener
{
	private JFrame aFrame;
	
	LogoutActionListener(JFrame pFrame)
	{
		this.aFrame = pFrame;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		aFrame.setVisible(false);
		aFrame.dispose();
		System.out.println("logout!");
		//TODO: close socket
		new LoginScreen();
	}
}

/*
 * Handles user game request
 */
class ListMouseAdapter extends MouseAdapter
{
	private JList<String> aList;
	
	//TODO: CHANGE String TO WHAT OBJECT WE NEED
	public ListMouseAdapter(JList<String> pList)
	{
		this.aList = pList;
	}
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = aList.locationToIndex(e.getPoint());
            System.out.println("Double clicked on Item " + index);
            
            SpringLayout layout = new SpringLayout();
            //user name player requested info to label
            String userRequested = aList.getSelectedValue();
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
			dialog.setResizable(false);
	
			dialog.setContentPane(requestPane);
			
			//add action listener to withdraw
			withdrawButton.addActionListener(new WithdrawListener(dialog));
	
			//TODO: UNCOMMENT BELOW
			//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();
	
			dialog.setVisible(true);
         }
    }
}

class WithdrawListener implements ActionListener
{
	private JDialog aWaitingRequest;
	
	WithdrawListener(JDialog pWaitingRequest)
	{
		this.aWaitingRequest = pWaitingRequest;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.aWaitingRequest.setVisible(false);
		this.aWaitingRequest.dispose();
		//TODO: SEND REQUEST WITHDRAWAL TO SERVER
		
	}
}

//TODO: MAKE GAMES LIST BIGGER
public class Lobby 
{
	//params: dialog box to close
    public Lobby(JDialog pLoginDialog) {
    	
    	pLoginDialog.setVisible(false);
    	pLoginDialog.dispose();
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowLobby();
            }
        });
    }
	
	private static void createAndShowLobby()
	//need input for account info (updates with Lobby is entered)
	//need input lists (updates at a certain interval)
	{
		JFrame frame = new JFrame("Battlesheeps Lobby");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setMinimumSize(new Dimension(640,480));
        
        //TODO: GET INPUT FROM SERVER (thru client)
        JPanel listsPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        
        //TODO: REPLACE String WITH OBJECT CONTAINING USER INFO
        String[] testUserList = {"Alice","Bob","Carl"};
        JList<String> userList = new JList<String>(testUserList);
        MouseListener userListListener = new ListMouseAdapter(userList);
        userList.addMouseListener(userListListener);
        
        JScrollPane userPane = new JScrollPane(userList);
        listsPanel.add(userPane);

        layout.putConstraint(SpringLayout.NORTH, userPane, 10, SpringLayout.NORTH, listsPanel);
        layout.putConstraint(SpringLayout.WEST, userPane, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, userPane, -10, SpringLayout.EAST, listsPanel);
        
        String[] testGamesList = {"Game1","Game2","Game3"};
        JList<String> gamesList = new JList<String>(testGamesList);
        MouseListener gamesListListener = new ListMouseAdapter(gamesList);
        gamesList.addMouseListener(gamesListListener);
        
        JScrollPane gamesPane = new JScrollPane(gamesList);
        listsPanel.add(gamesPane);
        
        layout.putConstraint(SpringLayout.NORTH, gamesPane, 10, SpringLayout.SOUTH, userPane);
        layout.putConstraint(SpringLayout.WEST, gamesPane, 10, SpringLayout.WEST, listsPanel);
        layout.putConstraint(SpringLayout.EAST, gamesPane, -10, SpringLayout.EAST, listsPanel);
        layout.putConstraint(SpringLayout.SOUTH, gamesPane, -10, SpringLayout.SOUTH, listsPanel);

        listsPanel.setLayout(layout);
        listsPanel.setPreferredSize(new Dimension(200,200));
        //end listsPanel editing
        
        //TODO: set to account info from client
        JTextArea accountPanel = new JTextArea("Your Account\nFabio\n\nWins: 0\nLosses: 42\n\nJoined on: Feb.14/14");
        accountPanel.setBackground(frame.getBackground());
        //Prettify: Change Font
        
        frame.getContentPane().add(accountPanel);
        frame.getContentPane().add(listsPanel);

        //Prettify: Make menus same size as menu title
        JMenu battlesheepMenu = new JMenu("Battlesheeps");
        JMenuItem logoutButton = new JMenuItem("Logout");
        logoutButton.addActionListener(new LogoutActionListener(frame));
        battlesheepMenu.add(logoutButton);

        JMenuItem exitButton = new JMenuItem("Exit");
        
        exitButton.addActionListener(new ExitActionListener(frame));
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
        frame.setJMenuBar(menuBar);

        //set layout for main Panel
        layout.putConstraint(SpringLayout.NORTH, listsPanel, 10, SpringLayout.NORTH, frame.getContentPane());
        layout.putConstraint(SpringLayout.WEST, listsPanel, 10, SpringLayout.WEST, frame.getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, listsPanel, -10, SpringLayout.SOUTH, frame.getContentPane());
        layout.putConstraint(SpringLayout.EAST, listsPanel, -2, SpringLayout.WEST, accountPanel);
        
        layout.putConstraint(SpringLayout.NORTH, accountPanel, 10, SpringLayout.NORTH, frame.getContentPane());
        layout.putConstraint(SpringLayout.EAST, accountPanel, -10, SpringLayout.EAST, frame.getContentPane());
        layout.putConstraint(SpringLayout.SOUTH, accountPanel, -10, SpringLayout.SOUTH, frame.getContentPane());
        
        frame.getContentPane().setLayout(layout);
        frame.pack();
        frame.setVisible(true);
	}
	 
	//params: dialog box to close
    public static void main(String[] args) {
    	
    	//TODO: close dialog box
    	
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowLobby();
            }
        });
    }
}

