package battlesheeps.game;

import battlesheeps.board.*;

public class Game {

	public enum Visible {
		COVERED_BY_RADAR, COVERED_BY_SONAR, NOT_COVERED
	}
	
	private int aGameID;
	private String aPlayer1;
	private String aPlayer2;
	private int aTurnNum;
	private String aDateLastPlayed;
	
	private Square[][] aBoard;
	
	public Game() {
		
		//Filling the board with sea
		for (int i = 0; i<aBoard.length; i++) {
			for (int j = 0 ; j<aBoard[i].length; j++) {
				if (i >= 10 && i < 20 && (j==0 || j==29)) {
					aBoard[i][j] = new BaseSquare();
				}
				else {
					aBoard[i][j] = new Sea();
				}
			}
		}
		
		//Generating 24 random coral reefs within the center zone
		for (int i = 0; i<24; i++) {
			Coordinate newCoral = Coordinate.randomCoord(10, 19, 3, 26);
			if (aBoard[newCoral.getX()][newCoral.getY()] instanceof CoralReef) {
				//We are trying to put a coral reef where there is already one. Just ignore this loop iteration. 
				i--;
			}
			else {
				aBoard[newCoral.getX()][newCoral.getY()] = new CoralReef();
			}
		}
	}
	
	
	public String printBoard() {
		String s = "";
		for (int i = 0; i<aBoard.length; i++) {
			for (int j = 0 ; j<aBoard[i].length; j++) {
				s = s + aBoard[i][j].toString();
			}
			s = s + "\n";
		}
		return s;
	}

}
