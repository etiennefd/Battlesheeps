package battlesheeps.game;

import java.util.ArrayList;

import battlesheeps.accounts.Account;
import battlesheeps.board.*;
import battlesheeps.exceptions.InvalidCoordinateException;
import battlesheeps.ships.*;
import battlesheeps.ships.Ship.Damage;

public class Game {

	public enum Visible {
		COVERED_BY_RADAR, COVERED_BY_SONAR, NOT_COVERED
	}
	
	public enum MoveType {
		TURN_SHIP, TRANSLATE_SHIP, FIRE_CANNON, FIRE_TORPEDO, 
		DROP_MINE, PICKUP_MINE, TRIGGER_RADAR, REPAIR_SHIP
	}
	
	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
	
	private int aGameID;
	private Account aPlayer1;
	private Account aPlayer2;
	private int aTurnNum;
	private String aDateLastPlayed;
	
	private Square[][] aBoard;
	private ArrayList<Ship> aShipListP1;
	private ArrayList<Ship> aShipListP2;
	
	/**
	 * Constructor for Games. Accepts an ID and the two players. 
	 * Normally called from the GameManager based on the information held in the relevant GameRequest. 
	 * @param pGameID
	 * @param pPlayer1
	 * @param pPlayer2
	 */
	public Game(int pGameID, Account pPlayer1, Account pPlayer2) {
		
		aGameID = pGameID;
		aPlayer1 = pPlayer1;
		aPlayer2 = pPlayer2;
		aTurnNum = 0;
		aDateLastPlayed = "never";
		
		aBoard = new Square[30][30];
		//Filling the board with sea and base squares
		for (int x = 0; x<aBoard.length; x++) {
			for (int y = 0 ; y<aBoard[x].length; y++) {
				if (y >= 10 && y < 20 && (x==0 || x==29)) {
					aBoard[x][y] = new BaseSquare(Damage.UNDAMAGED);
				}
				else {
					aBoard[x][y] = new Sea();
				}
			}
		}
		
		//Put random coral reefs on the board
		generateCoralReefs();
		
		//Generating the ships for both players
		aShipListP1 = new ArrayList<Ship>();
		aShipListP1.add(new Cruiser());
		aShipListP1.add(new Cruiser());
		aShipListP1.add(new Destroyer());
		aShipListP1.add(new Destroyer());
		aShipListP1.add(new Destroyer());
		aShipListP1.add(new TorpedoBoat());
		aShipListP1.add(new TorpedoBoat());
		aShipListP1.add(new MineLayer());
		aShipListP1.add(new MineLayer());
		aShipListP1.add(new RadarBoat());
		
		aShipListP2 = new ArrayList<Ship>();
		aShipListP2.add(new Cruiser());
		aShipListP2.add(new Cruiser());
		aShipListP2.add(new Destroyer());
		aShipListP2.add(new Destroyer());
		aShipListP2.add(new Destroyer());
		aShipListP2.add(new TorpedoBoat());
		aShipListP2.add(new TorpedoBoat());
		aShipListP2.add(new MineLayer());
		aShipListP2.add(new MineLayer());
		aShipListP2.add(new RadarBoat());
		
	}
	
	/**
	 * Adds 24 coral reef squares to the board within the central 10*24 area. 
	 */
	public void generateCoralReefs() {
		for (int i = 0; i<24; i++) {
			Coordinate newCoral = Coordinate.randomCoord(10, 19, 3, 26);
			if (aBoard[newCoral.getX()][newCoral.getY()] instanceof CoralReef) {
				//We are trying to put a coral reef where there is already one. Just ignore this loop iteration. 
				i--;
			}
			else {
				aBoard[newCoral.getX()][newCoral.getY()] = new CoralReef();
			}
		}
	}
	
