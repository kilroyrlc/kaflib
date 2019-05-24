package kaflib.types;

import java.util.Collection;

public class IntegerHistogram extends Histogram<Integer> {

	private final int min;
	private final int max;
	private final int step;
	
	public IntegerHistogram(final int min, final int max, final int step) throws Exception {
		super();
		this.min = min;
		this.max = max;
		this.step = step;
	}
	
	private void check(final int value) throws Exception {
		if (value < min || value > max) {
			throw new Exception("Invalid value: " + value + " for range: " + min + "-" + max + ".");
		}
	}
	
	public void increment(final int value) throws Exception {
		increment(value, 1);
	}
	
	public void increment(final int value, final int amount) throws Exception {
		check(value);
		
		int bin = min + step;
		while (bin < value) {
			bin += step;
		}
		super.increment(bin - step, amount);
		
	}
	
	public void increment(final Collection<Integer> items) throws Exception {
		increment(items, 1);
	}
	
	public void increment(final Collection<Integer> items, int amount) throws Exception {
		for (Integer item : items) {
			increment(item, amount);
		}
	}
	
}
