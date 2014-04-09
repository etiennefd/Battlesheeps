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
		
		//This launches the server:
		server.start();
	}

}
