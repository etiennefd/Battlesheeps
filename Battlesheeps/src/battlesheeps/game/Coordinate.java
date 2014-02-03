package battlesheeps.game;

public class Coordinate {

	private int x;
	private int y;
	
	public Coordinate (int pX, int pY) {
		x = pX;
		y = pY;
	}
	
	public int getX () {
		return x;
	}
	
	public int getY () {
		return y;
	}

	/**
	 * Generates a new Coordinate whose x and y components are randomly selected within a range. 
	 * There is a range for x and a range for y. 
	 * @param pXmin 
	 * @param pXmax
	 * @param pYmin
	 * @param pYmax
	 * @return
	 */
	public static Coordinate randomCoord(int pXmin, int pXmax, int pYmin, int pYmax) {
		int rX = pXmin + (int) (Math.random() * ((pXmax - pXmin) + 1));
		int rY = pYmin + (int) (Math.random() * ((pYmax - pYmin) + 1));
		return new Coordinate(rX, rY);
	}
}
