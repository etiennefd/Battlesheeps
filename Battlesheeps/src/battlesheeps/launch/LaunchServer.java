package battlesheeps.launch;

import java.util.Hashtable;
import java.util.List;

import battlesheeps.accounts.Account;
import battlesheeps.networking.Server;
import battlesheeps.server.GameManager;
import battlesheeps.server.ServerGame;
import battlesheeps.server.ServerGame.ClientInfo;
import battlesheeps.ships.Ship;

public class LaunchServer {

	/**
	 * Creates and launches the server. 
	 */
	public static void main(String[] args) {
		
		Server server = new Server(); 
		
		//To add any pre-defined accounts or games to the server, add them here before calling start().
		
		//For the acceptance test, we define the following accounts: 
		Account master = new Account("master", "master");//has a saved game with Accounts end, base, and mines 
		Account end = new Account("end", "end");//to demonstrate the end game and winning/losing
		Account base = new Account("base", "base");//to demonstrate destroying the base
		Account mines = new Account("mines", "mines");//to demonstrate exploding/picking up mines
		
		GameManager gm = GameManager.getInstance();
		Hashtable<String,Account> accts = gm.getAccounts();
		accts.put(master.getUsername(), master);
		accts.put(end.getUsername(), end);
		accts.put(base.getUsername(), base);
		accts.put(mines.getUsername(), mines);
		
		//For the acceptance test, we define the following games: 
		ServerGame gameEnd = new ServerGame(1, gm.getAccount("master"), gm.getAccount("end"));
		gameEnd.setClientInfo(ClientInfo.GAME_UPDATE);
		gm.addGame(gameEnd);
		ServerGame gameBase = new ServerGame(2, gm.getAccount("master"), gm.getAccount("base"));
		gameBase.setClientInfo(ClientInfo.GAME_UPDATE);
		gm.addGame(gameBase);
		ServerGame gameMines = new ServerGame(3, gm.getAccount("master"), gm.getAccount("mines"));
		gameMines.setClientInfo(ClientInfo.GAME_UPDATE);
		gm.addGame(gameMines);
		
		//We modify the games
		//TODO
		List<Ship> p1ships = gameEnd.getP1ShipList();
		List<Ship> p2ships = gameEnd.getP2ShipList();
		
		//This launches the server:
		server.start();
	}

}
