package battlesheeps.ships;

public class MineLayer extends Ship {

	private int aMineSupply; 			//Number of mines on board. 

	public MineLayer() {
		
		super();
		aSize = 2;
		aSpeed = 6;
		aHeavyArmour = true;
		aRadarRangeLength = 6;
		aRadarRangeWidth = 5;
		aCannonRangeLength = 4;
		aCannonRangeWidth = 5;
		aTurnPoint = 1;	
		
		aMineSupply = 5;		//A mine layer starts with 5 mines!
	}
}
