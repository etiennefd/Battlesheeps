package battlesheeps.networking;

import java.io.Serializable;

public class ChatMessage implements Serializable
{
	private static final long serialVersionUID = -839714735900772921L;
	private String aMessage;
	private String aRecipient;
	
	public ChatMessage(String pMessage, String pRecipient){
		aMessage = pMessage;
		aRecipient = pRecipient;
	}
	
	public String getMessage(){
		return aMessage;
	}
	
	public String getRecipient(){
		return aRecipient;
	}
}
