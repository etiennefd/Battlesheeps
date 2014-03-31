package battlesheeps.client;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.board.*;
import battlesheeps.networking.ClientGamesAndMoves;
import battlesheeps.server.Move;
import battlesheeps.server.ServerGame.Direction;
import battlesheeps.server.ServerGame.MoveType;
import battlesheeps.ships.*;

import battlesheeps.board.BaseSquare;
import battlesheeps.board.Coordinate;
import battlesheeps.board.CoralReef;
import battlesheeps.board.InvalidCoordinateException;
import battlesheeps.board.MineSquare;
import battlesheeps.board.Sea;
import battlesheeps.board.ShipSquare;
import battlesheeps.board.Square;
import battlesheeps.server.ServerGame;
import battlesheeps.ships.MineLayer;
import battlesheeps.ships.Ship;
import battlesheeps.ships.Ship.Damage;

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
	private MessagePanel aMessagePanel;
	private LogPanel aLogPanel;
	private JPanel aChatPanel;
	private GameBoard aBoardPanel;
	
	private JSplitPane aSplitPane;
	
	//should know who it's player is 
	private String aMyUser;
	
	private Square[][] aCurrentVisibleBoard = new Square[30][30];
	private Ship aCurrentClickedShip; 
	private MoveType aCurrentClickedMove;
	
	private ClientGamesAndMoves myManager;
	
	//internal frame
	private Vector<JInternalFrame> internalFrame = new Vector<JInternalFrame>();

	public ClientGame(String pPlayer) {

		aMyUser = pPlayer;
		
		GraphicsDevice grdDevice;
		GraphicsConfiguration grcConfiguration;
		GraphicsEnvironment greEnvironment;

		greEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		grdDevice = greEnvironment.getDefaultScreenDevice();
		grcConfiguration = grdDevice.getDefaultConfiguration();

		this.aMainFrame = new JFrame(grcConfiguration); 

		this.aMainFrame.setPreferredSize(new Dimension(1000,725));
		
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
		
		aSplitPane = new JSplitPane();
		aSplitPane.setResizeWeight(0.8);
		
		background.add(aSplitPane);
		
		JSplitPane sidePanel = new JSplitPane();
		sidePanel.setResizeWeight(0.5);
		sidePanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		aSplitPane.setRightComponent(sidePanel);
		
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
		
		//MESSAGES
		aMessagePanel = new MessagePanel(this, pPlayer, "Opponent");
		sidePanel.setLeftComponent(aMessagePanel);
		
		//and creating the board panel 
		//sending a board full of sea to the gui
		Square[][] aCurrentVisibleBoard = new Square[30][30];

		for(int i = 0; i < 30; i++) { 
			for (int j = 0; j<30; j++) {
				aCurrentVisibleBoard[i][j] = new Sea();
			}
		}

		aBoardPanel = new GameBoard(600, pPlayer, aCurrentVisibleBoard, false, this);

		aBoardPanel.setPreferredSize(new Dimension(600, 600));
		aSplitPane.setLeftComponent(aBoardPanel);

		this.aDesktop.add(background);
		
		this.aMainFrame.pack();
		this.aMainFrame.setVisible(true);
		aBoardPanel.setVisible(true);
		aSplitPane.setDividerLocation(0.7);		
		
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
	
	public void setManager(ClientGamesAndMoves pManager) {
		myManager = pManager;
	}
	
	/***********************SETUP SECTION ****************************/
	
	public void showAvailablePositions(Ship s) {
		
		//let's keep track of which ship we want to show the moves for 
		aCurrentClickedShip = s;
		//TODO
		//let's compute the available green squares
		//and now let's tell the board to display the green squares 
		
	}
	
	//setup is done!
	public void setupComplete() {
		aBoardPanel.startGame();
	}
	
	//method to compute the available base positions 
	private List<Coordinate> computePositionsAtBase () {
		List<Coordinate> ls = new ArrayList<Coordinate>();
		//TODO
		return ls;
	}
	
	//call startGame() in GameBoard once setup is done... 
	
	/**********************IN GAME SECTION****************************/
	
	/*After each move, 
	 * 1. the board will be updated 
	 * 2. the log entries will be updated 
	 * 3. a message may be displayed  
	 */
	public void updateGame (ServerGame pGame) {
	
		boolean myTurn = aMyUser.equals(pGame.getTurnPlayer()); 
		
		List<Ship> myList;
		List<Ship> oppList;
		
		boolean isP1 = aMyUser.equals(pGame.getP1Username());
		
		if (isP1) {
			myList = pGame.getP1ShipList();
			oppList = pGame.getP2ShipList();
		}
		else {
			myList = pGame.getP2ShipList();
			oppList = pGame.getP1ShipList();
		}
		
		Square[][] tempBoard = computeVisibility(pGame.getBoard(), myList);
		for (int i = 0; i <30; i++) {
			for (int j = 0; j<30; j++) {
				aCurrentVisibleBoard[i][j] = tempBoard[i][j]; 
			}
		}
		
				
		if (myTurn){
			//my turn 
			aBoardPanel.updateTurn(aCurrentVisibleBoard, true);
			aMessagePanel.setYourTurn();
		} 
		else {
			aBoardPanel.updateTurn(aCurrentVisibleBoard, false);
			aMessagePanel.setNotYourTurn(myList, oppList);
		}
		
		aLogPanel.updateLogEntries(pGame.getLog());
	}
	
	/**
	 * Client tells message panel to display action menu for ship 
	 * @param pShip
	 */
	public void showShipMenu(Ship pShip) {
		
		aCurrentClickedShip = pShip;
		boolean atBase = isAtHomeBase(pShip);
		
		aMessagePanel.displayShipMenu(pShip, atBase);
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
		
		boolean isBaseWest = false;
		
		if (((BaseSquare)pBoard[0][10]).getOwner().equals(aMyUser)) isBaseWest = true;
		
		if (isBaseWest) {
			baseX = 1;
			visibleRadarList.add(new Coordinate(0, 9));
			visibleRadarList.add(new Coordinate(0, 20));
		}
		else {
			baseX = 28;
			visibleRadarList.add(new Coordinate(29, 9));
			visibleRadarList.add(new Coordinate(29, 20));
		}
		//make it 9 to 21 if you want the corners to be in radar
		for (int i = 10; i < 20; i++){
			visibleRadarList.add(new Coordinate(baseX, i));
		}
		
		//and a list to hold the visible squares by sonar
		List<Coordinate> visibleSonarList = new ArrayList<Coordinate>();

		//now calculate
		for (Ship ship : pShipList) {
			if (!ship.isSunk()) {
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
	
	//A Note about Moves: 
	//the Message Panel sends the chosen move to the Client
	//the Client then calculates where the move could occur on the board
	//and sends this list of Coordinates to the Game Board
	//or it sends the info straight to the Server in the case of radar changes 
	
	protected void translateSelected(Ship pShip) {
		
		//First, special case for kamikaze boat
		if (pShip instanceof KamikazeBoat) {
			aCurrentClickedMove = MoveType.TRANSLATE_KAMIKAZE;
			List<Coordinate> greenList = new ArrayList<Coordinate>();
			
			int headX = pShip.getHead().getX();
			int headY = pShip.getHead().getY();
			greenList.add(pShip.getHead());
			for (int x = headX-1; x <= headX+1; x++) {
				for (int y = headY-1; y <= headY+1; y++) {
					if (new Coordinate(x, y).inBounds()) {
						Square s = aCurrentVisibleBoard[x][y];
						if (clearSquare(s)) {
							greenList.add(new Coordinate(x, y));
						}
					}
				}
			}
			aBoardPanel.showAvailableMoves(greenList);
			return;
		}
		
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
		
		aCurrentClickedMove = MoveType.TURN_SHIP;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		//the method canTurn which will return true if you can rotate
		//to a particular destination square, so we simply need to test all 
		//turn destinations
		Direction direction = pShip.getDirection();
		int size = pShip.getSize();
		
		Coordinate head = pShip.getHead();
		int headX = head.getX();
		int headY = head.getY();
		
		Coordinate tail = pShip.getTail();
		int tailX = tail.getX();
		int tailY = tail.getY();
		
		if (pShip.canTurn180()) {
			//two cases: turning 90 and turning 180
			
			//Case 1: turning 90
			boolean canTurnPort;
			boolean canTurnStarboard;
			Coordinate port;
			Coordinate starboard;
			
			switch(direction) {
			case NORTH : 
				port = new Coordinate(headX-1, headY+1);
				canTurnPort = canTurn(port);
				starboard = new Coordinate(headX+1, headY+1);
				canTurnStarboard = canTurn(starboard);
				break;
			case SOUTH : 
				port = new Coordinate(headX+1, headY-1);
				canTurnPort = canTurn(port);
				starboard = new Coordinate(headX-1, headY-1);
				canTurnStarboard = canTurn(starboard);
				break;
			case WEST : 
				port = new Coordinate(headX+1, headY+1);
				canTurnPort = canTurn(port);
				starboard = new Coordinate(headX+1, headY-1);
				canTurnStarboard = canTurn(starboard);
				break;
			default : /*EAST*/ 
				port = new Coordinate(headX-1, headY-1);
				canTurnPort = canTurn(port);
				starboard = new Coordinate(headX-1, headY+1);
				canTurnStarboard = canTurn(starboard);
				break;
			}
			
			if (canTurnPort && port.inBounds()) greenList.add(port);
			if (canTurnStarboard && starboard.inBounds()) greenList.add(starboard);
			
			//Case 2: turning 180
			//i.e. can turn so its head is now its tail
			boolean canTurn180 = canTurn(tail);
			if (canTurn180) greenList.add(tail);
			
		} else { //one case: turning 90
			
			size = size - 1; //since we turn around the last block
			
			boolean canTurnPort;
			boolean canTurnStarboard;
			Coordinate port;
			Coordinate starboard;
			
			switch(direction) {
				case NORTH : 
					port = new Coordinate(tailX - size, tailY);
					canTurnPort = canTurn(port);
					starboard = new Coordinate(tailX + size, tailY);
					canTurnStarboard = canTurn(starboard);
					break;
				case SOUTH : 
					port = new Coordinate(tailX + size, tailY);
					canTurnPort = canTurn(port);
					starboard = new Coordinate(tailX - size, tailY);
					canTurnStarboard = canTurn(starboard);
					break;
				case WEST : 
					port = new Coordinate(tailX, tailY + size);
					canTurnPort = canTurn(port);
					starboard = new Coordinate(tailX, tailY - size);
					canTurnStarboard = canTurn(starboard);
					break;
				default : /*EAST*/ 
					port = new Coordinate(tailX, tailY - size);
					canTurnPort = canTurn(port);
					starboard = new Coordinate(tailX, tailY + size);
					canTurnStarboard = canTurn(starboard);
					break;
			}
			
			if (canTurnPort && port.inBounds()) greenList.add(port);
			if (canTurnStarboard && starboard.inBounds()) greenList.add(starboard);
		} 
		
		aBoardPanel.showAvailableMoves(greenList);
		
		
	}
	
	protected void cannonSelected(Ship pShip) {
		
		aCurrentClickedMove = MoveType.FIRE_CANNON;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		//Put entire cannon range (length * width) into greenList
		//then subtract: 
		//1. all your ships (including this one)
		//2. coral reefs
		//3. your base
		
		//Note: all except torpedo boat have a cannon range which goes behind them
		
		for (Coordinate c : pShip.getCannonCoordinates()) {
			
			int x = c.getX();
			int y = c.getY();
			boolean canFireOn = true;
			
			//coral reef is indestructible!! 
			if (aCurrentVisibleBoard[x][y] instanceof CoralReef) {
				canFireOn = false;
			}
			//you can't fire on your own base 
			if (aCurrentVisibleBoard[x][y] instanceof BaseSquare) {
				BaseSquare b = (BaseSquare)aCurrentVisibleBoard[x][y]; 
				String owner = b.getOwner();
				if (aMyUser.compareTo(owner) == 0) {
					canFireOn = false;
				}
			}
			//nor on your own ships 
			if (aCurrentVisibleBoard[x][y] instanceof ShipSquare) {
				ShipSquare sq = (ShipSquare)aCurrentVisibleBoard[x][y]; 
				Ship s = sq.getShip();
				if (aMyUser.compareTo(s.getUsername()) == 0) {
					canFireOn = false;
				}
			}
			
			if (canFireOn) greenList.add(c);
		}	
		
		aBoardPanel.showAvailableMoves(greenList);
		
	}
	
	protected void layMineSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.DROP_MINE;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		List<Coordinate> tempList = new ArrayList<Coordinate>();
		
		//CANNOT DROP MINE NEXT TO ANOTHER SHIP OR BASE
		//We first add to the tempList the 6 squares in the mine area, 
		//Then select (for the green list) only those that are
		//- outside the board
		//- not sea
		//- too close to some other ship or base
		
		Direction direction = pShip.getDirection();
		
		Coordinate head = pShip.getHead();
		int headX = head.getX();
		int headY = head.getY();
		
		Coordinate tail = pShip.getTail();
		int tailX = tail.getX();
		int tailY = tail.getY();
		
		switch(direction) {
		case NORTH: 
			tempList.add(new Coordinate(headX, headY-1));
			tempList.add(new Coordinate(headX-1, headY));
			tempList.add(new Coordinate(tailX-1, tailY));
			tempList.add(new Coordinate(tailX, tailY+1));
			tempList.add(new Coordinate(headX+1, headY));
			tempList.add(new Coordinate(tailX+1, tailY));
			break;
		case SOUTH: 
			tempList.add(new Coordinate(headX, headY+1));
			tempList.add(new Coordinate(headX-1, headY));
			tempList.add(new Coordinate(tailX-1, tailY));
			tempList.add(new Coordinate(tailX, tailY-1));
			tempList.add(new Coordinate(headX+1, headY));
			tempList.add(new Coordinate(tailX+1, tailY));
			break;
		case WEST: 
			tempList.add(new Coordinate(headX-1, headY));
			tempList.add(new Coordinate(headX, headY-1));
			tempList.add(new Coordinate(tailX, tailY-1));
			tempList.add(new Coordinate(tailX+1, tailY));
			tempList.add(new Coordinate(headX, headY+1));
			tempList.add(new Coordinate(tailX, tailY+1));
			break;
		case EAST: 
			tempList.add(new Coordinate(headX+1, headY));
			tempList.add(new Coordinate(headX, headY-1));
			tempList.add(new Coordinate(tailX, tailY-1));
			tempList.add(new Coordinate(tailX-1, tailY));
			tempList.add(new Coordinate(headX, headY+1));
			tempList.add(new Coordinate(tailX, tailY+1));
			break;
		}
		
		for (Coordinate c : tempList) {
			if (c.inBounds()) {
				if (aCurrentVisibleBoard[c.getX()][c.getY()] instanceof SonarSquare) {
					boolean tooCloseToShipOrBase = false;
					for (int x = c.getX()-1; x <= c.getX()+1; x++) {
						for (int y = c.getY()-1; y <= c.getY()+1; y++) {
							Square s = aCurrentVisibleBoard[x][y];
							if (s instanceof BaseSquare || 
									s instanceof ShipSquare && !((ShipSquare) s).getShip().equals(pShip)) {
								tooCloseToShipOrBase = true;
							}
						}
					}
					if (!tooCloseToShipOrBase) greenList.add(c);
				}
			}
		}
		
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void retrieveMineSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.PICKUP_MINE;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		List<Coordinate> tempList = new ArrayList<Coordinate>();
		
		//We first add to the tempList the 6 squares in the mine area, 
		//Then select (for the green list) only those that contain a mine
		
		Direction direction = pShip.getDirection();
		
		Coordinate head = pShip.getHead();
		int headX = head.getX();
		int headY = head.getY();
		
		Coordinate tail = pShip.getTail();
		int tailX = tail.getX();
		int tailY = tail.getY();
		
		switch(direction) {
		case NORTH: 
			tempList.add(new Coordinate(headX, headY-1));
			tempList.add(new Coordinate(headX-1, headY));
			tempList.add(new Coordinate(tailX-1, tailY));
			tempList.add(new Coordinate(tailX, tailY+1));
			tempList.add(new Coordinate(headX+1, headY));
			tempList.add(new Coordinate(tailX+1, tailY));
			break;
		case SOUTH: 
			tempList.add(new Coordinate(headX, headY+1));
			tempList.add(new Coordinate(headX-1, headY));
			tempList.add(new Coordinate(tailX-1, tailY));
			tempList.add(new Coordinate(tailX, tailY-1));
			tempList.add(new Coordinate(headX+1, headY));
			tempList.add(new Coordinate(tailX+1, tailY));
			break;
		case WEST: 
			tempList.add(new Coordinate(headX-1, headY));
			tempList.add(new Coordinate(headX, headY-1));
			tempList.add(new Coordinate(tailX, tailY-1));
			tempList.add(new Coordinate(tailX+1, tailY));
			tempList.add(new Coordinate(headX, headY+1));
			tempList.add(new Coordinate(tailX, tailY+1));
			break;
		case EAST: 
			tempList.add(new Coordinate(headX+1, headY));
			tempList.add(new Coordinate(headX, headY-1));
			tempList.add(new Coordinate(tailX, tailY-1));
			tempList.add(new Coordinate(tailX-1, tailY));
			tempList.add(new Coordinate(headX, headY+1));
			tempList.add(new Coordinate(tailX, tailY+1));
			break;
		}
		
		for (Coordinate c : tempList) {
			if (c.inBounds()) {
				if (aCurrentVisibleBoard[c.getX()][c.getY()] instanceof MineSquare) {
					greenList.add(c);
				}
			}
		}
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void torpedoSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.FIRE_TORPEDO;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		Direction torpedoDirection = pShip.getDirection(); 
		int headX = pShip.getHead().getX();
		int headY = pShip.getHead().getY();
		int x, y;
		
		//There is a case for each direction. 
		//For each case, loop over the 10 squares in front of the ship. 
		//If a square is: a coral reef/your own base/your own ship, stop looping. 
		//If the square is something else, add it to the greenList. 
		//If the square is not clear (sea/radar/sonar) stop looping (but after adding to the green list). 
		switch (torpedoDirection) {
		case NORTH: 
			x = headX;
			for (y = headY - 1; y >= 0 && y >= headY - 10; y--) {
				Square s = aCurrentVisibleBoard[x][y]; 
				if ((s instanceof BaseSquare && ((BaseSquare) s).getOwner().equals(pShip.getUsername()))
						|| s instanceof ShipSquare && ((ShipSquare) s).getShip().getUsername().equals(pShip.getUsername())
						|| s instanceof CoralReef) {
					break;
				}
				else {
					greenList.add(new Coordinate(x, y));
					if (!clearSquare(s)) break;
				}
			}
			break;
		case SOUTH: 
			x = headX;
			for (y = headY + 1; y < aCurrentVisibleBoard.length && y <= headY + 10; y++) {
				Square s = aCurrentVisibleBoard[x][y]; 
				if ((s instanceof BaseSquare && ((BaseSquare) s).getOwner().equals(pShip.getUsername()))
						|| s instanceof ShipSquare && ((ShipSquare) s).getShip().getUsername().equals(pShip.getUsername())
						|| s instanceof CoralReef) {
					break;
				}
				else {
					greenList.add(new Coordinate(x, y));
					if (!clearSquare(s)) break;
				}
			}
			break;
		case WEST: 
			y = headY;
			for (x = headX - 1; x >= 0 && x >= headX - 10; x--) {
				Square s = aCurrentVisibleBoard[x][y]; 
				if ((s instanceof BaseSquare && ((BaseSquare) s).getOwner().equals(pShip.getUsername()))
						|| s instanceof ShipSquare && ((ShipSquare) s).getShip().getUsername().equals(pShip.getUsername())
						|| s instanceof CoralReef) {
					break;
				}
				else {
					greenList.add(new Coordinate(x, y));
					if (!clearSquare(s)) break;
				}
			}
			break;
		case EAST: 
			y = headY;
			for (x = headX + 1; x < aCurrentVisibleBoard.length && x <= headX + 10; x++) {
				Square s = aCurrentVisibleBoard[x][y]; 
				if ((s instanceof BaseSquare && ((BaseSquare) s).getOwner().equals(pShip.getUsername()))
						|| s instanceof ShipSquare && ((ShipSquare) s).getShip().getUsername().equals(pShip.getUsername())
						|| s instanceof CoralReef) {
					break;
				}
				else {
					greenList.add(new Coordinate(x, y));
					if (!clearSquare(s)) break;
				}
			}
			break;
		}
		
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void suicideSelected(Ship pShip) {
		aCurrentClickedMove = MoveType.SUICIDE_ATTACK;
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		//TODO
		
		
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	protected void turnExtendedRadarOn () {
		aCurrentClickedMove = MoveType.TRIGGER_RADAR;
		//we can tell the server right away that this move was selected
		//coordinate doesn't matter
		greenSelected(null, null);
	}

	protected void turnExtendedRadarOff () {
		aCurrentClickedMove = MoveType.TRIGGER_RADAR;
		//we can tell the server right away that this move was selected
		//coordinate doesn't matter
		greenSelected(null, null);
	}
	
	protected void baseRepairSelected() {
		aCurrentClickedMove = MoveType.REPAIR_SHIP;
		//we can tell the server right away that this move was selected
		//coordinate doesn't matter
		greenSelected(null, null);
	}
	
	/**
	 * Used for the selection of the second square in the kamikaze boat's translation or suicide attack. 
	 * Returns all nine square around and including pCenter. 
	 */
	protected void selectSecondSquare(Coordinate pCenter) {
		List<Coordinate> greenList = new ArrayList<Coordinate>();
		
		int cX = pCenter.getX();
		int cY = pCenter.getY();
		greenList.add(pCenter);
		for (int x = cX-1; x <= cX+1; x++) {
			for (int y = cY-1; y <= cY+1; y++) {
				if (new Coordinate(x, y).inBounds()) {
					Square s = aCurrentVisibleBoard[x][y];
					if (clearSquare(s) || s instanceof ShipSquare ) {
						greenList.add(new Coordinate(x, y));
					}
				}
			}
		}
		aBoardPanel.showAvailableMoves(greenList);
	}
	
	
	/**
	 * GUI tells Client that a green square (possibly two) was selected. 
	 * This indicates that the Player wants to move/fire/whatever in that square,
	 * so Client sends the info to the Server. 
	 * 
	 * NEW: this method now returns true if a move was completed, and false if the move is not done. 
	 * (E.g. only first half of the kamikaze translation was done)
	 * @param pCoord
	 */
	protected boolean greenSelected(Coordinate pCoord, Coordinate pSecondaryCoord) {
		//Will send move type, coordinate & ship to Server
		//send as Move Object to Server
		
		if (pSecondaryCoord == null 
				&& (aCurrentClickedMove == MoveType.TRANSLATE_KAMIKAZE || aCurrentClickedMove == MoveType.SUICIDE_ATTACK)) {
			//We still need to select a 2nd green square. 
			selectSecondSquare(pCoord);
			return false;
		}
		else {
			//Send the move to the server!
			Move move = new Move(pCoord, pSecondaryCoord, aCurrentClickedShip, aCurrentClickedMove, null);
			System.out.println("Ship belongs to: " + aCurrentClickedShip.getUsername() + " and it's id is: " + aCurrentClickedShip.getShipID());
			
			if (aCurrentClickedMove == MoveType.REPAIR_SHIP || aCurrentClickedMove == MoveType.TRIGGER_RADAR) {
				aMessagePanel.displayMessage("" + aCurrentClickedMove +"");
			} else {
				aMessagePanel.displayMessage(aCurrentClickedMove + " at " + "[" + pCoord.getX() + "," + pCoord.getY() + "]");
			}
			myManager.sendMove(move);
			
			return true;
		}
		
	}
	
	/**
	 * Returns true if square is of type Sea, Sonar or Radar 
	 * i.e. Not mine, ship, coral or base 
	 * @param pSquare
	 * @return
	 */
	private boolean clearSquare(Square pSquare) {
	
		if (pSquare instanceof Sea) return true;
		if (pSquare instanceof RadarSquare) return true;
		if (pSquare instanceof SonarSquare) return true;
		
		return false;
	}
	
	/**
	 * Returns true if there are no obstacles in the turn path to
	 * the Destination coordinate
	 * @param pDestination
	 * @return
	 */
	private boolean canTurn(Coordinate pDestination) {
		//Compute the turn area
		//Check for obstacles in it
		//If any return false
		
		int headX = aCurrentClickedShip.getHead().getX();
		int headY = aCurrentClickedShip.getHead().getY();
		int tailX = aCurrentClickedShip.getTail().getX();
		int tailY = aCurrentClickedShip.getTail().getY();
		Direction direction = aCurrentClickedShip.getDirection();

		boolean turnSuccess = true;

		//Here we look at 180 degrees turn. We assume ships able to do that are 3 squares long. 
		if (aCurrentClickedShip.canTurn180() && pDestination.equals(aCurrentClickedShip.getTail())) {
			ArrayList<Coordinate> listOfCoords = new ArrayList<Coordinate>();
			switch (direction) {
			case NORTH: 
				listOfCoords.add(new Coordinate(headX-1, headY));
				listOfCoords.add(new Coordinate(headX-1, headY+1));
				listOfCoords.add(new Coordinate(headX-1, headY+2));
				listOfCoords.add(new Coordinate(tailX+1, tailY));
				listOfCoords.add(new Coordinate(tailX+1, tailY-1));
				listOfCoords.add(new Coordinate(tailX+1, tailY-2));
				break; 
			case SOUTH: 
				listOfCoords.add(new Coordinate(headX-1, headY));
				listOfCoords.add(new Coordinate(headX-1, headY-1));
				listOfCoords.add(new Coordinate(headX-1, headY-2));
				listOfCoords.add(new Coordinate(tailX+1, tailY));
				listOfCoords.add(new Coordinate(tailX+1, tailY+1));
				listOfCoords.add(new Coordinate(tailX+1, tailY+2));
				break;
			case EAST: 
				listOfCoords.add(new Coordinate(headX, headY-1));
				listOfCoords.add(new Coordinate(headX-1, headY-1));
				listOfCoords.add(new Coordinate(headX-2, headY-1));
				listOfCoords.add(new Coordinate(tailX, tailY+1));
				listOfCoords.add(new Coordinate(tailX+1, tailY+1));
				listOfCoords.add(new Coordinate(tailX+2, tailY+1));
				break; 
			case WEST: 
				listOfCoords.add(new Coordinate(headX, headY-1));
				listOfCoords.add(new Coordinate(headX+1, headY-1));
				listOfCoords.add(new Coordinate(headX+2, headY-1));
				listOfCoords.add(new Coordinate(tailX, tailY+1));
				listOfCoords.add(new Coordinate(tailX-1, tailY+1));
				listOfCoords.add(new Coordinate(tailX-2, tailY+1));
				break; 
			}
			//Now we iterate over the list of coordinates we built
			for (Coordinate c : listOfCoords) {
				if (!c.inBounds() || !clearSquare(aCurrentVisibleBoard[c.getX()][c.getY()])) {
					turnSuccess = false;
					break;
				}
			}
		}

		//Turning 90 degrees but over center square
		else if (aCurrentClickedShip.canTurn180()) {
			ArrayList<Square> listOfSquares = new ArrayList<Square>();
			switch (direction) {
			case NORTH: 
				//Case turning left/port (west)
				if (pDestination.getX() < tailX) {
					if (headX == 0 || headX == aCurrentVisibleBoard.length-1) return false; //left/right border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX-1][headY]);
					listOfSquares.add(aCurrentVisibleBoard[tailX+1][tailY]);
					listOfSquares.add(aCurrentVisibleBoard[headX-1][headY+1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX+1][tailY-1]);
				}
				//Case turning right/starboard (east)
				else if (pDestination.getX() > tailX) {
					if (headX == 0 || headX == aCurrentVisibleBoard.length-1) return false; //left/right border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX+1][headY]);
					listOfSquares.add(aCurrentVisibleBoard[tailX-1][tailY]);
					listOfSquares.add(aCurrentVisibleBoard[headX+1][headY+1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX-1][tailY-1]);
				}
				break; 
			case SOUTH: 
				//Case turning right/starboard (west)
				if (pDestination.getX() < tailX) {
					if (headX == 0 || headX == aCurrentVisibleBoard.length-1) return false; //left/right border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX-1][headY]);
					listOfSquares.add(aCurrentVisibleBoard[tailX+1][tailY]);
					listOfSquares.add(aCurrentVisibleBoard[headX-1][headY-1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX+1][tailY+1]);
				}
				//Case turning left/port (east)
				else if (pDestination.getX() > tailX) {
					if (headX == 0 || headX == aCurrentVisibleBoard.length-1) return false; //left/right border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX+1][headY]);
					listOfSquares.add(aCurrentVisibleBoard[tailX-1][tailY]);
					listOfSquares.add(aCurrentVisibleBoard[headX+1][headY-1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX-1][tailY+1]);
				}
				break;
			case EAST: 
				//Case turning left/port (north)
				if (pDestination.getY() < tailY) {
					if (headY == 0 || headY == aCurrentVisibleBoard.length-1) return false; //top/bottom border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX][headY-1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX][tailY+1]);
					listOfSquares.add(aCurrentVisibleBoard[headX-1][headY-1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX+1][tailY+1]);
				}
				//Case turning right/starboard (south)
				else if (pDestination.getY() > tailY) {
					if (headY == 0 || headY == aCurrentVisibleBoard.length-1) return false; //top/bottom border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX][headY+1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX][tailY-1]);
					listOfSquares.add(aCurrentVisibleBoard[headX-1][headY+1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX+1][tailY-1]);
				}
				break; 
			case WEST: 
				//Case turning right/starboard (north)
				if (pDestination.getY() < tailY) {
					if (headY == 0 || headY == aCurrentVisibleBoard.length-1) return false; //top/bottom border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX][headY-1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX][tailY+1]);
					listOfSquares.add(aCurrentVisibleBoard[headX+1][headY-1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX-1][tailY+1]);
				}
				//Case turning left/port (south)
				else if (pDestination.getY() > tailY) {
					if (headY == 0 || headY == aCurrentVisibleBoard.length-1) return false; //top/bottom border of the board
					listOfSquares.add(aCurrentVisibleBoard[headX][headY+1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX][tailY-1]);
					listOfSquares.add(aCurrentVisibleBoard[headX+1][headY+1]);
					listOfSquares.add(aCurrentVisibleBoard[tailX-1][tailY-1]);
				}
				break; 
			}
			//Now we iterate over the list of squares we built
			for (Square s : listOfSquares) {
				if (!clearSquare(s)) {
					turnSuccess = false;
					break;
				}
			}
		}

		//90 degrees turn by a regular ship (the pivot is the tail)
		else {
			switch (direction) {
			case NORTH: 
				//Case turning left/port (west)
				if (pDestination.getX() < tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area 
					//(i provides the staircase pattern)
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX-1; x >= tailX-aCurrentClickedShip.getSize()+1 && i>0; x--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y >= tailY-i; y--) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break; 
							}
						}
						if (broke) break;
						i--; //The next column will be shorter by 1
					}
				}
				//Case turning right/starboard (east)
				else if (pDestination.getX() > tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX+1; x <= tailX+aCurrentClickedShip.getSize()-1 && i>0; x++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y >= tailY-i; y--) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break; 
							}
						}
						if (broke) break;
						i--;
					}
				}
				break; //from case NORTH

			case SOUTH: //same as north, but change the y's signs
				//Case turning right/starboard (west)
				if (pDestination.getX() < tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX-1; x >= tailX-aCurrentClickedShip.getSize()+1 && i>0; x--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y <= tailY+i; y++) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break; 
							}
						}
						if (broke) break;
						i--;
					}
				}
				//Case turning left/port (east) 
				else if (pDestination.getX() > tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX+1; x <= tailX+aCurrentClickedShip.getSize()-1 && i>0; x++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y <= tailY+i; y++) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break;
							}
						}
						if (broke) break;
						i--;
					}
				}
				break; //from case SOUTH

			case EAST: 
				//Case turning left/port (north)
				if (pDestination.getY() < tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area 
					//(i provides the staircase pattern)
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY-1; y >= tailY-aCurrentClickedShip.getSize()+1 && i>0; y--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x <= tailX+i; x++) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break;
							}
						}
						if (broke) break;
						i--; //The next column will be shorter by 1
					}
				}
				//Case turning right/starboard (south)
				else if (pDestination.getY() > tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY+1; y <= tailY+aCurrentClickedShip.getSize()-1 && i>0; y++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x <= tailX+i; x++) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break;
							}
						}
						if (broke) break;
						i--;
					}
				}
				break; //from case EAST

			case WEST: //same as east, but change the x's signs
				//Case turning right/starboard (north)
				if (pDestination.getY() < tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area 
					//(i provides the staircase pattern)
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY-1; y >= tailY-aCurrentClickedShip.getSize()+1 && i>0; y--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x >= tailX-i; x--) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break;
							}
						}
						if (broke) break;
						i--; //The next column will be shorter by 1
					}
				}
				//Case turning left/port (south)
				else if (pDestination.getY() > tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = aCurrentClickedShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY+1; y <= tailY+aCurrentClickedShip.getSize()-1 && i>0; y++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x >= tailX-i; x--) {
							if (x < 0 || y < 0 || x >= aCurrentVisibleBoard.length || y >= aCurrentVisibleBoard.length) {
								return false; //too close to the edge of the board
							}
							Square s = aCurrentVisibleBoard[x][y];
							//Looking for obstacles
							if (!clearSquare(s)) {
								turnSuccess = false;
								broke = true;
								break;
							}
						}
						if (broke) break;
						i--;
					}
				}
				break; //from case WEST
			}

		}
		return turnSuccess;
	}
	
	/**
	 * Returns true if the ship is at its home base.
	 * @param pShip
	 * @return
	 */
	private boolean isAtHomeBase(Ship pShip) {
		//to figure out if a ship is at home base: 
		//for each square surrounding it, 
		//check if the square is a base square and undamaged 
		//if yes, check if it is owned by this user  
		//if yes, then the ship is at home base 
		
		boolean atHome = false;

		Direction myDirection = pShip.getDirection();
		int startX, startY;

		if (myDirection == Direction.NORTH || myDirection == Direction.WEST) { 
			startX = pShip.getHead().getX() - 1;
			startY = pShip.getHead().getY() - 1;
		} else {
			startX = pShip.getTail().getX() - 1;
			startY = pShip.getTail().getY() - 1;
		}

		for (int i = startX; i < (startX + pShip.getSize() + 2); i++) {
			for (int j = startY; j < (startY + pShip.getSize() + 2); j++) {
				Coordinate c = new Coordinate(i,j);
				//if the coordinate is in bounds 
				if (c.inBounds()) {
					//then we check if its a base square 
					if (aCurrentVisibleBoard[i][j] instanceof BaseSquare) {
						BaseSquare base = (BaseSquare) aCurrentVisibleBoard[i][j];
						//checking that the base square is undamaged 
						if (base.getDamage() == Damage.UNDAMAGED) { 
							String owner = base.getOwner();
							//and if so, we check if the base belongs to this user
							if (owner.compareTo(aMyUser) == 0) {
								atHome = true;
								break;
							}
						}
					}
				}
			}

			if (atHome) break;
		}

		return atHome;

	}
	
}
