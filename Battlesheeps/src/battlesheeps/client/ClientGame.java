package battlesheeps.client;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.board.*;
import battlesheeps.server.Move;
import battlesheeps.server.ServerGame.Direction;
import battlesheeps.server.ServerGame.MoveType;
import battlesheeps.ships.*;

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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;


/* This will open a window with the following panels:
 * 1) the board game (fixed square, width a multiple of 30) 
 * 2) the ship message panel 
 * 3) a scrollable log entries (displays log entries)
 * 4) a scrollable chat panel (displays chat messages and allows user to enter one)
 * 
 * It also holds the game logic on the Client side. 
 */
public class ClientGame {

	private JDesktopPane aDesktop; //outer window
	private JFrame aMainFrame; //main 
	private GameBoard aBoardPanel;
	private MessagePanel aMessagePanel;
	private LogPanel aLogPanel;
	private JPanel aChatPanel;

	//should know who it's player is 
	private String aMyUser;
	private String aMyOpponent;
	private boolean aPlayer1; //if true, base is on WEST, else EAST
	
	private Square[][] aCurrentVisibleBoard;
	private Ship aCurrentClickedShip; 
	private MoveType aCurrentClickedMove;
	
	//internal frame
	private Vector<JInternalFrame> internalFrame = new Vector<JInternalFrame>();

	public ClientGame(String pPlayer, ServerGame pGame) {

		aMyUser = pPlayer;
		if (pPlayer.compareTo(pGame.getP1Username()) == 0) {
			aPlayer1 = true;
			aMyOpponent = pGame.getP2Username();
		}
		else{
			aPlayer1 = false;
			aMyOpponent = pGame.getP1Username();
		}
		
		GraphicsDevice grdDevice;
		GraphicsConfiguration grcConfiguration;
		GraphicsEnvironment greEnvironment;

		greEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		grdDevice = greEnvironment.getDefaultScreenDevice();
		grcConfiguration = grdDevice.getDefaultConfiguration();

		this.aMainFrame = new JFrame(grcConfiguration); 

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
		splitPane.setResizeWeight(0.8);
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
		JPanel chat = new JPanel();
		sideBottom.setRightComponent(chat);
		
		String opponent;
		if (aPlayer1) opponent = pGame.getP2Username();
		else opponent = pGame.getP1Username();
		
		//MESSAGES
		aMessagePanel = new MessagePanel(this, pPlayer, opponent);
		sidePanel.setLeftComponent(aMessagePanel);
		
		boolean isTurn;
		
		if (pPlayer.compareTo(pGame.getTurnPlayer()) == 0) {
			isTurn = true;
			aMessagePanel.setYourTurn();
		} else {
			isTurn = false;
			
			List<Ship> myList;
			List<Ship> oppList;
			
			if (aPlayer1) {
				myList = pGame.getP1ShipList();
				oppList = pGame.getP2ShipList();
			}
			else {
				myList = pGame.getP2ShipList();
				oppList = pGame.getP1ShipList();
			}
			
			aMessagePanel.setNotYourTurn(myList, oppList);
		}
		
		//computing the visible board to send the GUI
		List<Ship> shipList;
		if (aPlayer1) shipList = pGame.getP1ShipList();
		else shipList = pGame.getP2ShipList();
		aCurrentVisibleBoard = computeVisibility(pGame.getBoard(), shipList);
	
		//and creating the board panel 
		aBoardPanel = new GameBoard(600, pPlayer, aCurrentVisibleBoard, isTurn, this);
		
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
		
		if (aPlayer1) {
			myList = pGame.getP1ShipList();
			oppList = pGame.getP2ShipList();
		}
		else {
			myList = pGame.getP2ShipList();
			oppList = pGame.getP1ShipList();
		}
		
		aCurrentVisibleBoard = computeVisibility(pGame.getBoard(), myList);
				
		if (turnPlayer.compareTo(aMyUser) == 0){
			//my turn 
			aBoardPanel.updateTurn(aCurrentVisibleBoard, true);
			aMessagePanel.setYourTurn();
		} 
		else {
			aBoardPanel.updateTurn(aCurrentVisibleBoard, false);
			aMessagePanel.setNotYourTurn(myList, oppList);
		}
		
		//TODO
		//aLogPanel.addLogEntry(pLogEntry);
	}
	
	/**
	 * Client tells message panel to display menu for ship 
	 * @param pShip
	 */
	public void showShipMenu(Ship pShip) {
		
		aCurrentClickedShip = pShip;
		
		String p1;
		if (aPlayer1) p1 = aMyUser;
		else p1 = aMyOpponent;
		
		boolean atBase = pShip.isAtHomeBase(p1);
		aMessagePanel.displayShipMenu(pShip, atBase);
	}
	
	public void displayWaitingMessage() {
		aMessagePanel.displayMessage("Waiting for Server");
	}
	
