package battlesheeps.client;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import battlesheeps.networking.ClientLogin;
import battlesheeps.networking.LoginMessage;
import battlesheeps.networking.LoginMessage.LoginType;

class LoginListener implements ActionListener
{
	private JTextField aUsernameField;
	private JTextField aPasswordField;
	private JFrame aLoginFrame;
	
	public LoginListener(JTextField pUsernameField, JTextField pPasswordField, JFrame pLoginFrame)
	{
		this.aUsernameField = pUsernameField;
		this.aPasswordField = pPasswordField;
		this.aLoginFrame = pLoginFrame;
	}
	
	private void sendLoginInfo(String pUsername, String pPassword, JDialog loginDialog, JFrame loginFrame)
	{
		LoginMessage loginInfo = new LoginMessage(LoginType.LOGIN,pUsername, pPassword);
		ClientLogin loginClient = new ClientLogin(loginDialog, loginFrame);
		
		loginClient.sendMessage(loginInfo);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if(aUsernameField.getText().equals("") || aPasswordField.getText().equals("")) //check password??
		{
			JOptionPane.showMessageDialog(null, "Please enter a valid username and password.", 
					"Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
		else
		{ 
			final JOptionPane optionPane = new JOptionPane("Logging in...", 
					JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, 
					null, new Object[]{}, null);
	
			final JDialog dialog = new JDialog();
			dialog.setTitle("");
			dialog.setModal(true);
	
			dialog.setContentPane(optionPane);
	
			//TODO: UNCOMMENT BELOW to prevent "logging in" dialog to be closed
			//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();
	
			dialog.setVisible(true);
			
			//sends login input to server for verification
			sendLoginInfo(aUsernameField.getText(), aPasswordField.getText(), dialog, aLoginFrame);
			
			/*if(loginSuccess)
			{
				//auto-close login screen + popup for user
				aLoginFrame.setVisible(false);
				aLoginFrame.dispose();
				dialog.setVisible(false);
				dialog.dispose();
			}*/
			
		}
	}
	
}

class AccountWizard implements ActionListener
{
	
	public AccountWizard()
	{
		//empty on purpose constructor
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		JFrame accountWizard = new JFrame("Create Account");
        accountWizard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        accountWizard.setMinimumSize(new Dimension(275,225));
        accountWizard.setMaximumSize(new Dimension(275,225));
        accountWizard.setResizable(false);
        
        SpringLayout layout = new SpringLayout();
        
        Font f = new Font("serif",Font.BOLD,20);
        
        JLabel title = new JLabel("CREATE ACCOUNT");
        title.setFont(f);
        
        layout.putConstraint(SpringLayout.NORTH, title, 10, SpringLayout.NORTH, accountWizard.getContentPane());
        layout.putConstraint(SpringLayout.WEST, title, 25, SpringLayout.WEST, accountWizard.getContentPane());
        
        accountWizard.getContentPane().add(title);
        
        JLabel usernameLabel = new JLabel("Username: ");
        accountWizard.getContentPane().add(usernameLabel);
        
        layout.putConstraint(SpringLayout.NORTH, usernameLabel, 25, SpringLayout.SOUTH, title);
        layout.putConstraint(SpringLayout.WEST, usernameLabel, 5, SpringLayout.WEST, accountWizard.getContentPane());
        
        JTextField usernameField = new JTextField("",15);
        accountWizard.getContentPane().add(usernameField);
        
        layout.putConstraint(SpringLayout.NORTH, usernameField, 25, SpringLayout.SOUTH, title);
        layout.putConstraint(SpringLayout.WEST, usernameField, 0, SpringLayout.EAST, usernameLabel);
        
        JLabel passwordLabel = new JLabel("Password: ");
        accountWizard.getContentPane().add(passwordLabel);
        
        layout.putConstraint(SpringLayout.NORTH, passwordLabel, 10, SpringLayout.SOUTH, usernameLabel);
        layout.putConstraint(SpringLayout.WEST, passwordLabel, 5, SpringLayout.WEST, accountWizard.getContentPane());
        
        JTextField passwordField = new JPasswordField("",15);
        accountWizard.getContentPane().add(passwordField);
        
        layout.putConstraint(SpringLayout.NORTH, passwordField, 5, SpringLayout.SOUTH, usernameField);
        layout.putConstraint(SpringLayout.WEST, passwordField, 2, SpringLayout.EAST, passwordLabel);
                    
        JLabel password2Label = new JLabel("Confirm Password: ");
        accountWizard.getContentPane().add(password2Label);
        
        layout.putConstraint(SpringLayout.NORTH, password2Label, 10, SpringLayout.SOUTH, passwordLabel);
        layout.putConstraint(SpringLayout.WEST, password2Label, 5, SpringLayout.WEST, accountWizard.getContentPane());
        
        JTextField password2Field = new JPasswordField("",10);
        accountWizard.getContentPane().add(password2Field);
        
        layout.putConstraint(SpringLayout.NORTH, password2Field, 5, SpringLayout.SOUTH, passwordField);
        layout.putConstraint(SpringLayout.WEST, password2Field, 1, SpringLayout.EAST, password2Label);
        
        JButton createAccountButton = new JButton("Create Account"); 
        accountWizard.getContentPane().add(createAccountButton);
        
        layout.putConstraint(SpringLayout.NORTH, createAccountButton, 10, SpringLayout.SOUTH, password2Field);
        layout.putConstraint(SpringLayout.WEST, createAccountButton, 100, SpringLayout.WEST, accountWizard.getContentPane());
        
        createAccountButton.addActionListener(new CreateAccountListener(usernameField, passwordField, password2Field, accountWizard));
        
        accountWizard.getContentPane().setLayout(layout);
        
        //Display the window.
        accountWizard.pack();
        accountWizard.setVisible(true);
	}
}

class CreateAccountListener implements ActionListener {
	private JTextField aUsernameField;
	private JTextField aPasswordField;
	private JTextField aConfirmField;
	private JFrame aWizard;
	
	public CreateAccountListener(JTextField pUsernameField, JTextField pPasswordField, JTextField pPassword2Field, JFrame pCallingFrame)
	{
		this.aUsernameField = pUsernameField;
		this.aPasswordField = pPasswordField;
		this.aConfirmField = pPassword2Field;
		this.aWizard = pCallingFrame;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (!aPasswordField.getText().equals(aConfirmField.getText()))
		{
			JOptionPane.showMessageDialog(null, "Passwords don't match, please try again", 
					"Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
		else if(aUsernameField.getText().equals("") || aPasswordField.getText().equals("")) //check password??
		{
			JOptionPane.showMessageDialog(null, "Please enter a valid username and password.", 
					"Invalid Input", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			//TODO: send info to server!!!
			
			//auto-close login screen + popup for user
			aWizard.setVisible(false);
			aWizard.dispose();
		
			final JOptionPane optionPane = new JOptionPane("Creating account...", 
					JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, 
					null, new Object[]{}, null);
	
			final JDialog dialog = new JDialog();
			dialog.setTitle("");
			dialog.setModal(true);
	
			dialog.setContentPane(optionPane);
	
			//TODO: UNCOMMENT BELOW
			//dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();
	
			dialog.setVisible(true);
			//TODO: send dialog to client so it knows to close it :)
		}
	}
	
}

public class LoginScreen {
	
	public LoginScreen()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowLogin();
            }
		});
	}
	
    /**
     * Create the GUI and show it.
     */
    private static void createAndShowLogin() {
        //Create and set up the window.
    	
        JFrame frame = new JFrame("Battlesheeps Login Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setMinimumSize(new Dimension(275,225));
        frame.setMaximumSize(new Dimension(275,225));
        frame.setResizable(false);
        
        SpringLayout layout = new SpringLayout(); 
        
        Font f = new Font("serif",Font.BOLD,20);
        
        JLabel title = new JLabel("BATTLESHEEPS");
        title.setFont(f);
        
        layout.putConstraint(SpringLayout.NORTH, title, 10, SpringLayout.NORTH, frame.getContentPane());
        layout.putConstraint(SpringLayout.WEST, title, 40, SpringLayout.WEST, frame.getContentPane());
        
        frame.getContentPane().add(title);
        
        JLabel usernameLabel = new JLabel("Username: ");
        frame.getContentPane().add(usernameLabel);
        
        layout.putConstraint(SpringLayout.NORTH, usernameLabel, 25, SpringLayout.SOUTH, title);
        layout.putConstraint(SpringLayout.WEST, usernameLabel, 5, SpringLayout.WEST, frame.getContentPane());
        
        JTextField usernameField = new JTextField("",15);
        frame.getContentPane().add(usernameField);
        
        layout.putConstraint(SpringLayout.NORTH, usernameField, 25, SpringLayout.SOUTH, title);
        layout.putConstraint(SpringLayout.WEST, usernameField, 0, SpringLayout.EAST, usernameLabel);
        
        JLabel passwordLabel = new JLabel("Password: ");
        frame.getContentPane().add(passwordLabel);
        
        layout.putConstraint(SpringLayout.NORTH, passwordLabel, 10, SpringLayout.SOUTH, usernameLabel);
        layout.putConstraint(SpringLayout.WEST, passwordLabel, 5, SpringLayout.WEST, frame.getContentPane());
        
        JTextField passwordField = new JPasswordField("",15);
        frame.getContentPane().add(passwordField);
        
        layout.putConstraint(SpringLayout.NORTH, passwordField, 5, SpringLayout.SOUTH, usernameField);
        layout.putConstraint(SpringLayout.WEST, passwordField, 2, SpringLayout.EAST, passwordLabel);
                      
        JButton loginButton = new JButton("LOGIN"); 
        frame.getContentPane().add(loginButton);
        
        layout.putConstraint(SpringLayout.NORTH, loginButton, 10, SpringLayout.SOUTH, passwordField);
        layout.putConstraint(SpringLayout.WEST, loginButton, 135, SpringLayout.WEST, frame.getContentPane());
        
        loginButton.addActionListener(new LoginListener(usernameField, passwordField,frame));
        
        JButton createButton = new JButton("Create Account");
        frame.getContentPane().add(createButton);
        
        createButton.addActionListener(new AccountWizard());
        
        layout.putConstraint(SpringLayout.NORTH, createButton, 10, SpringLayout.SOUTH, loginButton);
        layout.putConstraint(SpringLayout.WEST, createButton, 100, SpringLayout.WEST, frame.getContentPane());
 
        frame.getContentPane().setLayout(layout);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowLogin();
            }
        });
    }
}
