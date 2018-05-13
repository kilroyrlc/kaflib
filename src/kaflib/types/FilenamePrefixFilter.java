package kaflib.types;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines a filename filter that goes by extension.
 */
public class FilenamePrefixFilter implements FilenameFilter {

	private final List<String> prefices;
	
	/**
	 * Creates the file filter with the given file extensions.  
	 * @param prefices
	 */
	public FilenamePrefixFilter(final String... prefices) {
		this.prefices = new LinkedList<String>();
		for (String extension : prefices) {
			if (extension.startsWith(".")) {
				extension = extension.substring(1);
			}
			this.prefices.add(extension);
		}
	}
	
	@Override
	public boolean accept(File dir, String name) {
		for (String prefix : prefices) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		
		return false;
	}

}
