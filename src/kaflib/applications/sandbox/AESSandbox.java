package kaflib.applications.sandbox;

import kaflib.gui.composite.TwoPasswordConfirmPanel;
import kaflib.utils.KeyPair;

public class AESSandbox {
	
	public static void main(String args[]) {
		try {
	    	KeyPair keys = TwoPasswordConfirmPanel.promptForPasswords();
	    	if (keys == null) {
	    		return;
	    	}
	    	
	    	
	    	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
