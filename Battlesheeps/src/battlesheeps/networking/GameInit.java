package battlesheeps.networking;

import java.io.Serializable;

public class GameInit implements Serializable
{
	private static final long serialVersionUID = -1384303561217977304L;
	private String aUsername;
	private String aOpponent;
	private int aGameID;
	
	public GameInit(String pUsername, String pOpponent, int pGameID){
		aUsername = pUsername;
		aOpponent = pOpponent;
		aGameID = pGameID;
	}

	public String getUsername(){
		return aUsername;
	}

	public int getGameID(){
		return aGameID;
	}

	public String getOpponent(){
		return aOpponent;
	}
	
}
