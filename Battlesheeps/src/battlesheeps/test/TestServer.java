package battlesheeps.test;

import battlesheeps.networking.ServerGamesAndMoves;

public class TestServer {
//opens Server
	public static void main(String[] args) {
		new Thread(new ServerGamesAndMoves()).start();
	}
}
