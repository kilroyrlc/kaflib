package kaflib.types;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a directory subtype of File that ensures it's always a directory.
 */
public class Directory extends File {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the directory.
	 * @param parent
	 * @param child
	 * @throws Exception
	 */
	public Directory(File parent, String child) throws Exception {
		super(parent, child);
		if (!this.isDirectory()) {
			throw new Exception("Not a directory.");
		}
	}
	
	/**
	 * Returns all files (including implicit directories) in this directory.
	 * @return
	 */
	public Set<File> contents() {
		Set<File> files = new HashSet<File>();
		files.addAll(Arrays.asList(listFiles()));
		return files;
	}
	
	/**
	 * Returns all files in this directory.
	 * @return
	 */
	public Set<File> files() {
		Set<File> files = new HashSet<File>();
		for (File file : listFiles()) {
			if (!file.isDirectory()) {
				files.add(file);
			}
		}
		return files;
	}
	
	/**
	 * Returns all directories in this directory.
	 * @return
	 */
	public Set<Directory> directories() throws Exception {
		Set<Directory> directories = new HashSet<Directory>();
		for (File file : listFiles()) {
			if (file.isDirectory()) {
				directories.add(Directory.create(file));
			}
		}
		return directories;
	}
	
	/**
	 * Creates a directory from a file.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static final Directory create(final File file) throws Exception {
		if (file.isDirectory()) {
			return (Directory) file;
		}
		else {
			throw new Exception("File: " + file + " is not a directory.");
		}
	}
	
}
