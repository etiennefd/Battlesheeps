package battlesheeps.test;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import battlesheeps.accounts.Account;
import battlesheeps.board.*;
import battlesheeps.client.ClientGame;
import battlesheeps.networking.ClientChat;
import battlesheeps.networking.ClientGamesAndMoves;
import battlesheeps.networking.ServerGamesAndMoves;
import battlesheeps.server.GameManager;
import battlesheeps.server.LogEntry;
import battlesheeps.server.LogEntry.LogType;
import battlesheeps.server.ServerGame;
import battlesheeps.server.ServerGame.ClientInfo;
import battlesheeps.ships.*;
import battlesheeps.ships.Ship.Damage;

public class TestGameBoard{

	private ClientGame client;
	private ServerGame myGame;
	
	public TestGameBoard() {

		Account a1 = new Account("dave", "12345");
		Account a2 = new Account("bob", "password");
		Account a3 = new Account("dinkle", "IamAdinkle");
		Account a4 = new Account("bobs", "password");
		final Account player = new Account("Julius Caesar", "abc");
		Account opponent = new Account("Alexander the Great", "def");
		
		GameManager gm = GameManager.getInstance();
		Hashtable<String,Account> accts = gm.getAccounts();
		accts.put(a1.getUsername(), a1);
		accts.put(a2.getUsername(), a2);
		accts.put(a3.getUsername(), a3);
		accts.put(a4.getUsername(), a4);
		accts.put(player.getUsername(), player);
		accts.put(opponent.getUsername(), opponent);
		
		myGame = new ServerGame(1, gm.getAccount("Julius Caesar"), gm.getAccount("Alexander the Great"));
		myGame.setClientInfo(ClientInfo.GAME_UPDATE);
		gm.addGame(myGame);
		
		List<Ship> p1ships = myGame.getP1ShipList();
		List<Ship> p2ships = myGame.getP2ShipList();
		
		//0-1 CRUISER, 2-4 DESTROYER, 5-6 TORPEDO, 7-8 MINE, 9 RADAR, 10 KAMIKAZE
		
		//P1 ships 
		
		//cruisers 
		myGame.setShipPosition(p1ships.get(0), new Coordinate(5, 10), new Coordinate(1, 10));
		myGame.setShipPosition(p1ships.get(1), new Coordinate(5, 11), new Coordinate(1, 11));
		
		//destroyer 
		myGame.setShipPosition(p1ships.get(2), new Coordinate(4, 12), new Coordinate(1, 12));
		myGame.setShipPosition(p1ships.get(3), new Coordinate(4, 13), new Coordinate(1, 13));
		myGame.setShipPosition(p1ships.get(4), new Coordinate(4, 14), new Coordinate(1, 14));
		
		//torpedo
		myGame.setShipPosition(p1ships.get(5), new Coordinate(3, 15), new Coordinate(1, 15));
		myGame.setShipPosition(p1ships.get(6), new Coordinate(3, 16), new Coordinate(1, 16));
		
		//mine 
		myGame.setShipPosition(p1ships.get(7), new Coordinate(2, 17), new Coordinate(1, 17));
		myGame.setShipPosition(p1ships.get(8), new Coordinate(2, 18), new Coordinate(1, 18));
		
		//radar
		myGame.setShipPosition(p1ships.get(9), new Coordinate(3, 19), new Coordinate(1, 19));
		
		//kamikaze
		myGame.setShipPosition(p1ships.get(10), new Coordinate(0, 20), new Coordinate(0, 20));
		
		//P2 ships 
		
		//cruisers 
		myGame.setShipPosition(p2ships.get(0), new Coordinate(24, 10), new Coordinate(28, 10));
		myGame.setShipPosition(p2ships.get(1), new Coordinate(24, 11), new Coordinate(28, 11));
		
		//destroyer 
		myGame.setShipPosition(p2ships.get(2), new Coordinate(25, 12), new Coordinate(28, 12));
		myGame.setShipPosition(p2ships.get(3), new Coordinate(25, 13), new Coordinate(28, 13));
		myGame.setShipPosition(p2ships.get(4), new Coordinate(25, 14), new Coordinate(28, 14));
		
		//torpedo
		myGame.setShipPosition(p2ships.get(5), new Coordinate(26, 15), new Coordinate(28, 15));
		myGame.setShipPosition(p2ships.get(6), new Coordinate(26, 16), new Coordinate(28, 16));
		
		//mine 
		myGame.setShipPosition(p2ships.get(7), new Coordinate(27, 17), new Coordinate(28, 17));
		myGame.setShipPosition(p2ships.get(8), new Coordinate(27, 18), new Coordinate(28, 18));
		
		//radar
		myGame.setShipPosition(p2ships.get(9), new Coordinate(26, 19), new Coordinate(28, 19));
		
		//kamikaze
		myGame.setShipPosition(p2ships.get(10), new Coordinate(29, 20), new Coordinate(29, 20));

		
		//Add some mines
		myGame.addMine(new Coordinate(7, 5));
		myGame.addMine(new Coordinate(3, 27));
		myGame.addMine(new Coordinate(22, 1));
		
		System.out.println(myGame.printBoard());
		
		LogEntry log1 = new LogEntry(LogType.CANNON_MISS, 1, 1, 1);
		LogEntry log2 = new LogEntry(LogType.MINE_EXPLOSION, 24, 5, 2);
		LogEntry log3 = new LogEntry(LogType.TORPEDO_HIT_REEF, 15, 16, 3);
		
//		myGame.setLogEntry(log1);
//		myGame.setLogEntry(log2);
//		myGame.setLogEntry(log3);
		new Thread(new ServerGamesAndMoves()).start();
		
//		ServerGame game2 = new ServerGame(2, gm.getAccount("Julius Caesar"), gm.getAccount("Alexander the Great"));
//		game2.setClientInfo(ClientInfo.NEW_GAME);
//		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				client = new ClientGame(player.getUsername());
				
				ClientGamesAndMoves pManager = new ClientGamesAndMoves(player.getUsername(), null, 1, client);
				client.setupComplete();
			}
			
		});
		
