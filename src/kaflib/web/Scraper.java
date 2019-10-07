package kaflib.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import kaflib.types.Directory;
import kaflib.types.Pair;
import kaflib.utils.FileUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a web scraper type.
 */
public class Scraper {
	private WebClient htmlunit_client;
	private Proxy proxy;
	private static final String TOR_ADDRESS = "127.0.0.1";
	private static final int TOR_PORT = 9150;

	private HashMap<String, String> properties;
	

	public Scraper(final boolean useTor, final boolean useHTMLUnit) {
		this(useTor, useHTMLUnit, BrowserVersion.FIREFOX_60);
	}
	
	public Scraper(final boolean useTor, final boolean useHTMLUnit, final BrowserVersion browser) {
		htmlunit_client = null;
		proxy = null;
		properties = new HashMap<String, String>();
		
		if (useHTMLUnit) {
			htmlunit_client = new WebClient(browser);      
			if (useTor) {
				ProxyConfig prc = new ProxyConfig(TOR_ADDRESS, TOR_PORT, true);
				htmlunit_client.getOptions().setProxyConfig(prc); 
			}
			
			htmlunit_client.getOptions().setThrowExceptionOnScriptError(false);
		    htmlunit_client.getOptions().setThrowExceptionOnFailingStatusCode(false);
			htmlunit_client.getOptions().setJavaScriptEnabled(true);
			htmlunit_client.getOptions().setCssEnabled(false);
	        htmlunit_client.waitForBackgroundJavaScriptStartingBefore(1000);
			htmlunit_client.waitForBackgroundJavaScript(7000);
	        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
		}
		if (useTor) {
			proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(TOR_ADDRESS, TOR_PORT));
			properties.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		}
	}
	

	
	public void addRequestProperty(final Pair<String, String> property) {
		properties.put(property.getKey(), property.getValue());
	}
	
	public String getHTML(final URL url) throws Exception {
		if (htmlunit_client != null) {
	        HtmlPage page = htmlunit_client.getPage(url);    
	        return page.asXml();
		}
		else if (proxy == null) {
			return FileUtils.read(url);
		}
		else if (url.toString().startsWith("https")){
			String text;
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(proxy);
			for (String key : properties.keySet()) {
				connection.addRequestProperty(key, properties.get(key));
			}
			connection.setConnectTimeout(7000);
			connection.setReadTimeout(7000);
			connection.connect();
			text = StringUtils.read(connection.getInputStream());
			connection.disconnect();
			return text;
		}
		else if (url.toString().startsWith("http:")) {
			String text;
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
			for (String key : properties.keySet()) {
				connection.addRequestProperty(key, properties.get(key));
			}
			connection.setConnectTimeout(7000);
			connection.setReadTimeout(7000);
			connection.connect();
			text = StringUtils.read(connection.getInputStream());
			connection.disconnect();
			return text;			
		}
		else {
			throw new Exception("Unrecognized protocol: " + url + ".");
		}
	}
	
	public void downloadAll(final Directory directory, final URL url) throws Exception {
		if (htmlunit_client == null) {
			throw new Exception("Not supported.");
		}
        HtmlPage page = htmlunit_client.getPage(url);    
        page.save(directory);
		
	}
	
	public void download(final File file, final URL url) throws Exception {
		if (proxy == null) {
			FileUtils.download(file, url);
		}
		
		InputStream input = null;
		HttpURLConnection connection_ref;
		if (url.toString().startsWith("https")){
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(proxy);
			connection_ref = (HttpURLConnection) connection;
			for (String key : properties.keySet()) {
				connection.addRequestProperty(key, properties.get(key));
			}
			connection.setConnectTimeout(7000);
			connection.setReadTimeout(30000);
			connection.connect();
			input = connection.getInputStream();
		}
		else if (url.toString().startsWith("http:")) {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
			connection_ref = (HttpURLConnection) connection;
			for (String key : properties.keySet()) {
				connection.addRequestProperty(key, properties.get(key));
			}
			connection.setConnectTimeout(7000);
			connection.setReadTimeout(30000);
			connection.connect();
			input = connection.getInputStream();
		}
		else {
			throw new Exception("Unrecognized protocol: " + url + ".");
		}
		if (input == null) {
			throw new Exception("Got no input stream from connection.");
		}
		OutputStream output = new FileOutputStream(file);

		byte[] bytes = new byte[2048];
		int length;

		while ((length = input.read(bytes)) != -1) {
			output.write(bytes, 0, length);
		}
		input.close();
		output.close();
		connection_ref.disconnect();

	}
	
	
	public static void main(String args[]) {
		try {
			Scraper scraper = new Scraper(true, true);
			System.out.println(scraper.getHTML(new URL("http://")));
			scraper.download(new File("C:\\Temp", "temp.jpg"), new URL("http://"));
			scraper.downloadAll(new Directory("C:\\Temp\\tmp"), new URL("http://"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
