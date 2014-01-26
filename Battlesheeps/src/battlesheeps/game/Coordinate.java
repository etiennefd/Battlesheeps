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

	public static Coordinate randomCoord(int pXmin, int pXmax, int pYmin, int pYmax) {
		int rX = pXmin + (int) (Math.random() * ((pXmax - pXmin) + 1));
		int rY = pYmin + (int) (Math.random() * ((pYmax - pYmin) + 1));
		return new Coordinate(rX, rY);
	}
}
