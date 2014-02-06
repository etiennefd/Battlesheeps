package battlesheeps.ships;

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
	
	/**
	 * Matches a Coordinate target to a cell in the damage array of this ship. Then, changes the value of that 
	 * cell to Damaged/Destroyed (affected by heavy cannons and heavy armour). 
	 * @param pTarget
	 * @param pHeavyCannons
	 */
	public void setDamage(Coordinate pTarget, boolean pHeavyCannons) {
		int targetX = pTarget.getX();
		int targetY = pTarget.getY();
		Direction direction = this.getDirection();
		int damageIndex = 0; 
		int x, y;
		
		switch (direction) {
		case NORTH: 
			x = aLocationHead.getX();
			for (y = aLocationHead.getY(); y <= aLocationTail.getY(); y++, damageIndex++) {
				if (targetY == y && targetX == x) {
					break;
				}
			}
			break;
		case SOUTH: 
			x = aLocationHead.getX();
			for (y = aLocationHead.getY(); y >= aLocationTail.getY(); y--, damageIndex++) {
				if (targetY == y && targetX == x) {
					break;
				}
			}
			break;
		case WEST: 
			y = aLocationHead.getY();
			for (x = aLocationHead.getX(); x <= aLocationTail.getX(); x++, damageIndex++) {
				if (targetY == y && targetX == x) {
					break;
				}
			}
			break;
		case EAST: 
			y = aLocationHead.getY();
			for (x = aLocationHead.getX(); x >= aLocationTail.getX(); x--, damageIndex++) {
				if (targetY == y && targetX == x) {
					break;
				}
			}
			break;
		}
		//Here we change the Damage value of the computed cell. 
		if (aDamage[damageIndex] == Damage.DAMAGED) {
			aDamage[damageIndex] = Damage.DESTROYED;
		}
		else if (aDamage[damageIndex] == Damage.UNDAMAGED) {
			if (aHeavyArmour && !pHeavyCannons) {
				aDamage[damageIndex] = Damage.DAMAGED;
			}
			else {
				aDamage[damageIndex] = Damage.DESTROYED;
			}
		}
		//else no effect, as the square is already destroyed. 
	}
	
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
	
	public Damage getDamage(int pIndex) {
		return aDamage[pIndex];
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
