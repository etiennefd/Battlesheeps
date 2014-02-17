package battlesheeps.server;

import java.util.ArrayList;
import java.util.Hashtable;

import battlesheeps.accounts.Account;
import battlesheeps.accounts.Account.Status;

/**
 * This is the class that creates games (ServerGame) and holds them in a list. 
 * It will probably have to receive input from the client (game requests, moves, chat messages). 
 * Moves will be sent to the appropriate ServerGame, identified by a gameID. 
 * @author etienne
 *
 */
public class GameManager {
	private static Hashtable<Integer, ServerGame> ALL_GAMES;
	private static Hashtable<String, Account> ALL_ACCOUNTS;
	private static final GameManager INSTANCE = new GameManager();
	
	private GameManager()
	{
		ALL_GAMES = new Hashtable<Integer, ServerGame>();
		// TODO create ALL_GAMES from file
		ALL_ACCOUNTS = new Hashtable<String, Account>();
		// TODO create ALL_ACCOUNTS from file
	}
	/**
	 * Support for singleton
	 * @return The only instance of GameManager
	 */
	public static GameManager getInstance()
	{
		return INSTANCE;
	}
	
	public Hashtable<String, Account> getAccounts()
	{
		return ALL_ACCOUNTS;
	}
	/**
	 * @param aGameID The ID of the game you want.
	 * @return The game OR NULL if game not found.
	 */
	public ServerGame getGame(int aGameID){
		return ALL_GAMES.get(aGameID);
	}

	public ArrayList<Account> getOnlineUsers(){
		ArrayList<Account> list = new ArrayList<Account>();
		for (Account acct : ALL_ACCOUNTS.values()){
			if (acct.getAvailability() == Status.AVAILABLE){
				list.add(acct);
			}
		}
		return list;
	}
}
