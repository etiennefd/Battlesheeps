package battlesheeps.ships;

import battlesheeps.game.Coordinate;

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
		
	protected int aSize;					//Length of the ship, in squares. 
	protected int aMaxSpeed;				//Number of squares the ship can move forward when undamaged
	protected boolean aHeavyArmour;			//True if the ship has heavy armour, false for normal armour
	protected int aRadarRangeLength;		//Length of radar (parallel to ship)
	protected int aRadarRangeWidth;			//Width of radar (perpendicular to ship)
	protected int aCannonRangeLength;		//Length of cannon range (parallel to ship)
	protected int aCannonRangeWidth;		//Width of cannon range (perpendicular to ship)
	protected int aTurnPoint;				//int between 0 and aSize-1. Represents the square that doesn't move when turning, 0 being the head.
	
	protected int aActualSpeed;				//Number of squares the ship can move forward, given current amount of damage
	protected Damage[] aDamage;				//Array of the length of the ship providing information on the damage status of every square
	
	
	/**
	 * This method should be called by the subclass after the construction of a ship
	 */
	public void initializeShip() {
		aLocationHead = null;
		aLocationTail = null;
		
		aActualSpeed = aMaxSpeed;
		aDamage = new Damage[aSize];
		for (int i = 0; i<aDamage.length; i++) {
			aDamage[i] = Damage.UNDAMAGED;
		}
	}
	
	public void setLocation(Coordinate pHead, Coordinate pTail) {
		aLocationHead = pHead;
		aLocationTail = pTail;
	}
	
	public int getSize() {
		return aSize;
	}
	
	public int getActualSpeed() {
		return aActualSpeed;
	}
	
	
}
