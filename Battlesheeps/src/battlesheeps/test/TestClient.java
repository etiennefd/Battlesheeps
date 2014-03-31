package battlesheeps.test;

import battlesheeps.client.ClientGame;
import battlesheeps.networking.ClientGamesAndMoves;

public class TestClient {
	
	public static void main(String[] args) {
		ClientGame cl = new ClientGame("a");
		cl.addBoard("a");
		cl.setupComplete();
		ClientGamesAndMoves manager = new ClientGamesAndMoves("a", null, 1, cl); 
	}
}
