package kaflib.types;

import java.io.Serializable;

/**
 * Defines an integer percent type.
 */
public class Percent implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int min;
	private final int max;
	private int value;
	
	public Percent(final int value) throws Exception {
		this(value, 0, 100);
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
	
	public void set(final int newValue) throws Exception {
		if (newValue < min || newValue > max) {
			throw new Exception("Invalid min/value/max: " + min + " / " + value + " / " + max + ".");
		}
		value = newValue;
	}
	
	public int get() {
		return value;
	}
	
	public String toString() {
		return value + "%";
	}
	
	public int of(final int value) throws Exception {
		return (this.value * value) / 100;
	}
}
