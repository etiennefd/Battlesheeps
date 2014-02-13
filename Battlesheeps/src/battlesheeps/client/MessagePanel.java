package battlesheeps.client;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import battlesheeps.ships.*;

import java.util.ArrayList;
import javax.swing.JFrame;
import battlesheeps.accounts.Account;

public class MessagePanel extends JPanel {
	
	private static final long serialVersionUID = 5567892L;
	private ClientGame aClient; /*change to proper client later*/
	private String aPlayer;
	private String aOpponent;
	
	public MessagePanel(ClientGame client, String pPlayer, String pOpponent) {
		super();
		
		//the panel will need to communicate with the client 
		aClient = client;
		
		//the names of the players
		aPlayer = pPlayer;
		aOpponent = pOpponent;
		
		//we want to add messages/buttons from top to bottom
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);
	}
	
	/**
	 * Displays a message for this player's turn. 
	 */
	public void setYourTurn() {
		//display text about it being your turn 
		//first remove all old components
		this.removeAll();
		JLabel yourTurn = new JLabel("It's your turn!");
		JLabel makeMove = new JLabel("Click on a ship for a menu.");
		
		this.add(yourTurn);
		this.add(makeMove);
		//and lastly validate the changes
		this.validate();
		
	}
	
	/**
	 * Displays a message about waiting for the opponent and ship statistic
	 */
	public void setNotYourTurn(List<Ship> pYourShips, List<Ship> pOpponentShips) {
		
		this.removeAll();
		
		JLabel opponentTurn = new JLabel("Waiting for " +aOpponent + " to make a move.");
		JLabel space = new JLabel ("  ");
		
		String[] columnNames = {" ", aPlayer, aOpponent};
		String[][] shipStats = new String[5][3];
		
		shipStats[0][0] = "Cruiser";
		shipStats[1][0] = "Destroyer";
		shipStats[2][0] = "Mine Layer";
		shipStats[3][0] = "Radar Boat";
		shipStats[4][0] = "Torpedo Boat";
		
		Integer numCruiser = new Integer(0);
		Integer numDestroyer = new Integer(0);
		Integer numMine = new Integer(0);
		Integer numRadar = new Integer(0);
		Integer numTorpedo = new Integer(0);
		
		for (Ship ship : pYourShips) {
			if (!ship.isSunk()) {
				if (ship instanceof Cruiser) numCruiser++;
				if (ship instanceof Destroyer) numDestroyer++;
				if (ship instanceof MineLayer) numMine++;
				if (ship instanceof RadarBoat) numRadar++;
				if (ship instanceof TorpedoBoat) numTorpedo++;
			}
		}
		
		shipStats[0][1] = numCruiser.toString();
		shipStats[1][1] = numDestroyer.toString();
		shipStats[2][1] = numMine.toString();
		shipStats[3][1] = numRadar.toString();
		shipStats[4][1] = numTorpedo.toString();
		
		numCruiser = 0;
		numDestroyer = 0;
		numMine = 0;
		numRadar = 0;
		numTorpedo = 0;
		
		for (Ship ship : pOpponentShips) {
			if(!ship.isSunk()) {
				if (ship instanceof Cruiser) numCruiser++;
				if (ship instanceof Destroyer) numDestroyer++;
				if (ship instanceof MineLayer) numMine++;
				if (ship instanceof RadarBoat) numRadar++;
				if (ship instanceof TorpedoBoat) numTorpedo++;
			}
		}
		
		shipStats[0][2] = numCruiser.toString();
		shipStats[1][2] = numDestroyer.toString();
		shipStats[2][2] = numMine.toString();
		shipStats[3][2] = numRadar.toString();
		shipStats[4][2] = numTorpedo.toString();		
		
		String[][] shipData = {columnNames, shipStats[0], shipStats[1], 
				shipStats[2], shipStats[3], shipStats[4]};
	
		JTable shipTable = new JTable(shipData, columnNames);
		
		this.add(opponentTurn);
		this.add(space);
		this.add(shipTable);
		
		//and lastly validate the changes
		this.validate();
		
	}
	
	/**
	 * Displays a menu for the given ship.  TODO : addActionListeners
	 * @param pShip
	 */
	public void displayShipMenu(Ship pShip){
		
		//first removing all prior components
		this.removeAll();
		
		//show ship details 
		JLabel nameLabel;
		JLabel speedLabel;
		JLabel weaponLabel;
		JLabel armourLabel;
		
		if (pShip instanceof Cruiser) {
			nameLabel = new JLabel("Cool Cruiser");
			weaponLabel = new JLabel("Weapon: Cannon");
			armourLabel = new JLabel("Armour: Heavy");
		} 
		else if (pShip instanceof Destroyer) {
			nameLabel = new JLabel("Deadly Destroyer");
			weaponLabel = new JLabel("Weapon: Cannon");
			armourLabel = new JLabel("Armour: Normal");
		}
		else if (pShip instanceof MineLayer) {
			nameLabel = new JLabel("Magnificent Mine Layer");
			int mines = ((MineLayer)pShip).getMineSupply();
			weaponLabel = new JLabel("Weapons: Cannon, Mines (" + mines + ")");
			armourLabel = new JLabel("Armour: Heavy");		
		}
		else if (pShip instanceof RadarBoat) {
			nameLabel = new JLabel("Radioactive Radar Boat");
			weaponLabel = new JLabel("Weapons: Cannon");
			armourLabel = new JLabel("Armour: Normal");
		} 
		else /*TORPEDO*/ {
			nameLabel = new JLabel("Terrifying Torpedo Boat");
			weaponLabel = new JLabel("Weapons: Cannon, Torpedo");
			armourLabel = new JLabel("Armour: Normal");
		}
		
		int maxSpeed = pShip.getMaxSpeed();
		int actualSpeed = pShip.getActualSpeed();
		speedLabel = new JLabel("Speed: " + actualSpeed + "/" + maxSpeed);
		
		this.add(nameLabel);
		this.add(speedLabel);
		this.add(armourLabel);
		this.add(weaponLabel);
		
		//add buttons
		//each button will inform the client about the move
		//the client will then tell the game board to display the move options 
		
		JButton moveButton = new JButton("Move");
		JButton turnButton = new JButton("Turn");
		JButton cannonButton = new JButton("Fire cannon");
		
		this.add(moveButton);
		this.add(turnButton);
		this.add(cannonButton);
		
		//Mine layers can lay mines as well as do all the normal moves
		if (pShip instanceof MineLayer) {
			//you can only lay a mine if you have at least 1
			if (((MineLayer)pShip).getMineSupply() > 0) {
				JButton layMineButton = new JButton("Lay mine");
				this.add(layMineButton);
			}
			JButton retrieveMineButton = new JButton("Retrieve mine in vicinity");
			this.add(retrieveMineButton);
		} //Radar Boats can extend its radar 
		else if (pShip instanceof RadarBoat){
			if (((RadarBoat)pShip).isExtendedRadarOn()) {
				JButton turnOffButton = new JButton("Turn off extended radar");
				this.add(turnOffButton);
			}else {
				JButton turnOnButton = new JButton("Turn on extended radar");
				this.add(turnOnButton);
			}
		} //Torpedo boats can fire torpedos 
		else if (pShip instanceof TorpedoBoat) {
			JButton torpedoButton = new JButton("Fire torpedo");
			this.add(torpedoButton);
		}
		//and revalidating 
		this.validate();
	}
	
