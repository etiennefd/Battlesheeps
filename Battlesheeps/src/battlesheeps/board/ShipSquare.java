package battlesheeps.board;

import java.io.Serializable;

import battlesheeps.ships.Cruiser;
import battlesheeps.ships.Destroyer;
import battlesheeps.ships.MineLayer;
import battlesheeps.ships.RadarBoat;
import battlesheeps.ships.Ship;
import battlesheeps.ships.Ship.Damage;
import battlesheeps.ships.TorpedoBoat;

public class ShipSquare implements Square, Serializable 
{
	private static final long serialVersionUID = 1932779428353868210L;
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
	
	public boolean isHead() {
		return aHead;
	}

	public Damage getDamage() {
		return aDamage;
	}
}
