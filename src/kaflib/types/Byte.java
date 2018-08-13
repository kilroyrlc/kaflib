package kaflib.types;

import java.util.Collections;
import java.util.List;

import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines an 8-bit unsigned type.
 */
public class Byte implements Comparable<Byte> {
	private int value;
	
	public static final Byte FF = new Byte(0xff);
	
	public Byte() {
		value = 0;
	}
	
	public Byte(final double value) {
		this((int) value);
	}
	
	public Byte(final Byte value) {
		this.value = value.getValue();
	}
	
	public Byte(final int value) {
		this.value = value & 0xff;
	}
	
	public Byte(final int value, final boolean checkOverflow) throws Exception {
		if (checkOverflow) {
			set(value);
		}
		else {
			this.value = value & 0xff;
		}
	}
	
	public Byte(final Percent percent) {
		value = (percent.get() * 255) / 100;
	}
	
	public final void set(final int value) throws Exception {
		if (value > 0xff) {
			throw new Exception("Value greater than 255: " + value + ".");
		}
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public void add(final Byte other) throws Exception {
		if (value + other.getValue() > 0xff) {
			throw new Exception("Value greater than 255: " + 
								(value + other.getValue()) + ".");
		}		
		value += other.getValue();
	}
	
	public void addOrMax(final Byte other) {
		value += other.getValue();
		if (value > 255) {
			value = 255;
		}
	}
	
	public void subOrMin(final Byte other) {
		value -= other.getValue();
		if (value < 0) {
			value = 0;
		}
	}
	
	/**
	 * Takes a percentage of the byte value.
	 * @param percent
	 * @return
	 * @throws Exception
	 */
	public void multiply(final Percent percent) throws Exception {
		value = (value * percent.get()) / 100;
	}
	
	/**
	 * Sets to other * otherPct + this * (100 - otherPercent)
	 * @param other
	 * @param otherPct
	 * @throws Exception
	 */
	public void combine(final Byte other, final Percent otherPct) throws Exception {
		CheckUtils.checkRange(otherPct.get(), 0, 100);
		multiply(otherPct.getComplement());
		Byte o = other;
		o.multiply(otherPct);
		add(o);
	}
	
	public static Byte combine(final Byte a, final Byte b, final Percent aPct) throws Exception {
		CheckUtils.checkRange(aPct.get(), 0, 100);
		return new Byte(((a.getValue() * aPct.get()) / 100) +
					    ((b.getValue() * aPct.getComplement().get()) / 100),
					    true);
	}
	
	public void sub(final Byte other) throws Exception {
		if (value < other.getValue()) {
			throw new Exception("Value less than 0: " + 
							    (value - other.getValue()) + ".");
		}		
		value -= other.getValue();
	}
	
	public static int getAbsoluteDifference(final Byte a, final Byte b) {
		return Math.abs(a.getValue() - b.getValue());
	}
	
	public int hashCode() {
		return String.valueOf(value).hashCode();
	}

	public boolean equals(final Object other) {
		if (other instanceof Byte) {
			return equals((Byte) other);
		}
		else {
			return false;
		}
	}
		
	public Percent getPercent() throws Exception {
		return new Percent(0, 255, value);
	}
	
	public boolean equals(final Byte other) {
		return value == other.getValue();
	}

	public String toString() {
		return String.format("%02x", value);
	}
	
	@Override
	public int compareTo(Byte o) {
		if (value < o.getValue()) {
			return -1;
		}
		else if (value > o.getValue()) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	public static Byte random(final Byte max) throws Exception {
		return new Byte(RandomUtils.randomInt(0, max.getValue()));
	}
	
	public static int or(final int i, final Byte b) {
		return i | b.getValue();
	}
	
	/**
	 * Returns the median from the collection.  Sorts the collection instead
	 * of copying it.
	 * @param values
	 * @return
	 */
	public static Byte getMedian(List<Byte> values) {
		Collections.sort(values);
		return values.get(values.size() / 2);
	}

	public static int or(final Byte msb, 
						 final Byte smsb, 
						 final Byte slsb, 
						 final Byte lsb) {
		int value = msb.getValue();
		value = value << 8;
		value = value | smsb.getValue();
		value = value << 8;
		value = value | slsb.getValue();
		value = value << 8;
		value = value | lsb.getValue();
		return value;
	}

}
