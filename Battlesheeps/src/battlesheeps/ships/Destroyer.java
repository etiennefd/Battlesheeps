package battlesheeps.ships;

import battlesheeps.accounts.Account;

public class Destroyer extends Ship {

	public Destroyer(Account pPlayer, int pShipID) {
		
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
