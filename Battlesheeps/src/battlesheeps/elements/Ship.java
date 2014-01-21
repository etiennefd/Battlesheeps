package battlesheeps.elements;

import battlesheeps.game.Coordinate;

public abstract class Ship {


	private Coordinate aLocationHead;	//x and y coordinates of the bow of the ship on the board (between 0 and 29)
	private Coordinate aLocationTail;	//x and y coordinates of the stern of the ship
		
	private int aSize;					//Length of the ship, in squares. 
	private int aSpeed;					//Number of squares the ship can move forward
	private boolean aHeavyArmour;		//True if the ship has heavy armour, false for normal armour
	private int[] aRadarRange;			//TODO Array of size 2 containing two corners of the radar range 
	private int aTurnPoint;				//int between 0 and aSize-1. Represents the square that doesn't move when turning.
	
	public Ship() {
		aLocationHead = null;
		aLocationTail = null;
	}
	
}