	/**
	 * Places a ship on the board, i.e. sets the appropriate squares on the board to ShipSquares.
	 * Also sets the ship's internal location parameters. 
	 *  
	 * TODO maybe it would be good to have another setShipPosition that takes pHead and a Direction 
	 * (because this one is annoying to use since it needs the pHead and pTail to correspond to the length of the ship)
	 * @param pShip
	 * @param pHead
	 * @param pTail
	 */
	public void setShipPosition(Ship pShip, Coordinate pHead, Coordinate pTail) {
		
		//Are the head and the tail on the same X column? Do they correspond to the length of the ship? 
		if (pHead.getX() == pTail.getX() && Math.abs(pHead.getY() - pTail.getY()) == pShip.getSize() - 1) {
			
			removeShip(pShip);
			pShip.setLocation(pHead, pTail);
			
			//Updating the board. First, the head
			aBoard[pHead.getX()][pHead.getY()] = new ShipSquare(pShip, Damage.UNDAMAGED, true);
			//Then everything that is between the head and the tail
			//We must check whether to increase or decrease y (does the head have a higher Y position than the tail?)
			if (pHead.getY() > pTail.getY()) {
				for (int y = pHead.getY() - 1; y > pTail.getY(); y--) {
					aBoard[pHead.getX()][y] = new ShipSquare(pShip, Damage.UNDAMAGED, false);
				}
			}
			else if (pHead.getY() < pTail.getY()) {
				for (int y = pHead.getY() + 1; y < pTail.getY(); y++) {
					aBoard[pHead.getX()][y] = new ShipSquare(pShip, Damage.UNDAMAGED, false);
				}
			}
			//Finally, the tail
			aBoard[pTail.getX()][pTail.getY()] = new ShipSquare(pShip, Damage.UNDAMAGED, false);
		} 
		
		//Are the head and the tail on the same Y row? Do they correspond to the length of the ship? 
		else if (pHead.getY() == pTail.getY() && Math.abs(pHead.getX() - pTail.getX()) == pShip.getSize() - 1) {
			
			removeShip(pShip);
			pShip.setLocation(pHead, pTail);
			
			//Updating the board. First, the head
			aBoard[pHead.getX()][pHead.getY()] = new ShipSquare(pShip, Damage.UNDAMAGED, true);
			//Then everything that is between the head and the tail
			//We must check whether to increase or decrease y (does the head have a higher Y position than the tail?)
			if (pHead.getX() > pTail.getX()) {
				for (int x = pHead.getX() - 1; x > pTail.getX(); x--) {
					aBoard[x][pHead.getY()] = new ShipSquare(pShip, Damage.UNDAMAGED, false);
				}
			}
			else if (pHead.getX() < pTail.getX()) {
				for (int x = pHead.getX() + 1; x < pTail.getX(); x++) {
					aBoard[x][pHead.getY()] = new ShipSquare(pShip, Damage.UNDAMAGED, false);
				}
			}
			//Finally, the tail
			aBoard[pTail.getX()][pTail.getY()] = new ShipSquare(pShip, Damage.UNDAMAGED, false);
		}
		else {
			throw new InvalidCoordinateException();
		}
	}
	
