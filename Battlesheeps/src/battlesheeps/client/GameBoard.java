package battlesheeps.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.minueto.MinuetoColor;
import org.minueto.MinuetoEventQueue;
import org.minueto.handlers.MinuetoFocusHandler;
import org.minueto.handlers.MinuetoMouseHandler;
import org.minueto.image.MinuetoRectangle;
import org.minueto.window.MinuetoPanel;

import battlesheeps.board.BaseSquare;
import battlesheeps.board.Coordinate;
import battlesheeps.board.CoralReef;
import battlesheeps.board.MineSquare;
import battlesheeps.board.RadarSquare;
import battlesheeps.board.Sea;
import battlesheeps.board.ShipSquare;
import battlesheeps.board.SonarSquare;
import battlesheeps.board.Square;
import battlesheeps.server.ServerGame;
import battlesheeps.server.ServerGame.Direction;
import battlesheeps.ships.Ship;
import battlesheeps.ships.Ship.Damage;

public class GameBoard extends JInternalFrame implements MinuetoMouseHandler, MinuetoFocusHandler, Runnable{
	
	/**
	 * JInternalFrame to display the game board
	 */
	private static final long serialVersionUID = 55672340L;
	private MinuetoPanel aMinuetoPanel;
	private MinuetoEventQueue aEventQueue;
	private boolean open = true;
	
	private ClientGame aMyClient; //reference to client 

	private MinuetoRectangle aBoard;
	private String aUsername;
	private Square[][] aVisibleBoard;
	private int boardSize;
	private int increment;
	private boolean aMyTurn; //true if player's turn
	private List<Coordinate> aGreenList; //available squares to be moved to/fired at/etc
	private boolean aGreenPhase; //true if aGreenList is not empty
	private boolean aChosenMove; //true if player has chosen a move this turn
								//this is to stop the player from choosing more than one
	
	//The Colours 
	private MinuetoColor oceanColour = new MinuetoColor(new Color(53, 106, 172));
	private MinuetoColor baseColour = MinuetoColor.WHITE;
	private MinuetoColor baseDamagedColour = MinuetoColor.RED;
	private MinuetoColor coralColour = MinuetoColor.YELLOW; 
	private MinuetoColor mineColour = new MinuetoColor(new Color(32, 28, 31));
	private MinuetoColor radarColour = new MinuetoColor(new Color(88, 166, 203));
	private MinuetoColor sonarColour = new MinuetoColor(new Color(128, 102, 232));
	private MinuetoColor opponentShipUndamaged = new MinuetoColor(Color.DARK_GRAY);
	private MinuetoColor yourShipUndamaged = new MinuetoColor(Color.GRAY);
	private MinuetoColor opponentShipDamaged = new MinuetoColor(new Color(232, 231, 127)); //dark pink
	private MinuetoColor yourShipDamaged = new MinuetoColor(Color.PINK);
	private MinuetoColor opponentShipDestroyed = new MinuetoColor(new Color(239, 230, 82)); //dark red
	private MinuetoColor yourShipDestroyed = new MinuetoColor(Color.RED);
	private MinuetoColor moveColour = new MinuetoColor(new Color(72, 234, 92)); //green
	
	/**
	 * Creates a Minueto Panel which acts as the game board. 
	 * @param pSize : the size of the board (MUST BE A MULTIPLE OF 30) 
	 * @param pUser : this player 
	 * @param pVisibleBoard : the board visible to this player 
	 * @param pTurn : true if this player's turn 
	 * @param pMessages : the panel for displaying ship messages & menus 
	 */
	public GameBoard(int pSize, String pUser, Square[][] pVisibleBoard, boolean pTurn, ClientGame pClient) {
		
		super("Board for " + pUser);
		aUsername = pUser;
		//NOTE: size must be an integer divisible by 30
		boardSize = pSize;
		increment = boardSize/30;
		aVisibleBoard = pVisibleBoard;
		aMyTurn = pTurn;
		aMyClient = pClient;
		aChosenMove = false;
		
		createBoard();
		
		//will be used later on to show which squares are clickable 
		aGreenList = new ArrayList<Coordinate>(); 
		
		Thread thread = new Thread(this);
		thread.start();
		
	}
	
