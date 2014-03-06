package battlesheeps.ships;

import java.util.ArrayList;
import java.util.List;

import battlesheeps.accounts.Account;
import battlesheeps.board.Coordinate;
import battlesheeps.server.ServerGame.Direction;

public class TorpedoBoat extends Ship 
{
	private static final long serialVersionUID = -6257700737574162318L;

	public TorpedoBoat(Account pPlayer, int pShipID) {
		
		super();
		aSize = 3;
		aMaxSpeed = 9;
		aHeavyArmour = false;
		aRadarRangeLength = 6;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 5;
		aCannonRangeWidth = 5;
		aTurn180 = true; 
		aShipID = pShipID;
		
		initializeShip(pPlayer);
	}

	@Override 
	public List<Coordinate> getCannonCoordinates() {
		
		//The torpedo ship cannot fire its cannon behind it. 
		
		ArrayList<Coordinate> cannonCoords = new ArrayList<Coordinate>();
		Direction myDirection = this.getDirection();
		
		int headX = this.getTail().getX();
		int headY = this.getTail().getY();
		int startX, startY;
		
		switch (myDirection) {
			case NORTH : 
				
				startX = headX - aCannonRangeWidth/2; 
				startY = headY;
				
				for (int i = startX; i < (startX + aCannonRangeWidth); i++) {
					for (int j = startY; j > (startY - aCannonRangeLength); j--) {
						Coordinate c = new Coordinate(i,j);
						if (c.inBounds()){
							cannonCoords.add(c);
						}
					}
				}
				break;
			case SOUTH : 
			
				startX = headX - aCannonRangeWidth/2; 
				startY = headY;
				
				for (int i = startX; i < (startX + aCannonRangeWidth); i++) {
					for (int j = startY; j < (startY + aCannonRangeLength); j++) {
						Coordinate c = new Coordinate(i,j);
						if (c.inBounds()){
							cannonCoords.add(c);
						}
					}
				}
				break; 
			case WEST : 
				
				startX = headX; 
				startY = headY - aCannonRangeLength/2;
				
				for (int i = startX; i > (startX - aCannonRangeLength); i--) {
					for (int j = startY; j > (startY - aCannonRangeWidth); j--) {
						Coordinate c = new Coordinate(i,j);
						if (c.inBounds()){
							cannonCoords.add(c);
						}
					}
				}
				break;
			default : /*EAST*/
				
				startX = headX; 
				startY = headY - aCannonRangeLength/2;
				
				for (int i = startX; i < (startX + aCannonRangeLength); i++) {
					for (int j = startY; j < (startY + aCannonRangeWidth); j++) {
						Coordinate c = new Coordinate(i,j);
						if (c.inBounds()){
							cannonCoords.add(c);
						}
					}
				}
				break;
		}
		
		return cannonCoords;
	}
	
}
