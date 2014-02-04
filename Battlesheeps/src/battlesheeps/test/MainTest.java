package battlesheeps.test;

import battlesheeps.game.*;
import battlesheeps.game.Game.MoveType;
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
		
		Game g = new Game(0, null, null);
		
		Cruiser c = new Cruiser(null);
		g.setShipPosition(c, new Coordinate(5, 5), new Coordinate(5, 9));
		MineLayer m = new MineLayer(null);
		g.setShipPosition(m, new Coordinate(4, 3), new Coordinate(5, 3));
		g.addMine(new Coordinate(5, 1));
		System.out.println(g.printBoard());
		
		g.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(3, 3));
		System.out.println(g.printBoard());
		g.computeMoveResult(c, MoveType.TRANSLATE_SHIP, new Coordinate(5, 0));
		System.out.println(g.printBoard());
		g.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(3, 2));
		System.out.println(g.printBoard());
		g.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(4, 1));
		System.out.println(g.printBoard());
		g.computeMoveResult(c, MoveType.TURN_SHIP, new Coordinate(1, 6));
		System.out.println(g.printBoard());
	}

}
