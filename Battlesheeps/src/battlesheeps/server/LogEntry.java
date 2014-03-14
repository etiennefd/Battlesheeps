package battlesheeps.server;

import java.io.Serializable;

import battlesheeps.board.Coordinate;
import battlesheeps.ships.Ship;

public class LogEntry implements Serializable
{
	private static final long serialVersionUID = -7054533442523433536L;

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
	
	public void setAffectedPlayer (String pPlayer) {
		aAffectedPlayer = pPlayer;
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
		String s = "Turn " + aTurnNumber + " - Player " + player + ":\n";

		//Figure out which message to display
		switch (aLogType) {
		case CANNON_MISS: 
			s = s + "Cannon shot detected on square (" + x + ", " + y + "), but " +
				"it did not hit anything.";
			break;
		case CANNON_HIT_REEF: 
			s = s + "Cannon shot hit a reef on square (" + x + ", " + y + "), but " +
				"it had no effect.";
			break;
		case CANNON_HIT_SHIP: 
			s = s + "Cannon shot hit a ship on square (" + x + ", " + y + ").";
			break;
		case CANNON_HIT_BASE: 
			s = s + "Cannon shot hit the base on square (" + x + ", " + y + ").";
			break;
		case CANNON_HIT_MINE: 
			s = s + "Cannon shot detected on square (" + x + ", " + y + "), but " +
				"it did not hit anything."; //Players don't know it destroyed a mine
			break;
		case TORPEDO_HIT_REEF: 
			s = s + "Torpedo hit a reef on square (" + x + ", " + y + "), but " +
				"it had no effect.";
			break;
		case TORPEDO_HIT_SHIP: 
			s = s + "Torpedo hit a ship on square (" + x + ", " + y + ").";
			break;
		case TORPEDO_HIT_BASE: 
			s = s + "Torpedo hit the base on square (" + x + ", " + y + ").";
			break;
		case TORPEDO_HIT_MINE: 
			s = s + "Torpedo hit something underwater on square (" + x + ", " + y + ").";
			break;
		case MINE_EXPLOSION: 
			s = s + "Mine explosion on square (" + x + ", " + y + ").";
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
