package battlesheeps.networking;

import java.io.Serializable;
import java.util.ArrayList;

import battlesheeps.accounts.Account;

public class LobbyMessageToClient implements Serializable
{
	private static final long serialVersionUID = -2773499275423309266L;
	
	private ArrayList<LobbyMessageGameSummary> aGames;
	private ArrayList<Account> aOnlineAccounts;
	
	public LobbyMessageToClient(ArrayList<Account> pOnlineAccounts){
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
}
