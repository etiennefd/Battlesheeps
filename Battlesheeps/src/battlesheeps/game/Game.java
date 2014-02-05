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
		//Case NORTH or SOUTH
		if (pHead.getX() == pTail.getX() && Math.abs(pHead.getY() - pTail.getY()) == pShip.getSize() - 1) {

			//We remove the ship from the board and set its location parameters to new values
			removeShip(pShip);
			pShip.setLocation(pHead, pTail);
			
			//This index is used to keep track of the damage array
			int damageIndex = 0;
			
			//We put the head of the ship on the board
			aBoard[pHead.getX()][pHead.getY()] = new ShipSquare(pShip, pShip.getDamage(damageIndex), true);
			
			//We add everything that is between the head and the tail to the board
			//We must check whether to increase or decrease y (does the head have a higher Y position than the tail?)
			if (pHead.getY() > pTail.getY()) {
				for (int y = pHead.getY() - 1; y > pTail.getY(); y--) {
					damageIndex++;
					aBoard[pHead.getX()][y] = new ShipSquare(pShip, pShip.getDamage(damageIndex), false);
				}
			}
			else if (pHead.getY() < pTail.getY()) {
				for (int y = pHead.getY() + 1; y < pTail.getY(); y++) {
					damageIndex++;
					aBoard[pHead.getX()][y] = new ShipSquare(pShip, pShip.getDamage(damageIndex), false);
				}
			}
			
			//And we add the tail
			damageIndex++;
			aBoard[pTail.getX()][pTail.getY()] = new ShipSquare(pShip, pShip.getDamage(damageIndex), false);
			
		} 
		
		//Are the head and the tail on the same Y row? Do they correspond to the length of the ship? 
		else if (pHead.getY() == pTail.getY() && Math.abs(pHead.getX() - pTail.getX()) == pShip.getSize() - 1) {
			
			//We remove the ship from the board and set its location parameters to new values
			removeShip(pShip);
			pShip.setLocation(pHead, pTail);
			
			//This index is used to keep track of the damage array
			int damageIndex = 0;
			
			//We put the head of the ship on the board
			aBoard[pHead.getX()][pHead.getY()] = new ShipSquare(pShip, pShip.getDamage(damageIndex), true);
			
			//Then everything that is between the head and the tail
			//We must check whether to increase or decrease x (does the head have a higher X position than the tail?)
			if (pHead.getX() > pTail.getX()) {
				for (int x = pHead.getX() - 1; x > pTail.getX(); x--) {
					damageIndex++;
					aBoard[x][pHead.getY()] = new ShipSquare(pShip, pShip.getDamage(damageIndex), false);
				}
			}
			else if (pHead.getX() < pTail.getX()) {
				for (int x = pHead.getX() + 1; x < pTail.getX(); x++) {
					damageIndex++;
					aBoard[x][pHead.getY()] = new ShipSquare(pShip, pShip.getDamage(damageIndex), false);
				}
			}
			//Finally, the tail
			damageIndex++;
			aBoard[pTail.getX()][pTail.getY()] = new ShipSquare(pShip, pShip.getDamage(damageIndex), false);
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
			for (int x = headX; x >= tailX; x--) {
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
	 * Called from the GameManager to process a move on the board. This is a server-side method. 
	 * A private method will be called depending on which kind of move is sent. 
	 * 
	 * Important: this method does not check for the validity of inputs. It is assumed that the move has 
	 * already been checked by the client before sending it to the server. This method and the related private
	 * methods simply compute the outcome of the move given a ship and a location. 
	 * (For instance, fire cannon simply affects the target square, but does not take into account the 
	 * range of the ship that fired.)
	 * 
	 * In many cases, the move will do nothing if the inputs are wrong (e.g. trying to pickup a mine where there is none). 
	 * This can be changed (to throw exceptions or some other error message) if it would help debugging. 
	 * 
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
		case TRIGGER_RADAR: triggerRadar(pShip); break;
		case REPAIR_SHIP: pShip.repair(); break;
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
	 * 
	 * For simplicity of code (and because it kind of makes sense) mine explosions are triggered only when they 
	 * are within the turning area, and not if they are directly adjacent to it. 
	 * However, if a mine is found next to the ship after the completion of rotation, it does explode. 
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
		
		//Here we look at 180 degrees turn. We assume ships able to do that are 3 squares long. 
		if (pShip.canTurn180() && pDestination.equals(pShip.getTail())) {
			ArrayList<Square> listOfSquares = new ArrayList<Square>();
			switch (direction) {
			case NORTH: 
				listOfSquares.add(aBoard[headX-1][headY]);
				listOfSquares.add(aBoard[headX-1][headY+1]);
				listOfSquares.add(aBoard[headX-1][headY+2]);
				listOfSquares.add(aBoard[tailX+1][tailY]);
				listOfSquares.add(aBoard[tailX+1][tailY-1]);
				listOfSquares.add(aBoard[tailX+1][tailY-2]);
				break; 
			case SOUTH: 
				listOfSquares.add(aBoard[headX-1][headY]);
				listOfSquares.add(aBoard[headX-1][headY-1]);
				listOfSquares.add(aBoard[headX-1][headY-2]);
				listOfSquares.add(aBoard[tailX+1][tailY]);
				listOfSquares.add(aBoard[tailX+1][tailY+1]);
				listOfSquares.add(aBoard[tailX+1][tailY+2]);
				break;
			case EAST: 
				listOfSquares.add(aBoard[headX][headY-1]);
				listOfSquares.add(aBoard[headX-1][headY-1]);
				listOfSquares.add(aBoard[headX-2][headY-1]);
				listOfSquares.add(aBoard[tailX][tailY+1]);
				listOfSquares.add(aBoard[tailX+1][tailY+1]);
				listOfSquares.add(aBoard[tailX+2][tailY+1]);
				break; 
			case WEST: 
				listOfSquares.add(aBoard[headX][headY-1]);
				listOfSquares.add(aBoard[headX+1][headY-1]);
				listOfSquares.add(aBoard[headX+2][headY-1]);
				listOfSquares.add(aBoard[tailX][tailY+1]);
				listOfSquares.add(aBoard[tailX-1][tailY+1]);
				listOfSquares.add(aBoard[tailX-2][tailY+1]);
				break; 
			}
			//Now we iterate over the list of squares we built
			for (Square s : listOfSquares) {
				if (s instanceof ShipSquare) {
					turnSuccess = false;
					//TODO log entry
					System.out.println("Obstacle found");
				}
				else if (s instanceof MineSquare) {
					turnSuccess = false;
					mineExplode();
					//TODO log entry
				}
			}
			
			//If the rotation was successful: 
			if (turnSuccess) {
				//The head and tail of the ship have been swapped
				setShipPosition(pShip, pShip.getTail(), pShip.getHead());
			}
		}
		
		//Turning 90 degrees but over center square
		else if (pShip.canTurn180()) {
			ArrayList<Square> listOfSquares = new ArrayList<Square>();
			Coordinate destinationTail = null;
			switch (direction) {
			case NORTH: 
				//Case turning left/port (west)
				if (pDestination.getX() < tailX) {
					listOfSquares.add(aBoard[headX-1][headY]);
					listOfSquares.add(aBoard[tailX+1][tailY]);
					listOfSquares.add(aBoard[headX-1][headY+1]);
					listOfSquares.add(aBoard[tailX+1][tailY-1]);
					destinationTail = new Coordinate(pDestination.getX() + 2, pDestination.getY());
				}
				//Case turning right/starboard (east)
				else if (pDestination.getX() > tailX) {
					listOfSquares.add(aBoard[headX+1][headY]);
					listOfSquares.add(aBoard[tailX-1][tailY]);
					listOfSquares.add(aBoard[headX+1][headY+1]);
					listOfSquares.add(aBoard[tailX-1][tailY-1]);
					destinationTail = new Coordinate(pDestination.getX() - 2, pDestination.getY());
				}
				break; 
			case SOUTH: 
				//Case turning right/starboard (west)
				if (pDestination.getX() < tailX) {
					listOfSquares.add(aBoard[headX-1][headY]);
					listOfSquares.add(aBoard[tailX+1][tailY]);
					listOfSquares.add(aBoard[headX-1][headY-1]);
					listOfSquares.add(aBoard[tailX+1][tailY+1]);
					destinationTail = new Coordinate(pDestination.getX() + 2, pDestination.getY());
				}
				//Case turning left/port (east)
				else if (pDestination.getX() > tailX) {
					listOfSquares.add(aBoard[headX+1][headY]);
					listOfSquares.add(aBoard[tailX-1][tailY]);
					listOfSquares.add(aBoard[headX+1][headY-1]);
					listOfSquares.add(aBoard[tailX-1][tailY+1]);
					destinationTail = new Coordinate(pDestination.getX() - 2, pDestination.getY());
				}
				break;
			case EAST: 
				//Case turning left/port (north)
				if (pDestination.getY() < tailY) {
					listOfSquares.add(aBoard[headX][headY-1]);
					listOfSquares.add(aBoard[tailX][tailY+1]);
					listOfSquares.add(aBoard[headX-1][headY-1]);
					listOfSquares.add(aBoard[tailX+1][tailY+1]);
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() + 2);
				}
				//Case turning right/starboard (south)
				else if (pDestination.getY() > tailY) {
					listOfSquares.add(aBoard[headX][headY+1]);
					listOfSquares.add(aBoard[tailX][tailY-1]);
					listOfSquares.add(aBoard[headX-1][headY+1]);
					listOfSquares.add(aBoard[tailX+1][tailY-1]);
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() - 2);
				}
				break; 
			case WEST: 
				//Case turning right/starboard (north)
				if (pDestination.getY() < tailY) {
					listOfSquares.add(aBoard[headX][headY-1]);
					listOfSquares.add(aBoard[tailX][tailY+1]);
					listOfSquares.add(aBoard[headX+1][headY-1]);
					listOfSquares.add(aBoard[tailX-1][tailY+1]);
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() + 2);
				}
				//Case turning left/port (south)
				else if (pDestination.getY() > tailY) {
					listOfSquares.add(aBoard[headX][headY+1]);
					listOfSquares.add(aBoard[tailX][tailY-1]);
					listOfSquares.add(aBoard[headX+1][headY+1]);
					listOfSquares.add(aBoard[tailX-1][tailY-1]);
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() - 2);
				}
				break; 
			}
			//Now we iterate over the list of squares we built
			for (Square s : listOfSquares) {
				if (s instanceof ShipSquare) {
					turnSuccess = false;
					//TODO log entry
					System.out.println("Obstacle found");
				}
				else if (s instanceof MineSquare) {
					turnSuccess = false;
					mineExplode();
					//TODO log entry
				}
			}
			
			//If the rotation was successful: 
			if (turnSuccess) {
				if (destinationTail == null) {
					throw new InvalidCoordinateException();
				}
				else {
					//The head is on pDestination while the tail is on the destinationTail computed depending on direction
					setShipPosition(pShip, pDestination, destinationTail);
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
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX-1; x >= tailX-pShip.getSize()+1 && i>0; x--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y >= tailY-i; y--) {
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
						i--; //The next column will be shorter by 1
					}
				}
				//Case turning right/starboard (east)
				else if (pDestination.getX() > tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX+1; x <= tailX+pShip.getSize()-1 && i>0; x++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y >= tailY-i; y--) {
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
						i--;
					}
				}
				break; //from case NORTH
				
			case SOUTH: //same as north, but change the y's signs
				//Case turning right/starboard (west)
				if (pDestination.getX() < tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX-1; x >= tailX-pShip.getSize()+1 && i>0; x--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y <= tailY+i; y++) {
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
						i--;
					}
				}
				//Case turning left/port (east) 
				else if (pDestination.getX() > tailX) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int x = tailX+1; x <= tailX+pShip.getSize()-1 && i>0; x++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int y = tailY; y <= tailY+i; y++) {
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
						i--;
					}
				}
				break; //from case SOUTH
				
			case EAST: 
				//Case turning left/port (north)
				if (pDestination.getY() < tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area 
					//(i provides the staircase pattern)
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY-1; y >= tailY-pShip.getSize()+1 && i>0; y--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x <= tailX+i; x++) {
							Square s = aBoard[x][y];
							//Looking for non-exploding obstacles
							if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
								turnSuccess = false;
								System.out.println("Obstacle found");
								//TODO log entry
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
						i--; //The next column will be shorter by 1
					}
				}
				//Case turning right/starboard (south)
				else if (pDestination.getY() > tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY+1; y <= tailY+pShip.getSize()-1 && i>0; y++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x <= tailX+i; x++) {
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
						i--;
					}
				}
				break; //from case EAST
			
			case WEST: //same as east, but change the x's signs
				//Case turning right/starboard (north)
				if (pDestination.getY() < tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area 
					//(i provides the staircase pattern)
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY-1; y >= tailY-pShip.getSize()+1 && i>0; y--) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x >= tailX-i; x--) {
							Square s = aBoard[x][y];
							//Looking for non-exploding obstacles
							if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
								turnSuccess = false;
								System.out.println("Obstacle found");
								//TODO log entry
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
						i--; //The next column will be shorter by 1
					}
				}
				//Case turning left/port (south)
				else if (pDestination.getY() > tailY) {
					//i remembers how far we have to go along the length of a ship when looking at the turn area
					int i = pShip.getSize() - 1;
					boolean broke = false;
					//Outer loop: going away from the ship, up to the length of the ship - 1
					for (int y = tailY+1; y <= tailY+pShip.getSize()-1 && i>0; y++) {
						//Inner loop: looking along the ship's axis for every x, but with a limit i
						for (int x = tailX; x >= tailX-i; x--) {
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
						i--;
					}
				}
				break; //from case WEST
			}
			
			if (turnSuccess) {
				//A ship turning 90 degrees will have its head on the destination coordinate and its tail on the same square as before
				setShipPosition(pShip, pDestination, pShip.getTail());
			}
		}
		
		//After completing rotation, we check for mines next to the ship
		if (turnSuccess) {
			direction = pShip.getDirection();
			Square s; 
			int x, y;
			switch (direction) {
			case NORTH: 
				x = pShip.getHead().getX();
				y = pShip.getHead().getY() - 1;
				//Looking in front of head
				if (y >= 0) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking behind the tail
				y = pShip.getTail().getY() + 1;
				if (y < aBoard.length) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking on both sides of the ship
				y = pShip.getHead().getY();
				for (int i = y; i <= pShip.getTail().getY(); i++) {
					if (x-1 >= 0) {
						s = aBoard[x-1][i];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
					if (x+1 < aBoard.length) {
						s = aBoard[x+1][i];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
				}
				break;

			case SOUTH: 
				x = pShip.getHead().getX();
				y = pShip.getHead().getY() + 1;
				//Looking in front of head
				if (y < aBoard.length) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking behind the tail
				y = pShip.getTail().getY() - 1;
				if (y > 0) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking on both sides of the ship
				y = pShip.getHead().getY();
				for (int i = y; i >= pShip.getTail().getY(); i--) {
					if (x-1 >= 0) {
						s = aBoard[x-1][i];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
					if (x+1 < aBoard.length) {
						s = aBoard[x+1][i];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
				}
				break;

			case WEST: 
				y = pShip.getHead().getY();
				x = pShip.getHead().getX() - 1;
				//Looking in front of head
				if (x >= 0) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking behind the tail
				x = pShip.getTail().getX() + 1;
				if (x < aBoard.length) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking on both sides of the ship
				x = pShip.getHead().getX();
				for (int i = x; i <= pShip.getTail().getX(); i++) {
					if (y-1 >= 0) {
						s = aBoard[i][y-1];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
					if (y+1 < aBoard.length) {
						s = aBoard[i][y+1];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
				}
				break;

			case EAST: 
				y = pShip.getHead().getY();
				x = pShip.getHead().getX() + 1;
				//Looking in front of head
				if (x < aBoard.length) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking behind the tail
				x = pShip.getTail().getX() - 1;
				if (x > 0) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode();
						//TODO log
					}
				}
				//Looking on both sides of the ship
				x = pShip.getHead().getX();
				for (int i = x; i >= pShip.getTail().getX(); i--) {
					if (y-1 >= 0) {
						s = aBoard[i][y-1];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
					if (y+1 < aBoard.length) {
						s = aBoard[i][y+1];
						if (s instanceof MineSquare) {
							mineExplode();
							//TODO log
						}
					}
				}
				break;
			}
		}
	}
	
	private void fireCannon(Ship pShip, Coordinate pCoord) {
		Square s = aBoard[pCoord.getX()][pCoord.getY()];
		
		if (s instanceof Sea) {
			//Miss
			//TODO Log entry
		}
		else if (s instanceof CoralReef) { //Might not be needed if client doesn't accept reefs as a valid target
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
			if (((ShipSquare) s).getDamage() == Damage.DESTROYED) {
				//No effect
				//TODO log entry
			}
			else {
				boolean heavyCannons = false; 
				if (pShip instanceof Cruiser) {
					heavyCannons = true;
				}
				Ship target = ((ShipSquare) s).getShip();
				//Damage the ship
				target.setDamage(pCoord, heavyCannons);
				//Check if it sank
				if (target.isSunk()) {
					this.removeShip(target);
					//TODO Log entry
					//Also do stuff like checking if game ends
				}
				else {
					this.setShipPosition(target, target.getHead(), target.getTail());
					//TODO Log entry
				}
			}
		}
	}
	
	private void fireTorpedo(Ship pShip, Coordinate pCoord) {

	}
	
	/**
	 * Adds a mine to the specified square, but only if the ship is a mine layer and has mines in its supply. 
	 * Although this should be unnecessary since only valid input should be received. 
	 * @param pShip
	 * @param pCoord
	 */
	private void dropMine(Ship pShip, Coordinate pCoord) {
		if (pShip instanceof MineLayer) {
			MineLayer ml = (MineLayer) pShip;
			boolean success = ml.layMine();
			if (success) {
				aBoard[pCoord.getX()][pCoord.getY()] = new MineSquare();
			}
		}
	}
	
	/**
	 * Removes a mine and adds one to the mine layer's supply. 
	 * @param pShip
	 * @param pCoord
	 */
	private void pickupMine(Ship pShip, Coordinate pCoord) {
		if (pShip instanceof MineLayer) {
			MineLayer ml = (MineLayer) pShip;
			Square s = aBoard[pCoord.getX()][pCoord.getY()];
			if (s instanceof MineSquare) {
				aBoard[pCoord.getX()][pCoord.getY()] = new Sea();
				ml.retrieveMine();
			}
		}
	}
	
	/**
	 * Activates or deactivates the radar boat's long radar. 
	 * @param pShip
	 */
	private void triggerRadar(Ship pShip) {
		if (pShip instanceof RadarBoat) {
			RadarBoat rb = (RadarBoat) pShip; 
			rb.triggerRadar();
			//Recompute visibility
		}
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
