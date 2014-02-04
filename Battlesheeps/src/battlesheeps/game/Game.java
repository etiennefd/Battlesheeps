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
		aShipListP1.add(new Cruiser(aPlayer1));
		aShipListP1.add(new Cruiser(aPlayer1));
		aShipListP1.add(new Destroyer(aPlayer1));
		aShipListP1.add(new Destroyer(aPlayer1));
		aShipListP1.add(new Destroyer(aPlayer1));
		aShipListP1.add(new TorpedoBoat(aPlayer1));
		aShipListP1.add(new TorpedoBoat(aPlayer1));
		aShipListP1.add(new MineLayer(aPlayer1));
		aShipListP1.add(new MineLayer(aPlayer1));
		aShipListP1.add(new RadarBoat(aPlayer1));
		
		aShipListP2 = new ArrayList<Ship>();
		aShipListP2.add(new Cruiser(aPlayer2));
		aShipListP2.add(new Cruiser(aPlayer2));
		aShipListP2.add(new Destroyer(aPlayer2));
		aShipListP2.add(new Destroyer(aPlayer2));
		aShipListP2.add(new Destroyer(aPlayer2));
		aShipListP2.add(new TorpedoBoat(aPlayer2));
		aShipListP2.add(new TorpedoBoat(aPlayer2));
		aShipListP2.add(new MineLayer(aPlayer2));
		aShipListP2.add(new MineLayer(aPlayer2));
		aShipListP2.add(new RadarBoat(aPlayer2));
		
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
	 * Sets a square of the board to a MineSquare
	 * @param pCoord
	 */
	public void addMine(Coordinate pCoord) {
		if (aBoard[pCoord.getX()][pCoord.getY()] instanceof Sea) {
			aBoard[pCoord.getX()][pCoord.getY()] = new MineSquare();
		}
	}
	
	/**
	 * Reverts a MineSquare back to empty sea
	 * @param pCoord
	 */
	public void removeMine(Coordinate pCoord) {
		if (aBoard[pCoord.getX()][pCoord.getY()] instanceof MineSquare) {
			aBoard[pCoord.getX()][pCoord.getY()] = new Sea();
		}
	}
	
	
	/**
	 * Called from the GameManager to process a move on the board. 
	 * A private method will be called depending on which kind of move it is. 
	 * @param pShip
	 * @param pMove
	 */
	public void computeMoveResult(Ship pShip, MoveType pMove, Coordinate pCoord) { //I removed GameID and player because the game calls this instead of the gameManager
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
	 * Important note: When moving forward, pDestination represents the square where the HEAD of the ship will be 
	 * after the move. When going backwards pDestination represents where the TAIL will be.
	 * When moving to the side, pDestination may be any of the squares the ship will occupy after the move. 
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
					Square sl = aBoard[headX - 1][y]; //sl and sr are the squares immediately beside square s. 
					Square sr = aBoard[headX + 1][y]; //They are checked for mines (which explode if the ship is next to them)
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(headX, y+1); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y+1); 
						mineExplode();
						//TODO Log entry (possibly in mineExplode())
						break;
					}
					else if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
					else if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					Square sf = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() - 1];
					if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
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
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX, headY + 1);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					Square sf = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1];
					Square sfl = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()]; 
					Square sfr = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()]; 
					if (sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfl instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfr instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getX() < headX || pDestination.getX() > headX) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headY; i <= tailY; i++) {
					Square s = aBoard[pDestination.getX()][i];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						//movement fails
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
							//movement fails
							finalDestinationTail = pShip.getTail();
							finalDestinationHead = pShip.getHead();
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
				//After movement it is possible that the ship has come in contact with a mine
				if (!(pShip instanceof MineLayer)) {
					for (int i = headY; i <= tailY; i++) {
						Square s1 = aBoard[pDestination.getX() - 1][i];
						Square s2 = aBoard[pDestination.getX() + 1][i];
						if (s1 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
						if (s2 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
					Square sf1 = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() - 1];
					Square sf2 = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1];
					if (sf1 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sf2 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
				
			}
			break; //from case NORTH
			
			
		case SOUTH: 
			//***Case forward: 
			if (pDestination.getY() > headY) {
				//Check for every Y between head and destination
				for (int y = headY + 1; y <= pDestination.getY(); y++) {
					Square s = aBoard[headX][y];
					Square sl = aBoard[headX + 1][y]; //sl and sr are the squares immediately beside square s. 
					Square sr = aBoard[headX - 1][y]; //They are checked for mines (which explode if the ship is next to them)
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(headX, y-1); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y-1);
						mineExplode();
						//TODO Log entry
						break;
					}
					else if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
					else if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					Square sf = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() + 1];
					if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
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
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX, headY - 1);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					Square sf = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1];
					Square sfl = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()]; 
					Square sfr = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()]; 
					if (sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfl instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfr instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
				
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getX() < headX || pDestination.getX() > headX) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headY; i >= tailY; i--) {
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
					for (int i = headY; i >= tailY; i--) {
						Square s = aBoard[pDestination.getX()][i];
						if (s instanceof MineSquare) {
							finalDestinationTail = pShip.getTail();
							finalDestinationHead = pShip.getHead();
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
				//After movement it is possible that the ship has come in contact with a mine
				if (!(pShip instanceof MineLayer)) {
					for (int i = headY; i >= tailY; i--) {
						Square s1 = aBoard[pDestination.getX() - 1][i];
						Square s2 = aBoard[pDestination.getX() + 1][i];
						if (s1 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
						if (s2 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
					Square sf1 = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() + 1];
					Square sf2 = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1];
					if (sf1 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sf2 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
			}
			break; //from case SOUTH
			
			
		case WEST: 
			//***Case forward: 
			if (pDestination.getX() < headX) {
				//Check for every X between head and destination
				for (int x = headX - 1; x >= pDestination.getX(); x--) {
					Square s = aBoard[x][headY];
					Square sl = aBoard[x][headY - 1]; //sl and sr are the squares immediately beside square s. 
					Square sr = aBoard[x][headY + 1]; //They are checked for mines (which explode if the ship is next to them)
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(x+1, headY); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x+1, headY); 
						mineExplode();
						//TODO Log entry (possibly in mineExplode())
						break;
					}
					else if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
					else if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					Square sf = aBoard[finalDestinationHead.getX() - 1][finalDestinationHead.getY()];
					if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
				finalDestinationTail = new Coordinate (pDestination.getX() + pShip.getSize() - 1, finalDestinationHead.getY());
				
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
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX + 1, headY);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					Square sf = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()];
					Square sfl = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1]; 
					Square sfr = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1]; 
					if (sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfl instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfr instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getY() < headY || pDestination.getY() > headY) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headX; i <= tailX; i++) {
					Square s = aBoard[i][pDestination.getY()];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						//movement fails
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
							//movement fails
							finalDestinationTail = pShip.getTail();
							finalDestinationHead = pShip.getHead();
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
				//After movement it is possible that the ship has come in contact with a mine
				if (!(pShip instanceof MineLayer)) {
					for (int i = headX; i <= tailX; i++) {
						Square s1 = aBoard[i][pDestination.getY() - 1];
						Square s2 = aBoard[i][pDestination.getY() + 1];
						if (s1 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
						if (s2 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
					Square sf1 = aBoard[finalDestinationHead.getX() - 1][finalDestinationHead.getY()];
					Square sf2 = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()];
					if (sf1 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sf2 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
				
			}
			break; //from case WEST
			
			
		case EAST: 
			//***Case forward: 
			if (pDestination.getX() > headX) {
				//Check for every X between head and destination
				for (int x = headX + 1; x <= pDestination.getX(); x++) {
					Square s = aBoard[x][headY];
					Square sl = aBoard[x][headY + 1]; //sl and sr are the squares immediately beside square s. 
					Square sr = aBoard[x][headY - 1]; //They are checked for mines (which explode if the ship is next to them)
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(x-1, headY); //One square before the obstacle
						//TODO Create log entry/notify players
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x-1, headY);
						mineExplode();
						//TODO Log entry
						break;
					}
					else if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
					else if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
						mineExplode();
						//TODO log entry
						break;
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					Square sf = aBoard[finalDestinationHead.getX() + 1][finalDestinationHead.getY()];
					if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
				finalDestinationTail = new Coordinate (pDestination.getX() - pShip.getSize() + 1, finalDestinationHead.getY());
				
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
					//Failed to move
					finalDestinationTail = pShip.getTail();
					finalDestinationHead = pShip.getHead();
					mineExplode();
					//TODO Log entry
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX - 1, headY);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					Square sf = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()];
					Square sfl = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1]; 
					Square sfr = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1]; 
					if (sf instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfl instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sfr instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
				
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getY() < headY || pDestination.getY() > headY) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headX; i >= tailX; i--) {
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
					for (int i = headX; i >= tailX; i--) {
						Square s = aBoard[i][pDestination.getY()];
						if (s instanceof MineSquare) {
							finalDestinationTail = pShip.getTail();
							finalDestinationHead = pShip.getHead();
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
				//After movement it is possible that the ship has come in contact with a mine
				if (!(pShip instanceof MineLayer)) {
					for (int i = headX; i >= tailX; i--) {
						Square s1 = aBoard[i][pDestination.getY() - 1];
						Square s2 = aBoard[i][pDestination.getY() + 1];
						if (s1 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
						if (s2 instanceof MineSquare) {
							mineExplode();
							//Log entry
							//Note: we do not break: it is possible that the ships triggers more than one mine explosion
						}
					}
					Square sf1 = aBoard[finalDestinationHead.getX() + 1][finalDestinationHead.getY()];
					Square sf2 = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()];
					if (sf1 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
					if (sf2 instanceof MineSquare) {
						mineExplode();
						//TODO log entry
					}
				}
			}
			break; //from case EAST
		}
		
		//After computing the ship's new position, we change the board accordingly. 
		setShipPosition(pShip, finalDestinationHead, finalDestinationTail);
	}
	
	/**
	 * Changes the position of a ship by rotating it. 
	 * Assumes the pCoord input is valid (given the type of ship) and represents the new position of the head. 
	 * (Validity check should be performed by the client before sending the move to the game.)  
	 * @param pShip
	 * @param pCoord
	 */
	private void turnShip(Ship pShip, Coordinate pDestination) {
		//Compute the turn area
		//Check for obstacles in it
		//If any, resolve collision and cancel movement
		//Otherwise, compute new position of ship
		
		int headX = pShip.getHead().getX();
		int headY = pShip.getHead().getY();
		int tailX = pShip.getTail().getX();
		int tailY = pShip.getTail().getY();
		Direction direction = pShip.getDirection();
		
		boolean turnSuccess = true;
		
		//Here we look at 180 degrees turn
		if (pShip.canTurn180() && pDestination.equals(pShip.getTail())) {
			
		}
		else if (pShip.canTurn180()) {
			//Turning 90 degrees but over center square
		}
		else /*Some other ship turning over its tail*/{
			switch (direction) {
			case NORTH: 
				//Case turning left/port 
				if (pDestination.getX() < tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX-1; x > tailX-pShip.getSize()+1 && i>0; x--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y > tailY-i; y--) {
							Square s = aBoard[x][y];
							//Looking for non-exploding obstacles
							if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
								turnSuccess = false;
								//TODO log entry
								System.out.println("Obstacle found");
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								mineExplode();
								//TODO log entry
								broke = true;
								break; 
							}
						}
						if (broke) {
							break;
						}
					}
				}
				//Case turning right/starboard 
				if (pDestination.getX() > tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX+1; x < tailX+pShip.getSize()-1 && i>0; x++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y > tailY-i; y--) {
							Square s = aBoard[x][y];
							//Looking for non-exploding obstacles
							if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
								turnSuccess = false;
								//TODO log entry
								System.out.println("Obstacle found");
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								mineExplode();
								//TODO log entry
								broke = true;
								break; 
							}
						}
						if (broke) {
							break;
						}
					}
				}
				break; //from case NORTH
			case SOUTH: //same as north, but change the y's signs
				break;
			case WEST: //same as north, but swap x and y
				break; 
			case EAST: //same as west, but change the x's signs
				break;
			}
			
			if (turnSuccess) {
				//A ship turning 90 degrees will have its head on the destination coordinate and its tail on the same square as before
				setShipPosition(pShip, pDestination, pShip.getTail());
			}
		}
	}
	
	private void fireCannon(Ship pShip, Coordinate pCoord) {
		Square s = aBoard[pCoord.getX()][pCoord.getY()];
		
		if (s instanceof Sea) {
			//Miss
			//TODO Log entry
		}
		else if (s instanceof CoralReef) {
			//Hit coral reef (no effect)
			//TODO Log entry
		}
		else if (s instanceof MineSquare) {
			removeMine(pCoord);
			//TODO Log entry
		}
		else if (s instanceof BaseSquare) {
			//some method to SetBaseDamage
			//TODO Log entry
		}
		else if (s instanceof ShipSquare) {
			//some method to set ship damage; need to get which ship is affected though. Hopefully the reference to a ship in ShipSquare class is sufficient
			//TODO Log entry
		}
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
		//Note: mineExplode should remove the mine in addition to damaging a ship
	}

	/**
	 * Creates, as a string, an ASCII representation of the board. 
	 * @return
	 */
	public String printBoard() {
		String s = "Turn number: " + aTurnNum + "\n";
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
