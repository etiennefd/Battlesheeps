package battlesheeps.networking;

import java.io.Serializable;

public class LobbyMessageToServer implements Serializable
{
	private static final long serialVersionUID = 3168081652129542229L;

	public enum LobbyNotification {
		ENTERING, EXITING
	}
	
	private LobbyNotification aEnterOrExit;
	private String aUsername;
	
	public LobbyMessageToServer(LobbyNotification pNote, String pUsername){
		aEnterOrExit = pNote;
		aUsername = pUsername;		
	}
	
	public LobbyNotification getEnterOrExit(){
		return aEnterOrExit;
	}

	public String getUsername(){
		return aUsername;
	}
	
}
