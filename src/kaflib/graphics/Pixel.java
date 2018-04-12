package kaflib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kaflib.types.Byte;
import kaflib.utils.MathUtils;

/**
 * Defines an argb value with knowledge of its position.
 */
public class Pixel {
	private Opacity opacity;
	private Byte r;
	private Byte g;
	private Byte b;
	
	public static final int TRANSPARENT_BLACK = 0x00000000;
	public static final int TRANSPARENT_WHITE = 0x00ffffff;
	public static final int OPAQUE_BLACK = 		0xff000000;
	public static final int OPAQUE_WHITE =	 	0xffffffff;
	
	public Pixel() throws Exception {
		opacity = new Opacity();
		r = new Byte();
		g = new Byte();
		b = new Byte();
	}

	public Pixel(final boolean opaque, final int rgb) throws Exception {
		int value = 0;
		if (opaque) {
			value = 0xff;
		}
		
		b = new Byte(value & 0xff);
		value = value >> 8;
		g = new Byte(value & 0xff);
		value = value >> 8;
		r = new Byte(value & 0xff);
		value = value >> 8;
		opacity = new Opacity(value & 0xff);
	}
	
	public Pixel(final int opacity,
				 final int red,
				 final int green,
				 final int blue) throws Exception {
		this.opacity = new Opacity(opacity);
		r = new Byte(red);
		g = new Byte(green);
		b = new Byte(blue);
	}

	public Pixel(final int argb) throws Exception {
		int value = argb;
		b = new Byte(value & 0xff);
		value = value >> 8;
		g = new Byte(value & 0xff);
		value = value >> 8;
		r = new Byte(value & 0xff);
		value = value >> 8;
		opacity = new Opacity(value & 0xff);
	}

	/**
	 * Returns whether or not this pixel is compeletely opaque.
	 * @return
	 */
	public boolean isOpaque() {
		return opacity.getInt() == Opacity.OPAQUE;
	}
	
	/**
	 * Returns whether or not this pixel is completely transparent.
	 * @return
	 */
	public boolean isTransparent() {
		return opacity.getInt() == Opacity.TRANSPARENT;
	}
	
	/**
	 * @return the opacity
	 */
	public Opacity getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(final Opacity opacity) {
		this.opacity = opacity;
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
	
	public int getDelta(final Pixel other) {
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
	public int getDelta(final Collection<Pixel> others) throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		for (Pixel other : others) {
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
	public void blend(final Pixel other) throws Exception {
		r.combine(other.getR(), other.getOpacity().getPercent());
		g.combine(other.getG(), other.getOpacity().getPercent());
		b.combine(other.getB(), other.getOpacity().getPercent());
	}
	
	public int getARGB() {
		int value = 0;
		value = Byte.or(value, opacity.get());
		value = value << 8;
		value = Byte.or(value, r);
		value = value << 8;
		value = Byte.or(value, g);
		value = value << 8;
		value = Byte.or(value, b);
		return value;
	}
	
	public static Pixel getAverage(final Collection<Pixel> pixels) throws Exception {
		List<Integer> r = new ArrayList<Integer>();
		List<Integer> g = new ArrayList<Integer>();
		List<Integer> b = new ArrayList<Integer>();
		List<Integer> o = new ArrayList<Integer>();
		for (Pixel pixel : pixels) {
			r.add(pixel.getR().getValue());
			g.add(pixel.getG().getValue());
			b.add(pixel.getB().getValue());
			o.add(pixel.getOpacity().getInt());
		}
		return new Pixel(MathUtils.average(o),
						 MathUtils.average(r),
						 MathUtils.average(g),
						 MathUtils.average(b));
		
	}
	
	public String toString() {
		return "#" + opacity.toString() + r.toString() + g.toString() + b.toString();
	}
}
