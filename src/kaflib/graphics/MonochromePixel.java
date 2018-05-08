package kaflib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import kaflib.types.Byte;
import kaflib.types.Percent;
import kaflib.utils.CheckUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines an monochrome pixel.
 */
public class MonochromePixel extends Pixel implements Comparable<MonochromePixel> {
	public static final int MAX = 0x00ffffff;
	public static final int MIN = 0x00000000;

	private int value;
	
	public MonochromePixel() {
		super();
		value = 0;
	}
	
	public MonochromePixel(final int value) throws Exception {
		super();
		setValue(value);
	}
	
	public MonochromePixel(final int opacity, 
			   			   final int value) throws Exception {
		super(opacity);
		setValue(value);
	}
	
	public MonochromePixel(final Opacity opacity, 
						   final int value) throws Exception {
		super(opacity);
		setValue(value);
	}
	
	public MonochromePixel(final MonochromePixel pixel) {
		super(pixel.getOpacity());
		value = pixel.getValue();
	}

//	public MonochromePixel(final RGBPixel pixel, 
//						   boolean useLuminance) throws Exception {
//		super(pixel.getOpacity());
//		
//		if (useLuminance) {
//			setValue(pixel.getLuminance());
//		}
//		else {
//			setValue(pixel.getR().getValue() + 
//					 pixel.getG().getValue() + 
//					 pixel.getB().getValue());
//		}
//	}

	
	public int getValue() {
		return value;
	}

	public final void setValue(final int value) throws Exception {
		CheckUtils.checkRange(value, MIN, MAX);
		this.value = value;
	}
	
	public int getDelta(final MonochromePixel other) {
		return Math.abs(other.getValue() - value);
	}
	
	/**
	 * Returns the average deviation from this pixel value.
	 * @param others
	 * @return
	 * @throws Exception
	 */
	public int getDelta(final Collection<MonochromePixel> others) throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		for (MonochromePixel other : others) {
			deltas.add(getDelta(other));
		}
		return MathUtils.average(deltas);
	}
	
	/**
	 * Combines other with this pixel.  Uses the other transparency value
	 * to determine how much to blend.  This transparency value is not factored
	 * into the computation but remains.
	 * @param other
	 * @throws Exception
	 */
	public void blend(final MonochromePixel other) throws Exception {
		Percent other_percent = other.getOpacity().getPercent();
		CheckUtils.checkRange(other_percent.get(), 0, 100);

		// Scale this down to the complement of the other pixel's opacity.
		value = (value * other_percent.getComplement().get()) / 100;
		int other_value = (other.getValue() * other_percent.get()) / 100;
		value += other_value;
	}

	public int getARGB() {
		Byte channel = new Byte(value / 3);
		value = Byte.or(value, getOpacity().get());
		value = value << 8;
		value = Byte.or(value, channel);
		value = value << 8;
		value = Byte.or(value, channel);
		value = value << 8;
		value = Byte.or(value, channel);
		return value;
	}
	
	public static MonochromePixel getAverage(final Collection<MonochromePixel> pixels) throws Exception {
		List<Integer> values = new ArrayList<Integer>();
		List<Integer> o = new ArrayList<Integer>();
		for (MonochromePixel pixel : pixels) {
			if (pixel == null || 
				pixel.isTransparent()) {
				continue;
			}
			values.add(pixel.getValue());
			o.add(pixel.getOpacity().getInt());
		}
		return new MonochromePixel(MathUtils.average(o),
						 MathUtils.average(values));
		
	}
	
	public static MonochromePixel getMedianByRGB(final Collection<MonochromePixel> pixels) throws Exception {
		List<Integer> list = new ArrayList<Integer>();
		for (MonochromePixel pixel : pixels) {
			if (pixel != null && pixel.isOpaque()) {
				list.add(pixel.getValue());
			}
		}
		
		Collections.sort(list);
		return new MonochromePixel(list.get(list.size() / 2));
	}
	
	public String toString() {
		return super.toString() + String.format("%06x", value);
	}

	@Override
	public int compareTo(MonochromePixel o) {
		if (value > o.getValue()) {
			return 1;
		}
		else if (value < o.getValue()) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	public static MonochromePixel getRandomOpaque() throws Exception {
		return new MonochromePixel(RandomUtils.randomInt(MAX));
	}
	
}
