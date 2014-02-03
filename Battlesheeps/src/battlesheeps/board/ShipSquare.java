package battlesheeps.board;

import battlesheeps.ships.Ship.Damage;
import battlesheeps.ships.*;

public class ShipSquare implements Square {

	private Ship aShip;
	private Damage aDamage;
	private boolean aHead;

	public ShipSquare(Ship pShip, Damage pDamage, boolean pHead) {
		aShip = pShip;
		aDamage = pDamage;
		aHead = pHead;
	}
	
	public String toString() {
		String s = "s";
		if (aShip instanceof Cruiser) {
			s = "C";
		} else if (aShip instanceof Destroyer) {
			s = "D";
		} else if (aShip instanceof TorpedoBoat) {
			s = "T";
		} else if (aShip instanceof MineLayer) {
			s = "M";
		} else if (aShip instanceof RadarBoat) {
			s = "R";
		} else s = "S";
		
		if (aHead) s = s.toLowerCase();
		
		if (aDamage == Damage.UNDAMAGED) {
			s = s + "2";
		} else if (aDamage == Damage.DAMAGED) {
			s = s + "1";
		} else if (aDamage == Damage.DESTROYED) {
			s = s + "0";
		} else s = s + "9";
		
		return s;
	}
	
	public Ship getShip() {
		return aShip;
	}

}
