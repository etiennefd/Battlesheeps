package battlesheeps.board;

import java.io.Serializable;

public class MineSquare implements Square, Serializable 
{
	private static final long serialVersionUID = 7503315325395436337L;

	public String toString() {
		return "##";
	}

}