//TESTING	
//	public static void main(String[] args) {
//		JFrame frame = new JFrame();
//	
//		Account account1 = new Account("Player1", "abc");
//		Account account2 = new Account("Player2", "ebg");
//		MessagePanel me = new MessagePanel(new ClientGame(), account1.getUsername(), account2.getUsername());
//		
//		List<Ship> list1 = new ArrayList<Ship>();
//		Ship ship1 = new Cruiser(account1);
//		Ship ship2 = new MineLayer(account1);
//		Ship ship3 = new RadarBoat(account1);
//		Ship ship4 = new TorpedoBoat(account1);
//		Ship ship5 = new Destroyer(account1);
//		
//		list1.add(ship1);
//		list1.add(ship2);
//		list1.add(ship3);
//		list1.add(ship4);
//		list1.add(ship5);
//		
//		List<Ship> list2 = new ArrayList<Ship>();
//		Ship ship6 = new RadarBoat(account2);
//		Ship ship7 = new RadarBoat(account2);
//		Ship ship8 = new Destroyer(account2);
//		
//		list2.add(ship6);
//		list2.add(ship7);
//		list2.add(ship8);
//		
//		me.setNotYourTurn(list1, list2);
//		
//		Ship shipT = new TorpedoBoat(account1);
//		me.displayShipMenu(shipT);
//		frame.add(me);
//		frame.setVisible(true);
//	}
}
