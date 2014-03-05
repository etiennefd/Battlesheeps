package battlesheeps.networking;

import java.io.Serializable;
import java.util.ArrayList;

import battlesheeps.accounts.Account;

public class LobbyMessageToClient implements Serializable
{
	private static final long serialVersionUID = -6541532121428369798L;
	private ArrayList<LobbyMessageGameSummary> aGames;
	private ArrayList<Account> aOnlineAccounts;
	private Request aRequest;
	
	public LobbyMessageToClient(ArrayList<Account> pOnlineAccounts, Request pRequest){
		aRequest = pRequest;
		aOnlineAccounts = pOnlineAccounts;
	}
	
	public ArrayList<LobbyMessageGameSummary> getGames(){
		return aGames;
	}

	public ArrayList<Account> getOnlineAccounts(){
		return aOnlineAccounts;
	}
	
	public void setGames(ArrayList<LobbyMessageGameSummary> pGames){
		aGames = pGames;
	}

	public Request getRequest(){
		return aRequest;
	}

}
