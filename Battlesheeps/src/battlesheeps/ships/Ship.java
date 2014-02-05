package battlesheeps.ships;

import battlesheeps.accounts.Account;
import battlesheeps.exceptions.InvalidCoordinateException;
import battlesheeps.game.Coordinate;
import battlesheeps.game.Game.Direction;

/**
 * Ship is an abstract class containing the common functionality of actual ships such as 
 * cruisers, destroyers, etc. 
 * @author etienne
 *
 */
public abstract class Ship {

	public enum Damage {
		UNDAMAGED, DAMAGED, DESTROYED
	}

	private Coordinate aLocationHead;	//x and y coordinates of the bow of the ship on the board (between 0 and 29)
	private Coordinate aLocationTail;	//x and y coordinates of the stern of the ship
	
	private Account aPlayer; 
		
	protected int aSize;					//Length of the ship, in squares. 
	protected int aMaxSpeed;				//Number of squares the ship can move forward when undamaged
	protected boolean aHeavyArmour;			//True if the ship has heavy armour, false for normal armour
	protected int aRadarRangeLength;		//Length of radar (parallel to ship)
	protected int aRadarRangeWidth;			//Width of radar (perpendicular to ship)
	protected int aCannonRangeLength;		//Length of cannon range (parallel to ship)
	protected int aCannonRangeWidth;		//Width of cannon range (perpendicular to ship)
	protected boolean aTurn180;				//True if ship can rotate 180 degrees over its center; false if the ship can only rotate 90 degrees over its tail.
	
	protected int aActualSpeed;				//Number of squares the ship can move forward, given current amount of damage
	protected Damage[] aDamage;				//Array of the length of the ship providing information on the damage status of every square (head == 0)
	
	
	/**
	 * This method should be called by the subclass after the construction of a ship
	 */
	public void initializeShip(Account pPlayer) {
		aPlayer = pPlayer;
		aLocationHead = null;
		aLocationTail = null;
		
		aActualSpeed = aMaxSpeed;
		aDamage = new Damage[aSize];
		for (int i = 0; i<aDamage.length; i++) {
			aDamage[i] = Damage.UNDAMAGED;
		}
	}
	
	/**
	 * Repairs one square of the ship. The repaired square is the first one (starting at the head and moving toward the tail)
	 * that is not "undamaged". Whether the square is "damaged" or "destroyed" is irrelevant. 
	 * 
	 * Note: there is no check to see if the base is still intact. However, the action of repairing will be available
	 * only if the ship is docked to an undamaged square of the base, so once they are all destroyed no ships will 
	 * be reparable. 
	 */
	public void repair() {
		for (int i = 0; i < aDamage.length; i++) {
			if (aDamage[i] != Damage.UNDAMAGED) {
				aDamage[i] = Damage.UNDAMAGED;
				break;
			}
		}
	}
	
	public void setLocation(Coordinate pHead, Coordinate pTail) {
		aLocationHead = pHead;
		aLocationTail = pTail;
	}
	
	/*
	 * GETTERS
	 */
	
	public int getSize() {
		return aSize;
	}
	
	public int getActualSpeed() {
		return aActualSpeed;
	}
	
	public Coordinate getHead() {
		return aLocationHead;
	}
	
	public Coordinate getTail() {
		return aLocationTail;
	}
	
	public boolean canTurn180() {
		return aTurn180;
	}
	
	public String getUsername() {
		return aPlayer.getUsername();
	}
	/**
	 * Returns one of North, South, East, or West, depending on the position of the ship's head and tail.  
	 * @return
	 */
	public Direction getDirection() {
		if (aLocationHead.getX() == aLocationTail.getX()) {
			//Both head and tail in same column: either facing North or South
			if (aLocationHead.getY() > aLocationTail.getY()) return Direction.SOUTH;
			else return Direction.NORTH;
		}
		else if (aLocationHead.getY() == aLocationTail.getY()){
			//Both head and tail in same row: either facing East or West
			if (aLocationHead.getX() > aLocationTail.getX()) return Direction.EAST;
			else return Direction.WEST;
		}
		else {
			throw new InvalidCoordinateException();
		}
	}
	
	
}
