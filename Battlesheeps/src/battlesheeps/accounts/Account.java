package battlesheeps.accounts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Account implements Serializable
{
	private static final long serialVersionUID = 8547702498479534517L;

	public enum Status {
		OFFLINE, IN_GAME, AVAILABLE
	}
	
	private String aUsername;
	private String aPassword;

	private Status aAvailability; 
	private int aNumGamesWon; 
	private int aNumGamesLost;
	
	private ArrayList<Integer> aCurrentGames;
	
	public Account(String pUsername, String pPassword) {
		aUsername = pUsername; 
		aPassword = pPassword;
		aAvailability = Status.OFFLINE;
		aNumGamesWon = 0; 
		aNumGamesLost = 0; 
		aCurrentGames = new ArrayList<Integer>();
	}
	
	/* GETTERS */
	public String getUsername() {
		return aUsername;
	}
	
	public Status getAvailability(){
		return aAvailability;
	}
	
	public String getPassword(){
		return aPassword;
	}
	
	public int getNumGamesWon(){
		return aNumGamesWon;
	}
	
	public int getNumGamesLost(){
		return aNumGamesLost;
	}
	
	public Iterator<Integer> getCurrentGames(){
		return aCurrentGames.iterator();
	}
	
	public ArrayList<Integer> getCurrentGamesList(){
		return aCurrentGames;
	}
	
	/* SETTERS */
	public void setAvailability(Status pAvailability){
		aAvailability = pAvailability;
	}
	
	public boolean addNewGame(int aGame){
		return aCurrentGames.add(aGame);
	}
	
	public void removeFinishedGame(int aGame){
		aCurrentGames.remove(aGame);
	}
	
	public String userString()
	{
		return aUsername + " (" + aNumGamesWon + " : " + aNumGamesLost + ")";
	}
}
