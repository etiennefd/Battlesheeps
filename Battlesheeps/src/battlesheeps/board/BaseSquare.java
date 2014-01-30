package battlesheeps.board;

import battlesheeps.ships.Ship.Damage;


public class BaseSquare implements Square {
	private Damage aDamage;
	
	public BaseSquare(Damage pDamage) {
		aDamage = pDamage;
	}

	public String toString() {
		String s = "B";
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
