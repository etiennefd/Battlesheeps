package battlesheeps.test;

import java.util.List;

import battlesheeps.accounts.Account;
import battlesheeps.board.*;
import battlesheeps.client.ClientGame;
import battlesheeps.server.ServerGame;
import battlesheeps.ships.*;
import battlesheeps.ships.Ship.Damage;

public class TestGameBoard{

	private ClientGame client;
	private ServerGame myGame;
	
	public TestGameBoard() {
		
		Account player = new Account("Player", "abc");
		Account opponent = new Account("Opponent", "def");

		myGame = new ServerGame(1, player, opponent);
		
		List<Ship> p1ships = myGame.getP1ShipList();
		List<Ship> p2ships = myGame.getP2ShipList();
		
		//0-1 CRUISER, 2-4 DESTROYER, 5-6 TORPEDO, 7-8 MINE, 9 - RADAR
		
		//P1 ships 
		
		//cruisers 
		myGame.setShipPosition(p1ships.get(0), new Coordinate(5, 5), new Coordinate(5, 9));
		myGame.setShipPosition(p1ships.get(1), new Coordinate(21, 17), new Coordinate(17, 17));
		
		//destroyer 
		myGame.setShipPosition(p1ships.get(2), new Coordinate(7, 20), new Coordinate(4, 20));
		myGame.setShipPosition(p1ships.get(3), new Coordinate(4,10), new Coordinate(1, 10));
		myGame.setShipPosition(p1ships.get(4), new Coordinate(4, 11), new Coordinate(1, 11));
		
		//torpedo
		myGame.setShipPosition(p1ships.get(5), new Coordinate(20, 25), new Coordinate(20, 27));
		myGame.setShipPosition(p1ships.get(6), new Coordinate(3, 12), new Coordinate(1, 12));
		
		//mine 
		myGame.setShipPosition(p1ships.get(7), new Coordinate(4, 3), new Coordinate(5, 3));
		myGame.setShipPosition(p1ships.get(8), new Coordinate(2, 13), new Coordinate(1, 13));
		
		//radar
		myGame.setShipPosition(p1ships.get(9), new Coordinate(0, 3), new Coordinate(2, 3));
		
		//P2 ships 
		
		//cruisers 
		myGame.setShipPosition(p2ships.get(0), new Coordinate(24, 5), new Coordinate(24, 1));
		myGame.setShipPosition(p2ships.get(1), new Coordinate(25, 29), new Coordinate(29, 29));
		
		//destroyer 
		myGame.setShipPosition(p2ships.get(2), new Coordinate(26, 28), new Coordinate(29, 28));
		myGame.setShipPosition(p2ships.get(3), new Coordinate(26, 27), new Coordinate(29, 27));
		myGame.setShipPosition(p2ships.get(4), new Coordinate(26, 26), new Coordinate(29, 26));
		
		//torpedo
		myGame.setShipPosition(p2ships.get(5), new Coordinate(27, 25), new Coordinate(29, 25));
		myGame.setShipPosition(p2ships.get(6), new Coordinate(27, 24), new Coordinate(29, 24));
		
		//mine 
		myGame.setShipPosition(p2ships.get(7), new Coordinate(5, 0), new Coordinate(5, 1));
		myGame.setShipPosition(p2ships.get(8), new Coordinate(28, 23), new Coordinate(29, 23));
		
		//radar
		myGame.setShipPosition(p2ships.get(9), new Coordinate(4, 29), new Coordinate(6, 29));
		

		
		//Add some mines
		myGame.addMine(new Coordinate(7, 5));
		myGame.addMine(new Coordinate(3, 27));
		myGame.addMine(new Coordinate(22, 1));
		
		client = new ClientGame(player.getUsername(), myGame);
	
		
	}
	
	public static void main(String[] args) {
		new TestGameBoard();
	}

}