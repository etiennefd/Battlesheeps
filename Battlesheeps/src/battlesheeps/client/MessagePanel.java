package battlesheeps.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import battlesheeps.ships.*;

public class MessagePanel extends JPanel {
	
	private static final long serialVersionUID = 5567892L;
	private ClientGame aClient; /*change to proper client later*/
	private String aPlayer;
	private String aOpponent;
	private Ship aCurrentShip;
	
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
		this.repaint();
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
		shipTable.setEnabled(false);
		
		this.add(opponentTurn);
		this.add(space);
		this.add(shipTable);
		
		//and lastly validate the changes
		this.validate();
		this.repaint();
		
	}
	
	/**
	 * Displays a menu for the given ship.  TODO : addActionListeners
	 * @param pShip
	 */
	public void displayShipMenu(Ship pShip, boolean pTouchingBase){
		
		aCurrentShip = pShip;
		
		//first removing all prior components
		this.removeAll();
		
		//show ship details 
		JLabel nameLabel = new JLabel("");
		JLabel speedLabel = new JLabel("");
		JLabel weaponLabel = new JLabel("");
		JLabel armourLabel = new JLabel("");
		
		if (pShip instanceof Cruiser) {
			nameLabel.setText("Cruiser");
			weaponLabel.setText("Weapon: Heavy cannon");
			armourLabel.setText("Armour: Heavy");
		} 
		else if (pShip instanceof Destroyer) {
			nameLabel.setText("Destroyer");
			weaponLabel.setText("Weapons: Cannon, Torpedoes");
			armourLabel.setText("Armour: Normal");
		}
		else if (pShip instanceof MineLayer) {
			nameLabel.setText("Mine Layer");
			int mines = ((MineLayer)pShip).getMineSupply();
			weaponLabel.setText("Weapons: Cannon, Mines (" + mines + ")");
			armourLabel.setText("Armour: Heavy");		
		}
		else if (pShip instanceof RadarBoat) {
			nameLabel.setText("Radar Boat");
			weaponLabel.setText("Weapons: Cannon");
			armourLabel.setText("Armour: Normal");
		} 
		else if (pShip instanceof TorpedoBoat) {
			nameLabel.setText("Torpedo Boat");
			weaponLabel = new JLabel("Weapons: Cannon, Torpedoes");
			armourLabel = new JLabel("Armour: Normal");
		}
		else if (pShip instanceof KamikazeBoat) {
			nameLabel.setText("Kamikaze Boat");
			weaponLabel.setText("Weapon: Suicide explosion");
			armourLabel.setText("Armour: Heavy");
		}
		
		int maxSpeed = pShip.getMaxSpeed();
		int actualSpeed = pShip.getActualSpeed();
		speedLabel.setText("Speed: " + actualSpeed + "/" + maxSpeed);
		
		this.add(nameLabel);
		this.add(speedLabel);
		this.add(armourLabel);
		this.add(weaponLabel);
		
		//add buttons
		//each button will inform the client about the move
		//the client will then tell the game board to display the move options 
		
		JButton moveButton = new JButton("Move");
		moveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aClient.translateSelected(aCurrentShip);
			}
		});
		
		JButton turnButton = new JButton("Turn");
		turnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//tell Client that turn was selected for this ship
				aClient.turnSelected(aCurrentShip);
			}
		});
		
		JButton cannonButton = new JButton("Fire cannon");
		cannonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//tell Client that fire cannon was selected for this ship
				aClient.cannonSelected(aCurrentShip);
			}
		});
		
		//Adding the move button
		//The button is not added if the radar boat's radar is on
		boolean moveAllowed = true;
		if (pShip instanceof RadarBoat) {
			if (((RadarBoat)pShip).isExtendedRadarOn()) {
				moveAllowed = false;
			}
		}
		if (moveAllowed) this.add(moveButton);
		
		//Adding the turn and cannon buttons: all ships have them except the kamikaze
		if (!(pShip instanceof KamikazeBoat)) {
			this.add(turnButton);
			this.add(cannonButton);
		}
		
		//Mine layers can lay mines as well as do all the normal moves
		//So we create and add a lay mine and a retrieve mine button
		if (pShip instanceof MineLayer) {
			//you can only lay a mine if you have at least 1
			if (((MineLayer)pShip).getMineSupply() > 0) {
				
				JButton layMineButton = new JButton("Lay mine");
				layMineButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//tell Client that lay mine was selected for this ship
						aClient.layMineSelected(aCurrentShip);
					}
				});
				this.add(layMineButton);
			}
			//you can always retrieve a mine... 
			//well, the client will determine if that's possible, not the message panel 
			JButton retrieveMineButton = new JButton("Retrieve mine in vicinity");
			retrieveMineButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//tell Client that retrieve mine was selected for this ship
					aClient.retrieveMineSelected(aCurrentShip);
				}
			});
			
			this.add(retrieveMineButton);
		} 
		
		//Radar Boats can extend their radar
		else if (pShip instanceof RadarBoat){
			if (((RadarBoat)pShip).isExtendedRadarOn()) {
				
				JButton turnOffButton = new JButton("Turn off extended radar");
				turnOffButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//tell Client that radar-off was selected for this ship
						aClient.turnExtendedRadarOff();
					}
				});
				
				this.add(turnOffButton);
			}
			else {
				JButton turnOnButton = new JButton("Turn on extended radar");
				turnOnButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//tell Client that radar-on was selected for this ship
						aClient.turnExtendedRadarOn();
					}
				});
				
				this.add(turnOnButton);
			}
		}
		//Torpedo boats and destroyers can fire torpedos 
		else if (pShip instanceof TorpedoBoat || pShip instanceof Destroyer) {
			JButton torpedoButton = new JButton("Fire torpedo");
			torpedoButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//tell Client that torpedo was selected for this ship
					aClient.torpedoSelected(aCurrentShip);
				}
			});
			
			this.add(torpedoButton);
		}
		
		else if (pShip instanceof KamikazeBoat) {
			JButton suicideButton = new JButton("Suicide attack");
			suicideButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//tell Client that suicide attack was selected for this ship
					aClient.suicideSelected(aCurrentShip);
				}
			});
			
			this.add(suicideButton);
		}
		
		//finally, if the ship is damaged and touching it's own base, repair ship is an option
		if (pShip.isDamaged() && pTouchingBase) {
			JButton baseButton = new JButton("Repair ship");
			baseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//tell Client that torpedo was selected for this ship
					aClient.baseRepairSelected();
				}
			});
			
			this.add(baseButton);
		}
		
		
		//and revalidating 
		this.repaint();
		this.validate();
	}
	
	public void displayMessage(String pMessage) {
		this.removeAll();
		
		JLabel message = new JLabel(pMessage);
		this.add(message);
		this.repaint();
		this.validate();
	}
}
