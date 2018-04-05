package kaflib.web;

import java.net.URL;
import java.net.URLEncoder;

/**
 * Collection of generic www utils.
 */
public class WebUtils {
	
	public static String getFilename(final URL url, int maxLength) throws Exception {
		String filename = url.toString();
		
		int index = filename.lastIndexOf('/');
		if (index >= 0) {
			filename = filename.substring(index + 1);
		}
		
		if (maxLength > 0 && filename.length() > maxLength) {
			filename = filename.substring(filename.length() - maxLength);		
		}
		return filename;
	}

	public static String getFilename(final URL url) throws Exception {
		return getFilename(url, -1);
	}
	
	/**
	 * Convenience function to call URL encoder.
	 * @param string
	 * @return
	 */
	public static String encodeURL(final String string) throws Exception {
		return URLEncoder.encode(string, "UTF-8");
	}
}
