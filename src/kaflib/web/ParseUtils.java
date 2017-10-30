package kaflib.web;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Set of utilities for dealing with the jsoup library.
 */
public class ParseUtils {

	/**
	 * Prints the html of the given element with all of its attributes.
	 * @param element
	 * @return
	 * @throws Exception
	 */
	public static String toAttributeString(final Element element) throws Exception {
		CheckUtils.check(element, "element");

		StringBuffer buffer = new StringBuffer();
		buffer.append(element.html());
		
		for (Attribute attribute : element.attributes()) {
			buffer.append("\n   " + attribute.toString());
		}
		return buffer.toString();
	}

	/**
	 * Performs a select.
	 * @param document
	 * @param tags
	 * @return
	 * @throws Exception
	 */
	public static Elements select(final Document document, final String... tags) throws Exception {
		return select(document, false, tags);
	}

	/**
	 * Performs a select.  If no rows are returned, optionally prints the values for each
	 * tag in succession.
	 * @param document
	 * @param tags
	 * @return
	 * @throws Exception
	 */
	public static Elements select(final Document document, final boolean printOnFailure, final String... tags) throws Exception {
		Elements rows = document.select(StringUtils.concatenate(" ", tags));
		
		if (rows != null && rows.size() > 0) {
			return rows;
		}

		if (printOnFailure) {
			StringBuffer buffer = new StringBuffer();
			for (String tag : tags) {
				buffer.append(tag);
				System.out.println(buffer.toString() + ": " + 
				                   document.select(buffer.toString()).size() + 
				                   " rows.");
				buffer.append(" ");
			}
		}
		
		return null;
	}

	/**
	 * Returns all links on the page.
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public static Set<URL> getLinks(final Elements source, final URL url) throws Exception {
		Set<URL> urls = new HashSet<URL>();
		if (source == null || source.size() == 0) {
			return urls;
		}
		
		Elements links = source.select("a");
		
		for (Element link : links) {
			if (link.hasAttr("href")) {
				String string = link.attr("href");

				// Relative.
				if (!string.contains(":")) {
					if (!url.toString().endsWith("/")) {
						urls.add(new URL(url + "/" + string));
					}
					else {
						urls.add(new URL(url + "/" + string));
					}
				}
				// Absolute.
				else {
					urls.add(new URL(string));
				}
			}
		}
		return urls;
	}

	
}
