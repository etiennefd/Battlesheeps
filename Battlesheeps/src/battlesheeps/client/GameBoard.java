package battlesheeps.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JInternalFrame;

import org.minueto.MinuetoColor;
import org.minueto.MinuetoEventQueue;
import org.minueto.handlers.MinuetoFocusHandler;
import org.minueto.handlers.MinuetoMouseHandler;
import org.minueto.image.MinuetoRectangle;
import org.minueto.window.MinuetoPanel;

import battlesheeps.board.CoralReef;
import battlesheeps.board.ShipSquare;
import battlesheeps.board.Square;
import battlesheeps.ships.Ship;
import battlesheeps.ships.Ship.Damage;


public class GameBoard extends JInternalFrame implements MinuetoMouseHandler, MinuetoFocusHandler, Runnable{
	
	/**
	 * JInternalFrame to display the game board
	 */
	private static final long serialVersionUID = 55672340L;
	private MinuetoRectangle aBoard;
	private String aUsername;
	private int boardSize;
	private MinuetoPanel aMinuetoPanel;
	private MinuetoEventQueue aEventQueue;
	private boolean open = true;
	
	public GameBoard(int pSize, String pUser) {
		
		super("Board for " + pUser);
		aUsername = pUser;
		//NOTE: size must be an integer divisible by 30
		boardSize = pSize;
		createBoard();
		
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
		aBoard = new MinuetoRectangle(boardSize, boardSize, MinuetoColor.BLUE, true);
		
		int increment = boardSize/30; 
		
		//Adding the base (10 squares)
		MinuetoRectangle base = new MinuetoRectangle(10*increment, increment, MinuetoColor.WHITE, true);
		aBoard.draw(base, (aBoard.getWidth()/3), 0); //Enemy base
		aBoard.draw(base, (aBoard.getWidth()/3), aBoard.getHeight()-increment);//Your base
		
		
		//Note that (0,0) is the top left corner
		//Drawing on the squares
		for (int i = increment; i < boardSize; i = i + increment) {
			aBoard.drawLine(MinuetoColor.BLACK, i, 0, i, boardSize);
		}
		
		for (int j = increment; j < boardSize; j = j + increment) {
			aBoard.drawLine(MinuetoColor.BLACK, 0, j, boardSize, j);
		}
		
		while(open) {
			//not entirely sure what 
			synchronized (aMinuetoPanel) {
				if (aMinuetoPanel.isVisible()) {
					
					// Handle all the events in the event queue.
					while (aEventQueue.hasNext()) {
						aEventQueue.handle();
					}
					
					aMinuetoPanel.clear(MinuetoColor.BLUE);
					
					aMinuetoPanel.draw(aBoard, 0,0);
					
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
	
	//assuming that the coordinates are the square coordinates e.g. (5,5)
	//and must be transformed into gameboard coordinates
	protected void addCoralReefs(Square[][] pBoard) {
		
		int increment = aBoard.getHeight()/30;
		MinuetoRectangle coralReef = new MinuetoRectangle(increment, increment, MinuetoColor.YELLOW, true);
		
		for (int i = 0; i < pBoard.length; i++) {
			for (int j = 0; j < pBoard.length; i++) {
				if (pBoard[i][j] instanceof CoralReef) {
					aBoard.draw(coralReef, i*increment, j*increment);
				}
			}
		}
	}
	
protected void addShips (Square[][] pBoard) {
		
		int increment = aBoard.getHeight()/30;

		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < 30; j++) {
				if (pBoard[i][j] instanceof ShipSquare) {
					
					ShipSquare shipSquare = (ShipSquare) pBoard[i][j];
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
						if (isOpponent) shipColor = new MinuetoColor(Color.DARK_GRAY); //dark gray
						else shipColor = new MinuetoColor(Color.GRAY); //gray
					}
					else if (shipDamage == Damage.DAMAGED) {
						if (isOpponent) shipColor = new MinuetoColor(new Color(232, 231, 127)); //dark pink
						else shipColor = new MinuetoColor(Color.PINK); //pink
					}
					else { //DESTROYED
						if (isOpponent) shipColor = new MinuetoColor(new Color(239, 230, 82)); //dark red
						else shipColor = new MinuetoColor(Color.RED); //red
					}
					//and lastly if it's the head or body
					if (shipSquare.isHead()) {
						battlesheeps.server.ServerGame.Direction shipDirection = ship.getDirection();
						switch (shipDirection) {
						case NORTH : drawShipHeadNorth(i, j, increment, shipColor);
						case SOUTH : drawShipHeadSouth(i, j, increment, shipColor);
						case EAST : drawShipHeadEast(i, j, increment, shipColor);
						default : drawShipHeadWest(i, j, increment, shipColor);
						}
					}
					else {
						drawShipBody(i,j,increment, shipColor);
					}

				}
			}
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
	private void drawShipBody(int pX, int pY, int pIncrement, MinuetoColor pColor) {
		MinuetoRectangle ship = new MinuetoRectangle(pIncrement, pIncrement, pColor, true);
		aBoard.draw(ship, pX*pIncrement, pY*pIncrement);
	}
	
	private void drawShipHeadNorth(int pX, int pY, int pIncrement, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = pX*pIncrement;
		points[1] = pY*pIncrement;
		//upper middle
		points[2] = (pX*pIncrement)+(pIncrement/2);
		points[3] = (pY*pIncrement)-pIncrement;
		//corner
		points[4] = (pX*pIncrement)+pIncrement;
		points[5] = pY*pIncrement;
		aBoard.drawPolygon(pColor, points);
	}

	private void drawShipHeadSouth(int pX, int pY, int pIncrement, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = pX*pIncrement;
		points[1] = pY*pIncrement;
		//lower middle
		points[2] = (pX*pIncrement)+(pIncrement/2);
		points[3] = (pY*pIncrement)+pIncrement;
		//corner
		points[4] = (pX*pIncrement)+pIncrement;
		points[5] = pY*pIncrement;
		aBoard.drawPolygon(pColor, points);
	}
	
	private void drawShipHeadEast(int pX, int pY, int pIncrement, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = pX*pIncrement;
		points[1] = pY*pIncrement;
		//lower middle
		points[2] = (pX*pIncrement) + pIncrement;
		points[3] = (pY*pIncrement)+(pIncrement/2);
		//corner
		points[4] = (pX*pIncrement);
		points[5] = (pY*pIncrement) + pIncrement;
		aBoard.drawPolygon(pColor, points);
	}
	
	private void drawShipHeadWest(int pX, int pY, int pIncrement, MinuetoColor pColor) {
		int[] points = new int[6];
		//corner
		points[0] = (pX*pIncrement)+ pIncrement;
		points[1] = pY*pIncrement;
		//lower middle
		points[2] = (pX*pIncrement);
		points[3] = (pY*pIncrement)+ (pIncrement/2);
		//corner
		points[4] = (pX*pIncrement) + pIncrement;
		points[5] = (pY*pIncrement) + pIncrement;
		aBoard.drawPolygon(pColor, points);
	}
//given mouse coordinates, board will convert to a game square
	protected int[] convertToSquare(int pX, int pY) {
		int[] square = new int[2]; //by default, I believe it's 0,0
		int increment = aBoard.getHeight()/30;
		square[0] = pX/increment; 
		square[1] = pY/increment;

		return square;
	}

	@Override
	public void handleGetFocus() {
		// TODO Auto-generated method stub
		//not sure how to handle focus... 
	}

	@Override
	public void handleLostFocus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMouseMove(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMousePress(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMouseRelease(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}