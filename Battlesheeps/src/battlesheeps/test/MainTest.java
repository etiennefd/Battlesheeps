package battlesheeps.test;

import battlesheeps.board.Coordinate;
import battlesheeps.server.*;
import battlesheeps.server.ServerGame.MoveType;
import battlesheeps.ships.*;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "Legend: \n" +
				"~  - Empty sea square\n" +
				"XX - Coral reef\n" +
				"B  - Base\n" +
				"C  - Cruiser\n" +
				"D  - Destroyer\n" +
				"T  - Torpedo boat\n" +
				"M  - Mine layer\n" +
				"R  - Radar boat\n" +
				"## - Mine\n" +
				" 2 - Undamaged\n" +
				" 1 - Damaged (heavy armored ships only)\n" +
				" 0 - Destroyed\n" +
				"Lower case indicates the head of a ship\n";
		System.out.println(s);
		
		ServerGame game = new ServerGame(0, null, null);
		
		//Add some ships and set their position
		Cruiser c = new Cruiser(null);
		game.setShipPosition(c, new Coordinate(5, 5), new Coordinate(5, 9));
		MineLayer m = new MineLayer(null);
		game.setShipPosition(m, new Coordinate(4, 3), new Coordinate(5, 3));
		RadarBoat r = new RadarBoat(null);
		game.setShipPosition(r, new Coordinate(0, 3), new Coordinate(2, 3));
		Destroyer d = new Destroyer(null);
		game.setShipPosition(d, new Coordinate(7, 25), new Coordinate(4, 25));
		TorpedoBoat t = new TorpedoBoat(null);
		game.setShipPosition(t, new Coordinate(26, 25), new Coordinate(26, 27));
		Cruiser c2 = new Cruiser(null);
		game.setShipPosition(c2, new Coordinate(24, 5), new Coordinate(24, 1));
		MineLayer m2 = new MineLayer(null);
		game.setShipPosition(m2, new Coordinate(5, 0), new Coordinate(5, 1));
		//Add some mines
		game.addMine(new Coordinate(7, 5));
		game.addMine(new Coordinate(3, 27));
		game.addMine(new Coordinate(22, 1));
		
		System.out.println(game.printBoard());
		
		//Some moves
		game.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(3, 3));
		System.out.println(game.printBoard());
		game.computeMoveResult(c, MoveType.TRANSLATE_SHIP, new Coordinate(5, 0));
		System.out.println(game.printBoard());
		game.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(3, 2));
		System.out.println(game.printBoard());
		game.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(4, 1));
		System.out.println(game.printBoard());
		game.computeMoveResult(d, MoveType.TURN_SHIP, new Coordinate(4, 28));
		System.out.println(game.printBoard());
		game.computeMoveResult(r, MoveType.TURN_SHIP, new Coordinate(1, 2));
		System.out.println(game.printBoard());
		game.computeMoveResult(r, MoveType.TURN_SHIP, new Coordinate(1, 4));
		System.out.println(game.printBoard());
		game.computeMoveResult(d, MoveType.FIRE_CANNON, new Coordinate(3, 27));
		System.out.println(game.printBoard());
		game.computeMoveResult(m, MoveType.PICKUP_MINE, new Coordinate(5, 1));
		System.out.println(game.printBoard());
		game.computeMoveResult(r, MoveType.FIRE_CANNON, new Coordinate(3, 1));
		System.out.println(game.printBoard());
		game.computeMoveResult(c, MoveType.FIRE_CANNON, new Coordinate(4, 1));
		System.out.println(game.printBoard());
		game.computeMoveResult(r, MoveType.FIRE_CANNON, new Coordinate(3, 1));
		System.out.println(game.printBoard());
		game.computeMoveResult(t, MoveType.TRANSLATE_SHIP, new Coordinate(25, 25));
		System.out.println(game.printBoard());
		game.computeMoveResult(c2, MoveType.TURN_SHIP, new Coordinate(20, 1));
		System.out.println(game.printBoard());
		game.computeMoveResult(c, MoveType.TURN_SHIP, new Coordinate(9, 6));
		System.out.println(game.printBoard());
	}

}
