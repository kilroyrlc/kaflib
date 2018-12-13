package kaflib.web;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaflib.utils.FileUtils;
import kaflib.utils.StringUtils;


public class GeoIP {
	
	public static final File directory = new File("data");

	public static final File ipv4 = new File(directory, "ipv4.csv");
	public static final File ipv6 = new File(directory, "ipv6.csv");
	public static final File locations = new File(directory, "locations.csv");

	private final Map<IP, String> data;
	
	public GeoIP() throws Exception {
		if (!ipv4.exists() || !ipv6.exists() || !locations.exists()) {
			throw new Exception("Data files do not exist: " + ipv4.getAbsolutePath() + ".");
		}
		data = new HashMap<IP, String>();
		readFile();
	}
	
	private void readFile() throws Exception {
		BufferedReader reader = FileUtils.getReader(ipv4);
		String line = reader.readLine();

		// Read the ipv4 data.
		while (line != null) {
			List<String> tokens = StringUtils.parse(line);
			if (tokens.get(0).indexOf('/') == -1) {
				data.put(new IP(tokens.get(0)), "" + tokens.get(1));
			}
			else {
				data.put(new IP(tokens.get(0).substring(0, tokens.get(0).indexOf('/'))), 
						 "" + tokens.get(1));
			}
			line = reader.readLine();
		}		
		
		// Read the country data.
		reader = FileUtils.getReader(locations);
		line = reader.readLine();
		while (line != null) {
			List<String> tokens = StringUtils.parse(line);
			for (IP ip : data.keySet()) {
				if (data.get(ip).equals(tokens.get(0))) {
					if (tokens.size() == 6) {
						data.put(ip, tokens.get(5));
					}
					else if (tokens.size() == 4 || tokens.size() == 5) {
						data.put(ip, tokens.get(3));
					}
					else {
						throw new Exception("Invalid line:\n" + line);
					}
				}
			}
			
			line = reader.readLine();
		}
		
	}

	public String getExact(final IP ip) throws Exception {
		if (data.containsKey(ip)) {
			return data.get(ip);
		}
		return null;
	}
	
	public String getNearest(final IP ip) throws Exception {
		if (getExact(ip) != null){
			return getExact(ip);
		}
		
		IP value = null;
		int digits = 0;
		int difference = Integer.MAX_VALUE;

		for (IP element : data.keySet()) {
			for (int i = 0; i < ip.getAddress().length; i++) {
				int diff = Math.abs(ip.getAddress()[i] - element.getAddress()[i]);
				if (diff != 0) {
					if (value == null || i > digits || (i == digits && diff < difference)) {
						value = element;
						digits = i;
						difference = diff;
					}
					break;
				}
			}
		}
		
		data.put(ip, data.get(value));
		
		return data.get(value);
	}
	
	private static GeoIP singleton = null;
	
	public static GeoIP getStatic() throws Exception {
		if (singleton == null) {
			singleton = new GeoIP();
		}
		return singleton;
	}
	
}
