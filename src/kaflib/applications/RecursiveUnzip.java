package kaflib.applications;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import kaflib.types.Directory;
import kaflib.utils.FileUtils;

public class RecursiveUnzip  extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Runs the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {		
			RecursiveUnzip app = new RecursiveUnzip();
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
		int count = 0;
	    JFileChooser chooser = new JFileChooser();	
	    chooser = new JFileChooser();
	    chooser.setMultiSelectionEnabled(false);
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION ||
	    	chooser.getSelectedFile() == null) {
	    	System.out.println("Operation aborted.");
	    	return;
	    }	    

	    Set<Directory> directories = new HashSet<Directory>();
	    directories.add(new Directory(chooser.getSelectedFile()));
	    Directory processed = new Directory(chooser.getSelectedFile(), "processed");
	    if (!processed.exists()) {
	    	processed.mkdir();
	    }

	    while (directories.size() > 0) {
	    	Directory directory = directories.iterator().next();
	    	directories.remove(directory);
	    	
	    	for (File file : directory.listFiles()) {
	    		if (file.isDirectory()) {
	    			directories.add(new Directory(file));
	    		}
	    		else if (FileUtils.isArchiveFile(file)) {
	    			Directory outdir = new Directory(file.getParentFile(), FileUtils.getFilenameWithoutExtension(file));
	    			directories.add(outdir);
	    			System.out.println(file);
	    			FileUtils.unzip(file, outdir);
	    			if (file.exists()) {
	    				FileUtils.copyTo(processed, file);
	    				file.delete();
	    			}
	    		}
	    		else {
	    			
	    		}
	    	}
	    }

    	System.out.println("Processed: " + count + " files.");
	}

}