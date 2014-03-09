package battlesheeps.networking;

import java.io.Serializable;

public class LobbyMessageToServer implements Serializable
{
	private static final long serialVersionUID = 2098808939389356495L;

	public enum LobbyNotification {
		ENTERING, EXITING, GAME_REQUEST
	}
	private LobbyNotification aEnterOrExit;
	private String aUsername;
	private Request aRequest;

	public LobbyMessageToServer(LobbyNotification pNote, String pUsername, Request pRequest){
		aEnterOrExit = pNote;
		aUsername = pUsername;
		aRequest = pRequest;
	}
	
	public LobbyNotification getEnterOrExit(){
		return aEnterOrExit;
	}

	public Request getRequest(){
		return aRequest;
	}

	public String getUsername()
	{
		return aUsername;
	}
	
}
