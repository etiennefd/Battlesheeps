package battlesheeps.server;

import battlesheeps.board.Coordinate;
import battlesheeps.server.ServerGame.MoveType;
import battlesheeps.ships.Ship;

public class Move {
	
	private Coordinate aCoord;
	private Ship aShip;
	private MoveType aMoveType;
	
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

	
	
}
