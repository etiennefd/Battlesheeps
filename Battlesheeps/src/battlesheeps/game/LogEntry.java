package battlesheeps.game;

import battlesheeps.game.Coordinate;

public class LogEntry {

	private Coordinate aCoordinate;
	private String aMessage;
	
	public LogEntry(String pMessage, Coordinate pCoordinate){
		aCoordinate = pCoordinate;
		aMessage = pMessage;
	}
	
	public Coordinate getCoordinate(){
		return aCoordinate;
	}
	
	public String getMessage(){
		return aMessage;
	}
}
