package battlesheeps.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import battlesheeps.server.GameManager;

public class Server
{
	private Thread[] threads = new Thread[4];
	/**
	 * The Server should be started from the main method in this class. If you desire to 
	 * add a game to the gameManager manually (hard-coded) do so in this main method before
	 * creating the server. This process should ONLY be ended by hitting enter in the console.
	 * Terminating the process by any other means (hitting eclipse's red button) will cause
	 * any progress to be lost.
	 */
	public Server()
	{
		threads[0] = new Thread(new ServerLogin());
		threads[1] = new Thread(new ServerLobby());
		threads[2] = new Thread(new ServerGamesAndMoves());
		threads[3] = new Thread(new ServerChat());
	}
	
	public void start(){
		GameManager gm = GameManager.getInstance();
		
		threads[0].start();
		threads[1].start();
		threads[2].start();
		threads[3].start();
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    	try{
    		stdIn.readLine();				// Server will wait here
    		
    		System.out.println("closing");
    		gm.saveToFile();
    		System.exit(0);
    	}
    	catch (IOException e1){
    		gm.saveToFile();
    		System.exit(1);
    	}
	}
	public static void main(String[] args)
	{
		new Server().start();
	}
	
}
