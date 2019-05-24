package kaflib.types;

import java.io.File;

import kaflib.utils.FileUtils;

/**
 * Defines a hash of a file that uses size and md5.
 */
public class FileHash {
	private final String md5;
	private final long size;
	private final String combined;
	private final int hash_code;
	private static final String SEPARATOR = " ";
	
	/**
	 * Constructs the hash.
	 * @param file
	 * @throws Exception
	 */
	public FileHash(final File file) throws Exception {
		md5 = FileUtils.getMD5Base64(file);
		size = file.length();
		combined = md5 + SEPARATOR + size;
		hash_code = combined.hashCode();
	}
	
	/**
	 * Constructs a hash based on a serial string produced by toString().
	 * @param serial
	 * @throws Exception
	 */
	public FileHash(final String serial) throws Exception {
		int index = serial.indexOf(SEPARATOR);
		if (index < 1) {
			throw new Exception("Invalid serialized hash: " + serial + ".");
		}
		md5 = serial.substring(0, index);
		size = Long.valueOf(serial.substring(index + 1));
		combined = md5 + SEPARATOR + size;
		hash_code = combined.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof FileHash) {
			return equals((FileHash) o);
		}
		else {
			return false;
		}
	}
	
	protected long size() {
		return size;
	}
	
	protected String md5() {
		return md5;
	}
	
	public int hashCode() {
		return hash_code;
	}

	public String toSerial() {
		return combined;
	}
	
	public String toString() {
		return combined;
	}
	
	public boolean equals(final FileHash o) {
		return o.md5().equals(md5) && o.size() == size;
	}
	
	
}
