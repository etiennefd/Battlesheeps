package battlesheeps.test;

import javax.swing.SwingUtilities;

import battlesheeps.client.ClientGame;
import battlesheeps.networking.ClientGamesAndMoves;

public class TestClient {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ClientGame cl = new ClientGame("q");
				ClientGamesAndMoves manager = new ClientGamesAndMoves("q", null, 2, cl);
//				cl.setupComplete();
			}
			
		});
		 
	}
}
