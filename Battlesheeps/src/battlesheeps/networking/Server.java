package battlesheeps.networking;

public class Server
{
	public Server()
	{
		new Thread(new ServerLogin()).start();
		new Thread(new ServerLobby()).start();
		new Thread(new ServerGamesAndMoves()).start();
		new Thread(new ServerChat()).start();
	}
	
	public static void main(String[] args)
	{
		new Server();
	}
}
