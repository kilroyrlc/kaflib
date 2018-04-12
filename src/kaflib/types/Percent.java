package kaflib.types;

import java.io.Serializable;

/**
 * Defines an integer percent type.
 */
public class Percent implements Serializable {
	public static final Percent ZERO = new Percent(0);
	public static final Percent ONE_HUNDRED = new Percent(100);
	
	private static final long serialVersionUID = 1L;
	private final int min;
	private final int max;
	private int value;
	
	public Percent(final int value) {
		this.value = value;
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
	}

	public Percent(final float value) {
		this.value = (int) (value * 100);
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
	}
	
	public Percent(final String value) throws Exception {
		String temp = new String(value);
		temp = temp.trim();
		if (temp.endsWith("%")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		temp = temp.trim();

		this.min = 0;
		this.max = 100;
		this.value = Integer.valueOf(temp);
		if (min >= max || this.value < min || this.value > max) {
			throw new Exception("Invalid min/value/max: " + min + " / " + value + " / " + max + ".");
		}
	}
	
	public Percent(final int value, final int min, final int max) throws Exception {
		if (min >= max || value < min || value > max) {
			throw new Exception("Invalid min/value/max: " + min + " / " + value + " / " + max + ".");
		}
		
		this.min = min;
		this.max = max;
		this.value = value;
	}
	
	public float getFloat() throws Exception {
		return ((float) value) / 100;
	}
	
	public double getDouble() {
		return ((double) value) / 100;
	}
	
	public void set(final int newValue) throws Exception {
		if (newValue < min || newValue > max) {
			throw new Exception("Invalid min/value/max: " + min + " / " + value + " / " + max + ".");
		}
		value = newValue;
	}
	
	public int get() {
		return value;
	}
	
	public Percent getComplement() throws Exception {
		if (value > 100) {
			throw new Exception("No complement for " + toString() + ".");
		}
		return new Percent(100 - value);
	}
	
	public String toString() {
		return value + "%";
	}
	
	public int of(final int value) throws Exception {
		return (this.value * value) / 100;
	}
}
