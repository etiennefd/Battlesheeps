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
	
	private int aGameID;
	private Account aPlayer1;
	private Account aPlayer2;
	private int aTurnNum;
	private String aDateLastPlayed;
	
	private Square[][] aBoard;
	private ArrayList<Ship> aShipListP1;
	private ArrayList<Ship> aShipListP2;
	
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
	 * Adds pNum coral reef squares to the board within the central 10*24 area. 
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
