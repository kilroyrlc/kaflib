package kaflib.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a sequential list of boolean values.
 */
public class Bitstream {

	private List<Boolean> bits;
	
	/**
	 * Creates the type.
	 * @param bytes
	 * @throws Exception
	 */
	public Bitstream(final byte bytes[]) throws Exception {
		bits = new ArrayList<Boolean>();
		
		for (byte b : bytes) {
			boolean t[] = getBits(b);
			for (boolean z : t) {
				bits.add(z);
			}
		}
	}

	/**
	 * Returns whether or not the stream is empty.
	 * @return
	 */
	public boolean isEmpty() {
		return bits.isEmpty();
	}
	
	/**
	 * Removes the specified number of bits, places them in the integer.
	 * To support 32-bit, the max count is 32.
	 * @param bitCount
	 * @return
	 * @throws Exception
	 */
	public int remove(final int bitCount) throws Exception {
		if (bitCount > 32) {
			throw new Exception("Max bit count is 32.");
		}
		
		int value = 0;
		
		for (int i = 0; i < bitCount; i++) {
			if (bits.size() == 0) {
				return value;
			}
			value = value << 1;
			if (bits.remove(0)) {
				value = value | 1;
			}
		}
		
		return value;
	}
	
	
	/**
	 * Converts the specified byte to an array of boolean.
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static final boolean[] getBits(final byte b) throws Exception {
		boolean bits[] = new boolean[8];
		int asInt = b;
		
		for (int i = 0; i < 8; i++) {
			if ((asInt & 0x01) == 0) {
				bits[i] = false;
			}
			else {
				bits[i] = true;
			}
			asInt = asInt >> 1;
		}
		
		return bits;
	}
	
}