	/**
	 * Changes the squares that were occupied by a ship to sea squares. 
	 * Does not affect the ship's parameters at all. 
	 * Should be called when modifying the position of a ship or when a ship is sunk. 
	 * @param pShip
	 */
	public void removeShip(Ship pShip) {
		if(pShip.getHead() == null || pShip.getTail() == null) {
			return; //this means the ship doesn't have a position yet, so nothing to remove
		}
		
		int headX = pShip.getHead().getX();
		int headY = pShip.getHead().getY();
		int tailX = pShip.getTail().getX();
		int tailY = pShip.getTail().getY();
		Direction direction = pShip.getDirection();
		
		switch (direction) {
		case NORTH: 
			for (int y = headY; y <= tailY; y++) {
				aBoard[headX][y] = new Sea();
			}
			break;
		case SOUTH: 
			for (int y = headY; y >= tailY; y--) {
				aBoard[headX][y] = new Sea();
			}
			break;
		case EAST: 
			for (int x = headX; x >= tailY; x--) {
				aBoard[x][headY] = new Sea();
			}
			break;
		case WEST: 
			for (int x = headX; x <= tailX; x++) {
				aBoard[x][headY] = new Sea();
			}
			break;
		}
	}
	
	
	/**
	 * Called from the GameManager to process a move on the board. 
	 * A private method will be called depending on which kind of move it is. 
	 * @param pShip
	 * @param pMove
	 */
	public void computeMoveResult(Ship pShip, MoveType pMove, Coordinate pCoord) { //Removed GameID and player because the game calls this instead of the gameManager
		switch (pMove) {
		case TRANSLATE_SHIP: translateShip(pShip, pCoord); break;
		case TURN_SHIP: turnShip(pShip, pCoord); break;
		case FIRE_CANNON: fireCannon(pShip, pCoord); break;
		case FIRE_TORPEDO: fireTorpedo(pShip, pCoord); break;
		case DROP_MINE: dropMine(pShip, pCoord); break;
		case PICKUP_MINE: pickupMine(pShip, pCoord); break;
		case TRIGGER_RADAR: triggerRadar(pShip, pCoord); break;
		case REPAIR_SHIP: repairShip(pShip, pCoord); break;
		}
	}
	
