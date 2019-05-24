package kaflib.types;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.utils.FileUtils;

/**
 * Defines a manifest of file hashes.
 */
public class FileHashSet {

	private final Set<FileHash> set;
	private final Set<String> extensions;
	
	public FileHashSet(final String... allowedExtensions) throws Exception {
		this(null, allowedExtensions);
	}
	
	public FileHashSet(final File file, final String... allowedExtensions) throws Exception {
		set = new HashSet<FileHash>();
		if (file != null && file.exists()) {
	    	for (String line : FileUtils.readLines(file)) {
	    		set.add(new FileHash(line));
	    	}
		}
		if (allowedExtensions.length > 0) {
			extensions = new HashSet<String>();
			for (String extension : allowedExtensions) {
				extensions.add(extension);
			}
		}
		else {
			extensions = null;
		}
	}
	
	public int size() {
		return set.size();
	}
	
	public void add(final File file) throws Exception {
		if (file.isDirectory()) {
			return;
		}
		if (extensions == null || FileUtils.matchesExtensions(file, extensions)) {
			set.add(new FileHash(file));
		}
	}
	
	public boolean contains(final File file) throws Exception {
		return set.contains(new FileHash(file));
	}
	
	public void toFile(final File file) throws Exception {
	    List<String> lines = new ArrayList<String>();
	    for (FileHash hash : set) {
	    	lines.add(hash.toSerial());
	    }
	    FileUtils.write(file, lines, "\n");
	}
	
}
