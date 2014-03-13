package battlesheeps.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import battlesheeps.accounts.Account;
import battlesheeps.board.BaseSquare;
import battlesheeps.board.Coordinate;
import battlesheeps.board.CoralReef;
import battlesheeps.board.MineSquare;
import battlesheeps.board.Sea;
import battlesheeps.board.ShipSquare;
import battlesheeps.board.Square;
import battlesheeps.exceptions.InvalidCoordinateException;
import battlesheeps.server.LogEntry.LogType;
import battlesheeps.ships.Cruiser;
import battlesheeps.ships.Destroyer;
import battlesheeps.ships.MineLayer;
import battlesheeps.ships.RadarBoat;
import battlesheeps.ships.Ship;
import battlesheeps.ships.Ship.Damage;
import battlesheeps.ships.TorpedoBoat;

public class ServerGame implements Serializable 
{
	private static final long serialVersionUID = 8385471662601246081L;

	public enum MoveType {
		TURN_SHIP, TRANSLATE_SHIP, FIRE_CANNON, FIRE_TORPEDO, 
		DROP_MINE, PICKUP_MINE, TRIGGER_RADAR, REPAIR_SHIP
	}
	
	public enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
	
	public enum ClientInfo {
		NEW_GAME, NEW_CORAL, FINAL_CORAL, SHIP_INIT, GAME_UPDATE
	}
	
	//Fields related to the identity of the game
	private int aGameID;
	private Account aPlayer1;
	private Account aPlayer2;
	private int aTurnNum;			//Odd -> it is P1's turn. Even -> it is P2's turn.  
	private Date aDateLastPlayed;	//TODO update this when a turn is done.
	private ClientInfo aClientInfo;
	
	//Fields related to the contents of the game
	private Square[][] aBoard;
	private ArrayList<Ship> aShipListP1;
	private ArrayList<Ship> aShipListP2;
	private LinkedList<LogEntry> aLogEntryList = new LinkedList<LogEntry>();
	
	//Fields related to the ending of the game
	private boolean aGameComplete;
	private Account aWinner;
	
