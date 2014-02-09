package battlesheeps.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Random;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.minueto.MinuetoColor;
import org.minueto.MinuetoEventQueue;
import org.minueto.MinuetoStopWatch;
import org.minueto.MinuetoTool;
import org.minueto.handlers.MinuetoFocusHandler;
import org.minueto.handlers.MinuetoKeyboard;
import org.minueto.handlers.MinuetoKeyboardHandler;
import org.minueto.handlers.MinuetoMouseHandler;
import org.minueto.image.MinuetoFont;
import org.minueto.image.MinuetoImage;
import org.minueto.image.MinuetoText;
import org.minueto.window.MinuetoPanel;


/* This will open a window with the following panels:
 * 1) the board game (fixed square, width a multiple of 30) 
 * 2) the ship info panel 
 * 3) a scrollable log entries (displays log entries)
 * 4) a scrollable chat panel (displays chat messages and allows user to enter one)
 * 
 */
public class LaunchGame {

	private JDesktopPane desktop; //outer window
	private JFrame mainFrame; //main 
	private GameBoard boardPanel;

	private boolean open = true;
	private int one = 1;
	
	//internal frame
	private Vector<JInternalFrame> internalFrame = new Vector<JInternalFrame>();

	public LaunchGame() {

		//no idea what this does! 
//		GraphicsDevice				grdDevice;
//		GraphicsConfiguration		grcConfiguration;
//		GraphicsEnvironment 		greEnvironment;
//
//		greEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		grdDevice = greEnvironment.getDefaultScreenDevice();
//		grcConfiguration = grdDevice.getDefaultConfiguration();
//
//		this.mainFrame = new JFrame(grcConfiguration); 
		
		this.mainFrame = new JFrame();
		this.mainFrame.setPreferredSize(new Dimension(1000,700));
		
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.mainFrame.setResizable(true);
		this.mainFrame.setFocusTraversalKeysEnabled(false); //what?? 
		this.mainFrame.setSize(MinuetoTool.getDisplayWidth() - 100, 
				MinuetoTool.getDisplayHeight() - 100);

		this.mainFrame.setJMenuBar(createMenu()); //yay! a menu!

		this.desktop = new JDesktopPane();
		this.desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		desktop.setBackground(new Color(126, 175, 152));

		this.mainFrame.setContentPane(desktop);
		this.mainFrame.setTitle("Battlesheeps");
		
		boardPanel = new GameBoard(600, "player1");
		this.desktop.add(boardPanel);
		
		//create Ship Messages Panel
		
		//create Log Entries Panel
		
		//create Chat Messages Panel
		
		this.mainFrame.pack();
		this.mainFrame.setVisible(true);
		boardPanel.setVisible(true);
	
	}

	/*
	 * Menu will contain: 
	 * ... 
	 */
	private JMenuBar createMenu() {

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem newItem = new JMenuItem("Item1");
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//do something  
			}
		});
		fileMenu.add(newItem);
		
		fileMenu.addSeparator();
		
		JMenuItem newItem2 = new JMenuItem("Item2");
		newItem2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//do something
			}
		});
		fileMenu.add(newItem2);
		
		fileMenu.addSeparator();
		
		JMenuItem newItem3 = new JMenuItem("Item3");
		newItem3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//do something
			}
		});
		fileMenu.add(newItem3);
		 
		fileMenu.addSeparator();
		
		JMenuItem hideItem = new JMenuItem("Item4");
		hideItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					//do something
			}
		});
		fileMenu.add(hideItem);	
		
		fileMenu.addSeparator();
		
		fileMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Need more cleanup here
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);

		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(mainFrame, "Battlesheeps game by Stefan Battiston, Etienne Fortier-Dubois, Lei Lopez and Kate Sprung", "About", JOptionPane.PLAIN_MESSAGE);
			}		
		});
		helpMenu.add(aboutItem);

		return menuBar;

	}		
	//This will be moved to Client Game
	public static void main(String[] args) {

		LaunchGame main = new LaunchGame();
	
	}

}