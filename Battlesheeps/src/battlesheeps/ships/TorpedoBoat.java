package battlesheeps.ships;

import battlesheeps.accounts.Account;

public class TorpedoBoat extends Ship {


	public TorpedoBoat(Account pPlayer) {
		
		super();
		aSize = 3;
		aMaxSpeed = 9;
		aHeavyArmour = false;
		aRadarRangeLength = 6;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 5;
		aCannonRangeWidth = 5;
		aTurn180 = true; 
		
		initializeShip(pPlayer);
	}
}
