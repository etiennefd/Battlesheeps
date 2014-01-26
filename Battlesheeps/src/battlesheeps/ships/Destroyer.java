package battlesheeps.ships;

public class Destroyer extends Ship {

	public Destroyer() {
		
		super();
		aSize = 4;
		aMaxSpeed = 8;
		aHeavyArmour = false;
		aRadarRangeLength = 8;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 12;
		aCannonRangeWidth = 9;
		aTurnPoint = 3;	
		
		initializeShip();
	}
}
