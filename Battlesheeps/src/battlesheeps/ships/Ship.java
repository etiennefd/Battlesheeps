package battlesheeps.ships;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.accounts.Account;
import battlesheeps.board.Coordinate;
import battlesheeps.exceptions.InvalidCoordinateException;
import battlesheeps.server.ServerGame.Direction;

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

	private Coordinate head;	//x and y coordinates of the bow of the ship on the board (between 0 and 29)
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
		head = null;
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
		head = pHead;
		aLocationTail = pTail;
	}
	
	/**
	 * Matches a Coordinate target to a cell in the damage array of this ship.
	 * Will throw an InvalidCoordinateException if the Coordinate doesn't match the ship's position. 
	 * @param pTarget
	 * @param pHeavyCannons
	 */
	public int getDamageIndex(Coordinate pTarget) {
		int targetX = pTarget.getX();
		int targetY = pTarget.getY();
		Direction direction = this.getDirection();
		int tempIndex = 0; 
		int damageIndex = -1;
		int x, y;
		
		switch (direction) {
		case NORTH: 
			x = head.getX();
			for (y = head.getY(); y <= aLocationTail.getY(); y++, tempIndex++) {
				if (targetY == y && targetX == x) {
					damageIndex = tempIndex;
					break;
				}
			}
			break;
		case SOUTH: 
			x = head.getX();
			for (y = head.getY(); y >= aLocationTail.getY(); y--, tempIndex++) {
				if (targetY == y && targetX == x) {
					damageIndex = tempIndex;
					break;
				}
			}
			break;
		case WEST: 
			y = head.getY();
			for (x = head.getX(); x <= aLocationTail.getX(); x++, tempIndex++) {
				if (targetY == y && targetX == x) {
					damageIndex = tempIndex;
					break;
				}
			}
			break;
		case EAST: 
			y = head.getY();
			for (x = head.getX(); x >= aLocationTail.getX(); x--, tempIndex++) {
				if (targetY == y && targetX == x) {
					damageIndex = tempIndex;
					break;
				}
			}
			break;
		}
		if (damageIndex >= 0) {
			return damageIndex;
		}
		else {
			throw new InvalidCoordinateException();
		}
	}
	
	/**
	 * Changes the value of the specified cell in the damage array of the ship. 
	 * Returns true if some damage was done, and false otherwise (index out of array bounds, or square already destroyed). 
	 * 
	 * The proper way of dealing damage to a ship is to call: 
	 * ship.setDamageAtIndex(ship.getDamageIndex(*some coordinate*), *heavyCannons*);
	 * @param pIndex
	 * @param pHeavyCannons
	 */
	public boolean setDamageAtIndex(int pIndex, boolean pHeavyCannons) {
		if (pIndex < 0 || pIndex >= aDamage.length) {
			return false;
		}
		
		if (aDamage[pIndex] == Damage.DESTROYED) {
			return false;
		}
		
		if (aDamage[pIndex] == Damage.DAMAGED) {
			aDamage[pIndex] = Damage.DESTROYED;
		}
		else if (aDamage[pIndex] == Damage.UNDAMAGED) {
			if (aHeavyArmour && !pHeavyCannons) {
				aDamage[pIndex] = Damage.DAMAGED;
			}
			else {
				aDamage[pIndex] = Damage.DESTROYED;
			}
		}
		return true;
	}
	
	/**
	 * Tests whether all the squares of the ship have been destroyed. 
	 * Should be called immediately after dealing damage to the ship. 
	 * @return
	 */
	public boolean isSunk() {
		boolean sunk = true;
		for (int i = 0; i < aDamage.length; i++) {
			if (aDamage[i] != Damage.DESTROYED) {
				sunk = false;
			}
		}
		return sunk;
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
		return head;
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
	
	public Damage getDamageAtIndex(int pIndex) {
		return aDamage[pIndex];
	}
	
	/**
	 * Returns one of North, South, East, or West, depending on the position of the ship's head and tail.  
	 * @return
	 */
	public Direction getDirection() {
		if (head.getX() == aLocationTail.getX()) {
			//Both head and tail in same column: either facing North or South
			if (head.getY() > aLocationTail.getY()) return Direction.SOUTH;
			else return Direction.NORTH;
		}
		else if (head.getY() == aLocationTail.getY()){
			//Both head and tail in same row: either facing East or West
			if (head.getX() > aLocationTail.getX()) return Direction.EAST;
			else return Direction.WEST;
		}
		else {
			throw new InvalidCoordinateException();
		}
	}
	
	/**
	 *Returns a list of ALL coordinates which fall within the ship's radar range 
	 *(Note that this includes coordinates of the ship itself which are in radar range)  
	 */
	public List<Coordinate> getRadarRange() {
		List<Coordinate> list = new ArrayList<Coordinate>();
		int startX;
		int startY;
		
		Direction shipDirection = this.getDirection();
		
		if (shipDirection == Direction.WEST) {
			startX = aLocationTail.getX() - 1;
			startY = aLocationTail.getY() - (aRadarRangeWidth/2);
			int minX = startX - aRadarRangeLength;
			int maxY = startY + aRadarRangeWidth;

			for (int i = startX; i > minX ; i--) {
				for (int j = startY; j < maxY; j++) {
					Coordinate coord = new Coordinate(i,j);
					if (coord.inBounds()){
						list.add(coord);
					}
				}
			}
		}
		else if (shipDirection == Direction.EAST) {

			startX = aLocationTail.getX() + 1;
			startY = aLocationTail.getY() - (aRadarRangeWidth/2);
			int maxX = startX + aRadarRangeLength;
			int maxY = startY + aRadarRangeWidth;

			for (int i = startX; i < maxX ; i++) {
				for (int j = startY; j < maxY; j++) {
					Coordinate coord = new Coordinate(i,j);
					if (coord.inBounds()){
						list.add(coord);
					}
				}
			}
		}
		else if (shipDirection == Direction.NORTH) {

			startX = aLocationTail.getX() - (aRadarRangeWidth/2);
			startY = aLocationTail.getY() - 1;
			int maxX = startX + aRadarRangeWidth; 
			int minY = startY - aRadarRangeLength; 

			for (int i = startX; i < maxX ; i++) {
				for (int j = startY; j > minY; j--) {
					Coordinate coord = new Coordinate(i,j);
					if (coord.inBounds()){
						list.add(coord);
					}
				}
			}
		}
		else /*SOUTH*/{

			startX = aLocationTail.getX() - (aRadarRangeWidth/2);
			startY = aLocationTail.getY() + 1;
			int maxX = startX + aRadarRangeWidth; 
			int maxY = startY + aRadarRangeLength; 

			for (int i = startX; i < maxX ; i++) {
				for (int j = startY; j < maxY; j++) {
					Coordinate coord = new Coordinate(i,j);
					if (coord.inBounds()){
						list.add(coord);
					}
				}
			}				
		}

		return list;
	}	
	
}