	/**
	 * Computes the new position of a ship and any side effects (such as encountering another ship or a mine)
	 * when a ship translates (i.e. moves on the board). 
	 * Summary: 
	 * - Get the current location of the ship and its direction (north, south, etc.)
	 * - Based on this, determine if the ship is moving forward, backward, or to the sides
	 * - Based on this, look for obstacles/mines and compute new position of the ship
	 * - Update ship and board accordingly. 
	 * 
	 * This method assumes valid input. 
	 * (I.e. the destination of the ship may only be one square behind the tail of the ship, one square
	 * port or starboard of the head, or somewhere in front of the head depending on the ship's speed. 
	 * Inputs will be validated in the GUI itself.)
	 * 
	 * Important note: pDestination represents the square where the HEAD of the ship will be after the move, 
	 * except when going backwards. Then, pDestination represents where the TAIL will be. 
	 * 
	 * TODO recompute visibility at the end of the move
	 * 
	 * @param pShip
	 * @param pDestination
	 */
	private void translateShip(Ship pShip, Coordinate pDestination) {

		int headX = pShip.getHead().getX();
		int headY = pShip.getHead().getY();
		int tailX = pShip.getTail().getX();
		int tailY = pShip.getTail().getY();
		Direction direction = pShip.getDirection();
		
		//Since the place where the ship stops movement might be different from pDestination, we have these two variables: 
		Coordinate finalDestinationHead = null;  
		Coordinate finalDestinationTail = null;
		
		switch (direction) {
		case NORTH: 
			//***Case forward: 
			if (pDestination.getY() < headY) {
				//Check for every Y between head and destination
				for (int y = headY - 1; y >= pDestination.getY(); y--) {
					Square s = aBoard[headX][y];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(headX, y+1); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y); //The square with the mine itself
						mineExplode();
						//TODO Log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
				}
				finalDestinationTail = new Coordinate (pDestination.getX(), finalDestinationHead.getY() + pShip.getSize() - 1);
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getY() > tailY) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					//TODO Create log entry/notify players
				}
				else if (s instanceof MineSquare) {
					finalDestinationTail = pDestination; //The square with the mine itself
					finalDestinationHead = new Coordinate (headX, headY + 1);
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX, headY + 1);
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getX() < headX || pDestination.getX() > headX) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headY; i <= tailY; i++) {
					Square s = aBoard[pDestination.getX()][i];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationTail = pShip.getTail();
						finalDestinationHead = pShip.getHead();
						//Log entry
						break;
					}
				}
				//Now if there wasn't an obstacle, we look for mines in the same area
				if (finalDestinationHead == null) {
					for (int i = headY; i <= tailY; i++) {
						Square s = aBoard[pDestination.getX()][i];
						if (s instanceof MineSquare) {
							finalDestinationTail = new Coordinate (pDestination.getX(), tailY);
							finalDestinationHead = new Coordinate (pDestination.getX(), headY);
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
				}
				//If there still is no final destination, no obstacle/mine was found and movement succeeds. 
				if (finalDestinationHead == null) {
					finalDestinationTail = new Coordinate (pDestination.getX(), tailY);
					finalDestinationHead = new Coordinate (pDestination.getX(), headY);
				}
			}
			break; //from case NORTH
			
			
		case SOUTH: 
			//***Case forward: 
			if (pDestination.getY() > headY) {
				//Check for every Y between head and destination
				for (int y = headY + 1; y <= pDestination.getY(); y++) {
					Square s = aBoard[headX][y];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(headX, y-1); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y); //The square with the mine itself
						mineExplode();
						//TODO Log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
				}
				finalDestinationTail = new Coordinate (pDestination.getX(), finalDestinationHead.getY() - pShip.getSize() + 1);
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getY() < tailY) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					//TODO Create log entry/notify players
				}
				else if (s instanceof MineSquare) {
					finalDestinationTail = pDestination; //The square with the mine itself
					finalDestinationHead = new Coordinate (headX, headY - 1);
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX, headY - 1);
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getX() < headX || pDestination.getX() > headX) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headY; i <= tailY; i++) {
					Square s = aBoard[pDestination.getX()][i];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationTail = pShip.getTail();
						finalDestinationHead = pShip.getHead();
						//Log entry
						break;
					}
				}
				//Now if there wasn't an obstacle, we look for mines in the same area
				if (finalDestinationHead == null) {
					for (int i = headY; i <= tailY; i++) {
						Square s = aBoard[pDestination.getX()][i];
						if (s instanceof MineSquare) {
							finalDestinationTail = new Coordinate (pDestination.getX(), tailY);
							finalDestinationHead = new Coordinate (pDestination.getX(), headY);
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
				}
				//If there still is no final destination, no obstacle/mine was found and movement succeeds. 
				if (finalDestinationHead == null) {
					finalDestinationTail = new Coordinate (pDestination.getX(), tailY);
					finalDestinationHead = new Coordinate (pDestination.getX(), headY);
				}
			}
			break; //from case SOUTH
			
			
		case WEST: 
			//***Case forward: 
			if (pDestination.getX() < headX) {
				//Check for every X between head and destination
				for (int x = headX - 1; x >= pDestination.getX(); x--) {
					Square s = aBoard[x][headY];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(x+1, headY); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x, headY); //The square with the mine itself
						mineExplode();
						//TODO Log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
				}
				finalDestinationTail = new Coordinate (finalDestinationHead.getX() + pShip.getSize() - 1, pDestination.getY());
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getX() > tailX) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					//TODO Create log entry/notify players
				}
				else if (s instanceof MineSquare) {
					finalDestinationTail = pDestination; //The square with the mine itself
					finalDestinationHead = new Coordinate (headX + 1, headY);
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX + 1, headY);
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getY() < headY || pDestination.getY() > headY) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headX; i <= tailX; i++) {
					Square s = aBoard[i][pDestination.getY()];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationTail = pShip.getTail();
						finalDestinationHead = pShip.getHead();
						//Log entry
						break;
					}
				}
				//Now if there wasn't an obstacle, we look for mines in the same area
				if (finalDestinationHead == null) {
					for (int i = headX; i <= tailX; i++) {
						Square s = aBoard[i][pDestination.getY()];
						if (s instanceof MineSquare) {
							finalDestinationTail = new Coordinate (tailX, pDestination.getY());
							finalDestinationHead = new Coordinate (headX, pDestination.getY());
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
				}
				//If there still is no final destination, no obstacle/mine was found and movement succeeds. 
				if (finalDestinationHead == null) {
					finalDestinationTail = new Coordinate (tailX, pDestination.getY());
					finalDestinationHead = new Coordinate (headX, pDestination.getY());
				}
			}
			break; //from case WEST
			
			
		case EAST: 
			//***Case forward: 
			if (pDestination.getX() > headX) {
				//Check for every X between head and destination
				for (int x = headX + 1; x <= pDestination.getX(); x++) {
					Square s = aBoard[x][headY];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(x-1, headY); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x, headY); //The square with the mine itself
						mineExplode();
						//TODO Log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
				}
				finalDestinationTail = new Coordinate (finalDestinationHead.getX() - pShip.getSize() + 1, pDestination.getY());
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getX() < tailX) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					//TODO Create log entry/notify players
				}
				else if (s instanceof MineSquare) {
					finalDestinationTail = pDestination; //The square with the mine itself
					finalDestinationHead = new Coordinate (headX - 1, headY);
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX - 1, headY);
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getY() < headY || pDestination.getY() > headY) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headX; i <= tailX; i++) {
					Square s = aBoard[i][pDestination.getY()];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationTail = pShip.getTail();
						finalDestinationHead = pShip.getHead();
						//Log entry
						break;
					}
				}
				//Now if there wasn't an obstacle, we look for mines in the same area
				if (finalDestinationHead == null) {
					for (int i = headX; i <= tailX; i++) {
						Square s = aBoard[i][pDestination.getY()];
						if (s instanceof MineSquare) {
							finalDestinationTail = new Coordinate (tailX, pDestination.getY());
							finalDestinationHead = new Coordinate (headX, pDestination.getY());
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
				}
				//If there still is no final destination, no obstacle/mine was found and movement succeeds. 
				if (finalDestinationHead == null) {
					finalDestinationTail = new Coordinate (tailX, pDestination.getY());
					finalDestinationHead = new Coordinate (headX, pDestination.getY());
				}
			}
			break; //from case EAST
		}
		
		//After computing the ship's new position, we change the board accordingly. 
		setShipPosition(pShip, finalDestinationHead, finalDestinationTail);
	}
	
	private void turnShip(Ship pShip, Coordinate pCoord) {

	}
	private void fireCannon(Ship pShip, Coordinate pCoord) {

	}
	private void fireTorpedo(Ship pShip, Coordinate pCoord) {

	}
	private void dropMine(Ship pShip, Coordinate pCoord) {

	}
	private void pickupMine(Ship pShip, Coordinate pCoord) {

	}
	private void triggerRadar(Ship pShip, Coordinate pCoord) {

	}
	private void repairShip(Ship pShip, Coordinate pCoord) {

	}
	
	private void mineExplode() {
		//TODO
		System.out.println("BOOOOOOOM!!!!!");
	}

	/**
	 * Creates, as a string, an ASCII representation of the board. 
	 * @return
	 */
	public String printBoard() {
		String s = "Legend: \n" +
				"~  - Empty sea square\n" +
				"XX - Coral reef\n" +
				"B  - Base\n" +
				"C  - Cruiser\n" +
				"D  - Destroyer\n" +
				"T  - Torpedo boat\n" +
				"M  - Mine layer\n" +
				"R  - Radar boat\n" +
				"MM - Mine\n" +
				" 2 - Undamaged\n" +
				" 1 - Damaged (heavy armored ships only)\n" +
				" 0 - Destroyed\n" +
				"Lower case indicates the head of a ship\n";
		s = s + "                       1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2\n";
		s = s + "   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 x\n";
		for (int y = 0; y<aBoard.length; y++) {
			if (y < 10) s = s + " " + y + " ";
			else s = s + y + " ";
			for (int x = 0 ; x<aBoard[y].length; x++) {
				s = s + aBoard[x][y].toString();
			}
			s = s + "\n";
		}
		s = s + "y";
		return s;
	}

}
