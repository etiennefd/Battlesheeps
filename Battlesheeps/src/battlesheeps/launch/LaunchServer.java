package battlesheeps.launch;

import battlesheeps.networking.Server;

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
