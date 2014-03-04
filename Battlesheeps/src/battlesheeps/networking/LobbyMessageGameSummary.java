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
	
	public LobbyMessageGameSummary(int pGameID, Account pPlayer1, Account pPlayer2, int pTurnNum, Date pDate){
		aGameID = pGameID;
		aPlayer1 = pPlayer1;
		aPlayer2 = pPlayer2;
		aTurnNum = pTurnNum;
		aDateLastPlayed = pDate;
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
	
}
