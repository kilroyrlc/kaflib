package kaflib.types;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Defines a list of files in a directory with attached properties.
 */
public class FileList<T> implements Serializable, Iterable<File> {
	
	private static final long serialVersionUID = 1L;
	private final Directory directory;
	private final Map<File, T> files;
	private final Map<Directory, FileList<T>> directories;

	/**
	 * Creates a file list.
	 * @param directory
	 * @throws Exception
	 */
	public FileList(final Directory directory) throws Exception {
		this(directory, false);
	}
	
	/**
	 * Creates a file list.
	 * @param directory
	 * @throws Exception
	 */
	public FileList(final Directory directory, boolean filesOnly) throws Exception {
		this.directory = directory;
		files = new HashMap<File, T>();
		directories = new HashMap<Directory, FileList<T>>();
		
		for (File file : this.directory.listFiles()) {
			if (file.isDirectory() && !filesOnly) {
				directories.put(new Directory(file), new FileList<T>(new Directory(file)));
			}
			else {
				files.put(file, null);
			}
		}
	}

	@Override
	public Iterator<File> iterator() {
		return files.keySet().iterator();
	}
	
	public Pair<Set<File>, Set<File>> resync() throws Exception {
		Set<File> files_now = directory.files();
		Set<Directory> directories_now = directory.directories();
		Set<File> added = new HashSet<File>();
		Set<File> removed = new HashSet<File>();
		
		for (File file : files_now) {
			if (!files.containsKey(file)) {
				added.add(file);
				files.put(file, null);
			}
		}
		for (Directory directory : directories_now) {
			if (!directories.containsKey(directory)) {
				added.add(directory);
				directories.put(directory, new FileList<T>(directory));
			}
		}
		for (File file : files.keySet()) {
			if (!files_now.contains(file)) {
				removed.add(file);
				files.remove(file);
			}
		}
		for (Directory directory : directories.keySet()) {
			if (!directories_now.contains(directory)) {
				removed.add(directory);
				directories.remove(directory);
			}
		}
		
		return new Pair<Set<File>, Set<File>>(added, removed);
	}
	
	/**
	 * Returns the info associated with the file, null if uninitialized.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public T getInfo(final File file) throws Exception {
		if (!files.containsKey(file)) {
			throw new Exception("Could not find file: " + file + " in list.");
		}
		return files.get(file);
	}
	
	/**
	 * Sets the info associated with the file.
	 * @param file
	 * @param info
	 * @throws Exception
	 */
	public void setInfo(final File file, final T info) throws Exception {
		if (!files.containsKey(file)) {
			throw new Exception("Could not find file: " + file + " in list.");
		}
		files.put(file, info);
	}
	
	/**
	 * Returns the file list for the subdirectory.
	 * @param directory
	 * @return
	 * @throws Exception
	 */
	public FileList<T> getSubdirectory(final Directory directory) throws Exception {
		if (!directories.containsKey(directory)) {
			throw new Exception("Could not find directory: " + directory + " in list.");
		}
		return directories.get(directory);
	}
	
	/**
	 * Returns all files.
	 * @return
	 */
	public Set<File> getFiles() {
		return files.keySet();
	}
	
	/**
	 * Returns all directories.
	 * @return
	 */
	public Set<Directory> getDirectories() {
		return directories.keySet();
	}
	
}
