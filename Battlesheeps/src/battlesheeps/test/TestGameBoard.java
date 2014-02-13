package battlesheeps.test;

import org.minueto.handlers.MinuetoKeyboard;
import org.minueto.handlers.MinuetoKeyboardHandler;

import battlesheeps.accounts.Account;
import battlesheeps.board.*;
import battlesheeps.client.ClientGame;
import battlesheeps.server.ServerGame;
import battlesheeps.ships.*;
import battlesheeps.ships.Ship.Damage;

public class TestGameBoard implements MinuetoKeyboardHandler{

	private Square[][] startBoard;
	private Square[][] boardAfterMove;
	private ClientGame client;
	private ServerGame myGame;
	
	public TestGameBoard() {
		
		Account player = new Account("Player", "abc");
		Account opponent = new Account("Opponent", "def");
		
		startBoard = new Square[30][30];
		boardAfterMove = new Square[30][30];
		
		//first filling the boards with ocean 
		Sea seaSquare = new Sea();
		for (int i = 0; i <30; i++) {
			for (int j = 0; j < 30; j++) {
				startBoard[i][j] = seaSquare;
				boardAfterMove[i][j] = seaSquare;
			}
		}
		
		//now adding some coral reefs (same to both boards) 
		
		CoralReef reefSquare = new CoralReef();
		startBoard[5][5] = reefSquare;
		boardAfterMove[5][5] = reefSquare;
		startBoard[10][10] = reefSquare;
		boardAfterMove[10][10] = reefSquare;
		startBoard[15][15] = reefSquare;
		boardAfterMove[15][15] = reefSquare;
		startBoard[25][25] = reefSquare;
		boardAfterMove[25][25] = reefSquare;
		startBoard[18][7] = reefSquare;
		boardAfterMove[18][7] = reefSquare;
		
		//now adding two ships to the startBoard
		
		Ship ship1 = new MineLayer(player);
		Ship ship2 = new MineLayer(player);
		
		ship1.setLocation(new Coordinate(6,1), new Coordinate(5,1));
		ship2.setLocation(new Coordinate(20,20), new Coordinate(20,21));
		
		ShipSquare shipSquareHead1 = new ShipSquare(ship1, Damage.UNDAMAGED, true);
		ShipSquare shipSquareTail1 = new ShipSquare(ship1, Damage.UNDAMAGED, false);
		startBoard[6][1] = shipSquareHead1;
		startBoard[5][1] = shipSquareTail1;
		
		ShipSquare shipSquareHead2 = new ShipSquare(ship1, Damage.UNDAMAGED, true);
		ShipSquare shipSquareTail2 = new ShipSquare(ship1, Damage.UNDAMAGED, false);
		startBoard[20][20] = shipSquareHead2;
		startBoard[20][21] = shipSquareTail2;
		
		myGame = new ServerGame(1, player, opponent);
		client = new ClientGame(player.getUsername(), myGame);
		
		//now making some changes 
		
		//moving ship 1
		ship1.setLocation(new Coordinate(10,1), new Coordinate(9,1));
		shipSquareHead1 = new ShipSquare(ship1, Damage.UNDAMAGED, true);
		boardAfterMove[10][1] = shipSquareHead1;
		boardAfterMove[9][1] = shipSquareTail1;
		
		//damaging ship 2
		shipSquareHead2 = new ShipSquare(ship1, Damage.DESTROYED, true);
		shipSquareTail2 = new ShipSquare(ship1, Damage.UNDAMAGED, false);
		boardAfterMove[20][20] = shipSquareHead2;
		boardAfterMove[20][21] = shipSquareTail2;
		
		
	}
	
	public static void main(String[] args) {
		new TestGameBoard();
	}

	@Override
	   public void handleKeyPress(int value) {

	      switch(value) {

	         case MinuetoKeyboard.KEY_Q:
	            System.exit(0);
	            break;
	         case MinuetoKeyboard.KEY_Z: 
	        	client.updateGame(myGame);
	         default:
	            //Ignore all other keys
	      }
	   }

	@Override
	public void handleKeyRelease(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleKeyType(char arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
