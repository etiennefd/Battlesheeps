package battlesheeps.client;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.board.Coordinate;
import battlesheeps.board.*;
import battlesheeps.server.ServerGame.Direction;
import battlesheeps.ships.*;

/**
 * ClientGame holds the information and methods to be displayed to (and get input from) the user. 
 * It has a reference to a full game state held in the ServerGame class. 
 * 
 * Important methods: 
 * - some way of updating (i.e. getting a new ServerGame reference)
 * - some way of computing the visible area (idea: create a new board matrix but replace invisible squares with sea)
 * - compute the range/valid inputs of various moves
 *
 */
import battlesheeps.board.Coordinate;
import battlesheeps.board.MineSquare;
import battlesheeps.board.Sea;
import battlesheeps.board.ShipSquare;
import battlesheeps.board.Square;
import battlesheeps.server.ServerGame;
import battlesheeps.ships.MineLayer;
import battlesheeps.ships.Ship;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.minueto.MinuetoColor;
import org.minueto.MinuetoEventQueue;
import org.minueto.MinuetoStopWatch;
import org.minueto.MinuetoTool;
import org.minueto.handlers.MinuetoFocusHandler;
import org.minueto.handlers.MinuetoKeyboard;
import org.minueto.handlers.MinuetoKeyboardHandler;
import org.minueto.handlers.MinuetoMouseHandler;
import org.minueto.image.MinuetoFont;
import org.minueto.image.MinuetoImage;
import org.minueto.image.MinuetoText;
import org.minueto.window.MinuetoPanel;


/* This will open a window with the following panels:
 * 1) the board game (fixed square, width a multiple of 30) 
 * 2) the ship info panel 
 * 3) a scrollable log entries (displays log entries)
 * 4) a scrollable chat panel (displays chat messages and allows user to enter one)
 * 
 */
public class ClientGame {

	private JDesktopPane aDesktop; //outer window
	private JFrame aMainFrame; //main 
	private GameBoard aBoardPanel;
	private MessagePanel aMessagePanel;
	private LogPanel aLogPanel;
//	private ChatPanel aChatPanel;

	private boolean open = true;
	private int one = 1;
	
	//should know who it's player is 
	private String aMyUser;
	
	//internal frame
	private Vector<JInternalFrame> internalFrame = new Vector<JInternalFrame>();

	public ClientGame(String pPlayer, ServerGame pGame) {

		aMyUser = pPlayer;
		
		this.aMainFrame = new JFrame();
		this.aMainFrame.setPreferredSize(new Dimension(1000,700));
		
		this.aMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.aMainFrame.setResizable(true);
		this.aMainFrame.setFocusTraversalKeysEnabled(false); //what?? 
//		this.aMainFrame.setSize(MinuetoTool.getDisplayWidth() - 100, 
//				MinuetoTool.getDisplayHeight() - 100);

		this.aMainFrame.setJMenuBar(createMenu()); //yay! a menu!

		this.aDesktop = new JDesktopPane();
		this.aDesktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		aDesktop.setBackground(new Color(126, 175, 152));

		this.aMainFrame.setContentPane(aDesktop);
		this.aMainFrame.setTitle("Battlesheeps");
		
		this.aMainFrame.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel background = new JPanel();
		background.setLayout(new BoxLayout(background, BoxLayout.X_AXIS));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.7);
		background.add(splitPane);
		
		JSplitPane sidePanel = new JSplitPane();
		sidePanel.setResizeWeight(0.5);
		sidePanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(sidePanel);
		
		JSplitPane sideBottom = new JSplitPane();
		sideBottom.setOrientation(JSplitPane.VERTICAL_SPLIT);
		sideBottom.setResizeWeight(0.5);
		sidePanel.setRightComponent(sideBottom);
		
		//LOG
		aLogPanel = new LogPanel();
		sideBottom.setLeftComponent(aLogPanel);
		
		//CHAT
//		aChatPanel = new ChatPanel();
//		sideBottom.setRightComponent(aChatPanel);
		
		String opponent;
		if (pPlayer.compareTo(pGame.getP1Username()) == 0) {
			opponent = pGame.getP2Username();
		} else opponent = pGame.getP1Username();
		
		//MESSAGES
		aMessagePanel = new MessagePanel(this, pPlayer, opponent);
		sidePanel.setLeftComponent(aMessagePanel);
		
		boolean turn;
		
		if (pPlayer.compareTo(pGame.getTurnPlayer()) == 0) {
			turn = true;
			aMessagePanel.setYourTurn();
		} else {
			turn = false;
			
			List<Ship> myList;
			List<Ship> oppList;
			
			if (aMyUser.compareTo(pGame.getP1Username()) == 0) {
				myList = pGame.getP1ShipList();
				oppList = pGame.getP2ShipList();
			}
			else {
				myList = pGame.getP2ShipList();
				oppList = pGame.getP1ShipList();
			}
			
			aMessagePanel.setNotYourTurn(myList, oppList);
		}
		
