package battlesheeps.ships;

import battlesheeps.accounts.Account;

public class RadarBoat extends Ship 
{
	private static final long serialVersionUID = 189425481040172189L;
	private boolean aExtendedRadar;
	
	public RadarBoat(Account pPlayer, int pShipID) {
		
		super();
		aSize = 3;
		aMaxSpeed = 3;
		aHeavyArmour = false;
		aRadarRangeLength = 6;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 5;
		aCannonRangeWidth = 3;
		aTurn180 = true; 
		aShipID = pShipID;
		
		initializeShip(pPlayer);
		
		aExtendedRadar = false;
	}
	
	public void triggerRadar() {
		
		if (aExtendedRadar) {
			//extended radar is on, so turning it off
			aRadarRangeLength = 6;
		} else {
			//extended radar is off, so turning it on
			aRadarRangeLength = 12;
		}
		//and changing the boolean 
		aExtendedRadar = !aExtendedRadar;
	}
	//Somewhere in the compute radar range method: if(aExtendedRadar) {multiply radarRangeLength by 2}

	public int getActualSpeed() {
		if (aExtendedRadar) {
			return 0;
		}
		else {
			return aActualSpeed;
		}
	}
	/**
	 * Getter for extended radar 
	 * @return true if radar is on 
	 */
	public boolean isExtendedRadarOn() {
		return aExtendedRadar;
	}
}
