package battlesheeps.test;

import battlesheeps.game.*;
import battlesheeps.ships.*;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Game g = new Game(0, null, null);
		Cruiser cruiser = new Cruiser();
		g.setShipPosition(cruiser, new Coordinate(1, 1), new Coordinate(1, 5));
		System.out.println(g.printBoard());
	}

}
