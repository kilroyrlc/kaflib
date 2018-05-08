package kaflib.graphics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kaflib.types.Byte;

/**
 * Defines an argb value with knowledge of its position.
 */
public abstract class Pixel {
	private Opacity opacity;
	
	protected Pixel() {
		opacity = new Opacity();
	}

	
	protected Pixel(final boolean opaque) {
		if (!opaque) {
			opacity = Opacity.TRANSPARENT;
		}
		else {
			opacity = Opacity.OPAQUE;
		}
	}
	
	protected Pixel(final int opacity) throws Exception {
		this.opacity = new Opacity(opacity);
	}

	protected Pixel(final Byte opacity) {
		this.opacity = new Opacity(opacity);
	}
	
	protected Pixel(final Opacity opacity) {
		this.opacity = opacity;
	}
	
	/**
	 * Returns whether or not this pixel is compeletely opaque.
	 * @return
	 */
	public boolean isOpaque() {
		return opacity.equals(Opacity.OPAQUE);
	}
	
	/**
	 * Returns whether or not this pixel is completely transparent.
	 * @return
	 */
	public boolean isTransparent() {
		return opacity.equals(Opacity.TRANSPARENT);
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
	
	public Opacity getMedianOpacity(List<Pixel> pixels) throws Exception { 
		Collections.sort(pixels, new Comparator<Pixel>(){
			@Override
			public int compare(Pixel o1, Pixel o2) {
				return o1.getOpacity().compareTo(o2.getOpacity());
			}});
		return pixels.get(pixels.size() / 2).getOpacity();
	}
	

	public abstract int getARGB();
	
	public String toString() {
		return "#" + opacity.toString();
	}


}
