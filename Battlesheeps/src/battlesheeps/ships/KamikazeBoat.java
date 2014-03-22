package battlesheeps.ships;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.board.Coordinate;

public class KamikazeBoat extends Ship {

	private static final long serialVersionUID = -7993528551904441093L;

	public KamikazeBoat(String pPlayer, int pShipID) {

		super();
		aSize = 1;
		aMaxSpeed = 2;
		aHeavyArmour = true;
		aRadarRangeLength = 5;
		aRadarRangeWidth = 5;
		aCannonRangeLength = 0;
		aCannonRangeWidth = 0;
		aTurn180 = false; 
		aShipID = pShipID;

		initializeShip(pPlayer);
	}
	
	/**
	 * Overriding the ship's method since the kamikaze boat's radar 
	 * completely surrounds it, unlike the other ships. 
	 * (Code adapted from the mine layer class)
	 */
	@Override 
	public List<Coordinate> getRadarRange() {
		
		List<Coordinate> list = new ArrayList<Coordinate>();
		
		//Empty list if the ship is not on board
		if (this.getDirection() == null) {
			return list;
		}
		
		Coordinate head = this.getHead();
		int startX = head.getX() - 2;
		int startY = head.getY() - 2; 
		int maxX = startX + aRadarRangeLength;
		int maxY = startY + aRadarRangeWidth;
		
		//Cycle	through the range
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
