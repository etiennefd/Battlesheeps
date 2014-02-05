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
}
