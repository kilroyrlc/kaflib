package kaflib.graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines a raster canvas of pixels (argb values).  Uninitialized values start
 * as transparent black.  This is 
 */
public class Canvas {
	private final RGBPixel pixels[][];

	public Canvas(final Canvas image) throws Exception {
		pixels = new RGBPixel[image.getWidth()][image.getHeight()];
	    for (int i = 0; i < pixels.length; i++) {
	    	for (int j = 0; j < pixels[0].length; j++) {
	    		pixels[i][j] = image.get(i, j);
	    	}
	    }
	}
	
	public Canvas(final BufferedImage image) throws Exception {
		pixels = new RGBPixel[image.getWidth()][image.getHeight()];
	    for (int i = 0; i < pixels.length; i++) {
	    	for (int j = 0; j < pixels[0].length; j++) {
	    		pixels[i][j] = new RGBPixel(image.getRGB(i, j));
	    	}
	    }
	}
	
	public Canvas(final int width, final int height) throws Exception {
		CheckUtils.checkPositive(width, "width");
		CheckUtils.checkPositive(height, "height");
		pixels = new RGBPixel[width][height];
	}
	
	public void checkDimensions(final Canvas other) throws Exception {
		if (getWidth() != other.getWidth() ||
			getHeight() != other.getHeight()) {
			throw new Exception("Mismatched dimensions: " + 
								toString() + " vs " + 
								other.toString() + ".");
		}
	}
	
	public List<RGBPixel> get(final Collection<Coordinate> coordinates) throws Exception {
		List<RGBPixel> pixels = new ArrayList<RGBPixel>();
		for (Coordinate coordinate : coordinates) {
			if (!isValid(coordinate)) {
				continue;
			}
			pixels.add(get(coordinate));
		}
		return pixels;
	}

	/**
	 * Takes a collection of coordinates, returns the average difference 
	 * between each pixel and the average.
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public int getAverageDelta(final Collection<Coordinate> coordinates) throws Exception {
		List<RGBPixel> pixels = get(coordinates);
		RGBPixel average = RGBPixel.getAverage(pixels);
		return average.getDelta(pixels);
	}
	
	public int getAverageDelta(final int samples) throws Exception {
		List<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < samples; i++) {
			Coordinate coordinate = getRandomLocation();
			Coordinate adjacent = coordinate.getRandomAdjacent();
			if (adjacent.isWithin(getWidth(), getHeight())) {
				values.add(get(coordinate).getDelta(get(adjacent)));
			}
		}
		return MathUtils.average(values);
	}
	
	public boolean isValid(final Coordinate coordinate) {
		return coordinate.isWithin(getWidth(), getHeight());
	}
	
	public RGBPixel getAverage(final Collection<Coordinate> coordinates) throws Exception {
		Set<RGBPixel> pixels = new HashSet<RGBPixel>();
		for (Coordinate coordinate : coordinates) {
			if (isValid(coordinate)) {
				pixels.add(get(coordinate));
			}
		}
		return RGBPixel.getAverage(pixels);
	}
	
	public RGBPixel getMedianByLuminance(final Collection<Coordinate> coordinates) throws Exception {
		List<RGBPixel> pixels = new ArrayList<RGBPixel>();
		for (Coordinate coordinate : coordinates) {
			if (isValid(coordinate)) {
				pixels.add(get(coordinate));
			}
		}
		return RGBPixel.getMedianByLuminance(pixels);
	}
	
	/**
	 * Returns a random pixel value.
	 * @return
	 */
	public RGBPixel getRandom() throws Exception {
		return get(getRandomLocation());
	}
	
	public boolean isNull(final Coordinate location) throws Exception {
		return isNull(location.getX(), location.getY());
	}
	
	
	public boolean isNull(int x, int y) throws Exception {
		CheckUtils.checkRange(x, 0, pixels.length - 1, "x value");
		CheckUtils.checkRange(y, 0, pixels[0].length - 1, "y value");
		if (pixels[x][y] == null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public RGBPixel get(final Coordinate location) throws Exception {
		return get(location.getX(), location.getY());
	}
	
	public RGBPixel get(int x, int y) throws Exception {
		CheckUtils.checkRange(x, 0, pixels.length - 1, "x value");
		CheckUtils.checkRange(y, 0, pixels[0].length - 1, "y value");
		if (pixels[x][y] == null) {
			return new RGBPixel(RGBPixel.TRANSPARENT_BLACK);
		}
		
		return pixels[x][y];
	}
	
	public Coordinate getRandomLocation() throws Exception {
		return new Coordinate(RandomUtils.randomInt(getWidth()), 
							  RandomUtils.randomInt(getHeight()));
	}
	
	public String toString() {
		return getWidth() + "x" + getHeight() + " canvas";
	}
	
	public int getWidth() {
		return pixels.length;
	}
	
	public int getHeight() {
		return pixels[0].length;
	}

	public void set(final Coordinate coordinate, final RGBPixel value) throws Exception {
		if (!isValid(coordinate)) {
			return;
		}
		pixels[coordinate.getX()][coordinate.getY()] = value;
	}
	
	public void set(final Set<Coordinate> coordinates, final RGBPixel value) throws Exception {
		for (Coordinate coordinate : coordinates) {
			set(coordinate, value);
		}
	}
	
	public void set(int x, int y, RGBPixel value) throws Exception {
		CheckUtils.check(value, "pixel value");
		CheckUtils.checkRange(x, 0, pixels.length);
		CheckUtils.checkRange(y, 0, pixels[0].length);
		pixels[x][y] = value;
	}
	

	/**
	 * Applies one canvas to this one, using the opacity of the parameter to
	 * determine how to blend.  The opacity of this layer remains as-is, and
	 * is not factored into the blend.
	 * @param canvas
	 * @throws Exception
	 */
	public void blend(final Canvas canvas) throws Exception {
		checkDimensions(canvas);
	    for (int i = 0; i < pixels.length; i++) {
	    	for (int j = 0; j < pixels[0].length; j++) {
	    		pixels[i][j].blend(canvas.get(i, j));
	    	}
	    }		
	}
	
	public BufferedImage toBufferedImage() throws Exception {

	    BufferedImage image = new BufferedImage(pixels.length, 
	    										pixels[0].length, 
	    										BufferedImage.TYPE_INT_ARGB);
	    for (int i = 0; i < pixels.length; i++) {
	    	for (int j = 0; j < pixels[0].length; j++) {
	    		image.setRGB(i, j, get(i, j).getARGB());
	    	}
	    }
	    return image;
	}

}
