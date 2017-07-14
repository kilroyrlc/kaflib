package kaflib.applications;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import kaflib.utils.FileUtils;

/**
 * Presents a file chooser, copies files to an output directory, named their 
 * their hash value mapped to base64 and truncated.
 */
public class RenameFilesToHash extends JFrame {

	private static final long serialVersionUID = 1L;
	public static final int NAV_WIDTH = 250;
	public static final int MAX_LENGTH = 24;
	
	private File output;
	
	/**
	 * Runs the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {		
			RenameFilesToHash app = new RenameFilesToHash();
			app.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * Does the work.
	 * @throws Exception
	 */
	public void run() throws Exception {
	    JFileChooser chooser = new JFileChooser();
	    chooser.setMultiSelectionEnabled(true);
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

	    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION ||
	    	chooser.getSelectedFiles().length == 0) {
	    	return;
	    }
	    File directory = chooser.getSelectedFiles()[0].getParentFile();
	    output = new File(directory, "hash_output");
	    if (!output.exists()) {
		    output.mkdir();
	    }
	    
	    StringBuffer collisions = new StringBuffer();
	    for (File file : chooser.getSelectedFiles()) {
		    try {    	
		    	System.out.println(file.getName());
	    		FileUtils.renameToBase64Hash(file, output, MAX_LENGTH);
		    }
		    catch (Exception e) {
		    	collisions.append(e.getMessage() + "\n");
		    }
	    }
	    if (collisions.length() > 0) {
	    	JOptionPane.showMessageDialog(this, "Problems: " + collisions.toString());
	    }
	}

}
