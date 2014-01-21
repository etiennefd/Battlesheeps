package battlesheeps.elements;

public class Weapon {

	public enum WeaponType {
		CANNON, MINE, TORPEDO 
	}
	
	private WeaponType aType;		//Enum 
	private int[] aRange; 			//Array of size two representing the dimension of the weapon's range
	private boolean aHeavy; 		//true if the weapon can destroy heavy armour in one hit; false otherwise.
}
