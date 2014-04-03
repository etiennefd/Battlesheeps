package battlesheeps.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import battlesheeps.ships.Ship;

public class ShipButton extends JButton{
	/**
	 * 
	 */
	private Ship aShip;
	private ClientGame aClient;
	private static final long serialVersionUID = 1L;
	
	ShipButton(String pTitle, Ship pShip, ClientGame pClient) {
		super(pTitle);
		aShip = pShip;
		aClient = pClient;
	}

	public void addShipListener() {
		this.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				aClient.showAvailableBasePositions(aShip);	
			}
		
		});
		
	}
}
