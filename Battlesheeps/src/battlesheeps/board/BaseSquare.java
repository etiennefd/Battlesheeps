package battlesheeps.board;

import java.io.Serializable;

import battlesheeps.accounts.Account;
import battlesheeps.ships.Ship.Damage;


public class BaseSquare implements Square, Serializable {
	private static final long serialVersionUID = -6794223046387774769L;
	private Damage aDamage;
	private String aOwner;
	
	public BaseSquare(Damage pDamage, String pOwner) {
		aDamage = pDamage;
		aOwner = pOwner;
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
	
	public Damage getDamage() {
		return aDamage;
	}
	
	public String getOwner() {
		return aOwner;
	}

}
