package battlesheeps.board;

import battlesheeps.ships.Ship.Damage;
import battlesheeps.ships.*;

public class ShipSquare implements Square {

	private Ship aShipType;
	private Damage aDamage;
	private boolean aHead;

	public ShipSquare(Ship pType, Damage pDamage, boolean pHead) {
		aShipType = pType;
		aDamage = pDamage;
		aHead = pHead;
	}
	
	public String toString() {
		String s = "s";
		if (aShipType instanceof Cruiser) {
			s = "C";
		} else if (aShipType instanceof Destroyer) {
			s = "D";
		} else if (aShipType instanceof TorpedoBoat) {
			s = "T";
		} else if (aShipType instanceof MineLayer) {
			s = "M";
		} else if (aShipType instanceof RadarBoat) {
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

}
