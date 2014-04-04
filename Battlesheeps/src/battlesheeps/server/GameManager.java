package battlesheeps.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import battlesheeps.accounts.Account;

/**
 * This is the class that creates games (ServerGame) and holds them in a list. 
 * It will probably have to receive input from the client (game requests, moves, chat messages). 
 * Moves will be sent to the appropriate ServerGame, identified by a gameID. 
 *
 */
public class GameManager 
{
	private static final GameManager INSTANCE = new GameManager();
	private static Hashtable<Integer, ServerGame> ALL_GAMES;
	private static Hashtable<String, Account> ALL_ACCOUNTS;
	private static int gameIDcounter;
	
//	public static void main(String[] args)
//	{
//		Account a1 = new Account("dave", "12345");
//		Account a2 = new Account("bob", "password");
//		Account a3 = new Account("dinkle", "IamAdinkle");
//		Account a4 = new Account("bobs", "password");
//		
//		GameManager gm = GameManager.getInstance();
//		ServerGame g1 = new ServerGame(gm.generateGameID(), a1, a2);
//		ServerGame g2 = new ServerGame(gm.generateGameID(), a2, a3);
//		ServerGame g3 = new ServerGame(gm.generateGameID(), a3, a1);
//		
//		Hashtable<String,Account> accts = gm.getAccounts();
//		accts.put(a1.getUsername(), a1);
//		accts.put(a2.getUsername(), a2);
//		accts.put(a3.getUsername(), a3);
//		accts.put(a4.getUsername(), a4);
//		
//		gm.addGame(g1);
//		gm.addGame(g2);
//		gm.addGame(g3);
//		
//		GameManager gm = GameManager.getInstance();
//		Hashtable<String,Account> accts = gm.getAccounts();
//		for (String s : accts.keySet()){
//			System.out.println(accts.get(s).toString());
//		}
//		for (int i=1; i<4; i++){
//			System.out.println(gm.getGame(i).toString());
//		}
//		gm.close();
//	}
	
	private GameManager()
	{
//		gameIDcounter = 0;
//		ALL_GAMES = new Hashtable<Integer, ServerGame>();
//		ALL_ACCOUNTS = new Hashtable<String, Account>();
		try
		{
			InputStream file = new FileInputStream("allGames.ser");
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);
			
			try {
				gameIDcounter = input.readInt();
				ALL_GAMES = (Hashtable<Integer, ServerGame>) input.readObject();
			}
			finally {
				input.close();
			}
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		
		try
		{
			InputStream file = new FileInputStream("allAccounts.ser");
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);
			
			try {
				ALL_ACCOUNTS = (Hashtable<String, Account>) input.readObject();
			}
			finally {
				input.close();
			}
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		catch (ClassNotFoundException e){
			e.printStackTrace();
		}
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
	
	public void saveToFile(){
		// TODO ensure file is overwritten
		try{
			OutputStream file = new FileOutputStream("allGames.ser", false);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			
			try {
				output.writeInt(gameIDcounter);
				output.writeObject(ALL_GAMES);
			}
			finally {
				output.close();
			}
		}  
		catch(IOException e){
			System.err.println("Error saving ALL_GAMES: " + e);
		}
		try{
			OutputStream file = new FileOutputStream("allAccounts.ser", false);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			
			try {
				output.writeObject(ALL_ACCOUNTS);
			}
			finally {
				output.close();
			}
		}  
		catch(IOException e){
			System.err.println("Error saving ALL_ACCOUNTS: " + e);
		}
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
	
	public void endGame(int pGameID){
		ServerGame ending = ALL_GAMES.get(pGameID);
		
		Account p1 = ALL_ACCOUNTS.get(ending.getP1Username());
		Account p2 = ALL_ACCOUNTS.get(ending.getP2Username());
		
		p1.removeFinishedGame(pGameID);
		p2.removeFinishedGame(pGameID);
		
		if (ending.getWinnerName().equals(p1.getUsername())){
			p1.incrementGamesWon();
			p2.incrementGamesLost();
		} 
		else {
			p1.incrementGamesLost();
			p2.incrementGamesWon();
		}
		
		ALL_GAMES.remove(pGameID);
	}
	
	public int generateGameID(){
		return ++gameIDcounter;
	}

}
