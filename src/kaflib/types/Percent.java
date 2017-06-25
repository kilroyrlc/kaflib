package kaflib.types;

/**
 * Defines an integer percent type.
 */
public class Percent {
	private final int min;
	private final int max;
	private int value;
	
	public Percent(final int value) throws Exception {
		this(value, 0, 100);
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
