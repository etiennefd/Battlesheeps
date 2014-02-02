package battlesheeps.test;

import battlesheeps.game.*;
import battlesheeps.game.Game.MoveType;
import battlesheeps.ships.*;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Game g = new Game(0, null, null);
		
		Cruiser c = new Cruiser();
		g.setShipPosition(c, new Coordinate(5, 5), new Coordinate(5, 9));
		MineLayer m = new MineLayer();
		g.setShipPosition(m, new Coordinate(4, 3), new Coordinate(5, 3));
		g.addMine(new Coordinate(5, 1));
		System.out.println(g.printBoard());
		
		g.computeMoveResult(m, MoveType.TRANSLATE_SHIP, new Coordinate(3, 3));
		g.computeMoveResult(c, MoveType.TRANSLATE_SHIP, new Coordinate(5, 2));
		System.out.println(g.printBoard());
	}

}