	private void createBoard() {
		
		JInternalFrame boardFrame = this;

		boardFrame.setResizable(false); //cannot resize the board
		boardFrame.setMaximizable(false);//or maximize it
		boardFrame.setLayout(new FlowLayout());
		boardFrame.setSize(new Dimension(boardSize, boardSize)); //should make the window a bit bigger?? 
	
		aMinuetoPanel = new MinuetoPanel(boardSize, boardSize); //create the window
		aEventQueue = new MinuetoEventQueue(); // Create and register event queue
					
		aMinuetoPanel.registerMouseHandler(this, aEventQueue);
		aMinuetoPanel.registerFocusHandler(this, aEventQueue);

		boardFrame.setContentPane(aMinuetoPanel);

		boardFrame.pack();
	}
	
	public void setVisible(boolean arg0) {
		super.setVisible(arg0);
		
		// make the minueto panel visible as well 
		if (aMinuetoPanel != null) aMinuetoPanel.setVisible(arg0);
	}
	
	public void run() {
		aBoard = new MinuetoRectangle(boardSize, boardSize, new MinuetoColor(new Color(53, 106, 172)), true);
		
		this.drawBoard();
		
		while(open) {
			//synchronized (aMinuetoPanel) {
				if (aMinuetoPanel.isVisible()) {
					
					// Handle all the events in the event queue.
					while (aEventQueue.hasNext()) {
						aEventQueue.handle();
					}
					
					aMinuetoPanel.clear(MinuetoColor.WHITE);
					
					aMinuetoPanel.draw(aBoard, 0,0);
					
					aMinuetoPanel.render();
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		//	}
			Thread.yield();
		}
		aMinuetoPanel.close(); //not sure if I should be closing it? 
		dispose(); // or disposing of it? 
	}
	
	/**
	 * Updates the game board and the Message panel depending on whose turn it is
	 * @param pNewBoard : the new game board 
	 * @param pTurn : true if this player's turn
	 */
	public void updateTurn(Square[][] pVisibleBoard, boolean pTurn) {
		
		aMyTurn = pTurn;
		aVisibleBoard = pVisibleBoard;
		aChosenMove = false;
		//and redraw the board
		drawBoard();
	}
	
	/**
	 * The GUI will display the given coordinates in green, 
	 * which means the player may click on it to activate move. 
	 * @param pList 
	 */
	public void showAvailableMoves(List<Coordinate> pList) {
		
		aGreenList = pList;
		aGreenPhase = true;
		
		MinuetoRectangle greenRectangle = new MinuetoRectangle(increment, increment, moveColour, true);
		for (Coordinate c : aGreenList) {
			int x = c.getX();
			int y = c.getY();
			aBoard.draw(greenRectangle,x*increment, y*increment);
			
			//re-drawing the ships on top
			if (aVisibleBoard[x][y] instanceof ShipSquare) {
				this.addShip(x, y);
			}//as well as any mines 
			if (aVisibleBoard[x][y] instanceof MineSquare) {
				MinuetoRectangle mine = new MinuetoRectangle(increment, increment, mineColour, true);
				aBoard.draw(mine, x*increment, y*increment);
			}
		}
		
		//And re-drawing on the lines to separate the squares
		for (int i = increment; i < boardSize; i = i + increment) {
			aBoard.drawLine(MinuetoColor.BLACK, i, 0, i, boardSize);
		}
		for (int j = increment; j < boardSize; j = j + increment) {
			aBoard.drawLine(MinuetoColor.BLACK, 0, j, boardSize, j);
		}
	}
	
	/**
	 * Draws the board 
	 */
	private void drawBoard () {
		
		aBoard.clear(oceanColour);
		//Coral Reef 
		MinuetoRectangle coralReef = new MinuetoRectangle(increment, increment, coralColour, true);
		//Ocean
		MinuetoRectangle ocean = new MinuetoRectangle(increment, increment, oceanColour, true);
		//Mine
		MinuetoRectangle mine = new MinuetoRectangle(increment, increment, mineColour, true);
		//Radar
		MinuetoRectangle radar = new MinuetoRectangle(increment, increment, radarColour, true);
		//Sonar
		MinuetoRectangle sonar = new MinuetoRectangle(increment, increment, sonarColour, true);

		for (int i = 0; i < aVisibleBoard.length; i++) {
			for (int j = 0; j < aVisibleBoard.length; j++) {
				if (aVisibleBoard[i][j] instanceof CoralReef) aBoard.draw(coralReef, i*increment, j*increment);
				else if (aVisibleBoard[i][j] instanceof ShipSquare) this.addShip(i, j);
				else if (aVisibleBoard[i][j] instanceof BaseSquare) this.addBase(i,j);
				else if (aVisibleBoard[i][j] instanceof MineSquare) aBoard.draw(mine, i*increment, j*increment);
				else if (aVisibleBoard[i][j] instanceof RadarSquare) aBoard.draw(radar, i*increment, j*increment);
				else if (aVisibleBoard[i][j] instanceof SonarSquare) aBoard.draw(sonar, i*increment, j*increment);
				else aBoard.draw(ocean,  i*increment, j*increment);
			}
		}
		
		//Note that (0,0) is the top left corner
		//Drawing on the squares
		for (int i = increment; i < boardSize; i = i + increment) {
			aBoard.drawLine(MinuetoColor.BLACK, i, 0, i, boardSize);
		}
		
		for (int j = increment; j < boardSize; j = j + increment) {
			aBoard.drawLine(MinuetoColor.BLACK, 0, j, boardSize, j);
		}
		
				
	}
	/**
	 * Draws on a base square. May be undamaged or destroyed.
	 * @param pSquareX : the x-coordinate of the square 
	 * @param pSquareY : the y-coordinate of the square 
	 */
	private void addBase (int pSquareX, int pSquareY) {
		//need to determine if there's damage
		BaseSquare baseSquare = (BaseSquare) aVisibleBoard[pSquareX][pSquareY];
		Damage baseDamage = baseSquare.getDamage();
		
		MinuetoRectangle base;
		if (baseDamage == Damage.DESTROYED) {
			base = new MinuetoRectangle(increment, increment, baseDamagedColour, true);
		}
		else {
			base = new MinuetoRectangle(increment, increment, baseColour, true);
		}
		
		aBoard.draw(base, pSquareX*increment, pSquareY*increment);
	}
	
	/**
	 * Draws on a ship square. May be a head, body or tail. 
	 * May be undamaged, damaged or destroyed. 
	 * @param pSquareX : the x-coordinate of the square
	 * @param pSquareY : the y-coordinate of the square
	 */
	private void addShip (int pSquareX, int pSquareY) {

		ShipSquare shipSquare = (ShipSquare) aVisibleBoard[pSquareX][pSquareY];
		Ship ship = shipSquare.getShip();

		//we need to determine if it's yours (gray) or opponent's (dark gray)
		boolean isOpponent;
		if ((ship.getUsername()).compareTo(aUsername)==0) isOpponent = false;
		else isOpponent = true;

		//and also if it's undamaged, damaged or destroyed
		Damage shipDamage = shipSquare.getDamage();
		MinuetoColor shipColor;
		//you can open paint and choose a colour to get the RGB values 
		if (shipDamage == Damage.UNDAMAGED) {
			if (isOpponent) shipColor = opponentShipUndamaged; 
			else shipColor = yourShipUndamaged; 
		}
		else if (shipDamage == Damage.DAMAGED) {
			if (isOpponent) shipColor = opponentShipDamaged; 
			else shipColor = yourShipDamaged; 
		}
		else { //DESTROYED
			if (isOpponent) shipColor = opponentShipDestroyed; //dark red
			else shipColor = yourShipDestroyed; //red
		}
		//and lastly if it's the head or body
		if (shipSquare.isHead()) {
			Direction shipDirection = ship.getDirection();
			switch (shipDirection) {
			case NORTH : 
				drawShipHeadNorth(pSquareX, pSquareY, shipColor); 
				break;
			case SOUTH : 
				drawShipHeadSouth(pSquareX, pSquareY, shipColor); 
				break;
			case EAST : 
				drawShipHeadEast(pSquareX, pSquareY, shipColor); 
				break;
				/*or WEST*/
			default : drawShipHeadWest(pSquareX, pSquareY, shipColor); break;
			}
		}
		else {
			drawShipBody(pSquareX, pSquareY, shipColor);
		}
	}

/*Color schema: 
 * gray ==> your ship, undamaged
 * pink ==> your ship, damaged
 * red ==> your ship, destroyed
 * dark gray ==> opponent's ship, undamaged 
 * dark pink ==> opponent's ship, damaged
 * dark red ==> opponent's ship, destroyed
 */
	private void drawShipBody(int pX, int pY, MinuetoColor pColor) {
		MinuetoRectangle ship = new MinuetoRectangle(increment, increment, pColor, true);
		aBoard.draw(ship, pX*increment, pY*increment);
	}
	
	private void drawShipHeadNorth(int pX, int pY, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = pX*increment;
		points[1] = (pY+1)*increment;
		//upper middle
		points[2] = (pX*increment)+(increment/2);
		points[3] = ((pY+1)*increment)-increment ;
		//corner
		points[4] = (pX*increment)+increment;
		points[5] = (pY+1)*increment;
		aBoard.drawPolygon(pColor, points);
	}

	private void drawShipHeadSouth(int pX, int pY, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = pX*increment;
		points[1] = pY*increment;
		//lower middle
		points[2] = (pX*increment)+(increment/2);
		points[3] = (pY*increment)+increment;
		//corner
		points[4] = (pX*increment)+increment;
		points[5] = pY*increment;
		aBoard.drawPolygon(pColor, points);
	}
	
	private void drawShipHeadEast(int pX, int pY, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = pX*increment;
		points[1] = pY*increment;
		//lower middle
		points[2] = (pX*increment) + increment;
		points[3] = (pY*increment)+(increment/2);
		//corner
		points[4] = (pX*increment);
		points[5] = (pY*increment) + increment;
		aBoard.drawPolygon(pColor, points);
	}
	
	private void drawShipHeadWest(int pX, int pY, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = (pX*increment)+ increment;
		points[1] = pY*increment;
		//lower middle
		points[2] = (pX*increment);
		points[3] = (pY*increment)+ (increment/2);
		//corner
		points[4] = (pX*increment) + increment;
		points[5] = (pY*increment) + increment;
		aBoard.drawPolygon(pColor, points);
	}
	
	//given mouse coordinates, board will convert to a game square
	private Coordinate convertToSquare(int pX, int pY) {

		int x = pX/increment; 
		int y = pY/increment;
		Coordinate square = new Coordinate(x, y);

		return square;
	}
	
	@Override
	public void handleGetFocus() {
		
	}

	@Override
	public void handleLostFocus() {
		
	}

	@Override
	public void handleMouseMove(int arg0, int arg1) {
		
	}

	@Override
	public void handleMousePress(int arg0, int arg1, int arg2) {

		//we only allow key presses if it's this player's turn! 
		if (aMyTurn) {
			Coordinate coord = convertToSquare(arg0, arg1);

			if (coord.inBounds()) {
				
				Square currentSquare = aVisibleBoard[coord.getX()][coord.getY()];
				
				if (currentSquare instanceof ShipSquare) {
					if (!aChosenMove) {
						ShipSquare shipSquare = (ShipSquare)aVisibleBoard[coord.getX()][coord.getY()];
						Ship ship = shipSquare.getShip();
						//if the ship is this player's ship, then display menu
						if (ship.getUsername().compareTo(aUsername) == 0) {
						aMyClient.showShipMenu(ship);
						}
					}
				}
				
				if (aGreenPhase) {
					if (aGreenList.contains(coord) && !aChosenMove){
						//tell Client about choice
						aChosenMove = true;
						aMyClient.greenSelected(coord); 
						//and getting rid of ship menu
					}
					//In any case, resetting the green list
					aGreenList = new ArrayList<Coordinate>();
					aGreenPhase = false;
					//and redrawing the board 
					drawBoard();		
				}
			}
		} 
		
		
	}

	@Override
	public void handleMouseRelease(int arg0, int arg1, int arg2) {
		
	}
}