//		
//		ClientGame clientO = new ClientGame(opponent.getUsername());
//		ClientGamesAndMoves oManager = new ClientGamesAndMoves(opponent.getUsername(), null, 2, clientO);
		
	}
	
	public static void main(String[] args) {
		new TestGameBoard();
	}

}

/* TODO
 * THIS SHOULD BE REMOVED AND REMPLACED WITH THE SETUP PHASE!
 * THIS IS JUST DEFAULT SHIP POSITIONS FOR THE DEMO
 */
//0-1 CRUISER, 2-4 DESTROYER, 5-6 TORPEDO, 7-8 MINE, 9 - RADAR
//P1 ships 

////cruisers 
//setShipPosition(aShipListP1.get(0), new Coordinate(5, 10), new Coordinate(1, 10));
//setShipPosition(aShipListP1.get(1), new Coordinate(5, 11), new Coordinate(1, 11));
//
////destroyer 
//setShipPosition(aShipListP1.get(2), new Coordinate(4, 12), new Coordinate(1, 12));
//setShipPosition(aShipListP1.get(3), new Coordinate(4, 13), new Coordinate(1, 13));
//setShipPosition(aShipListP1.get(4), new Coordinate(4, 14), new Coordinate(1, 14));
//
////torpedo
//setShipPosition(aShipListP1.get(5), new Coordinate(3, 15), new Coordinate(1, 15));
//setShipPosition(aShipListP1.get(6), new Coordinate(3, 16), new Coordinate(1, 16));
//
////mine 
//setShipPosition(aShipListP1.get(7), new Coordinate(2, 17), new Coordinate(1, 17));
//setShipPosition(aShipListP1.get(8), new Coordinate(2, 18), new Coordinate(1, 18));
//
////radar
//setShipPosition(aShipListP1.get(9), new Coordinate(3, 19), new Coordinate(1, 19));
//
////P2 ships 
//
////cruisers 
//setShipPosition(aShipListP2.get(0), new Coordinate(24, 10), new Coordinate(28, 10));
//setShipPosition(aShipListP2.get(1), new Coordinate(24, 11), new Coordinate(28, 11));
//
////destroyer 
//setShipPosition(aShipListP2.get(2), new Coordinate(25, 12), new Coordinate(28, 12));
//setShipPosition(aShipListP2.get(3), new Coordinate(25, 13), new Coordinate(28, 13));
//setShipPosition(aShipListP2.get(4), new Coordinate(25, 14), new Coordinate(28, 14));
//
////torpedo
//setShipPosition(aShipListP2.get(5), new Coordinate(26, 15), new Coordinate(28, 15));
//setShipPosition(aShipListP2.get(6), new Coordinate(26, 16), new Coordinate(28, 16));
//
////mine 
//setShipPosition(aShipListP2.get(7), new Coordinate(27, 17), new Coordinate(28, 17));
//setShipPosition(aShipListP2.get(8), new Coordinate(27, 18), new Coordinate(28, 18));
//
////radar
//setShipPosition(aShipListP2.get(9), new Coordinate(26, 19), new Coordinate(28, 19));
