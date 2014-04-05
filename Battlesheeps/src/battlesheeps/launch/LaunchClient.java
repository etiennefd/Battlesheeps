package battlesheeps.launch;

import battlesheeps.client.LoginScreen;

public class LaunchClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoginScreen.createAndShowLogin();
		//		System.out.println("Creating Login Screen\n");
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                LoginScreen.createAndShowLogin();
//            }
//        });
	}

}
