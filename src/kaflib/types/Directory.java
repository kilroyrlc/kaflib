package kaflib.types;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

	public Directory(String path) throws Exception {
		super(path);
		if (!this.isDirectory()) {
			throw new Exception("Not a directory.");
		}
	}
	
	public Directory(File file) throws Exception {
		super(file.getAbsolutePath());
		if (!this.isDirectory()) {
			throw new Exception("Not a directory.");
		}
	}
	
	public List<String> listNames() {
		List<String> names = new ArrayList<String>();
		for (File file : listFiles()) {
			names.add(file.getName());
		}
		return names;
	}
	
	public boolean contains(final String filename) {
		for (String name : listNames()) {
			if (name.equals(filename)) {
				return true;
			}
		}
		return false;
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
	 * Returns whether or not this directory has subdirectories.
	 * @param directory
	 * @return
	 */
	public boolean isLeaf() {
		File files[] = listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns all directories in this directory.
	 * @return
	 */
	public Set<Directory> directories() throws Exception {
		Set<Directory> directories = new HashSet<Directory>();
		for (File file : listFiles()) {
			if (file.isDirectory()) {
				directories.add(new Directory(file));
			}
		}
		return directories;
	}
	
}