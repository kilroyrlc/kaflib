package kaflib.graphics;

import kaflib.types.Percent;
import kaflib.types.Byte;

public class Opacity {
	private final Byte value;

	public static final int OPAQUE = 	  		0xff;
	public static final int THREE_QUARTER =		0xbe;
	public static final int HALF =		 		0x7f;
	public static final int QUARTER =	 		0x3f;
	public static final int TRANSPARENT = 		0x00;
	
	public Opacity() throws Exception {
		this.value = new Byte(0xff);
	}
	
	public Opacity(final int value) throws Exception {
		this.value = new Byte(value);
	}

	public Opacity(final Byte value) throws Exception {
		this.value = value;
	}

	public Opacity(final Percent percent) throws Exception {
		this.value = new Byte(percent);
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
	
}
