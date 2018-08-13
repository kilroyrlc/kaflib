package kaflib.types;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines a filename filter that goes by extension.
 */
public class FilenameExtensionFilter implements FilenameFilter {

	private final List<String> extensions;
	
	/**
	 * Creates the file filter with the given file extensions.  
	 * @param extensions
	 */
	public FilenameExtensionFilter(final String... extensions) {
		this.extensions = new LinkedList<String>();
		for (String extension : extensions) {
			if (extension.startsWith(".")) {
				extension = extension.substring(1);
			}
			this.extensions.add(extension);
		}
	}
	
	@Override
	public boolean accept(File dir, String name) {
		for (String extension : extensions) {
			if (name.toLowerCase().endsWith("." + extension.toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}

}
