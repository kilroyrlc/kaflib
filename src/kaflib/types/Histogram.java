package kaflib.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaflib.utils.StringUtils;

/**
 * Defines a histogram, a map of values to counts.
 * @param <T>
 */
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
		
		if (histogram.get(item) > 0) {
			if (Integer.MAX_VALUE - histogram.get(item) < amount) {
				throw new Exception("Overflow of: " + item.toString() + 
						 			" trying to increment " + histogram.get(item) + 
						 			" by " + amount + ".");
			}
		}
		
		histogram.put(item, histogram.get(item) + amount);
	}
	
	/**
	 * Returns the most frequent entry in the histogram, or null if it is 
	 * empty.
	 * @return
	 */
	public T getMostFrequent() {
		T most = null;		
		int max = 0;
		for (T key : histogram.keySet()) {
			if (histogram.get(key) > max) {
				max = histogram.get(key);
				most = key;
			}
		}
		return most;
	}
	
	/**
	 * Returns the values ranked most frequent to least.
	 * @return
	 */
	public List<T> getRanked() {
		List<T> list = new ArrayList<T>();
		list.addAll(histogram.keySet());
		Collections.sort(list, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				if (histogram.get(o1) > histogram.get(o2)) {
					return -1;
				}
				else if (histogram.get(o1) < histogram.get(o2)) {
					return 1;
				}
				else {
					return 0;
				}
			}
		});
		return list;
	}

	/**
	 * Returns the histogram as a line:
	 * (key, count);(key, count);(key, count);...
	 * @return
	 * @throws Exception
	 */
	public String toSerialLine() throws Exception {
		StringBuffer buffer = new StringBuffer();
		
		for (T item : histogram.keySet()) {
			if (item.toString().contains(");(")) {
				throw new Exception("Name contains reserved string.");
			}
			
			buffer.append("(");
			buffer.append(item.toString());
			buffer.append(",");
			buffer.append(histogram.get(item));
			buffer.append(");");
		}
		return new String(buffer);
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

	/**
	 * Reads a histogram from the serialized form.
	 * @param serial
	 * @throws Exception
	 */
	public static Histogram<String> createHistogram(final String serial) throws Exception {
		Histogram<String> histogram = new Histogram<String>();
		
		String values[] = serial.split("\\)\\;\\(");
		// Chomp the leading and trailing ();
		values[0] = values[0].substring(1);
		values[values.length - 1] = values[values.length - 1].
								substring(0, values[values.length - 1].length() - 2);
		
		for (String value : values) {
			int split = value.lastIndexOf(',');
			if (split < 0) {
				throw new Exception("No comma delimiter for " + value + ".");
			}
			histogram.increment(value.substring(0, split), 
						  StringUtils.toInt(value.substring(split + 1).trim()));
		}
		return histogram;
	}
	
}
