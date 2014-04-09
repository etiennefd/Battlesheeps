package battlesheeps.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.minueto.MinuetoColor;
import org.minueto.MinuetoEventQueue;
import org.minueto.handlers.MinuetoFocusHandler;
import org.minueto.handlers.MinuetoMouseHandler;
import org.minueto.image.MinuetoFont;
import org.minueto.image.MinuetoImage;
import org.minueto.image.MinuetoRectangle;
import org.minueto.image.MinuetoText;
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

	private MinuetoRectangle aBorder;
	private MinuetoRectangle aBoard;
	private String aUsername;
	private Square[][] aVisibleBoard;
	private int aBoardSize;
	private int aIncrement;
	private boolean aMyTurn; //true if player's turn
	private List<Coordinate> aGreenList; //available squares to be moved to/fired at/etc
	private boolean aGreenPhase; //true if aGreenList is not empty
	private boolean aChosenMove; //true if player has chosen a move this turn
								//this is to stop the player from choosing more than one
	
	//The following two coordinates are important for the kamikaze ship. The first is set on the first click on a 
	//green square, and the second is set on the second click on a green square. 
	private Coordinate aFirstChosenCoord = null;
	private Coordinate aSecondChosenCoord = null;
	
	private boolean aGameInProgress = false; //this is false for setup, and true for game in progress 
	
	//The Colours 
	private MinuetoColor aMoveColour = new MinuetoColor(new Color(53, 106, 172));
	private MinuetoColor aRadarColour = new MinuetoColor(new Color(176, 248, 80));
	private MinuetoColor aSonarColour = new MinuetoColor(new Color(85, 186, 6));
	
	//The Font
	private MinuetoFont aFont = new MinuetoFont (MinuetoFont.SansSerif, 12, true, false);
	
	//The Images
	private MinuetoImage aTree; 
	private MinuetoImage aGrass;
	private MinuetoImage aMine;
	private MinuetoImage aTarget;
	private MinuetoImage aBase;
	private MinuetoImage aBaseDestroyed;
	private MinuetoImage aOpponentBase;
	private MinuetoImage aOpponentBaseDestroyed;
	private MinuetoImage aSheep;
	private MinuetoImage aSheepDamaged;
	private MinuetoImage aSheepDestroyed;
	private MinuetoImage aSheepHead;
	private MinuetoImage aSheepHeadDamaged;
	private MinuetoImage aSheepHeadDestroyed;
	private MinuetoImage aSheepRadar;
	private MinuetoImage aSheepRadarDestroyed;
	private MinuetoImage aOpponentSheep;
	private MinuetoImage aOpponentSheepDamaged;
	private MinuetoImage aOpponentSheepDestroyed;
	private MinuetoImage aOpponentSheepHead;
	private MinuetoImage aOpponentSheepHeadDamaged;
	private MinuetoImage aOpponentSheepHeadDestroyed;
	private MinuetoImage aOpponentSheepRadar;
	private MinuetoImage aOpponentSheepRadarDestroyed;
	
	private MinuetoRectangle aRadar;
	private MinuetoRectangle aSonar;

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
		aBoardSize = pSize;
		aIncrement = aBoardSize/30;
		aVisibleBoard = pVisibleBoard;
		aMyTurn = pTurn;
		aMyClient = pClient;
		aChosenMove = false;
		 
		createFrame();
		
		//will be used later on to show which squares are clickable 
		aGreenList = new ArrayList<Coordinate>(); 
		
		//creating a bigger rectangle so as to show the numbers 
		aBorder = new MinuetoRectangle(aBoardSize+25, aBoardSize+25, MinuetoColor.WHITE, true);

		//adding the numbers beside the board: 
		for (int i = 0; i < 30; i++) {
			Integer in = new Integer(i);
			MinuetoText number = new MinuetoText(in.toString(), aFont, MinuetoColor.BLACK, false);
			aBorder.draw(number, aBoardSize+5, (i)*aIncrement);
			aBorder.draw(number, ((i)*(aIncrement))+4, aBoardSize+5);
		}
		
		//and here's the actual board
		aBoard = new MinuetoRectangle(aBoardSize, aBoardSize, aMoveColour, true);
		
		//and loading the images - ONCE! 
		loadImages();
		
	}
	
	public void startThread() {
		/****DANGER ZONE: STARTING THE THREAD!*****/
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	private void createFrame() {
		
		JInternalFrame boardFrame = this;

		boardFrame.setResizable(false); //cannot resize the board
		boardFrame.setMaximizable(false);//or maximize it
		boardFrame.setLayout(new FlowLayout());
		boardFrame.setSize(new Dimension(aBoardSize+25, aBoardSize+25)); //should make the window a bit bigger?? 
	
		aMinuetoPanel = new MinuetoPanel(aBoardSize+25, aBoardSize+25); //create the window
		aEventQueue = new MinuetoEventQueue(); // Create and register event queue
					
		aMinuetoPanel.registerMouseHandler(this, aEventQueue);
		aMinuetoPanel.registerFocusHandler(this, aEventQueue);

		boardFrame.setContentPane(aMinuetoPanel);

		boardFrame.pack();
	}
	
	private void loadImages() {
		
		//Loading the images 
		BufferedImage img = null;
		
		//Tree (Coral)  
		try {
			img = ImageIO.read(new File("tree.png"));
		} catch (IOException e) {
		}
		aTree = new MinuetoImage(img);
		
		//Grass (Ocean)
		try {
			img = ImageIO.read(new File("grass.png"));
		} catch (IOException e) {
		}
		aGrass = new MinuetoImage(img);

		//Mine
		try {
			img = ImageIO.read(new File("mine.png"));
		} catch (IOException e) {
		}
		aMine = new MinuetoImage(img);
		
		//Target
		try {
			img = ImageIO.read(new File("target.png"));
		} catch (IOException e) {
		}
		aTarget = new MinuetoImage(img);

		//Base
		try {
			img = ImageIO.read(new File("base.png"));
		} catch (IOException e) {
		}
		aBase = new MinuetoImage(img);
		
		//BaseDestroyed
		try {
			img = ImageIO.read(new File("basedestroyed.png"));
		} catch (IOException e) {
		}
		aBaseDestroyed = new MinuetoImage(img);
		
		//Opponent base
		try {
			img = ImageIO.read(new File("baseopponent.png"));
		} catch (IOException e) {
		}
		aOpponentBase = new MinuetoImage(img);
		
		//Opponent Base Destroyed
		try {
			img = ImageIO.read(new File("baseopponentdestroyed.png"));
		} catch (IOException e) {
		}
		aOpponentBaseDestroyed = new MinuetoImage(img);
		
		//Sheep
		try {
			img = ImageIO.read(new File("sheep.png"));
		} catch (IOException e) {
		}
		aSheep = new MinuetoImage(img);
		
		//Sheep Damaged
		try {
			img = ImageIO.read(new File("sheepdamaged.png"));
		} catch (IOException e) {
		}
		aSheepDamaged = new MinuetoImage(img);
		
		//Sheep Destroyed
		try {
			img = ImageIO.read(new File("sheepdestroyed.png"));
		} catch (IOException e) {
		}
		aSheepDestroyed = new MinuetoImage(img);
		
		//Sheep Head
		try {
			img = ImageIO.read(new File("sheephead.png"));
		} catch (IOException e) {
		}
		aSheepHead = new MinuetoImage(img);
		
		//Sheep Head Damaged
		try {
			img = ImageIO.read(new File("sheepheaddamaged.png"));
		} catch (IOException e) {
		}
		aSheepHeadDamaged = new MinuetoImage(img);
		
		//Sheep Head Destroyed
		try {
			img = ImageIO.read(new File("sheepheaddestroyed.png"));
		} catch (IOException e) {
		}
		aSheepHeadDestroyed = new MinuetoImage(img);

		//Radar Sheep
		try {
			img = ImageIO.read(new File("sheepradar.png"));
		} catch (IOException e) {
		}
		aSheepRadar = new MinuetoImage(img);
		
		//Radar Sheep Destroyed
		try {
			img = ImageIO.read(new File("sheepradardestroyed.png"));
		} catch (IOException e) {
		}
		aSheepRadarDestroyed = new MinuetoImage(img);
		
		//Opponent Sheep
		try {
			img = ImageIO.read(new File("sheepopponent.png"));
		} catch (IOException e) {
		}
		aOpponentSheep = new MinuetoImage(img);
		
		//Opponent Sheep Damaged
		try {
			img = ImageIO.read(new File("sheepopponentdamaged.png"));
		} catch (IOException e) {
		}
		aOpponentSheepDamaged = new MinuetoImage(img);
		
		//Opponent Sheep Destroyed
		try {
			img = ImageIO.read(new File("sheepopponentdestroyed.png"));
		} catch (IOException e) {
		}
		aOpponentSheepDestroyed = new MinuetoImage(img);
		
		//Opponent Sheep Head
		try {
			img = ImageIO.read(new File("sheepopponenthead.png"));
		} catch (IOException e) {
		}
		aOpponentSheepHead = new MinuetoImage(img);
		
		//Opponent Sheep Head Damaged
		try {
			img = ImageIO.read(new File("sheepopponentheaddamaged.png"));
		} catch (IOException e) {
		}
		aOpponentSheepHeadDamaged = new MinuetoImage(img);
		
		//Opponent Sheep Head Destroyed
		try {
			img = ImageIO.read(new File("sheepopponentheaddestroyed.png"));
		} catch (IOException e) {
		}
		aOpponentSheepHeadDestroyed = new MinuetoImage(img);
		
		//Opponent Radar Sheep
		try {
			img = ImageIO.read(new File("sheepopponentradar.png"));
		} catch (IOException e) {
		}
		aOpponentSheepRadar = new MinuetoImage(img);
		
		//Opponent Radar Sheep Destroyed
		try {
			img = ImageIO.read(new File("sheepopponentradardestroyed.png"));
		} catch (IOException e) {
		}
		aOpponentSheepRadarDestroyed = new MinuetoImage(img);
		
		
		//Radar
		aRadar = new MinuetoRectangle(aIncrement, aIncrement, aRadarColour, true);
		//Sonar
		aSonar = new MinuetoRectangle(aIncrement, aIncrement, aSonarColour, true);
		
	}
	
	public void setVisible(boolean arg0) {
		super.setVisible(arg0);
		
		// make the minueto panel visible as well 
		if (aMinuetoPanel != null) aMinuetoPanel.setVisible(arg0);
	}
	
	public void run() {
		
		this.drawBoard();
		
		while(open) {
			synchronized (aMinuetoPanel) {
				if (aMinuetoPanel.isVisible()) {
					
					// Handle all the events in the event queue.
					while (aEventQueue.hasNext()) {
						aEventQueue.handle();
					}
					
					aMinuetoPanel.clear(MinuetoColor.WHITE);
					aBorder.draw(aBoard, 0, 0);	
					drawLines();
					aMinuetoPanel.draw(aBorder, 0,0);
					
					aMinuetoPanel.render();
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			Thread.yield();
		}
		aMinuetoPanel.close(); //not sure if I should be closing it? 
		dispose(); // or disposing of it? 
	}
	
	/**
	 * Method to redraw the board
	 * @param pVisibleBoard
	 */
	public void redrawBoard(Square[][] pVisibleBoard) {

		aVisibleBoard = pVisibleBoard;
		drawBoard();
	}
	
	/**
	 * Method to be called when setup is over. 
	 */
	public void startGame() {
		//resetting some attributes
		aGreenPhase = false;
		aGreenList = new ArrayList<Coordinate>();
		//and setting game in progress to be true!
		aGameInProgress = true;
	}
	
	public boolean getStatus() {
		return aGameInProgress;
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
		
		drawBoard();
		aGreenList = pList;
		aGreenPhase = true;
		aChosenMove = false;
		
		MinuetoRectangle blueRectangle = new MinuetoRectangle(aIncrement, aIncrement, aMoveColour, true);
		
		//load available moves
		String imageFile = "target.png";
		BufferedImage img = null;
		try {
			//System.out.println(imageFile);
			img = ImageIO.read(new File(imageFile));
		} catch (IOException e) {
			System.out.println("fail image reading");
		}
		MinuetoImage targetRectangle = new MinuetoImage(img);
		
		for (Coordinate c : aGreenList) {
		
			int x = c.getX();
			int y = c.getY();
			
			if (aVisibleBoard[x][y] instanceof MineSquare) {
				aBoard.draw(targetRectangle, x*aIncrement, y*aIncrement);
			} else if (aVisibleBoard[x][y] instanceof ShipSquare) {
				aBoard.draw(targetRectangle,x*aIncrement, y*aIncrement);	
			} else if (aVisibleBoard[x][y] instanceof BaseSquare) {
				aBoard.draw(targetRectangle,x*aIncrement, y*aIncrement);
			} else {
				aBoard.draw(blueRectangle,x*aIncrement, y*aIncrement);
			}
		}
		
		this.drawLines();
	}
	
	/**
	 * Draws the board 
	 */
	private void drawBoard () {
		
		aBoard.clear(aRadarColour);

		for (int i = 0; i < aVisibleBoard.length; i++) {
			for (int j = 0; j < aVisibleBoard.length; j++) {
				if (aVisibleBoard[i][j] instanceof CoralReef) aBoard.draw(aTree, i*aIncrement, j*aIncrement);
				else if (aVisibleBoard[i][j] instanceof ShipSquare) this.addShip(i, j);
				else if (aVisibleBoard[i][j] instanceof BaseSquare) this.addBase(i,j);
				else if (aVisibleBoard[i][j] instanceof MineSquare) aBoard.draw(aMine, i*aIncrement, j*aIncrement);
				else if (aVisibleBoard[i][j] instanceof RadarSquare) aBoard.draw(aRadar, i*aIncrement, j*aIncrement);
				else if (aVisibleBoard[i][j] instanceof SonarSquare) aBoard.draw(aSonar, i*aIncrement, j*aIncrement);
				else aBoard.draw(aGrass,  i*aIncrement, j*aIncrement);
			}
		}
		
		this.drawLines();
				
	}
	
	private void drawLines() {
		
		//Note that (0,0) is the top left corner
		//Drawing on the squares
		for (int i = aIncrement; i <= aBoardSize; i = i + aIncrement) {
			aBorder.drawLine(MinuetoColor.BLACK, i, 0, i, aBoardSize+25);
		}
		for (int j = aIncrement; j <= aBoardSize; j = j + aIncrement) {
			aBorder.drawLine(MinuetoColor.BLACK, 0, j, aBoardSize+25, j);
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
		
		String myOwner = baseSquare.getOwner();
		MinuetoImage base; 
		
		if (myOwner.compareTo(aUsername) == 0) {
			if (baseDamage == Damage.DESTROYED) {
				base = aBaseDestroyed;
			} else base = aBase;
		} else {
			if (baseDamage == Damage.DESTROYED) {
				base = aOpponentBaseDestroyed;
			} else base= aOpponentBase;
		}

		aBoard.draw(base, pSquareX*aIncrement, pSquareY*aIncrement);
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
		MinuetoImage sheep;
		
		
		if (ship.getShipID() == 9 && !shipSquare.isHead()) {
			//is Radar Boat and not the head
			//we distinguish it with a different colouring scheme  
			if (shipDamage == Damage.UNDAMAGED) {
				if (isOpponent) sheep = aOpponentSheepRadar; 
				else sheep = aSheepRadar; 
			}
			else { //DESTROYED (Radar ships cannot be damaged)
				if (isOpponent) sheep = aOpponentSheepRadarDestroyed; //dark red
				else sheep = aSheepRadarDestroyed; //red
			}
			
		} else { //not Radar boat (or Radar boat head)
			
			//HEAD
			if (shipSquare.isHead()) {
				if (shipDamage == Damage.UNDAMAGED) {
					if (isOpponent) sheep = aOpponentSheepHead; 
					else sheep = aSheepHead; 
				}
				else if (shipDamage == Damage.DAMAGED) {
					if (isOpponent) sheep = aOpponentSheepHeadDamaged; 
					else sheep = aSheepHeadDamaged; 
				}
				else { //DESTROYED
					if (isOpponent) sheep = aOpponentSheepHeadDestroyed; //dark red
					else sheep = aSheepHeadDestroyed; //red
				}
			//BODY
			} else {
				if (shipDamage == Damage.UNDAMAGED) {
					if (isOpponent) sheep = aOpponentSheep; 
					else sheep = aSheep; 
				}
				else if (shipDamage == Damage.DAMAGED) {
					if (isOpponent) sheep = aOpponentSheepDamaged; 
					else sheep = aSheepDamaged; 
				}
				else { //DESTROYED
					if (isOpponent) sheep = aOpponentSheepDestroyed; //dark red
					else sheep = aSheepDestroyed; //red
				}
			}
		}
		
		aBoard.draw(sheep, pSquareX*aIncrement, pSquareY*aIncrement);
	}
	
	//given mouse coordinates, board will convert to a game square
	private Coordinate convertToSquare(int pX, int pY) {

		int x = pX/aIncrement; 
		int y = pY/aIncrement;
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
		
		//SETUP PHASE
		if (!aGameInProgress) {
			
			Coordinate coord = convertToSquare(arg0, arg1);

			if (coord.inBounds()) {
			
				Square currentSquare = aVisibleBoard[coord.getX()][coord.getY()];
				
				//Clicked on ship ==> tell client. 
				if (currentSquare instanceof ShipSquare) {
					//haven't yet chosen a move for the ship
					aChosenMove = false;
					//it has to be your own ship (enemies are not visible)
					//so let's tell the client to calculate the base positions
					Ship s = ((ShipSquare)currentSquare).getShip();
					aMyClient.showAvailableBasePositions(s);
				}//Clicked on green square ==> tell Client
				else if (aGreenList.contains(coord) && !aChosenMove) {
					aChosenMove = true;
					aGreenList = new ArrayList<Coordinate>();
					aMyClient.placedShip(coord);
				}
				else { //Clicked somewhere else ==> just redraw board
					aChosenMove = false; 
					aGreenList = new ArrayList<Coordinate>();
					drawBoard();
					//plus tell Client 
					aMyClient.reshowShipSetup();
				}
				
				
			}
		
		}
		//GAME IN PROGRESS
		else {
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
						//If a valid move was chosen (i.e. clicked on a green square)
						if (aGreenList.contains(coord) && !aChosenMove){
							//Updating either the first or the second coord
							if (aFirstChosenCoord == null) {
								aFirstChosenCoord = coord;
							}
							else {
								aSecondChosenCoord = coord;
							}
							//tell Client about choice
							aChosenMove = aMyClient.greenSelected(aFirstChosenCoord, aSecondChosenCoord);
							//If a move was actually chosen (and completed) we reset the chosen coordinates and end the green phase
							if (aChosenMove) {
								aFirstChosenCoord = null;
								aSecondChosenCoord = null;
								aGreenPhase = false;
								//Resetting the green list
								aGreenList = new ArrayList<Coordinate>();
								//Redrawing the board 
								drawBoard();
							}
						}
						else {
							//Clicked on a non-green square? Then reset the chosen coords and end the green phase
							aFirstChosenCoord = null;
							aSecondChosenCoord = null;
							aGreenPhase = false;
							//Resetting the green list
							aGreenList = new ArrayList<Coordinate>();
							//Redrawing the board 
							drawBoard();
						}
						
						
							
					}
				}
			} 
		}
		
	}

	@Override
	public void handleMouseRelease(int arg0, int arg1, int arg2) {
		
	}
}
