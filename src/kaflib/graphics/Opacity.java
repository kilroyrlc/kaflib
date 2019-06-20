package kaflib.graphics;

import kaflib.types.Percent;
import kaflib.types.Byte;

/**
 * Defines an opacity type for ARGB.
 */
public class Opacity implements Comparable<Opacity> {
	private final Byte value;

	public static final Opacity OPAQUE = 	  		new Opacity(new Byte(0xff));
	public static final Opacity THREE_QUARTER =		new Opacity(new Byte(0xbe));
	public static final Opacity HALF =		 		new Opacity(new Byte(0x7f));
	public static final Opacity QUARTER =	 		new Opacity(new Byte(0x3f));
	public static final Opacity TRANSPARENT = 		new Opacity(new Byte(0x00));
	
	public Opacity() {
		this.value = new Byte(0xff);
	}
	
	public Opacity(final int value) throws Exception {
		this.value = new Byte(value);
	}

	public Opacity(final Byte value) {
		this.value = value;
	}

	public Opacity(final Percent percent) throws Exception {
		this.value = new Byte(percent);
	}
	
	public boolean greaterThanThreeQuarter() {
		return getInt() > THREE_QUARTER.getInt();
	}
	
	public boolean greaterThanHalf() {
		return getInt() > HALF.getInt();
	}
	
	/**
	 * Returns the opacity as a percentage of 255.
	 * @return
	 * @throws Exception
	 */
	public Percent getPercent() throws Exception {
		return value.getPercent();
	}
	
	public int getInt() {
		return value.getValue();
	}
	
	public Byte get() {
		return value;
	}
	
	public String toString() {
		return value.toString();
	}

	@Override
	public int compareTo(Opacity o) {
		if (value.getValue() > o.getInt()) {
			return 1;
		}
		else if (value.getValue() < o.getInt()) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
}
