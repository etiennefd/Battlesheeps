package battlesheeps.server;

import java.util.Hashtable;

import battlesheeps.accounts.Account;

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
	private static GameManager INSTANCE;
	
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
		if (INSTANCE == null){
			synchronized (GameManager.class){
				if (INSTANCE == null){
					INSTANCE = new GameManager();
				}
			}
		}
		return INSTANCE;
	}
	
	public Hashtable<String, Account> getAccounts()
	{
		return ALL_ACCOUNTS;
	}
	
	public Account getAccount(String pUsername){
		return ALL_ACCOUNTS.get(pUsername);
	}
	/**
	 * @param pGameID The ID of the game you want.
	 * @return The game OR NULL if game not found.
	 */
	public ServerGame getGame(int pGameID){
		return ALL_GAMES.get(pGameID);
	}
	
	public void addGame(ServerGame pGame){
		ALL_GAMES.put(pGame.getGameID(), pGame);
	}
	
	public int generateGameID(){
		// TODO make this more better-er.
		return (int) (Math.random() * 1001) + 1;
	}

}