	private Square[][] computeVisibility(Square[][] pBoard, List<Ship> pShipList) {

		//at the start everything is visible 
		Square[][] visibleBoard = new Square[30][30];
		
		for (int i = 0; i <30; i++) {
			for (int j = 0; j < 30; j++){
				visibleBoard[i][j] = pBoard[i][j];
			}
		}
		//a list to hold the visible squares by radar
		List<Coordinate> visibleRadarList = new ArrayList<Coordinate>();
	
		//plus need to add area around player's base to list
		int baseX;
		if (aPlayer1) {
			baseX = 1;
			visibleRadarList.add(new Coordinate(0, 9));
			visibleRadarList.add(new Coordinate(0, 20));
		}
		else {
			baseX = 28;
			visibleRadarList.add(new Coordinate(29, 9));
			visibleRadarList.add(new Coordinate(29, 20));
		}
		
		for (int i = 9; i < 21; i++){
			visibleRadarList.add(new Coordinate(baseX, i));
		}
		
		//and a list to hold the visible squares by sonar
		List<Coordinate> visibleSonarList = new ArrayList<Coordinate>();

		//now calculate
		for (Ship ship : pShipList) {

			List<Coordinate> radarRange = ship.getRadarRange();

			for (Coordinate c : radarRange) {
				//if the list doesn't already contain this coordinate
				if (!(visibleRadarList.contains(c))) {
					//then add it
					visibleRadarList.add(c);
				}
				
				//and if the ship is a Mine Layer
				//we need to add it to the sonar list (sonar = radar)
				if (ship instanceof MineLayer) {
					if (!(visibleSonarList.contains(c))) {
						visibleSonarList.add(c);
					}
				}
			}
		}

		Sea seaSquare = new Sea();
	
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j <30; j++) {

				//covering up any mines that shouldn't be visible 
				if (visibleBoard[i][j] instanceof MineSquare) {
					//if the sonar list doesn't contain this coordinate 
					if (!(visibleSonarList.contains(new Coordinate(i,j)))){
						//then cover it with sea 
						visibleBoard[i][j] = seaSquare;
					}
				}
				//and covering all enemy ships which aren't in the radar
				if (visibleBoard[i][j] instanceof ShipSquare) {
					//if the radar list doesn't contain this coordinate 
					if (!(visibleRadarList.contains(new Coordinate(i,j)))) {
						//and it's not your own ship 
						String user = (((ShipSquare)visibleBoard[i][j]).getShip()).getUsername();
						//then cover it with sea
						if (!(user.compareTo(aMyUser)== 0)) visibleBoard[i][j] = seaSquare;

					}
				}
			}
		}

		//and some for sonar (which trumps radar) 
		SonarSquare sonar = new SonarSquare();
		for (Coordinate c : visibleSonarList) {
			if (visibleBoard[c.getX()][c.getY()] instanceof Sea) {
				visibleBoard[c.getX()][c.getY()] = sonar;
			}
		}
		
		//and turning all the sea squares into radar squares if they're in the list
		RadarSquare radar = new RadarSquare();
		for (Coordinate c : visibleRadarList) {
			if (visibleBoard[c.getX()][c.getY()] instanceof Sea) {
				visibleBoard[c.getX()][c.getY()] = radar;
			}
		}

		return visibleBoard;

	}
	//the Message Panel sends the chosen move to the Client
	//the Client then calculates where the move could occur on the board
	//and sends this info to the Game Board
	//or it sends the info straight to the Server in the case of radar changes 
	protected void translateSelected(Ship pShip) {
		/*Translate ship allows the ship to be moved 
		 * a) back one square, as long as that square is Sea
		 * b) to either side, provided the length of the ship is all Sea
		 * c) to the front, for as far as their actual speed allows, and it's Sea
		 */
		aCurrentClickedMove = MoveType.TRANSLATE_SHIP;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		Direction direction = pShip.getDirection();
		
		//MOVING BACKWARDS
		Coordinate tail = pShip.getTail();
		int tailX = tail.getX();
		int tailY = tail.getY();
		
		Coordinate behindTail;
		
		switch(direction){
		case NORTH : 
			behindTail = new Coordinate(tailX,tailY+1);
			break;
		case SOUTH : 
			behindTail = new Coordinate(tailX, tailY-1);
			break;
		case WEST : 
			behindTail = new Coordinate(tailX+1, tailY);
			break;
		default : /*EAST*/
			behindTail = new Coordinate(tailX-1, tailY);
			break;
		}
		
		//if the coordinate is in bounds and is Sea, then we can move there
		if (behindTail.inBounds()) {
			if (clearSquare(aCurrentVisibleBoard[behindTail.getX()][behindTail.getY()])){
				greenList.add(behindTail);
			}
		}
		
		//MOVING SIDEWAYS
		int size = pShip.getSize();
		//creating a list for each side of the boat
		//for the boat to move sideways, each of those squares must be clear 
		Coordinate[] starboard = new Coordinate[size];
		Coordinate[] port = new Coordinate[size];
		
		switch(direction) {
		case NORTH: 
			for (int i = 0; i < size; i++) {
				port[i] = new Coordinate(tailX-1, tailY-i);
				starboard[i] = new Coordinate(tailX+1, tailY-i);
			}
			break;
		case SOUTH: 
			for (int i = 0; i < size; i++) {
				port[i] = new Coordinate(tailX-1, tailY+i);
				starboard[i] = new Coordinate(tailX+1, tailY+i);
			}
			break;
		case WEST: 
			for (int i = 0; i < size; i++) {
				port[i] = new Coordinate(tailX-i, tailY+1);
				starboard[i] = new Coordinate(tailX-i, tailY-1);
			}
			break;
		default : /*EAST*/
			for (int i = 0; i < size; i++) {
				port[i] = new Coordinate(tailX+i, tailY-1);
				starboard[i] = new Coordinate(tailX+i, tailY+1);
			}
			break;
		}
		
		boolean allClearPort = true;
		boolean allClearStarboard = true;
		
		for (Coordinate c : port) {
			if (c.inBounds()) {
				Square currentSquare = aCurrentVisibleBoard[c.getX()][c.getY()];
				//if the square at that coordinate isn't Sea,
				//then you can't move there 
				if (!(clearSquare(currentSquare))) {
					allClearPort = false;
					break;
				}
				//or if the coordinate is out of bounds
			} else {
				allClearPort = false;
				break;
			}
		}
		
		if (allClearPort) {
			for (Coordinate c : port) greenList.add(c);
		}
		
		for (Coordinate c : starboard) {
			if (c.inBounds()) {
				Square currentSquare = aCurrentVisibleBoard[c.getX()][c.getY()];
				//if the square at that coordinate isn't Sea,
				//then you can't move there 
				if (!(clearSquare(currentSquare))) {
					allClearStarboard = false;
					break;
				}
				//or if the coordinate is out of bounds
			} else {
				allClearStarboard = false;
				break;
			}
		}
		
		if (allClearStarboard) {
			for (Coordinate c : starboard) greenList.add(c);
		}
		
		//MOVING FORWARDS
		Coordinate head = pShip.getHead();
		int headX = head.getX();
		int headY = head.getY();
		int speed = pShip.getActualSpeed();
		
		Coordinate[] forward = new Coordinate[speed];
		
		switch(direction) {
		case NORTH: 
			for (int i = 0; i<speed; i++){
				forward[i] = new Coordinate(headX, headY-(i+1));
			}
			break;
		case SOUTH: 
			for (int i = 0; i<speed; i++){
				forward[i] = new Coordinate(headX, headY+(i+1));
			}
			break;
		case WEST: 
			for (int i = 0; i<speed; i++){
				forward[i] = new Coordinate(headX-(i+1), headY);
			}
			break;
		default : /*EAST*/
			for (int i = 0; i<speed; i++){
				forward[i] = new Coordinate(headX+(i+1), headY);
			}
			break;
		}
		
		//only allowing you to move forward to Sea 
		//breaking once you reach either a square which is not Sea or out of bounds
		for (Coordinate c : forward){
			if (c.inBounds()){
				if (clearSquare(aCurrentVisibleBoard[c.getX()][c.getY()])){
					greenList.add(c);
				}
				else break;
			}
			else break;
		}
		
		aBoardPanel.showAvailableMoves(greenList);
		
	}
	
	protected void turnSelected (Ship pShip) {
		
		aCurrentClickedMove = MoveType.TRANSLATE_SHIP;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		
		
		aBoardPanel.showAvailableMoves(greenList);
		
		
	}
	
	protected void cannonSelected(Ship pShip) {
		
		aCurrentClickedMove = MoveType.FIRE_CANNON;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		
		
		aBoardPanel.showAvailableMoves(greenList);
		
	}
	
	protected void layMineSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.DROP_MINE;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		
		
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void retrieveMineSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.PICKUP_MINE;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		
		
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void torpedoSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.FIRE_TORPEDO;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		
		
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void turnExtendedRadarOn (Ship pShip) {
		aCurrentClickedMove = MoveType.TRIGGER_RADAR;
	}

	protected void turnExtendedRadarOff (Ship pShip) {
		aCurrentClickedMove = MoveType.TRIGGER_RADAR;
	}
	
	protected void baseRepairSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.REPAIR_SHIP;
	}
	
	/**
	 * GUI tells Client that a green square was selected. 
	 * This indicates that the Player wants to move/fire/whatever in that square,
	 * so Client sends the info to the Server. 
	 * @param pCoord
	 */
	protected void greenSelected(Coordinate pCoord) {
		//Will send move type, coordinate & ship to Server
		//send as Move Object??? 
		Move move = new Move(pCoord, aCurrentClickedShip, aCurrentClickedMove);
		
		System.out.println(aCurrentClickedMove.toString() + " move made at [" + pCoord.getX() + "," + pCoord.getY() +"] ");
	}
	
	/**
	 * returns true if square is of type Sea, Sonar or Radar 
	 * i.e. Not mine, ship, coral or base 
	 * @param square
	 * @return
	 */
	private boolean clearSquare(Square square) {
	
		if (square instanceof Sea) return true;
		if (square instanceof RadarSquare) return true;
		if (square instanceof SonarSquare) return true;
		
		return false;
	}
}