	/**
	 * Constructor for Games. Accepts an ID and the two players. 
	 * Normally called from the GameManager based on the information held in the relevant GameRequest. 
	 * @param pGameID
	 * @param pPlayer1
	 * @param pPlayer2
	 */
	public ServerGame(int pGameID, Account pPlayer1, Account pPlayer2) {
		
		aGameID = pGameID;
		aPlayer1 = pPlayer1;
		aPlayer2 = pPlayer2;
		aTurnNum = 1;
		aDateLastPlayed = new Date();
		aClientInfo = ClientInfo.NEW_GAME;
		
		aPlayer1.addNewGame(aGameID);
		aPlayer2.addNewGame(aGameID);
		
		aGameComplete = false; 
		aWinner = null;
		
		aBoard = new Square[30][30];
		//Filling the board with sea and base squares
		for (int x = 0; x<aBoard.length; x++) {
			for (int y = 0 ; y<aBoard[x].length; y++) {
				if (y >= 10 && y < 20 && x==0) {
					//Creating player 1's base squares (at the far left of the board)
					aBoard[x][y] = new BaseSquare(Damage.UNDAMAGED, aPlayer1);
				}
				else if (y >= 10 && y < 20 && x==29) {
					//Creating player 2's base squares (at the far right of the board)
					aBoard[x][y] = new BaseSquare(Damage.UNDAMAGED, aPlayer2);
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
		aShipListP1.add(new Cruiser(aPlayer1, 0));
		aShipListP1.add(new Cruiser(aPlayer1, 1));
		aShipListP1.add(new Destroyer(aPlayer1, 2));
		aShipListP1.add(new Destroyer(aPlayer1, 3));
		aShipListP1.add(new Destroyer(aPlayer1, 4));
		aShipListP1.add(new TorpedoBoat(aPlayer1, 5));
		aShipListP1.add(new TorpedoBoat(aPlayer1, 6));
		aShipListP1.add(new MineLayer(aPlayer1, 7));
		aShipListP1.add(new MineLayer(aPlayer1, 8));
		aShipListP1.add(new RadarBoat(aPlayer1, 9));
		
		aShipListP2 = new ArrayList<Ship>();
		aShipListP2.add(new Cruiser(aPlayer2, 0));
		aShipListP2.add(new Cruiser(aPlayer2, 1));
		aShipListP2.add(new Destroyer(aPlayer2, 2));
		aShipListP2.add(new Destroyer(aPlayer2, 3));
		aShipListP2.add(new Destroyer(aPlayer2, 4));
		aShipListP2.add(new TorpedoBoat(aPlayer2, 5));
		aShipListP2.add(new TorpedoBoat(aPlayer2, 6));
		aShipListP2.add(new MineLayer(aPlayer2, 7));
		aShipListP2.add(new MineLayer(aPlayer2, 8));
		aShipListP2.add(new RadarBoat(aPlayer2, 9));
		
	}
	
	/**
	 * Adds 24 coral reef squares to the board within the central 10*24 area.
	 * Also first removes anything that is in the central area and puts Sea squares there.  
	 * Can be called repeatedly in case of disagreement between players. 
	 */
	public void generateCoralReefs() {
		//Wipe out whatever's in the central area
		for (int x = 10; x<=19; x++) {
			for (int y = 3 ; y<=26; y++) {
				aBoard[x][y] = new Sea();
			}
		}
		//Add 24 coral reef squares in there
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
			aBoard[pHead.getX()][pHead.getY()] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), true);
			
			//We add everything that is between the head and the tail to the board
			//We must check whether to increase or decrease y (does the head have a higher Y position than the tail?)
			if (pHead.getY() > pTail.getY()) {
				for (int y = pHead.getY() - 1; y > pTail.getY(); y--) {
					damageIndex++;
					aBoard[pHead.getX()][y] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), false);
				}
			}
			else if (pHead.getY() < pTail.getY()) {
				for (int y = pHead.getY() + 1; y < pTail.getY(); y++) {
					damageIndex++;
					aBoard[pHead.getX()][y] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), false);
				}
			}
			
			//And we add the tail
			damageIndex++;
			aBoard[pTail.getX()][pTail.getY()] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), false);
			
		} 
		
		//Are the head and the tail on the same Y row? Do they correspond to the length of the ship? 
		else if (pHead.getY() == pTail.getY() && Math.abs(pHead.getX() - pTail.getX()) == pShip.getSize() - 1) {
			
			//We remove the ship from the board and set its location parameters to new values
			removeShip(pShip);
			pShip.setLocation(pHead, pTail);
			
			//This index is used to keep track of the damage array
			int damageIndex = 0;
			
			//We put the head of the ship on the board
			aBoard[pHead.getX()][pHead.getY()] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), true);
			
			//Then everything that is between the head and the tail
			//We must check whether to increase or decrease x (does the head have a higher X position than the tail?)
			if (pHead.getX() > pTail.getX()) {
				for (int x = pHead.getX() - 1; x > pTail.getX(); x--) {
					damageIndex++;
					aBoard[x][pHead.getY()] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), false);
				}
			}
			else if (pHead.getX() < pTail.getX()) {
				for (int x = pHead.getX() + 1; x < pTail.getX(); x++) {
					damageIndex++;
					aBoard[x][pHead.getY()] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), false);
				}
			}
			//Finally, the tail
			damageIndex++;
			aBoard[pTail.getX()][pTail.getY()] = new ShipSquare(pShip, pShip.getDamageAtIndex(damageIndex), false);
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
	 * The method returns true if the move triggered the end of the game. 
	 * 
	 * @param pShip
	 * @param pMove
	 */
	public synchronized boolean computeMoveResult(Ship pShip, MoveType pMove, Coordinate pCoord) { //I removed GameID and player because the game calls this instead of the gameManager
		
		switch (pMove) {
		case TRANSLATE_SHIP: translateShip(pShip, pCoord); break;
		case TURN_SHIP: turnShip(pShip, pCoord); break;
		case FIRE_CANNON: fireCannon(pShip, pCoord); break;
		case FIRE_TORPEDO: fireTorpedo(pShip); break;
		case DROP_MINE: dropMine(pShip, pCoord); break;
		case PICKUP_MINE: pickupMine(pShip, pCoord); break;
		case TRIGGER_RADAR: triggerRadar(pShip); break;
		case REPAIR_SHIP: pShip.repair(); break;
		}
		
		aTurnNum++;
		
		return aGameComplete; //Eventually we might want to return the winner instead (Null if game still going on). 
							  //Then we would probably not need the field aGameComplete
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
	 * @param pShip
	 * @param pDestination
	 */
	private void translateShip(Ship pShip, Coordinate pDestination) {

		Coordinate headCoord = pShip.getHead();
		Coordinate tailCoord = pShip.getTail();
		int headX = headCoord.getX();
		int headY = headCoord.getY();
		int tailX = tailCoord.getX();
		int tailY = tailCoord.getY();
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
						aLogEntryList.add(new LogEntry(LogType.COLLISION, headX, y, aTurnNum));
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y+1); 
						mineExplode(new Coordinate(headX, y), headCoord, pShip);
						break;
					}
					//sl and sr are the squares immediately beside square s.
					//They are checked for mines (which explode if the ship is next to them)
					else {
						if (headX - 1 >=0) {
							Square sl = aBoard[headX - 1][y]; 
							if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
								finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
								mineExplode(new Coordinate(headX-1, y), headCoord, pShip);
								break;
							}
						}
						if (headX + 1 < aBoard.length){
							Square sr = aBoard[headX + 1][y]; 
							if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
								finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
								mineExplode(new Coordinate(headX+1, y), headCoord, pShip);
								break;
							}
						}
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					if (finalDestinationHead.getY()-1 >= 0) {
						Square sf = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() - 1];
						if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX(), finalDestinationHead.getY() - 1), headCoord, pShip);
						}
					}
					
				}
				finalDestinationTail = new Coordinate (pDestination.getX(), finalDestinationHead.getY() + pShip.getSize() - 1);
				
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getY() > tailY) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					aLogEntryList.add(new LogEntry(LogType.COLLISION, pDestination.getX(), pDestination.getY(), aTurnNum));
				}
				else if (s instanceof MineSquare) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					mineExplode(pDestination, tailCoord, pShip);
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX, headY + 1);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					if (finalDestinationTail.getY() + 1 < aBoard.length) {
						Square sf = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1];
						if (sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY() + 1), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getX() - 1 >= 0) {
						Square sfl = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()]; 
						if (sfl instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX() - 1, finalDestinationTail.getY()), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getX() + 1 < aBoard.length) {
						Square sfr = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()]; 
						if (sfr instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX() + 1, finalDestinationTail.getY()), tailCoord, pShip);
						}
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
						finalDestinationTail = tailCoord;
						finalDestinationHead = headCoord;
						aLogEntryList.add(new LogEntry(LogType.COLLISION, pDestination.getX(), i, aTurnNum));
						break;
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
						if (pDestination.getX() - 1 >= 0) {
							Square s1 = aBoard[pDestination.getX() - 1][i];
							if (s1 instanceof MineSquare) {
								mineExplode(new Coordinate(pDestination.getX() - 1, i), new Coordinate(headX, i), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
						if (pDestination.getX() + 1 < aBoard.length) {
							Square s2 = aBoard[pDestination.getX() + 1][i];
							if (s2 instanceof MineSquare) {
								mineExplode(new Coordinate(pDestination.getX() + 1, i), new Coordinate(headX, i), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
					}
					if (finalDestinationHead.getY() - 1 >= 0) {
						Square sf1 = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() - 1];
						if (sf1 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX(), finalDestinationHead.getY() - 1), headCoord, pShip);
						}
					}
					if (finalDestinationTail.getY() + 1 < aBoard.length) {
						Square sf2 = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1];
						if (sf2 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY() + 1), tailCoord, pShip);
						}
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
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(headX, y-1); //One square before the obstacle
						aLogEntryList.add(new LogEntry(LogType.COLLISION, headX, y, aTurnNum));
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(headX, y-1);
						mineExplode(new Coordinate(headX, y), headCoord, pShip);
						break;
					}
					else {
						//sl and sr are the squares immediately beside square s
						//They are checked for mines (which explode if the ship is next to them)
						if (headX + 1 < aBoard.length) {
							Square sl = aBoard[headX + 1][y];  
							if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
								finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
								mineExplode(new Coordinate(headX+1, y), headCoord, pShip);
								break;
							}
						}
						if (headX - 1 >= 0) {
							Square sr = aBoard[headX - 1][y]; 
							if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
								finalDestinationHead = new Coordinate(headX, y); //The square next to the mine
								mineExplode(new Coordinate(headX-1, y), headCoord, pShip);
								break;
							}
						}
					}
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					if (finalDestinationHead.getY() + 1 < aBoard.length) {
						Square sf = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() + 1];
						if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX(), finalDestinationHead.getY() + 1), headCoord, pShip);
						}
					}
				}
				finalDestinationTail = new Coordinate (pDestination.getX(), finalDestinationHead.getY() - pShip.getSize() + 1);
				
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getY() < tailY) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					aLogEntryList.add(new LogEntry(LogType.COLLISION, pDestination.getX(), pDestination.getY(), aTurnNum));
				}
				else if (s instanceof MineSquare) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					mineExplode(pDestination, tailCoord, pShip);
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX, headY - 1);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					if (finalDestinationTail.getY() - 1 >= 0) {
						Square sf = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1];
						if (sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY() - 1), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getX() + 1 < aBoard.length) {
						Square sfl = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()]; 
						if (sfl instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX()+1, finalDestinationTail.getY()), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getX() - 1 >= 0) {
						Square sfr = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()]; 
						if (sfr instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX()-1, finalDestinationTail.getY()), tailCoord, pShip);
						}
					}
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getX() < headX || pDestination.getX() > headX) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headY; i >= tailY; i--) {
					Square s = aBoard[pDestination.getX()][i];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationTail = tailCoord;
						finalDestinationHead = headCoord;
						aLogEntryList.add(new LogEntry(LogType.COLLISION, pDestination.getX(), i, aTurnNum));
						break;
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
						if (pDestination.getX() - 1 >= 0) {
							Square s1 = aBoard[pDestination.getX() - 1][i];
							if (s1 instanceof MineSquare) {
								mineExplode(new Coordinate(pDestination.getX() - 1, i), new Coordinate(headX, i), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
						if (pDestination.getX() + 1 < aBoard.length) {
							Square s2 = aBoard[pDestination.getX() + 1][i];
							if (s2 instanceof MineSquare) {
								mineExplode(new Coordinate(pDestination.getX() + 1, i), new Coordinate(headX, i), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
					}
					if (finalDestinationHead.getY() + 1 < aBoard.length) {
						Square sf1 = aBoard[finalDestinationHead.getX()][finalDestinationHead.getY() + 1];
						if (sf1 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX(), finalDestinationHead.getY() + 1), headCoord, pShip);
						}
					}
					if (finalDestinationHead.getY() - 1 >= 0) {
						Square sf2 = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1];
						if (sf2 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY() - 1), tailCoord, pShip);
						}
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
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(x+1, headY); //One square before the obstacle
						aLogEntryList.add(new LogEntry(LogType.COLLISION, x, headY, aTurnNum));
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x+1, headY); 
						mineExplode(new Coordinate(x, headY), headCoord, pShip);
						break;
					}
					//sl and sr are the squares immediately beside square s.
					//They are checked for mines (which explode if the ship is next to them)
					else {
						if (headY - 1 >= 0) {
							Square sl = aBoard[x][headY - 1]; 
							if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
								finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
								mineExplode(new Coordinate(x, headY-1), headCoord, pShip);
								break;
							}
						}
						if (headY + 1 < aBoard.length) {
							Square sr = aBoard[x][headY + 1]; 
							if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
								finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
								mineExplode(new Coordinate(x, headY+1), headCoord, pShip);
								break;
							}
						}
					}
					
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine. 
					if (finalDestinationHead.getX() - 1 >= 0) {
						Square sf = aBoard[finalDestinationHead.getX() - 1][finalDestinationHead.getY()];
						if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX() - 1, finalDestinationHead.getY()), headCoord, pShip);
						}
					}
				}
				finalDestinationTail = new Coordinate (pDestination.getX() + pShip.getSize() - 1, finalDestinationHead.getY());
				
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getX() > tailX) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					aLogEntryList.add(new LogEntry(LogType.COLLISION, pDestination.getX(), pDestination.getY(), aTurnNum));
				}
				else if (s instanceof MineSquare) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					mineExplode(pDestination, tailCoord, pShip);
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX + 1, headY);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					if (finalDestinationTail.getX() + 1 < aBoard.length) {
						Square sf = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()];
						if (sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX() + 1, finalDestinationTail.getY()), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getY() - 1 >= 0) {
						Square sfl = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1]; 
						if (sfl instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY() - 1), tailCoord, pShip);
						}
					}
					
					if (finalDestinationTail.getY() + 1 < aBoard.length) {
						Square sfr = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1]; 
						if (sfr instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY() + 1), tailCoord, pShip);
						}
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
						finalDestinationTail = tailCoord;
						finalDestinationHead = headCoord;
						aLogEntryList.add(new LogEntry(LogType.COLLISION, i, pDestination.getY(), aTurnNum));
						break;
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
						if (pDestination.getY() - 1 >= 0) {
							Square s1 = aBoard[i][pDestination.getY() - 1];
							if (s1 instanceof MineSquare) {
								mineExplode(new Coordinate(i, pDestination.getY() - 1), new Coordinate(i, headY), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
						if (pDestination.getY() + 1 < aBoard.length) {
							Square s2 = aBoard[i][pDestination.getY() + 1];
							if (s2 instanceof MineSquare) {
								mineExplode(new Coordinate(i, pDestination.getY() + 1), new Coordinate(i, headY), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
					}
					if (finalDestinationHead.getX() - 1 >= 0) {
						Square sf1 = aBoard[finalDestinationHead.getX() - 1][finalDestinationHead.getY()];
						if (sf1 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX() - 1, finalDestinationHead.getY()), headCoord, pShip);
						}
					}
					if (finalDestinationHead.getX() + 1 < aBoard.length) {
						Square sf2 = aBoard[finalDestinationTail.getX() + 1][finalDestinationTail.getY()];
						if (sf2 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX() + 1, finalDestinationTail.getY()), tailCoord, pShip);
						}
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
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationHead = new Coordinate(x-1, headY); //One square before the obstacle
						aLogEntryList.add(new LogEntry(LogType.COLLISION, x, headY, aTurnNum));
						break;
					}
					else if (s instanceof MineSquare) {
						finalDestinationHead = new Coordinate(x-1, headY);
						mineExplode(new Coordinate(x, headY), headCoord, pShip);
						break;
					}
					//sl and sr are the squares immediately beside square s. 
					//They are checked for mines (which explode if the ship is next to them)
					else {
						if (headY + 1 < aBoard.length) {
							Square sl = aBoard[x][headY + 1]; 
							if (!(pShip instanceof MineLayer) && sl instanceof MineSquare) {
								finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
								mineExplode(new Coordinate(x, headY+1), headCoord, pShip);
								break;
							}
						}
						if (headY - 1 >= 0) {
							Square sr = aBoard[x][headY - 1]; 
							if (!(pShip instanceof MineLayer) && sr instanceof MineSquare) {
								finalDestinationHead = new Coordinate(x, headY); //The square next to the mine
								mineExplode(new Coordinate(x, headY-1), headCoord, pShip);
								break;
							}
						}
					}	
				}
				if (finalDestinationHead == null) {
					//Did not find an obstacle
					finalDestinationHead = pDestination;
					//We also have to check whether the square next to the head of the ship is a mine.
					if (finalDestinationHead.getX() + 1 < aBoard.length) {
						Square sf = aBoard[finalDestinationHead.getX() + 1][finalDestinationHead.getY()];
						if (!(pShip instanceof MineLayer) && sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX() + 1, finalDestinationHead.getY()), headCoord, pShip);
						}
					}
				}
				finalDestinationTail = new Coordinate (pDestination.getX() - pShip.getSize() + 1, finalDestinationHead.getY());
			}
			//***Case backward: remember, pDestination is where the tail tries to reach
			else if (pDestination.getX() < tailX) {
				Square s = aBoard[pDestination.getX()][pDestination.getY()];
				if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					aLogEntryList.add(new LogEntry(LogType.COLLISION, pDestination.getX(), pDestination.getY(), aTurnNum));
				}
				else if (s instanceof MineSquare) {
					//Failed to move
					finalDestinationTail = tailCoord;
					finalDestinationHead = headCoord;
					mineExplode(pDestination, tailCoord, pShip);
				} 
				else {
					//Successful movement
					finalDestinationTail = pDestination; 
					finalDestinationHead = new Coordinate (headX - 1, headY);
				}
				//We also have to check whether any square next to the tail of the ship is a mine. 
				if (!(pShip instanceof MineLayer)) {
					if (finalDestinationTail.getX() - 1 >= 0) {
						Square sf = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()];
						if (sf instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX() - 1, finalDestinationTail.getY()), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getY() + 1 < aBoard.length) {
						Square sfl = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() + 1]; 
						if (sfl instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY()+1), tailCoord, pShip);
						}
					}
					if (finalDestinationTail.getY() - 1 >= 0) {
						Square sfr = aBoard[finalDestinationTail.getX()][finalDestinationTail.getY() - 1]; 
						if (sfr instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX(), finalDestinationTail.getY()-1), tailCoord, pShip);
						}
					}
				}
			}
			//***Case port or starboard (both can use the same code)
			else if (pDestination.getY() < headY || pDestination.getY() > headY) {
				//We look at all squares left/right of the ship for obstacles. If there is one, movement fails. 
				for (int i = headX; i >= tailX; i--) {
					Square s = aBoard[i][pDestination.getY()];
					if (s instanceof ShipSquare || (s instanceof MineSquare && pShip instanceof MineLayer)) {
						finalDestinationTail = tailCoord;
						finalDestinationHead = headCoord;
						aLogEntryList.add(new LogEntry(LogType.COLLISION, i, pDestination.getY(), aTurnNum));						break;
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
						if (pDestination.getY() - 1 >= 0) {
							Square s1 = aBoard[i][pDestination.getY() - 1];
							if (s1 instanceof MineSquare) {
								mineExplode(new Coordinate(i, pDestination.getY()-1), new Coordinate(i, headY), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
						if (pDestination.getY() + 1 < aBoard.length) {
							Square s2 = aBoard[i][pDestination.getY() + 1];
							if (s2 instanceof MineSquare) {
								mineExplode(new Coordinate(i, pDestination.getY()+1), new Coordinate(i, headY), pShip);
								//Note: we do not break: it is possible that the ships triggers more than one mine explosion
							}
						}
					}
					if (finalDestinationHead.getX() + 1 < aBoard.length) {
						Square sf1 = aBoard[finalDestinationHead.getX() + 1][finalDestinationHead.getY()];
						if (sf1 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationHead.getX() + 1, finalDestinationHead.getY()), headCoord, pShip);
						}
					}
					if (finalDestinationHead.getX() - 1 >= 0) {
						Square sf2 = aBoard[finalDestinationTail.getX() - 1][finalDestinationTail.getY()];
						if (sf2 instanceof MineSquare) {
							mineExplode(new Coordinate(finalDestinationTail.getX() - 1, finalDestinationTail.getY()), tailCoord, pShip);
						}
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
				Square s = aBoard[c.getX()][c.getY()];
				if (s instanceof ShipSquare) {
					turnSuccess = false;
					aLogEntryList.add(new LogEntry(LogType.COLLISION, c.getX(), c.getY(), aTurnNum));
				}
				//There can't be a mine in the turning area of the ship, otherwise it would have exploded already. 
				//So we don't need to check for that. 
			}
			
			//If the rotation was successful: 
			if (turnSuccess) {
				//The head and tail of the ship have been swapped
				setShipPosition(pShip, pShip.getTail(), pShip.getHead());
			}
		}
		
		//Turning 90 degrees but over center square
		else if (pShip.canTurn180()) {
			ArrayList<Coordinate> listOfCoords = new ArrayList<Coordinate>();
			Coordinate destinationTail = null;
			switch (direction) {
			case NORTH: 
				//Case turning left/port (west)
				if (pDestination.getX() < tailX) {
					listOfCoords.add(new Coordinate (headX-1, headY));
					listOfCoords.add(new Coordinate (tailX+1, tailY));
					listOfCoords.add(new Coordinate (headX-1, headY+1));
					listOfCoords.add(new Coordinate (tailX+1, tailY-1));
					destinationTail = new Coordinate(pDestination.getX() + 2, pDestination.getY());
				}
				//Case turning right/starboard (east)
				else if (pDestination.getX() > tailX) {
					listOfCoords.add(new Coordinate (headX+1, headY));
					listOfCoords.add(new Coordinate (tailX-1, tailY));
					listOfCoords.add(new Coordinate (headX+1, headY+1));
					listOfCoords.add(new Coordinate (tailX-1, tailY-1));
					destinationTail = new Coordinate(pDestination.getX() - 2, pDestination.getY());
				}
				break; 
			case SOUTH: 
				//Case turning right/starboard (west)
				if (pDestination.getX() < tailX) {
					listOfCoords.add(new Coordinate (headX-1, headY));
					listOfCoords.add(new Coordinate (tailX+1, tailY));
					listOfCoords.add(new Coordinate (headX-1, headY-1));
					listOfCoords.add(new Coordinate (tailX+1, tailY+1));
					destinationTail = new Coordinate(pDestination.getX() + 2, pDestination.getY());
				}
				//Case turning left/port (east)
				else if (pDestination.getX() > tailX) {
					listOfCoords.add(new Coordinate (headX+1, headY));
					listOfCoords.add(new Coordinate (tailX-1, tailY));
					listOfCoords.add(new Coordinate (headX+1, headY-1));
					listOfCoords.add(new Coordinate (tailX-1, tailY+1));
					destinationTail = new Coordinate(pDestination.getX() - 2, pDestination.getY());
				}
				break;
			case EAST: 
				//Case turning left/port (north)
				if (pDestination.getY() < tailY) {
					listOfCoords.add(new Coordinate (headX, headY-1));
					listOfCoords.add(new Coordinate (tailX, tailY+1));
					listOfCoords.add(new Coordinate (headX-1, headY-1));
					listOfCoords.add(new Coordinate (tailX+1, tailY+1));
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() + 2);
				}
				//Case turning right/starboard (south)
				else if (pDestination.getY() > tailY) {
					listOfCoords.add(new Coordinate (headX, headY+1));
					listOfCoords.add(new Coordinate (tailX, tailY-1));
					listOfCoords.add(new Coordinate (headX-1, headY+1));
					listOfCoords.add(new Coordinate (tailX+1, tailY-1));
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() - 2);
				}
				break; 
			case WEST: 
				//Case turning right/starboard (north)
				if (pDestination.getY() < tailY) {
					listOfCoords.add(new Coordinate (headX, headY-1));
					listOfCoords.add(new Coordinate (tailX, tailY+1));
					listOfCoords.add(new Coordinate (headX+1, headY-1));
					listOfCoords.add(new Coordinate (tailX-1, tailY+1));
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() + 2);
				}
				//Case turning left/port (south)
				else if (pDestination.getY() > tailY) {
					listOfCoords.add(new Coordinate (headX, headY+1));
					listOfCoords.add(new Coordinate (tailX, tailY-1));
					listOfCoords.add(new Coordinate (headX+1, headY+1));
					listOfCoords.add(new Coordinate (tailX-1, tailY-1));
					destinationTail = new Coordinate(pDestination.getX(), pDestination.getY() - 2);
				}
				break; 
			}
			//Now we iterate over the list of coordinates we built
			for (Coordinate c : listOfCoords) {
				Square s = aBoard[c.getX()][c.getY()];
				if (s instanceof ShipSquare) {
					turnSuccess = false;
					aLogEntryList.add(new LogEntry(LogType.COLLISION, c.getX(), c.getY(), aTurnNum));
				}
				//Again, there can't be a mine in that area, so we don't check for that. 
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (y == tailY) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX, headY + pShip.getSize()-1-(headX-x));
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(headX,  y - (headX-x) + 1);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (y == tailY) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX, headY + pShip.getSize()-1-(x-headX));
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(headX,  y - (x - headX) + 1);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (y == tailY) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX, headY - pShip.getSize()+1+(headX-x));
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(headX,  y + (headX-x) - 1);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (y == tailY) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX, headY - pShip.getSize()+1+(x-headX));
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(headX, y + (x-headX) - 1);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (x == tailX) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX - pShip.getSize()+1+(headY-y), headY);
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(x + (headY-y) - 1, headY);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (x == tailX) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX - pShip.getSize()+1+(y-headY), headY);
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(x + (y-headY) - 1, headY);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (x == tailX) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX + pShip.getSize()-1-(headY-y), headY);
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(x - (headY-y) + 1, headY);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
								aLogEntryList.add(new LogEntry(LogType.COLLISION, x, y, aTurnNum));
								broke = true;
								break; 
							}
							//Looking for mines
							if (s instanceof MineSquare) {
								turnSuccess = false;
								//We need to compute which ship square gets hit. 
								Coordinate squareHit; 
								if (x == tailX) {
									//If the mine is on the same row as the tail, we use this formula to get the coordinates: 
									squareHit = new Coordinate(headX + pShip.getSize()-1-(y-headY), headY);
								}
								else {
									//If the mine is elsewhere in the turn area, we use this formula instead
									squareHit = new Coordinate(x - (y-headY) + 1, headY);
								}
								mineExplode(new Coordinate(x, y), squareHit, pShip);
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
						mineExplode(new Coordinate(x, y), pShip.getHead(), pShip);
					}
				}
				//Looking behind the tail
				y = pShip.getTail().getY() + 1;
				if (y < aBoard.length) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode(new Coordinate(x, y), pShip.getTail(), pShip);
					}
				}
				//Looking on both sides of the ship
				y = pShip.getHead().getY();
				for (int i = y; i <= pShip.getTail().getY(); i++) {
					if (x-1 >= 0) {
						s = aBoard[x-1][i];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(x-1, i), new Coordinate(x, i), pShip);
						}
					}
					if (x+1 < aBoard.length) {
						s = aBoard[x+1][i];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(x+1, i), new Coordinate(x, i), pShip);
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
						mineExplode(new Coordinate(x, y), pShip.getHead(), pShip);
					}
				}
				//Looking behind the tail
				y = pShip.getTail().getY() - 1;
				if (y > 0) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode(new Coordinate(x, y), pShip.getTail(), pShip);
					}
				}
				//Looking on both sides of the ship
				y = pShip.getHead().getY();
				for (int i = y; i >= pShip.getTail().getY(); i--) {
					if (x-1 >= 0) {
						s = aBoard[x-1][i];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(x-1, i), new Coordinate(x, i), pShip);
						}
					}
					if (x+1 < aBoard.length) {
						s = aBoard[x+1][i];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(x+1, i), new Coordinate(x, i), pShip);
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
						mineExplode(new Coordinate(x, y), pShip.getHead(), pShip);
					}
				}
				//Looking behind the tail
				x = pShip.getTail().getX() + 1;
				if (x < aBoard.length) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode(new Coordinate(x, y), pShip.getTail(), pShip);
					}
				}
				//Looking on both sides of the ship
				x = pShip.getHead().getX();
				for (int i = x; i <= pShip.getTail().getX(); i++) {
					if (y-1 >= 0) {
						s = aBoard[i][y-1];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(i, y-1), new Coordinate(i, y), pShip);
						}
					}
					if (y+1 < aBoard.length) {
						s = aBoard[i][y+1];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(i, y+1), new Coordinate(i, y), pShip);
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
						mineExplode(new Coordinate(x, y), pShip.getHead(), pShip);
					}
				}
				//Looking behind the tail
				x = pShip.getTail().getX() - 1;
				if (x > 0) {
					s = aBoard[x][y];
					if (s instanceof MineSquare) {
						mineExplode(new Coordinate(x, y), pShip.getTail(), pShip);
					}
				}
				//Looking on both sides of the ship
				x = pShip.getHead().getX();
				for (int i = x; i >= pShip.getTail().getX(); i--) {
					if (y-1 >= 0) {
						s = aBoard[i][y-1];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(i, y-1), new Coordinate(i, y), pShip);
						}
					}
					if (y+1 < aBoard.length) {
						s = aBoard[i][y+1];
						if (s instanceof MineSquare) {
							mineExplode(new Coordinate(i, y+1), new Coordinate(i, y), pShip);
						}
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Attacks whatever is on 
	 * @param pShip
	 * @param pCoord
	 */
	private void fireCannon(Ship pShip, Coordinate pCoord) {
		int x = pCoord.getX();
		int y = pCoord.getY();
		Square s = aBoard[x][y];
		
		if (s instanceof Sea) {
			//Miss
			aLogEntryList.add(new LogEntry(LogType.CANNON_MISS, x, y, aTurnNum));
		}
		else if (s instanceof CoralReef) { //Might not be needed if client doesn't accept reefs as a valid target
			//Hit coral reef (no effect)
			aLogEntryList.add(new LogEntry(LogType.CANNON_HIT_REEF, x, y, aTurnNum));
		}
		else if (s instanceof MineSquare) {
			removeMine(pCoord);
			aLogEntryList.add(new LogEntry(LogType.CANNON_HIT_MINE, x, y, aTurnNum));
		}
		else if (s instanceof BaseSquare) {
			aLogEntryList.add(new LogEntry(LogType.CANNON_HIT_BASE, x, y, aTurnNum));
			damageBase(pCoord);
		}
		else if (s instanceof ShipSquare) {
			aLogEntryList.add(new LogEntry(LogType.CANNON_HIT_SHIP, x, y, aTurnNum));
			if (((ShipSquare) s).getDamage() == Damage.DESTROYED) {
				//No effect
			}
			else {
				boolean heavyCannons = false; 
				if (pShip instanceof Cruiser) {
					heavyCannons = true;
				}
				Ship target = ((ShipSquare) s).getShip();
				//Here we call the method to damage the target
				damageShip(target, pCoord, heavyCannons);
			}
		}
	}
	
	private void fireTorpedo(Ship pShip) {
		Direction torpedoDirection = pShip.getDirection(); 
		int headX = pShip.getHead().getX();
		int headY = pShip.getHead().getY();
		int x, y;
		
		switch (torpedoDirection) {
		case NORTH: 
			x = headX;
			for (y = headY - 1; y >= 0 && y >= headY - 10; y--) {
				boolean hitSomething = torpedoCheck(x, y, torpedoDirection);
				if (hitSomething) break;
			}
			break;
		case SOUTH: 
			x = headX;
			for (y = headY + 1; y < aBoard.length && y <= headY + 10; y++) {
				boolean hitSomething = torpedoCheck(x, y, torpedoDirection);
				if (hitSomething) break;
			}
			break;
		case WEST: 
			y = headY;
			for (x = headX - 1; x >= 0 && x >= headX - 10; x--) {
				boolean hitSomething = torpedoCheck(x, y, torpedoDirection);
				if (hitSomething) break;
			}
			break;
		case EAST: 
			y = headY;
			for (x = headX + 1; x < aBoard.length && x <= headX + 10; x++) {
				boolean hitSomething = torpedoCheck(x, y, torpedoDirection);
				if (hitSomething) break;
			}
			break;
		}
	}
	
	/**
	 * Returns true if the torpedo hit a non-sea square at position (pX, pY). 
	 * Also performs necessary operations upon hit (e.g. damaging a ship). 
	 * This method is called repeatedly as the torpedo moves forward. 
	 * @param pX
	 * @param pY
	 * @return
	 */
	private boolean torpedoCheck(int pX, int pY, Direction pTorpedoDirection) {
		Square s = aBoard[pX][pY];
		if (s instanceof Sea) {
			return false; //Didn't hit anything
		}
		else {
			if (s instanceof CoralReef) {
				//nothing happens, except notify the player
				aLogEntryList.add(new LogEntry(LogType.TORPEDO_HIT_REEF, pX, pY, aTurnNum));
			}
			else if (s instanceof MineSquare) {
				removeMine(new Coordinate (pX, pY));
				aLogEntryList.add(new LogEntry(LogType.TORPEDO_HIT_MINE, pX, pY, aTurnNum));
			}
			else if (s instanceof ShipSquare) {
				aLogEntryList.add(new LogEntry(LogType.TORPEDO_HIT_SHIP, pX, pY, aTurnNum));
				
				boolean heavyCannons = false; //Torpedoes are never heavy, but presumably they could be with some rule changes
				Ship targetShip = ((ShipSquare) s).getShip();
				
				//Damage the targeted square
				damageShip(targetShip, new Coordinate(pX, pY), heavyCannons);
				
				//The rest of this else block is a procedure to damage another square, 
				//but only if target is hit from the side. 
				
				//leftOrTop is the coordinate one less in X (if ship is horizontal) or one less in Y (if ship is vertical)
				//rightOrBottom is the coordinate one more in X (if ship is horizontal) or one more in Y (if ship is vertical)
				Coordinate leftOrTop = null;
				Coordinate rightOrBottom = null; 
				
				Direction targetDirection = targetShip.getDirection();
				//If the torpedo was going North or South, there is side damage only if the target ship was facing East or West. 
				if ((pTorpedoDirection == Direction.NORTH || pTorpedoDirection == Direction.SOUTH) && 
						(targetDirection == Direction.WEST || targetDirection == Direction.EAST)) {
					leftOrTop = new Coordinate (pX-1, pY);
					rightOrBottom = new Coordinate (pX+1, pY); 
				}
				//If the torpedo was going East or West, there is side damage only if the target ship was facing South or North. 
				else if ((pTorpedoDirection == Direction.WEST || pTorpedoDirection == Direction.EAST) && 
						(targetDirection == Direction.NORTH || targetDirection == Direction.SOUTH)) {
					leftOrTop = new Coordinate (pX, pY-1);
					rightOrBottom = new Coordinate (pX, pY+1); 
				}
				
				int leftDamageIndex = -1, rightDamageIndex = -1;
				try {
					leftDamageIndex = targetShip.getDamageIndex(leftOrTop);
				}
				catch (InvalidCoordinateException e) {
					leftOrTop = null;
				}
				try {
					rightDamageIndex = targetShip.getDamageIndex(rightOrBottom);
				}
				catch (InvalidCoordinateException e) {
					rightOrBottom = null;
				}
				//This is the part where we select the side square to be destroyed. 
				//That square needs to exist and not be destroyed
				if (leftOrTop != null && (rightOrBottom == null || targetShip.getDamageAtIndex(rightDamageIndex) == Damage.DESTROYED)) {
					damageShip(targetShip, leftOrTop, heavyCannons);
				}
				else if (rightOrBottom != null && (leftOrTop == null || targetShip.getDamageAtIndex(leftDamageIndex) == Damage.DESTROYED)) {
					damageShip(targetShip, rightOrBottom, heavyCannons);
				}
				else if (leftOrTop != null && rightOrBottom != null) {
					//Both leftOrTop and rightOrBottom squares are intact, so choose one at random. 
					boolean chooseLeft = (Math.random() < 0.5);
					if (chooseLeft) {
						damageShip(targetShip, leftOrTop, heavyCannons);
					}
					else {
						damageShip(targetShip, rightOrBottom, heavyCannons);
					}
				} 
				//else if (leftOrTop == null && rightOrBottom == null) should never happen	
			}
			else if (s instanceof BaseSquare) {
				aLogEntryList.add(new LogEntry(LogType.TORPEDO_HIT_BASE, pX, pY, aTurnNum));
				damageBase(new Coordinate(pX, pY));
			}
			return true; //because we hit something
		}
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
	

	/**
	 * Makes a mine explode, thereby damaging up to 2 squares of the ship that triggered the explosion. 
	 * The mine also disappears forever. 
	 */
	private void mineExplode(Coordinate pMineCoord, Coordinate pDamagedCoord, Ship pDamagedShip) {
		int x = pDamagedCoord.getX();
		int y = pDamagedCoord.getY();
		
		aLogEntryList.add(new LogEntry(LogType.MINE_EXPLOSION, x, y, aTurnNum));
		
		removeMine(pMineCoord);
		boolean heavy = true;	//Mines always destroy heavy armor
		
		//We destroy the main square
		damageShip(pDamagedShip, pDamagedCoord, heavy);
		
		//Now there is stuff to determine the 2nd square to be damaged (similar to torpedoes)
		
		//leftOrTop is the coordinate one less in X (if ship is horizontal) or one less in Y (if ship is vertical)
		//rightOrBottom is the coordinate one more in X (if ship is horizontal) or one more in Y (if ship is vertical)
		Coordinate leftOrTop = null;
		Coordinate rightOrBottom = null; 
		
		//Here we determine the coordinates of the squares close to the impact
		Direction targetDirection = pDamagedShip.getDirection();
		if (targetDirection == Direction.WEST || targetDirection == Direction.EAST) {
			leftOrTop = new Coordinate (x-1, y);
			rightOrBottom = new Coordinate (x+1, y); 
		}
		else if (targetDirection == Direction.NORTH || targetDirection == Direction.SOUTH) {
			leftOrTop = new Coordinate (x, y-1);
			rightOrBottom = new Coordinate (x, y+1); 
		}
		
		int leftDamageIndex = -1, rightDamageIndex = -1;
		try {
			leftDamageIndex = pDamagedShip.getDamageIndex(leftOrTop);
		}
		catch (InvalidCoordinateException e) {
			leftOrTop = null;
		}
		try {
			rightDamageIndex = pDamagedShip.getDamageIndex(rightOrBottom);
		}
		catch (InvalidCoordinateException e) {
			rightOrBottom = null;
		}
		
		//Here we damage the square. If one of them is null or destroyed, we damage the other. 
		if (leftOrTop != null && (rightOrBottom == null || pDamagedShip.getDamageAtIndex(rightDamageIndex) == Damage.DESTROYED)) {
			damageShip(pDamagedShip, leftOrTop, heavy);
		}
		else if (rightOrBottom != null && (leftOrTop == null || pDamagedShip.getDamageAtIndex(leftDamageIndex) == Damage.DESTROYED)) {
			damageShip(pDamagedShip, rightOrBottom, heavy);
		}
		//otherwise, we damage the one closer to the head, i.e. the one with the lowest damage index. 
		else if (leftOrTop != null && rightOrBottom != null) {
			if (leftDamageIndex < rightDamageIndex) {
				damageShip(pDamagedShip, leftOrTop, heavy);
			}
			else {
				damageShip(pDamagedShip, rightOrBottom, heavy);
			}
		}
	}
	
	
	/**
	 * Deal damage to the square of a ship determined by a Coordinate. 
	 * After, checks whether the ship
	 * @param pAttackedShip
	 * @param pCoord
	 * @param pHeavy If true, damage ignores heavy armor and destroys the ship square regardless. 
	 */
	private void damageShip(Ship pAttackedShip, Coordinate pCoord, boolean pHeavy) {
		
		//Damage the ship
		boolean damageDealt = pAttackedShip.setDamageAtIndex(pAttackedShip.getDamageIndex(pCoord), pHeavy);
		
		if (damageDealt) {
			//Check if it sank
			if (pAttackedShip.isSunk()) {
				removeShip(pAttackedShip);
				
				LogEntry log = new LogEntry(LogType.SHIP_SUNK, pCoord.getX(), pCoord.getY(), aTurnNum);
				log.setSunkenShip(pAttackedShip);
				aLogEntryList.add(log);
				
				//Check if all ships in a team are sunk
				boolean p1Lost = true;
				for (Ship ship : aShipListP1) {
					if (!(ship.isSunk())) {
						p1Lost = false;
					}
				}
				boolean p2Lost = true;
				for (Ship ship : aShipListP2) {
					if (!(ship.isSunk())) {
						p2Lost = false;
					}
				}
				if (p1Lost) {
					aWinner = aPlayer2;
					aGameComplete = true;
				}
				else if (p2Lost) {
					aWinner = aPlayer1; 
					aGameComplete = true;
				}
			}
			else {
				setShipPosition(pAttackedShip, pAttackedShip.getHead(), pAttackedShip.getTail());
			}
		}
	}
	
	private void damageBase(Coordinate pCoord) {
		Square s = aBoard[pCoord.getX()][pCoord.getY()];
		if (s instanceof BaseSquare) {
			BaseSquare bs = (BaseSquare) s;
			if (bs.getDamage()==Damage.UNDAMAGED || bs.getDamage()==Damage.DAMAGED) {
				Account owner = bs.getOwner();
				aBoard[pCoord.getX()][pCoord.getY()] = new BaseSquare(Damage.DESTROYED, owner);
				
				if (isBaseDestroyed(owner)) {
					LogEntry log = new LogEntry(LogType.BASE_DESTROYED, pCoord.getX(), pCoord.getY(), aTurnNum);
					log.setAffectedPlayer(owner);
					aLogEntryList.add(log);
				}
				
			}
			//else the base square is already destroyed; nothing happens. 
		}
	}
	
	/**
	 * Checks whether pPlayer's base is completely destroyed, i.e. whether its 10 BaseSquares 
	 * have damage DESTROYED. 
	 * @param pPlayer
	 * @return
	 */
	private boolean isBaseDestroyed(Account pPlayer) {
		int x;
		if (aPlayer1 != null && pPlayer.equals(aPlayer1)) {
			x = 0;
		}
		else if (aPlayer2 != null && pPlayer.equals(aPlayer2)) {
			x = 29;
		}
		else {
			return false; //something is wrong with the input
		}
		
		boolean baseCompletelyDestroyed = true; 
		for (int i = 10; i < 20; i++) {
			Square s = aBoard[x][i];
			if (s instanceof BaseSquare) {
				BaseSquare bs = (BaseSquare) s;
				if (bs.getDamage()==Damage.UNDAMAGED || bs.getDamage()==Damage.DAMAGED) {
					baseCompletelyDestroyed = false;
				}
			}
		}
		return baseCompletelyDestroyed;
	}
	
	
	/*
	 * GETTERS
	 */
	/**
	 * Will return the necessary ship based on equality with the ship from the client.
	 * @param pShipFromClient
	 * @return Server ship
	 */
	public Ship matchWithShip(Ship pShipFromClient){
		if (pShipFromClient.getUsername() == this.getP1Username()){
			for (Ship thisShip: this.aShipListP1){
				if (pShipFromClient.equals(thisShip)){
					return thisShip;
				}
			}
		}
		else {
			for (Ship thisShip: this.aShipListP2){
				if (pShipFromClient.equals(thisShip)){
					return thisShip;
				}
			}
		}
		return null;
	}
	
	public ClientInfo getClientInfo(){
		return aClientInfo;
	}

	public void setClientInfo(ClientInfo pClientInfo){
		this.aClientInfo = pClientInfo;
	}

	public Square[][] getBoard() {
		return aBoard;
	}
	
	public ArrayList<Ship> getP1ShipList(){
		return aShipListP1;
	}
	
	public ArrayList<Ship> getP2ShipList(){
		return aShipListP2;
	}
	
	public String getP1Username() {
		return aPlayer1.getUsername();
	}
	
	public String getP2Username() {
		return aPlayer2.getUsername();
	}
	
	public int getGameID()	{
		return aGameID;
	}

	public int getTurnNum() {
		return aTurnNum;
	}

	public Date getDateLastPlayed() {
		return aDateLastPlayed;
	}

	public String getTurnPlayer() {
		if ((aTurnNum%2) == 0) {
			return aPlayer2.getUsername(); //even
		} else return aPlayer1.getUsername(); //odd 
	}
	
	public LinkedList<LogEntry> getLog() {
		return aLogEntryList;
	}
	
	
	/**
	 * Creates, as a string, an ASCII representation of the board. 
	 * @return
	 */
	public String printBoard() {
		String s = "\nTurn number: " + aTurnNum + "\n";
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
