package battlesheeps.ships;

import battlesheeps.game.Coordinate;

/**
 * Ship is an abstract class containing the common functionality of actual ships such as 
 * cruisers, destroyers, etc. 
 * @author etienne
 *
 */
public abstract class Ship {


	private Coordinate aLocationHead;	//x and y coordinates of the bow of the ship on the board (between 0 and 29)
	private Coordinate aLocationTail;	//x and y coordinates of the stern of the ship
		
	protected int aSize;					//Length of the ship, in squares. 
	protected int aSpeed;					//Number of squares the ship can move forward
	protected boolean aHeavyArmour;			//True if the ship has heavy armour, false for normal armour
	protected int aRadarRangeLength;		//Length of radar (parallel to ship)
	protected int aRadarRangeWidth;			//Width of radar (perpendicular to ship)
	protected int aCannonRangeLength;		//Length of cannon range (parallel to ship)
	protected int aCannonRangeWidth;		//Width of cannon range (perpendicular to ship)
	protected int aTurnPoint;				//int between 0 and aSize-1. Represents the square that doesn't move when turning, 0 being the head.
	
	public Ship() {
		aLocationHead = null;
		aLocationTail = null;
		
	}
	
	public int getSpeed() {
		return aSpeed;
	}
	
	
}
