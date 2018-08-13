package kaflib.types;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.utils.FileUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines a directory subtype of File that ensures it's always a directory.
 */
public class Directory extends File {

	private static final long serialVersionUID = 1L;
	private static final String LOCK = ".klock";

	/**
	 * Create the directory.
	 * @param parent
	 * @param child
	 * @throws Exception
	 */
	public Directory(File parent, String child) throws Exception {
		super(parent, child);
		if (exists() && !this.isDirectory()) {
			throw new Exception("Not a directory.");
		}
	}

	public Directory() throws Exception {
		super("/");
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
	
	public Set<File> listImages() throws Exception {
		Set<File> files = new HashSet<File>();
		for (File file : listFiles()) {
			if (!file.isDirectory() && FileUtils.isImageFile(file)) {
				files.add(file);
			}
		}
		return files;
	}

	public File getRandom(final String... extensions) throws Exception {
		Set<File> files = list(extensions);
		if (files.size() == 0) {
			return null;
		}
		return RandomUtils.getRandom(files);
	}
	
	public Set<File> list(final String... extensions) throws Exception {
		List<String> ext = new ArrayList<String>();
		for (String extension : extensions) {
			if (!extension.startsWith(".")) {
				ext.add("." + extension);
			}
			else {
				ext.add(extension);
			}
		}
		
		Set<File> files = new HashSet<File>();
		for (File file : listFiles()) {
			if (!file.isDirectory()) {
				for (String extension : ext) {
					if (file.getName().endsWith(extension)) {
						files.add(file);
					}
				}
			}
		}
		return files;
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
	
	private File getLockFile() {
		return new File(this, LOCK);
	}
	
	/**
	 * Writes a lock file to the directory, returns true if successful.
	 * @return
	 * @throws Exception
	 */
	public synchronized Integer lock(final boolean exclusive) throws Exception {
		File file = getLockFile();
		Integer combo = RandomUtils.randomInt();
		if (file.exists()) {
			return null;
		}
		else {
			if (exclusive) {
				FileUtils.write(file, "" + combo);
			}
			else {
				file.createNewFile();
			}
			return combo;
		}
	}
	
	/**
	 * Checks if the directory is locked.
	 * @return
	 */
	public boolean isLocked() {
		return getLockFile().exists();
	}
	
	/**
	 * Reemoves the lock file from the directory if the hashes match.
	 * @return
	 * @throws Exception
	 */
	public synchronized boolean unlock(final Integer combo) throws Exception {
		File file = getLockFile();
		if (!file.exists()) {
			return true;
		}
		
		String combo_string = FileUtils.readString(file, null).trim();
		if (combo_string.length() == 0) {
			file.delete();
			return true;
		}
			
		Integer value = Integer.valueOf(combo_string);
		
		if (value == combo) {
			file.delete();
			return true;
		}
		else {
			return false;
		}
	}
	
}
