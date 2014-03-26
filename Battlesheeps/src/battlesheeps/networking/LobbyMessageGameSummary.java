package battlesheeps.networking;

import java.io.Serializable;
import java.util.Date;

import battlesheeps.accounts.Account;

public class LobbyMessageGameSummary implements Serializable
{
	private static final long serialVersionUID = 3664851381843039181L;
	
	private int aGameID;
	private Account aPlayer1;
	private Account aPlayer2;
	private int aTurnNum;			//Odd -> it is P1's turn. Even -> it is P2's turn.  
	private Date aDateLastPlayed;
	private int aMyGame = 0; 		//for proper list display in Lobby, 1 if it's P1's Lobby, 2 if it's P2's
	
	public LobbyMessageGameSummary(int pGameID, Account pPlayer1, Account pPlayer2, int pTurnNum, Date pDate){
		aGameID = pGameID;
		aPlayer1 = pPlayer1;
		aPlayer2 = pPlayer2;
		aTurnNum = pTurnNum;
		aDateLastPlayed = pDate;
	}
	
	public int getMyGame() {
		return aMyGame;
	}

	public void setMyGame(int aMyGame) {
		this.aMyGame = aMyGame;
	}

	//	return 1 if player 1, 2 if player 2, 0 if neither
  	public int hasPlayer(Account pAccount)
	{
		if(pAccount.equals(aPlayer1))
		{
			return 1;
		}
		else if(pAccount.equals(aPlayer2))
		{
			return 2;
		}
		else
		{
			return 0;
		}
	}
	
	public int getGameID()	{
		return aGameID;
	}

	public Account getPlayer1(){
		return aPlayer1;
	}

	public Account getPlayer2(){
		return aPlayer2;
	}

	public int getTurnNum(){
		return aTurnNum;
	}

	public Date getDateLastPlayed(){
		return aDateLastPlayed;
	}
	@Override
	public String toString()
	{
		Account opponent = aPlayer1; 	//displaying in Player2's Lobby
		if(aMyGame ==1) 				//displaying in Player1's Lobby
		{
			opponent = aPlayer2;
		}
		return "<html>" + opponent.toString() + "<br>Turn " + aTurnNum + "<br>" +
					"Last turn played on " + aDateLastPlayed;
	}
}
