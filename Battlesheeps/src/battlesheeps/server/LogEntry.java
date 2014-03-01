package battlesheeps.server;

import battlesheeps.accounts.Account;
import battlesheeps.board.Coordinate;
import battlesheeps.ships.Ship;

public class LogEntry {

	public enum LogType {
		CANNON_MISS, CANNON_HIT_REEF, CANNON_HIT_SHIP, CANNON_HIT_BASE, CANNON_HIT_MINE, 
		TORPEDO_HIT_REEF, TORPEDO_HIT_SHIP, TORPEDO_HIT_BASE, TORPEDO_HIT_MINE, 
		MINE_EXPLOSION, SHIP_SUNK, BASE_DESTROYED, COLLISION
	}
	
	private int aCoordX;
	private int aCoordY;
	private LogType aLogType;
	private int aTurnNumber;
	private String aSunkenShip = "NONE";
	private String aAffectedPlayer = "NONE";
	
	public LogEntry(LogType pType, int pX, int pY, int pTurn){
		aCoordX = pX;
		aCoordY = pY;
		aLogType = pType;
		aTurnNumber = pTurn;
	}
	
	public Coordinate getCoordinate(){
		return new Coordinate (aCoordX, aCoordY);
	}
	
	public void setSunkenShip (Ship pShip) {
		aSunkenShip = pShip.getClass().getSimpleName();
		aAffectedPlayer = pShip.getUsername();
	}
	
	public void setAffectedPlayer (Account pPlayer) {
		aAffectedPlayer = pPlayer.getUsername();
	}
	
	public String toString(){
		//Get the x and y coordinates of the event
		int x = aCoordX;
		int y = aCoordY;
		
		//Get whose player it is based on the turn number
		int player;
		if ((aTurnNumber%2) == 0) {
			player = 2; 
		} else player = 1;  

		//Display the turn number and player
		String s = "Turn " + aTurnNumber + " -- Player " + player + "\n";

		//Figure out which message to display
		switch (aLogType) {
		case CANNON_MISS: 
			s = s + "A cannon shot was detected on square (" + x + ", " + y + "), but " +
				"it did not hit anything.";
			break;
		case CANNON_HIT_REEF: 
			s = s + "A cannon shot hit a reef on square (" + x + ", " + y + "), but " +
				"it had no effect.";
			break;
		case CANNON_HIT_SHIP: 
			s = s + "A cannon shot hit a ship on square (" + x + ", " + y + ").";
			break;
		case CANNON_HIT_BASE: 
			s = s + "A cannon shot hit the base on square (" + x + ", " + y + ").";
			break;
		case CANNON_HIT_MINE: 
			s = s + "A cannon shot was detected on square (" + x + ", " + y + "), but " +
				"it did not hit anything."; //Players don't know it destroyed a mine
			break;
		case TORPEDO_HIT_REEF: 
			s = s + "A torpedo hit a reef on square (" + x + ", " + y + "), but " +
				"it had no effect.";
			break;
		case TORPEDO_HIT_SHIP: 
			s = s + "A torpedo hit a ship on square (" + x + ", " + y + ").";
			break;
		case TORPEDO_HIT_BASE: 
			s = s + "A torpedo hit the base on square (" + x + ", " + y + ").";
			break;
		case TORPEDO_HIT_MINE: 
			s = s + "A torpedo hit something underwater on square (" + x + ", " + y + ").";
			break;
		case MINE_EXPLOSION: 
			s = s + "A mine exploded on square (" + x + ", " + y + ").";
			break;
		case SHIP_SUNK: 
			s = s + "The ship " + aSunkenShip + " belonging to player " + aAffectedPlayer + " has sunk!";
			break;
		case BASE_DESTROYED: 
			s = s + "The entire base belonging to player " + aAffectedPlayer + " was destroyed!";
			break;
		case COLLISION: 
			s = s + "A collision involving a ship occurred on square (" + x + ", " + y + ").";
			break;
		}

		return s;
	}
}
