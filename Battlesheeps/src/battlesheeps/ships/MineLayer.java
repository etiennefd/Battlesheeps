package battlesheeps.ships;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.board.Coordinate;
import battlesheeps.accounts.Account;
import battlesheeps.server.ServerGame.Direction;

public class MineLayer extends Ship {

	private int aMineSupply; 			//Number of mines on board. 

	public MineLayer(Account pPlayer) {
		
		super();
		aSize = 2;
		aMaxSpeed = 6;
		aHeavyArmour = true;
		aRadarRangeLength = 6;
		aRadarRangeWidth = 5;
		aCannonRangeLength = 4;
		aCannonRangeWidth = 5;
		aTurn180 = false; 
		
		initializeShip(pPlayer);
		
		aMineSupply = 5;		//A mine layer starts with 5 mines
	}
	
	/**
	 * Removes a mine from the supply. Returns false if there are no mines in the supply. 
	 * @return True if a mine is laid, false if no mine was in the supply. 
	 */
	public boolean layMine() {
		if (aMineSupply > 0) {
			aMineSupply--;
			return true;
		}
		else return false;
	}
	
	/**
	 * Adds a mine to the ship's supply. 
	 */
	public void retrieveMine() {
		aMineSupply++;
	}
	
	/*
	 * Getter for the number of mines. 
	 */
	public int getMineSupply() {
		return aMineSupply;
	}
	
	@Override 
	public List<Coordinate> getRadarRange() {
		
		List<Coordinate> list = new ArrayList<Coordinate>();
		
		int startX;
		int startY;
		int maxX;
		int maxY;
		
		Direction shipDirection = this.getDirection();
		Coordinate head = this.getHead();
		Coordinate tail = this.getTail();
		
		//we always want to start at the top left corner of the radar
		if (shipDirection == Direction.WEST || shipDirection == Direction.NORTH) {
			startX = head.getX() - 2;
			startY = head.getY() - 2; 
		} else { /*EAST or SOUTH*/
			startX = tail.getX() - 2;
			startY = tail.getY() - 2;
		}
		//and then we'll cycle across and down 
		if (shipDirection == Direction.WEST || shipDirection == Direction.EAST) {
			maxX = startX + aRadarRangeLength;
			maxY = startY + aRadarRangeWidth;
		} else { /*NORTH or SOUTH*/
			maxX = startX + aRadarRangeWidth;
			maxY = startY + aRadarRangeLength;
		}
			
		for (int i = startX; i < maxX; i++) {
			for (int j = startY; j < maxY; j++) {
				Coordinate coord = new Coordinate(i,j);
				if (coord.inBounds()){
					list.add(coord);
				}
			}
		}
		
		return list;
	}
	
}