		aBoardPanel = new GameBoard(600, pPlayer, pGame.getBoard(), turn, this);
		aBoardPanel.setPreferredSize(new Dimension(600, 600));
		splitPane.setLeftComponent(aBoardPanel);
		
		this.aMainFrame.add(background);
		
		this.aMainFrame.pack();
		this.aMainFrame.setVisible(true);
		aBoardPanel.setVisible(true);
	}

	/*
	 * Menu will contain: 
	 * ... 
	 */
	private JMenuBar createMenu() {

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem newItem = new JMenuItem("Item1");
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//do something  
			}
		});
		fileMenu.add(newItem);
		
		fileMenu.addSeparator();
		
		JMenuItem newItem2 = new JMenuItem("Item2");
		newItem2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//do something
			}
		});
		fileMenu.add(newItem2);
		
		fileMenu.addSeparator();
		
		JMenuItem newItem3 = new JMenuItem("Item3");
		newItem3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//do something
			}
		});
		fileMenu.add(newItem3);
		 
		fileMenu.addSeparator();
		
		JMenuItem hideItem = new JMenuItem("Item4");
		hideItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					//do something
			}
		});
		fileMenu.add(hideItem);	
		
		fileMenu.addSeparator();
		
		fileMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Need more cleanup here
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);

		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(aMainFrame, "Battlesheeps game by Stefan Battiston, Etienne Fortier-Dubois, Lei Lopez and Kate Sprung", "About", JOptionPane.PLAIN_MESSAGE);
			}		
		});
		helpMenu.add(aboutItem);

		return menuBar;

	}		
	
	/*After each move, 
	 * 1. the board will be updated 
	 * 2. the log entries will be updated 
	 * 3. a message may be displayed  
	 */
	public void updateGame (ServerGame pGame) {
		String turnPlayer = pGame.getTurnPlayer(); 
		
		List<Ship> myList;
		List<Ship> oppList;
		
		if (aMyUser.compareTo(pGame.getP1Username()) == 0) {
			myList = pGame.getP1ShipList();
			oppList = pGame.getP2ShipList();
		}
		else {
			myList = pGame.getP2ShipList();
			oppList = pGame.getP1ShipList();
		}
		
		Square[][] visibleBoard = computeVisibility(pGame.getBoard(), myList);
				
		if (turnPlayer.compareTo(aMyUser) == 0){
			//my turn 
			aBoardPanel.updateTurn(visibleBoard, true);
			aMessagePanel.setYourTurn();
		} 
		else {
			aBoardPanel.updateTurn(visibleBoard, false);
			aMessagePanel.setNotYourTurn(myList, oppList);
		}
		
		//aLogPanel.addLogEntry(pLogEntry);
	}
	
	/**
	 * Client tells message panel to display menu for ship 
	 * @param pShip
	 */
	public void showShipMenu(Ship pShip) {
		aMessagePanel.displayShipMenu(pShip);
		//TODO: figure out base repair 
	}
	
	private Square[][] computeVisibility(Square[][] pBoard, List<Ship> pShipList) {

		//at the start everything is visible 
		Square[][] visibleBoard = pBoard; 
		//a list to hold the visible squares
		List<Coordinate> visibleRadarList = new ArrayList<Coordinate>();
		List<Coordinate> visibleSonarList = new ArrayList<Coordinate>();
		//add area around player's base to list

		//now calculate
		for (Ship ship : pShipList) {

			List<Coordinate> radarRange = ship.getRadarRange();

			for (Coordinate c : radarRange) {

				if (!visibleRadarList.contains(c)) {

					visibleRadarList.add(c);

					//the sonar of a mineLayer is equal to its radar 
					if (ship instanceof MineLayer) visibleSonarList.add(c);

				}
			}
		}

		Sea seaSquare = new Sea();
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j <30; j++) {

				//covering up any mines that shouldn't be visible 
				if (visibleBoard[i][j] instanceof MineSquare) {
					if (!visibleSonarList.contains(new Coordinate(i,j))){
						visibleBoard[i][j] = seaSquare;
					}
				}
				//and covering all enemy ships which are not in the radar
				if (!visibleRadarList.contains(new Coordinate(i,j))) {

					if (visibleBoard[i][j] instanceof ShipSquare) {

						//and it's not your own ship! 
						String user = (((ShipSquare)visibleBoard[i][j]).getShip()).getUsername();
						if (user != aMyUser) visibleBoard[i][j] = seaSquare;

					}
				}
			}
		}
		return visibleBoard;

	}

}
