package battlesheeps.test;

import battlesheeps.client.ClientGame;
import battlesheeps.networking.ClientGamesAndMoves;

public class TestClient {
	
	public static void main(String[] args) {
		ClientGame cl = new ClientGame("opponent");
		ClientGamesAndMoves manager = new ClientGamesAndMoves("opponent", null, 1, cl); 
	}
}
