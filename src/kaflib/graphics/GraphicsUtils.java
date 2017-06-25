package kaflib.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import kaflib.types.Percent;

/**
 * Graphics utilites.
 */
public class GraphicsUtils {

	/**
	 * Creates a copy of the buffered image.
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage copy(final BufferedImage input) throws Exception {
		ColorModel model = input.getColorModel();
		boolean alpha = model.isAlphaPremultiplied();
		WritableRaster raster = input.copyData(null);
		return new BufferedImage(model, raster, alpha, null);	
	}
	
	/**
	 * Returns the red channel.
	 * @param rgb
	 * @return
	 * @throws Exception
	 */
	public static int getRed(final int rgb) throws Exception {
		return (rgb >> 16) & 0xff;
	}
	
	/**
	 * Returns the green channel.
	 * @param rgb
	 * @return
	 * @throws Exception
	 */
	public static int getGreen(final int rgb) throws Exception {
		return (rgb >> 8) & 0xff;
	}
	
	/**
	 * Returns the blue channel.
	 * @param rgb
	 * @return
	 * @throws Exception
	 */
	public static int getBlue(final int rgb) throws Exception {
		return rgb & 0xff;
	}
	
	/**
	 * Blends r, g, and b into originalRGB by the specified percent.
	 * @param r
	 * @param g
	 * @param b
	 * @param percent
	 * @param originalRGB
	 * @return
	 * @throws Exception
	 */
	public static int blendRGB(int r, int g, int b, 
							   final Percent percent, final int originalRGB) throws Exception {
		return getRGB(Math.min(0xff, Math.max(0, r + percent.of(r - getRed(originalRGB)))),
					  Math.min(0xff, Math.max(0, g + percent.of(g - getGreen(originalRGB)))),
					  Math.min(0xff, Math.max(0, b + percent.of(b - getBlue(originalRGB)))));
	}	
	
	/**
	 * Blends r, g, and b into originalRGB by the specified percent.
	 * @param blend
	 * @param percent
	 * @param original
	 * @return
	 * @throws Exception
	 */
	public static int blendRGB(final int blend, final Percent percent, final int original) throws Exception {
		return blendRGB(getRed(blend), getGreen(blend), getBlue(blend), percent, original);
	}
	
	/**
	 * Averages the two pixel values.
	 * @param rgb_a
	 * @param rgb_b
	 * @return
	 * @throws Exception
	 */
	public static int averageRGB(final int rgb_a, final int rgb_b) throws Exception {
		return getRGB((getRed(rgb_a) + getRed(rgb_b)) / 2,
					(getGreen(rgb_a) + getGreen(rgb_b)) / 2,
					(getBlue(rgb_a) + getBlue(rgb_b)) / 2);
	}
	
	/**
	 * Returns the color distance of each channel between the given pixels.
	 * @param rgb_a
	 * @param rgb_b
	 * @return
	 * @throws Exception
	 */
	public static int getDistance(final int rgb_a, final int rgb_b) throws Exception { 
		return Math.abs(getRed(rgb_a) - getRed(rgb_b)) +
			   Math.abs(getGreen(rgb_a) - getGreen(rgb_b)) +
			   Math.abs(getBlue(rgb_a) - getBlue(rgb_b));
							
	}
	
	/**
	 * Returns the r/g/b channels as a single value.
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static int getRGB(final int r, final int g, final int b) throws Exception {
		int rgb = r & 0xff;
		rgb = rgb << 8;
		rgb = rgb | (g & 0xff);
		rgb = rgb << 8;
		rgb = rgb | (b & 0xff);
		return rgb;
	}
}

