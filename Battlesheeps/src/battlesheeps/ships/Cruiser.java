package battlesheeps.ships;

import battlesheeps.accounts.Account;

public class Cruiser extends Ship {

	public Cruiser(Account pPlayer) {
		
		super();
		aSize = 5;
		aMaxSpeed = 10;
		aHeavyArmour = true;
		aRadarRangeLength = 10;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 15;
		aCannonRangeWidth = 11;
		aTurn180 = false; 
		
		initializeShip(pPlayer);
	}
}
