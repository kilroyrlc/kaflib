package kaflib.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import kaflib.utils.StringUtils;

public class Histogram<T> {
	private final Map<T, Integer> histogram;
	private Integer ceiling;
	
	public Histogram() {
		histogram = new HashMap<T, Integer>();
		ceiling = null;
	}

	public Histogram(final int ceiling) {
		histogram = new HashMap<T, Integer>();
		this.ceiling = ceiling;
	}

	
	public void setCeiling(final int ceiling) {
		this.ceiling = ceiling;
	}
	
	public void increment(final Collection<T> items) throws Exception {
		increment(items, 1);
	}
	
	public void increment(final Collection<T> items, int amount) throws Exception {
		for (T item : items) {
			increment(item, amount);
		}
	}

	public void increment(final T item) throws Exception {
		increment(item, 1);
	}
	
	public void increment(final T item, final int amount) throws Exception {
		if (!histogram.containsKey(item)) {
			histogram.put(item, 0);
		}
		
		if (ceiling != null && ceiling - histogram.get(item) < amount) {
			histogram.put(item, ceiling);
			return;
		}
		
		if (Integer.MAX_VALUE - histogram.get(item) < amount) {
			throw new Exception("Overflow of: " + item.toString());
		}
		
		histogram.put(item, histogram.get(item) + amount);
	}

	public String contentsToString() throws Exception {
		return contentsToString(0);
	}
	
	public String contentsToString(final int threshold) throws Exception {
		StringBuffer buffer = new StringBuffer();
		int length = 0;
		for (T item : histogram.keySet()) {
			if (item.toString().length() > length) {
				length = item.toString().length();
			}
		}
		
		for (T item : histogram.keySet()) {
			if (histogram.get(item) >= threshold) {
				buffer.append(StringUtils.resize(item.toString(), length));
				buffer.append(": ");
				buffer.append(histogram.get(item));
				buffer.append("\n");
			}
		}
		return new String(buffer);
	}
	
	public int get(final T item) {
		if (!histogram.containsKey(item)) {
			return 0;
		}
		else {
			return histogram.get(item);
		}
	}
	
}
