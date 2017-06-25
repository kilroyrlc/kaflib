package kaflib.applications;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import kaflib.utils.FileUtils;

/**
 * Presents a file chooser, copies all image files (supported extensions) to
 * an output directory, named their hash value.
 */
public class RenameImagesToHash extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final int NAV_WIDTH = 250;
	
	private File output;
	
	/**
	 * Runs the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {		
			RenameImagesToHash app = new RenameImagesToHash();
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
	    chooser.setMultiSelectionEnabled(false);
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
	    	return;
	    }
	    File directory = chooser.getSelectedFile();
	    output = new File(directory, "hash_output");
	    if (!output.exists()) {
		    output.mkdir();
	    }
	    
	    File files[] = directory.listFiles();
	    
	    for (File file : files) {
	    	if (FileUtils.isImageFile(file)) {
	    		System.out.println(file.getName());
	    		FileUtils.renameToHash(file, output);
	    	}
	    	else {
	    		System.out.println("!" + file.getName());
	    	}
	    }
	}

}
