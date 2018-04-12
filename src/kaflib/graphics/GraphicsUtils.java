package kaflib.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;

/**
 * Graphics utilites.
 */
public class GraphicsUtils {
	
	public enum Rotation {CLOCKWISE,
						  COUNTERCLOCKWISE,
						  ONE_EIGHTY};
	
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

	public static int getAlpha(final int argb) throws Exception {
		return (argb >> 24) & 0xff;
	}

	
	public static String getARGBString(final int argb) throws Exception {
		return String.format("#%08x", argb);
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
	 * Rotates the specified image by a multiple of 90.  This is a hard pixel
	 * transpose - there's no interpolation.
	 * @param image
	 * @param rotation
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage rotate(final BufferedImage image, 
									   final Rotation rotation) throws Exception {
		if (rotation == Rotation.CLOCKWISE) {
			BufferedImage output = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.setRGB(output.getWidth() - j - 1, i, image.getRGB(i, j));
				}
			}
			return output;
		}
		else if (rotation == Rotation.COUNTERCLOCKWISE) {
			BufferedImage output = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.setRGB(j, output.getHeight() - i - 1, image.getRGB(i, j));
				}
			}
			return output;			
		}
		else if (rotation == Rotation.ONE_EIGHTY) {
			BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.setRGB(output.getWidth() - i - 1, output.getHeight() - j - 1, image.getRGB(i, j));
				}
			}
			return output;
		}
		else {
			throw new Exception("Unsupported rotation: " + rotation + ".");
		}
		
	}
	
	/**
	 * Returns whether or not the supplied images are equal, pixel for pixel.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equal(BufferedImage a, BufferedImage b) {
		if (a.getWidth() != b.getWidth() ||
			a.getHeight() != b.getHeight()) {
			return false;
		}
        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                if (a.getRGB(x, y) != b.getRGB(x, y))
                    return false;
            }
        }
        return true;
	}
	
	public static int addLayer(final int baseARGB, final int layerARGB) throws Exception {
		double alpha = getAlpha(layerARGB) / 255;
		return getARGB(getAlpha(baseARGB),
					   (int) (getRed(layerARGB) * alpha + getRed(baseARGB) * (1 - alpha)),
					   (int) (getGreen(layerARGB) * alpha + getGreen(baseARGB) * (1 - alpha)),
					   (int) (getBlue(layerARGB) * alpha + getBlue(baseARGB) * (1 - alpha)));			   
	}

	public static Map<Integer, List<Integer>> getNeighbors(final BufferedImage image, final Coordinate location, final int radius) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();
		Map<Integer, List<Integer>> values = new HashMap<Integer, List<Integer>>();
		
		for (int i = Math.max(0, location.getX() - radius); i < Math.min(width, location.getX() + radius); i++) {
			for (int j = Math.max(0, location.getY() - radius); j < Math.min(height, location.getY() + radius); j++) {
				int distance = location.getDistance(new Coordinate(i, j));
				if (!values.containsKey(distance)) {
					values.put(distance, new ArrayList<Integer>());
				}
				if (distance <= radius) {
					values.get(distance).add(image.getRGB(i, j));
				}
			}
		}		
		return values;
	}
	

	/**
	 * Uses x and y as a centerpoint, returns a rectangle of the specified
	 * width and height, truncating if the edge of the image is reached.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param image
	 * @return
	 * @throws Exception
	 */
	public static Rectangle getRectangle(final int x, final int y, final int w, final int h, final BufferedImage image) throws Exception {
		int startX = Math.max(0, x - (w / 2));
		int startY = Math.min(image.getWidth() - 1, x + (w / 2));
		
		return new Rectangle(new Coordinate(startX, startY), 
						     new Coordinate(Math.min(image.getWidth() - startX, w), 
						    		 		Math.min(image.getHeight() - startY,  h)));
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
		return getRGB(0xff, r, g, b);
	}

	public static int getRGB(final double r, final double g, final double b) throws Exception {
		return getRGB(0xff, (int) r, (int) g, (int) b);
	}
	
	/**
	 * Returns the r/g/b channels as a single value.
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static int getRGB(final int a, final int r, final int g, final int b) throws Exception {
		int rgb = a & 0xff;
		rgb = rgb << 8;
		rgb = rgb | (r & 0xff);
		rgb = rgb << 8;
		rgb = rgb | (g & 0xff);
		rgb = rgb << 8;
		rgb = rgb | (b & 0xff);
		return rgb;
	}

	public static int getARGB(final int a, final int rgb) throws Exception {
		return getARGB(a, getRed(rgb), getGreen(rgb), getBlue(rgb));
	}
	
	public static int getARGB(final int a, final int r, final int g, final int b) throws Exception {
		return getRGB(a, r, g, b);
	}
	
	/**
	 * Reads the specified file to a buffered image.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage read(final File file) throws Exception {
		return ImageIO.read(file);
	}
	

	/**
	 * Reads the specified stream to a buffered image.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage read(final byte bytes[]) throws Exception {
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		return ImageIO.read(stream);
	}	

	/**
	 * Reads the specified stream to a buffered image.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage read(final InputStream stream) throws Exception {
		return ImageIO.read(stream);
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
	 * Scales the image.
	 * @param image
	 * @param factor
	 * @return
	 */
	public static BufferedImage getScaled(final BufferedImage image, float factor) {
		
		return GraphicsUtils.toBufferedImage(
				image.getScaledInstance((int) (image.getWidth() / factor), 
										(int) (image.getHeight() / factor), 
										Image.SCALE_SMOOTH), image.getType());
	}
	
	/**
	 * Scales the image to be within maxWidth and maxHeight, or just one
	 * constraint if the other is null.
	 * @param image
	 * @param factor
	 * @return
	 */
	public static BufferedImage getScaled(final BufferedImage image, 
										  final Integer maxWidth, 
										  final Integer maxHeight) throws Exception {
		if (maxWidth == null && maxHeight == null) {
			throw new Exception("Must specify at least one constraint.");
		}
		
		Float scale_x = null;
		if (maxWidth != null) {
			scale_x = (float) maxWidth / image.getWidth();
		}
		
		Float scale_y = null;
		if (maxHeight != null) {
			scale_y = (float) maxHeight / image.getHeight();
		}
		
		float scaling;
		if (scale_y == null) {
			scaling = scale_x;
		}
		else if (scale_x == null) {
			scaling = scale_y;
		}
		else {
			if (scale_x * image.getHeight() > maxHeight) {
				scaling = scale_y;
			}
			else {
				scaling = scale_x;
			}
		}
		
		return getScaled(image, scaling);

	}
	
	/**
	 * Converts the image to an imageicon.
	 * @param image
	 * @return
	 * @throws Exception
	 */
	public static ImageIcon getImageIcon(final BufferedImage image) throws Exception {
		return new ImageIcon(image);
	}
	
	/**
	 * Returns a cropped version of this image.
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

