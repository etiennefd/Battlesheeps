package battlesheeps.ships;

import battlesheeps.accounts.Account;

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
}
