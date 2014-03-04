package battlesheeps.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import battlesheeps.server.LogEntry;
import battlesheeps.server.LogEntry.LogType;
 
public class LogPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private JScrollPane aScroll;
	private JPanel aPanel;
	
	public LogPanel() {
		super();
		//MUST BE BORDER LAYOUT ... for some reason. Who knows why. 
		this.setLayout(new BorderLayout());
		aPanel = new JPanel();
		BoxLayout layout = new BoxLayout(aPanel, BoxLayout.PAGE_AXIS);
		aPanel.setLayout(layout);
		aScroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		aScroll.setViewportView(aPanel);
	
		this.add(aScroll, BorderLayout.CENTER);
	} 
	
	public void updateLogEntries(LinkedList<LogEntry> pLogs) {
		
		aPanel.removeAll();
	
		JLabel title = new JLabel("Log:");
		aPanel.add(title);
		
		//note: we probably want the newest entries to be displayed at the top
		//may have to display the list backwards
		for (int i = 0; i < pLogs.size(); i++) {
			JLabel newEntry = new JLabel((pLogs.get(i)).toString());
			aPanel.add(newEntry);
		}
		aPanel.repaint();
		aPanel.validate();
	}
	//test
	public static void main (String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(100, 100));
		
		LinkedList<LogEntry> list = new LinkedList<LogEntry>();
		LogEntry log1 = new LogEntry(LogType.CANNON_MISS, 1, 1, 1);
		LogEntry log2 = new LogEntry(LogType.MINE_EXPLOSION, 24, 5, 2);
		LogEntry log3 = new LogEntry(LogType.TORPEDO_HIT_REEF, 15, 16, 3);
		list.add(log1);
		list.add(log2); 
		list.add(log3);
		
		LogPanel p = new LogPanel();
		p.updateLogEntries(list);
		frame.add(p);
		frame.pack();
		frame.setVisible(true);
		
	}
}
