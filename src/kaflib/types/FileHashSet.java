package kaflib.types;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.utils.FileUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a manifest of file hashes.
 */
public class FileHashSet {

	private final Set<FileHash> set;
	private final Set<String> extensions;
	private String file_read_md5;
	
	public FileHashSet(final String... allowedExtensions) throws Exception {
		this(null, allowedExtensions);
	}
	

	public FileHashSet(final File file, final String... allowedExtensions) throws Exception {
		this(file, TypeUtils.getSet(allowedExtensions));
	}
	
	public FileHashSet(final File file, final Set<String> allowedExtensions) throws Exception {
		set = new HashSet<FileHash>();
		if (file != null && file.exists()) {
			file_read_md5 = FileUtils.getMD5Base64(file);
	    	for (String line : FileUtils.readLines(file)) {
	    		set.add(new FileHash(line));
	    	}
		}
		else {
			file_read_md5 = null;
		}
		if (allowedExtensions.size() == 0) {
			this.extensions = null;
		}
		else {
			this.extensions = allowedExtensions;
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
	
	public Set<FileHash> get() {
		return set;
	}
	
	public void addAll(final FileHashSet other) {
		set.addAll(other.get());
	}
	
	public Set<String> getExtensions() {
		return extensions;
	}
	
	public boolean contains(final File file) throws Exception {
		return set.contains(new FileHash(file));
	}
	

	public  void toFile(final File file) throws Exception {
		toFile(file, true);
	}
	
	private synchronized void toFile(final File file, 
									final boolean checkMD5) throws Exception {
		if (checkMD5 && 
			file_read_md5 != null && 
			file.exists() && 
			!FileUtils.getMD5Base64(file).equals(file_read_md5)) {
			readThenWrite(file, this);
		}
		else {
		    List<String> lines = new ArrayList<String>();
		    for (FileHash hash : set) {
		    	lines.add(hash.toSerial());
		    }
		    FileUtils.write(file, lines, "\n");
		    file_read_md5 = FileUtils.getMD5Base64(file);
		}
	}
	
	public static void readThenWrite(final File file, final FileHashSet set) throws Exception {
		FileHashSet from_file = new FileHashSet(file, set.getExtensions());
		from_file.addAll(set);
		from_file.toFile(file, false);
	}
	
}
