package battlesheeps.ships;

public class Destroyer extends Ship 
{
	private static final long serialVersionUID = 6501047028829862958L;

	public Destroyer(String pPlayer, int pShipID) {
		
		super();
		aSize = 4;
		aMaxSpeed = 8;
		aHeavyArmour = false;
		aRadarRangeLength = 8;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 12;
		aCannonRangeWidth = 9;
		aTurn180 = false; 
		aShipID = pShipID;
		
		initializeShip(pPlayer);
	}
}
