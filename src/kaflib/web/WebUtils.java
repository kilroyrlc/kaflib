package kaflib.web;

import java.net.URLEncoder;

/**
 * Collection of generic www utils.
 */
public class WebUtils {
	
	/**
	 * Convenience function to call URL encoder.
	 * @param string
	 * @return
	 */
	public static String encodeURL(final String string) throws Exception {
		return URLEncoder.encode(string, "UTF-8");
	}
}
