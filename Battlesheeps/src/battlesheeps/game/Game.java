package battlesheeps.game;

import java.util.ArrayList;

import battlesheeps.accounts.Account;
import battlesheeps.board.*;
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
		for (int i = 0; i<aBoard.length; i++) {
			for (int j = 0 ; j<aBoard[i].length; j++) {
				if (i >= 10 && i < 20 && (j==0 || j==29)) {
					aBoard[i][j] = new BaseSquare(Damage.UNDAMAGED);
				}
				else {
					aBoard[i][j] = new Sea();
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
			Coordinate newCoral = Coordinate.randomCoord(3, 26, 10, 19);
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
	 * @param pShip
	 * @param pHead
	 * @param pTail
	 */
	public void setShipPosition(Ship pShip, Coordinate pHead, Coordinate pTail) {
		
		//Are the head and the tail on the same X column? Do they correspond to the length of the ship? 
		if (pHead.getX() == pTail.getX() && Math.abs(pHead.getY() - pTail.getY()) == pShip.getSize() - 1) {
			
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
			System.out.println("Problem: coordinates don't match");//Should be an exception
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
	
	private void translateShip(Ship pShip, Coordinate pCoord) {
		/* TODO
		 * Get the current location of the ship
		 * Determine direction of translation (forward, backward, port, starboard)
		 * If forward: 
		 * 	determine if there are any obstacles (enemy ships or mine) between head of ship and goal point
		 *  if yes, determine new position and place the ship there
		 *  if it was a mine, call mineExplode()? (damage the ship and remove the mine)
		 *  if there was no obstacle, assign the ship's position to a new position based on pCoord (represents the new position of the head)
		 * If some other direction: 
		 *  do the same, except that obstacles have to be checked for every square of the ship facing the direction
		 * In all cases, recompute visibility
		 */
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
				" 0 - Destroyed\n";
		for (int i = 0; i<aBoard.length; i++) {
			for (int j = 0 ; j<aBoard[i].length; j++) {
				s = s + aBoard[i][j].toString();
			}
			s = s + "\n";
		}
		return s;
	}

}
