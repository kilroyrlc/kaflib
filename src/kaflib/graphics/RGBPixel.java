package kaflib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import kaflib.types.Byte;
import kaflib.types.Percent;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines an argb value.
 */
public class RGBPixel extends Pixel implements Comparable<RGBPixel> {
	private Byte r;
	private Byte g;
	private Byte b;
	
	public static final RGBPixel TRANSPARENT_BLACK = new RGBPixel(0x00000000);
	public static final RGBPixel TRANSPARENT_WHITE = new RGBPixel(0x00ffffff);
	public static final RGBPixel OPAQUE_BLACK = new RGBPixel(0xff000000);
	public static final RGBPixel OPAQUE_WHITE =	new RGBPixel(0xffffffff);
	public static final RGBPixel OPAQUE_RED = new RGBPixel(0xffff0000);
	public static final RGBPixel OPAQUE_GREEN =	new RGBPixel(0xff00ff00);
	public static final RGBPixel OPAQUE_BLUE = new RGBPixel(0xff0000ff);
	
	public RGBPixel() {
		super();
		r = new Byte();
		g = new Byte();
		b = new Byte();
	}

	
	public RGBPixel(final boolean opaque, final int rgb) {
		super(opaque);
		int value = rgb;
		
		b = new Byte(value & 0xff);
		value = value >> 8;
		g = new Byte(value & 0xff);
		value = value >> 8;
		r = new Byte(value & 0xff);
		value = value >> 8;
	}
	
	public RGBPixel(final boolean opaque,
				 final Byte red,
				 final Byte green,
				 final Byte blue) {
		super(opaque);
		r = red;
		g = green;
		b = blue;
	}
	
	public RGBPixel(final boolean opaque,
				 final int red,
				 final int green,
				 final int blue) throws Exception {
		super(opaque);
		r = new Byte(red, true);
		g = new Byte(green, true);
		b = new Byte(blue, true);
	}
	
	public RGBPixel(final int opacity,
				 final int red,
				 final int green,
				 final int blue) throws Exception {
		super(opacity);
		r = new Byte(red);
		g = new Byte(green);
		b = new Byte(blue);
	}

	public RGBPixel(final int argb) {
		super(new Byte((argb >> 24) & 0xff));
		
		int value = argb;
		b = new Byte(value & 0xff);
		value = value >> 8;
		g = new Byte(value & 0xff);
		value = value >> 8;
		r = new Byte(value & 0xff);
		value = value >> 8;
	}
	
	public RGBPixel(final RGBPixel pixel) {
		super(pixel.getOpacity());
		r = new Byte(pixel.getR());
		g = new Byte(pixel.getG());
		b = new Byte(pixel.getB());
	}

	/**
	 * Creates a new pixel that is a blend of a and b.  aDominant determines
	 * the likelihood that a will be the dominant pixel.  blendDominant
	 * determines how to blend, e.g. 90% dominant/10% non.
	 * @param a
	 * @param b
	 * @param aDominant
	 * @param blendDominant
	 */
	public RGBPixel(final RGBPixel a, 
					final RGBPixel b, 
					final Percent aDominant,
					final Percent blendDominant) throws Exception {
		super(Opacity.OPAQUE);
		// A is dominant.
		if (RandomUtils.randomBoolean(aDominant.get())) {
			this.r = Byte.combine(a.getR(), b.getR(), blendDominant);
			this.g = Byte.combine(a.getG(), b.getG(), blendDominant);
			this.b = Byte.combine(a.getB(), b.getB(), blendDominant);
		}
		// B is dominant.
		else {
			this.r = Byte.combine(b.getR(), a.getR(), blendDominant);
			this.g = Byte.combine(b.getG(), a.getG(), blendDominant);
			this.b = Byte.combine(b.getB(), a.getB(), blendDominant);
		}
	}

	public RGBPixel(final RGBPixel a, 
					final RGBPixel b, 
					final Percent pctA) throws Exception {
		super(Opacity.OPAQUE);
		this.r = Byte.combine(a.getR(), b.getR(), pctA);
		this.g = Byte.combine(a.getG(), b.getG(), pctA);
		this.b = Byte.combine(a.getB(), b.getB(), pctA);
	}
	
	
	public Byte getLuminance() {
		return GraphicsUtils.getLuminance(r, g, b);
	}
	
