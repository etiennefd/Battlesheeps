package battlesheeps.elements;

public class Ship {

	public enum ShipType {
		CRUISER, DESTROYER, TORPEDO_BOAT, MINE_LAYER, RADAR_BOAT
	}

	private Coordinate aLocationHead;	//x and y coordinates of the bow of the ship on the board (between 0 and 29)
	private Coordinate aLocationTail;	//x and y coordinates of the stern of the ship
	
	private ShipType aType;				//Enum
	
	private int aSize;					//Length of the ship, in squares. 
	private int aSpeed;					//Number of squares the ship can move forward
	private boolean aHeavyArmour;		//True if the ship has heavy armour, false for normal armour
	private int[] aRadarRange;			//Array of size 2 containing the dimensions of the radar 
	private int aTurnPoint;				//int between 0 and aSize-1. Represents the square that doesn't move when turning.
	private int mineSupply; 			//Number of mines on board. (Relevant only for MINE_LAYER). 
	
	
}
