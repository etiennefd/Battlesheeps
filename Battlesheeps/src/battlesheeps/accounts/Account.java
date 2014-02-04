package battlesheeps.accounts;

public class Account {

	public enum Status {
		OFFLINE, IN_GAME, AVAILABLE
	}
	
	private String aUsername;
	private String aPassword;
	private Status aAvailability; 
	private int aNumGamesWon; 
	private int aNumGamesLost;
	
	public Account(String pUsername, String pPassword) {
		aUsername = pUsername; 
		aPassword = pPassword;
		aAvailability = Status.OFFLINE;
		aNumGamesWon = 0; 
		aNumGamesLost = 0; 
	}
	/*getters*/
	public String getUsername() {
		return aUsername;
	}
}
