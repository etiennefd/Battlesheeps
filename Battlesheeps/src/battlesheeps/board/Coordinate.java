package battlesheeps.board;

import java.io.Serializable;

public class Coordinate implements Serializable
{
	private static final long serialVersionUID = 8467959046333824976L;
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

	/*
	 * Returns true if X and Y are between 0 and 29, inclusive. 
	 */
	public boolean inBounds() {
	
		if (this.x >= 0 && this.x <= 29) {
			if (this.y >= 0 && this.y <= 29) {
				return true;
			}
		}
		
		return false;
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
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true; 
		if (!(o instanceof Coordinate)) return false;
		Coordinate c = (Coordinate) o;
		if (c.x == this.x && c.y == this.y) return true;
		else return false;
	}
	public int hashCode(){
		return (x+1)*(y+100);
	}
}
