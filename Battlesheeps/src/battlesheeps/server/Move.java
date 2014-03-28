package battlesheeps.server;

import java.io.Serializable;

import battlesheeps.board.Coordinate;
import battlesheeps.server.ServerGame.MoveType;
import battlesheeps.ships.Ship;

public class Move implements Serializable 
{
	private static final long serialVersionUID = -1767292751081182904L;
	public enum ServerInfo {
		CORAL_REEF_ACCEPT, CORAL_REEF_DECLINE, SHIP_INIT, SHIP_INIT_COMPLETE
	}
	
	private Coordinate aCoord;
	private Coordinate aSecondaryCoord; //for Kamikaze moves
	private Ship aShip;
	private MoveType aMoveType;
	private ServerInfo aServerInfo;
	
	public Move(Coordinate pCoord, Coordinate pSecondaryCoord, Ship pShip, MoveType pMoveType, ServerInfo pServerInfo){
		this.aCoord = pCoord;
		this.aSecondaryCoord = pSecondaryCoord;
		this.aShip = pShip;
		this.aMoveType = pMoveType;
		this.aServerInfo = pServerInfo;
	}
	
	/**
	 * Getter for this move's coordinate. 
	 * @return coordinate 
	 */
	public Coordinate getCoord() {
		return aCoord;
	}
	/**
	 * Setter for coordinate 
	 * @param pCoord
	 */
	public void setCoord(Coordinate pCoord) {
		this.aCoord = pCoord;
	}

	/**
	 * Getter for this move's coordinate. 
	 * @return coordinate 
	 */
	public Coordinate getSecondaryCoord() {
		return aSecondaryCoord;
	}
	/**
	 * Setter for coordinate 
	 * @param pCoord
	 */
	public void setSecondaryCoord(Coordinate pCoord) {
		this.aSecondaryCoord = pCoord;
	}
	/**
	 * Getter for this move's ship. 
	 * @return
	 */
	public Ship getaShip() {
		return aShip;
	}
	/**
	 * Setter for ship. 
	 * @param pShip
	 */
	public void setShip(Ship pShip) {
		this.aShip = pShip;
	}
	/**
	 * Getter for this move's type (e.g. Turn) 
	 * @return
	 */
	public MoveType getMoveType() {
		return aMoveType;
	}
	/**
	 * Setter for the move type 
	 * @param aMoveType
	 */
	public void setMoveType(MoveType aMoveType) {
		this.aMoveType = aMoveType;
	}
	/**
	 * Getter for ServerInfo enum.
	 * @return
	 */
	public ServerInfo getServerInfo(){
		return aServerInfo;
	}

	
	
}
