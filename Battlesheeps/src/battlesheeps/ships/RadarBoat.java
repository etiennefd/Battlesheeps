package battlesheeps.ships;

public class RadarBoat extends Ship {
	
	private boolean aExtendedRadar;
	
	public RadarBoat() {
		
		super();
		aSize = 3;
		aMaxSpeed = 3;
		aHeavyArmour = false;
		aRadarRangeLength = 6;
		aRadarRangeWidth = 3;
		aCannonRangeLength = 5;
		aCannonRangeWidth = 3;
		aTurnPoint = 1;	
		
		initializeShip();
		
		aExtendedRadar = false;
	}
	
	public void triggerRadar() {
		aExtendedRadar = !aExtendedRadar;
	}
	
	//Somewhere in the compute radar range method: if(aExtendedRadar) {multiply radarRangeLength by 2}
}
