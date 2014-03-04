package battlesheeps.client;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import battlesheeps.board.Coordinate;
import battlesheeps.server.LogEntry;
 
public class LogPanel extends JScrollPane{
	
	JList aLogEntries = new JList();
	
	//do we need to specify height and width? 
	public LogPanel() {
		super();
		JLabel log = new JLabel("Log:");
		aLogEntries.add(log);
		this.add(aLogEntries);
	} 

	public void addLogEntry(LogEntry pEntry) {
		//Coordinate logCoord = pEntry.getCoordinate();
		String logMessage =  pEntry.toString();
		
		JLabel newEntry = new JLabel(logMessage);
		aLogEntries.add(newEntry);
	}
}
