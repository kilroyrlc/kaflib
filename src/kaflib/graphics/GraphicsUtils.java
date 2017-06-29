package kaflib.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import kaflib.types.Percent;
import kaflib.utils.CheckUtils;

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
	
	
	/**
	 * Converts and image to a buffered image.
	 * @param image
	 * @param type
	 * @return
	 */
	public static BufferedImage toBufferedImage(final Image image, int type) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage) image;
	    }
	
	    BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(image, 0, 0, null);
	    bGr.dispose();
	
	    return bimage;
	}

	/**
	 * Scales the image up.
	 * @param image
	 * @param factor
	 * @return
	 */
	public static BufferedImage getScaledUp(final BufferedImage image, int factor) {
		
		return GraphicsUtils.toBufferedImage(
				image.getScaledInstance(image.getWidth() * factor, 
										image.getHeight() * factor, 
										Image.SCALE_SMOOTH), image.getType());
	}
	
	/**
	 * Scales the image down.
	 * @param image
	 * @param factor
	 * @return
	 */
	public static BufferedImage getScaledDown(final BufferedImage image, int factor) {
		
		return GraphicsUtils.toBufferedImage(
				image.getScaledInstance(image.getWidth() / factor, 
										image.getHeight() / factor, 
										Image.SCALE_SMOOTH), image.getType());
	}
	
	/**
	 * Returns a cropped version of htis image.
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage getCropped(final BufferedImage image, 
										   final int x, 
										   final int y, 
										   final int width, 
										   final int height) throws Exception {
		CheckUtils.checkNonNegative(x);
		CheckUtils.checkNonNegative(y);
		CheckUtils.checkPositive(width);
		CheckUtils.checkPositive(height);
		
		if (x + width >= image.getWidth() || y + height >= image.getHeight()) {
			throw new Exception("Invalid crop (" + x + ", " + y + ") " + width + "x" + height + 
					            " for " + image.getWidth() + "x" + image.getHeight());
		}
		
		try {
			return image.getSubimage(x, y, width, height);
		}
		catch (Exception e) {
			System.out.println("Image: " + image.getWidth() + "x" + image.getHeight() + " crop: " + width + "x" + height + " at " + x + ", " + y + ".");
			throw e;
		}
	}
	
	/**
	 * Writes the image to a jpg file.
	 * @param image
	 * @param file
	 * @throws Exception
	 */
	public static void writeJPG(final BufferedImage image, final File file) throws Exception {
		CheckUtils.check(file, "file");
		CheckUtils.check(image, "image for file: "+ file.getName());
		
		ImageIO.write(image, "jpg", file);
	}
	
	/**
	 * Returns a guess as to a thumbnail for the image, based on contrast 
	 * and rule of thirds.
	 * @param input
	 * @param preshrink
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage getThumbnail(final BufferedImage input, 
											 final int preshrink, 
											 final int width, 
											 final int height) throws Exception {
		BufferedImage image = input;
		
		if (preshrink > 1 && image.getWidth() / preshrink > width && image.getHeight() / preshrink > height) {
			image = getScaledDown(input, preshrink);
		}
		
		if (width > image.getWidth() || height > image.getHeight()) {
			image = getScaledUp(input, 2);
		}
		
		if (width > image.getWidth() || height > image.getHeight()) {
			throw new Exception("Invalid thumbnail size: " + width + "x" + height + " for image: " + input.getWidth() + "x" + input.getHeight() + ".");
		}
		
		int seventh_width = image.getWidth() / 7;
		int seventh_height = image.getHeight() / 7;
		int half_width = (image.getWidth() - width) / 2;
		int half_height = (image.getHeight() - height) / 2;
		int sixseventh_width = Math.max(0, image.getWidth() - seventh_width - width);
		int sixseventh_height = Math.max(0, image.getHeight() - seventh_height - height);
		
		BufferedImage best_thumbnail = getCropped(image, 0, 0, width, height);
		int best_contrast = 0;
		
		BufferedImage thumbnail;
		
		// Top left.
		if (seventh_width + width < image.getWidth() && seventh_height + height < image.getHeight()) {
			thumbnail = getCropped(image, seventh_width, seventh_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}

		// Center left.
		if (seventh_width + width < image.getWidth() && half_height + height < image.getHeight()) {
			thumbnail = getCropped(image, seventh_width, half_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}

		// Bottom left.
		if (seventh_width + width < image.getWidth() && sixseventh_height + height < image.getHeight()) {
			thumbnail = getCropped(image, seventh_width, sixseventh_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}
		
		// Top right.
		if (sixseventh_width + width < image.getWidth() && seventh_height + height < image.getHeight()) {
			thumbnail = getCropped(image, sixseventh_width, seventh_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}

		// Center right.
		if (sixseventh_width + width < image.getWidth() && half_height + height < image.getHeight()) {
			thumbnail = getCropped(image, sixseventh_width, half_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}

		// Bottom right.
		if (sixseventh_width + width < image.getWidth() && sixseventh_height + height < image.getHeight()) {
			thumbnail = getCropped(image, sixseventh_width, sixseventh_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}
		
		// Center.
		if (half_width + width < image.getWidth() && half_height + height < image.getHeight()) {
			thumbnail = getCropped(image, half_width, half_height, width, height);
			int contrast = getContrast(thumbnail);
			if (contrast > best_contrast) {
				best_contrast = contrast;
				best_thumbnail = thumbnail;
			}
		}		
		
		return best_thumbnail;
	}
	
	/**
	 * Samples several axes for red channel deltas.
	 * @param image
	 * @return
	 */
	public static int getContrast(final BufferedImage image) throws Exception {
		int contrast = 0;
		contrast += getHorizontalContrast(image, image.getHeight() / 3);
		contrast += getHorizontalContrast(image, 2 * image.getHeight() / 5);
		contrast += getHorizontalContrast(image, 2 * image.getHeight() / 3);
		contrast += getHorizontalContrast(image, 5 * image.getHeight() / 7);
		contrast += getVerticalContrast(image, image.getWidth() / 3);
		contrast += getVerticalContrast(image, 2 * image.getWidth() / 5);
		contrast += getVerticalContrast(image, 2 * image.getWidth() / 3);
		contrast += getVerticalContrast(image, 5 * image.getWidth() / 7);
		return contrast;
	}
	
	
	/**
	 * Drags a box horizontally at y.  Returns the largest red channel delta in
	 * a ten-pixel radius.
	 * @param image
	 * @param y
	 * @return
	 */
	public static int getVerticalContrast(final BufferedImage image, int x) throws Exception {
		int contrast = 0;
		List<Integer> pixels = new ArrayList<Integer>();
		
		for (int i = 0; i < image.getHeight(); i++) {
			pixels.add(getRed(image.getRGB(x, i)));
			
			if (pixels.size() == 11) {
				pixels.remove(0);
				int delta = Collections.max(pixels) - Collections.min(pixels);
				contrast = Math.max(contrast, delta);
			}
			
		}
		
		return contrast;
	}
	
	/**
	 * Drags a box horizontally at y.  Returns the largest red channel delta in
	 * a ten-pixel radius.
	 * @param image
	 * @param y
	 * @return
	 */
	public static int getHorizontalContrast(final BufferedImage image, final int y) throws Exception {
		int contrast = 0;
		List<Integer> pixels = new ArrayList<Integer>();
		
		for (int i = 0; i < image.getWidth(); i++) {
			pixels.add(getRed(image.getRGB(i, y)));
			
			if (pixels.size() == 11) {
				pixels.remove(0);
				int delta = Collections.max(pixels) - Collections.min(pixels);
				contrast = Math.max(contrast, delta);
			}
			
		}
		
		return contrast;
	}

	/**
	 * Reads the url to a jpg file, returns the image.
	 * @param url
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage getJPG(final URL url, final File file) throws Exception {
		CheckUtils.check(url, "url");
		CheckUtils.check(file, "file");
		
		BufferedImage image = tryRead(url, 3);
	
		if (image == null) {
			throw new Exception("Failed to read url: " + url + ".");
		}
		
		if (file != null) {
			writeJPG(image, file);
		}
		return image;
	}
	
	/**
	 * Tries to read an image from a url.
	 * @param url
	 * @param tries
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage tryRead(final URL url, final int tries) throws Exception {
		for (int i = 0; i < tries; i++) {
			try {
				BufferedImage image = ImageIO.read(url);
				if (image != null) {
					return image;
				}
				Thread.sleep(500);
			}
			catch (Exception e) {
			}
		}
		
		throw new Exception("Could not read after " + tries + " retries.");
	}

}