	public void addNoise(final Byte max) throws Exception {
		if (RandomUtils.randomBoolean()) {
			r.addOrMax(Byte.random(max));
		}
		else {
			r.subOrMin(Byte.random(max));
		}
		if (RandomUtils.randomBoolean()) {
			g.addOrMax(Byte.random(max));
		}
		else {
			g.subOrMin(Byte.random(max));
		}
		if (RandomUtils.randomBoolean()) {
			b.addOrMax(Byte.random(max));
		}
		else {
			b.subOrMin(Byte.random(max));
		}
	}
	
	public void lighten(final Byte value) {
		r.addOrMax(value);
		g.addOrMax(value);
		b.addOrMax(value);
	}
	
	public void darken(final Byte value) {
		r.subOrMin(value);
		g.subOrMin(value);
		b.subOrMin(value);
	}
	
	/**
	 * @return the r
	 */
	public Byte getR() {
		return r;
	}

	/**
	 * @param r the r to set
	 */
	public void setR(final Byte r) {
		this.r = r;
	}

	/**
	 * @return the g
	 */
	public Byte getG() {
		return g;
	}

	/**
	 * @param g the g to set
	 */
	public void setG(final Byte g) {
		this.g = g;
	}

	/**
	 * @return the b
	 */
	public Byte getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(final Byte b) {
		this.b = b;
	}
	
	public int getDelta(final RGBPixel other) {
		return Byte.getAbsoluteDifference(r, other.getR()) +
			   Byte.getAbsoluteDifference(g, other.getG()) +
			   Byte.getAbsoluteDifference(b, other.getB());
	}
	
	/**
	 * Returns the average deviation from this pixel value.
	 * @param others
	 * @return
	 * @throws Exception
	 */
	public int getDelta(final Collection<RGBPixel> others) throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		for (RGBPixel other : others) {
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
	public void blend(final RGBPixel other) throws Exception {
		r.combine(other.getR(), other.getOpacity().getPercent());
		g.combine(other.getG(), other.getOpacity().getPercent());
		b.combine(other.getB(), other.getOpacity().getPercent());
	}
	
	public void blend(final RGBPixel other, final Percent otherPercent) throws Exception {
		r.combine(other.getR(), otherPercent);
		g.combine(other.getG(), otherPercent);
		b.combine(other.getB(), otherPercent);
	}
	
	public int getARGB() {
		int value = 0;
		value = Byte.or(value, getOpacity().get());
		value = value << 8;
		value = Byte.or(value, r);
		value = value << 8;
		value = Byte.or(value, g);
		value = value << 8;
		value = Byte.or(value, b);
		return value;
	}
	
	public static RGBPixel getAverage(final Collection<RGBPixel> pixels) throws Exception {
		List<Integer> r = new ArrayList<Integer>();
		List<Integer> g = new ArrayList<Integer>();
		List<Integer> b = new ArrayList<Integer>();
		List<Integer> o = new ArrayList<Integer>();
		for (RGBPixel pixel : pixels) {
			if (pixel == null || 
				pixel.isTransparent()) {
				continue;
			}
			r.add(pixel.getR().getValue());
			g.add(pixel.getG().getValue());
			b.add(pixel.getB().getValue());
			o.add(pixel.getOpacity().getInt());
		}
		return new RGBPixel(MathUtils.average(o),
						 MathUtils.average(r),
						 MathUtils.average(g),
						 MathUtils.average(b));
		
	}
	
	public static RGBPixel getMedianByLuminance(final List<RGBPixel> pixels) throws Exception {
		Collections.sort(pixels);
		return pixels.get(pixels.size() / 2);
	}
	
	public static RGBPixel getMedianByRGB(final Collection<RGBPixel> pixels) throws Exception {
		List<Byte> r = new ArrayList<Byte>();
		List<Byte> g = new ArrayList<Byte>();
		List<Byte> b = new ArrayList<Byte>();
		for (RGBPixel pixel : pixels) {
			if (pixel != null && pixel.isOpaque()) {
				r.add(pixel.getR());
				g.add(pixel.getG());
				b.add(pixel.getB());
			}
		}
		
		Collections.sort(r);
		Collections.sort(g);
		Collections.sort(b);
		return new RGBPixel(true, r.get(r.size() / 2), g.get(g.size() / 2), b.get(b.size() / 2));
	}
	
	public String toString() {
		return super.toString() + r.toString() + g.toString() + b.toString();
	}

	@Override
	public int compareTo(RGBPixel o) {
		return getLuminance().compareTo(o.getLuminance());
	}
	
	public static RGBPixel getRandomOpaque() throws Exception {
		return new RGBPixel(true, RandomUtils.randomInt());
	}
	
}
