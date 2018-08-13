package kaflib.web;

import java.util.Arrays;
import java.util.List;

import kaflib.utils.StringUtils;

public class IP {
	private final int address[];
	private final int port;
	private final String as_string;
	
	public IP(final IP ip) {
		address = Arrays.copyOf(ip.getAddress(), ip.getAddress().length);
		port = ip.getPort();
		as_string = ip.toString();
	}
	
	public IP(final String s) throws Exception {
		String string = s;
		
		if (string.contains(":")) {
			port = Integer.valueOf(string.substring(string.indexOf(":") + 1));
			string = string.substring(0, string.indexOf(":"));
		}
		else {
			port = -1;
		}
		
		List<String> ips = StringUtils.parse(string, ".", true);
		if (ips.size() < 4) {
			throw new Exception("Invalid ip: " + s + " length: " + ips.size() + ".");
		}
		address = new int[ips.size()];
		for (int i = 0; i < ips.size(); i++) {
			address[i] = Integer.valueOf(ips.get(i));
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(address[0]);
		for (int i = 1; i < address.length; i++) {
			buffer.append(".");
			buffer.append(address[i]);
		}
		if (port > 1) {
			buffer.append(":");
			buffer.append(port);
		}
		as_string = new String(buffer);
	}
	
	public int[] getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean startsWith(final String substring) {
		return as_string.startsWith(substring);
	}
	
	public boolean equals(final IP other) {
		if (address.length != other.getAddress().length) {
			return false;
		}
		for (int i = 0; i < address.length; i++) {
			if (address[i] != other.getAddress()[i]) {
				return false;
			}
		}
		return true;
	}
	
	public String toString() {
		return as_string;
	}
	
	public int hashCode() {
		return as_string.hashCode();
	}
	
	public static IP create(final String ip) {
		try {
			return new IP(ip);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
