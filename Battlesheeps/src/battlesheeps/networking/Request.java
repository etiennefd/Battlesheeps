package battlesheeps.networking;

import java.io.Serializable;

public class Request implements Serializable
{
	private static final long serialVersionUID = 8695308489354622466L;

	public enum LobbyRequest {
		REQUEST, REQUEST_WITHDRAW, ACCEPT, DECLINE
	}
	
	private LobbyRequest aType;
	private String aRequesterName;
	private String aRequestee;
	
	public Request(LobbyRequest pRequest, String pRequester, String pRequestee){
		aType = pRequest;
		aRequesterName = pRequester;
		aRequestee = pRequestee;
	}

	public LobbyRequest getType()
	{
		return aType;
	}

	public String getRequesterName()
	{
		return aRequesterName;
	}

	public String getRequestee()
	{
		return aRequestee;
	}
	
	public boolean equals(Object p1){
		if( p1 == null )
		{
			return false;
		}
		if( p1 == this )
		{
			return true;
		}
		if( p1.getClass() != getClass() )
		{
			return false;
		}
		return this.aRequestee.equals(((Request)p1).getRequestee()) &&
				this.aRequesterName.equals(((Request)p1).getRequesterName());
	}
	public int hashCode(){
		return (this.aRequestee.hashCode()+1)*this.aRequesterName.hashCode();
	}
}


