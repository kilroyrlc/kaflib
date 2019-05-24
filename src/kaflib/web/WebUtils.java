package kaflib.web;

import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static String getSite(final URL url) throws Exception {
		Pattern pattern = Pattern.compile("^http[s]?\\:\\/\\/([\\w\\.]+)\\/.*$");
		Matcher matcher = pattern.matcher(url.toString());
		if (!matcher.matches()) {
			throw new Exception("Could not parse: " + url + ".");
		}
		String site = matcher.group(1);
		if (site.startsWith("www.")) {
			return site.substring(4);
		}
		else {
			return site;
		}
		
	}
	
	public static boolean isWebURL(final String value) {
		return value.startsWith("http://") || value.startsWith("https://");
	}
	
	/**
	 * Convenience function to call URL encoder.
	 * @param string
	 * @return
	 */
	public static String encodeURL(final String string) throws Exception {
		return URLEncoder.encode(string, "UTF-8");
	}
	
	public static void main(String args[]) {
		try {
			System.out.println(WebUtils.getSite(new URL("http://chrisritchie.org/kilroy")));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
