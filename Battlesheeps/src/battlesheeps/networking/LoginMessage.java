package battlesheeps.networking;

import java.io.Serializable;

public class LoginMessage implements Serializable
{
	private static final long serialVersionUID = -5131835081795523079L;

	public enum LoginType {
		CREATE, LOGIN
	}
	
	private LoginType aType;
	private String aLogin;
	private String aPassword;
	
	public LoginMessage(LoginType pType, String pLogin, String pPassword)
	{
		aType = pType;
		aLogin = pLogin;
		aPassword = pPassword;
	}
	
	public LoginType getType()
	{
		return aType;
	}
	
	public String getLogin()
	{
		return aLogin;
	}
	
	public String getPassword()
	{
		return aPassword;
	}
	
}
