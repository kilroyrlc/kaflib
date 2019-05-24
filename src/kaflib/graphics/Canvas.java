package kaflib.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kaflib.graphics.GraphicsUtils.Rotation;
import kaflib.gui.ImageComponent;
import kaflib.types.Box;
import kaflib.types.Byte;
import kaflib.types.Coordinate;
import kaflib.types.Histogram;
import kaflib.types.IntegerHistogram;
import kaflib.types.Matrix;
import kaflib.types.Pair;
import kaflib.types.Percent;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a raster canvas of pixels (argb values).  Uninitialized values start
 * as transparent black.
 */
public class Canvas {
	public enum Orientation {
		SQUARE,
		LANDSCAPE,
		PORTRAIT
	}
	
	private static final int SIMILAR_SCALE_WIDTH = 600;
	private static final int SIMILAR_SAMPLES = 32;
	private static final int SIMILAR_THRESHOLD = 64;
	
	private final RGBPixel pixels[][];

	/**
	 * Reads an image file to a canvas.
	 * @param file
	 * @throws Exception
	 */
	public Canvas(final File file) throws Exception {
		this(GraphicsUtils.read(file));
	}
	
	/**
	 * Copy constructor.
	 * @param image
	 * @throws Exception
	 */
	public Canvas(final Canvas image) throws Exception {
		pixels = new RGBPixel[image.getWidth()][image.getHeight()];
	    for (int i = 0; i < pixels.length; i++) {
	    	for (int j = 0; j < pixels[0].length; j++) {
	    		pixels[i][j] = image.get(i, j);
	    	}
	    }
	}
	
	/**
	 * Creates a canvas from a buffered image.
	 * @param image
	 * @throws Exception
	 */
	public Canvas(final BufferedImage image) throws Exception {
		CheckUtils.check(image, "input image");
		
		pixels = new RGBPixel[image.getWidth()][image.getHeight()];
	    for (int i = 0; i < pixels.length; i++) {
	    	for (int j = 0; j < pixels[0].length; j++) {
	    		pixels[i][j] = new RGBPixel(image.getRGB(i, j));
	    	}
	    }
	}
	
	/**
	 * Creates a blank/black canvas of specified dimensions.
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public Canvas(final int width, final int height) throws Exception {
		this(width, height, false);
	}
	
	/**
	 * Creates a blank/black canvas of specified dimensions.
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public Canvas(final int width, final int height, final boolean randomize) throws Exception {
		CheckUtils.checkPositive(width, "width");
		CheckUtils.checkPositive(height, "height");
		pixels = new RGBPixel[width][height];
		if (randomize) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					pixels[i][j] = RGBPixel.getRandomOpaque();
				}
			}
		}
		
	}

	/**
	 * Creates a canvas with all pixels equal to the given value.
	 * @param width
	 * @param height
	 * @param color
	 * @throws Exception
	 */
	public Canvas(final int width, final int height, final RGBPixel color) throws Exception {
		CheckUtils.checkPositive(width, "width");
		CheckUtils.checkPositive(height, "height");
		pixels = new RGBPixel[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixels[i][j] = new RGBPixel(color);
			}
		}
	}

	/**
	 * Creates a canvas with pixels randomly one or another color.
	 * @param width
	 * @param height
	 * @param colorA
	 * @param colorB
	 * @param aPct
	 * @throws Exception
	 */
	public Canvas(final int width, 
				  final int height, 
				  final RGBPixel colorA,
				  final RGBPixel colorB,
				  final Percent aPct) throws Exception {
		CheckUtils.checkPositive(width, "width");
		CheckUtils.checkPositive(height, "height");
		pixels = new RGBPixel[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (RandomUtils.randomBoolean((aPct))) {
					pixels[i][j] = new RGBPixel(colorA);
				}
				else {
					pixels[i][j] = new RGBPixel(colorB);
				}
			}
		}
	}
	
	/**
	 * Creates a canvas with pixels randomly/uniformly one of the supplied 
	 * colors.
	 * @param width
	 * @param height
	 * @param colors
	 * @throws Exception
	 */
	public Canvas(final int width, 
				  final int height, 
				  final RGBPixel... colors) throws Exception {
		CheckUtils.checkPositive(width, "width");
		CheckUtils.checkPositive(height, "height");
		CheckUtils.checkNonEmpty(colors, "colors");
		
		int count = colors.length;
		pixels = new RGBPixel[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rand = RandomUtils.randomInt(count);
				pixels[i][j] = new RGBPixel(colors[rand]);
			}
		}
	}
	
	/**
	 * Check that the dimensions here match another.
	 * @param other
	 * @throws Exception
	 */
	public void checkDimensions(final Canvas other) throws Exception {
		if (getWidth() != other.getWidth() ||
			getHeight() != other.getHeight()) {
			throw new Exception("Mismatched dimensions: " + 
								toString() + " vs " + 
								other.toString() + ".");
		}
	}
	

	public double getAspectRatio() {
		return (double) getWidth() / (double) getHeight();
	}
	
	public boolean aspectRatioMatches(final Canvas other, final Integer sigFigs) {
		double a = getAspectRatio();
		double b = other.getAspectRatio();
		if (sigFigs == null || sigFigs < 1) {
			return a == b;
		}
		int multiplier = sigFigs * 10;
		int aint = (int) (a * multiplier);
		int bint = (int) (b * multiplier);
		return aint == bint;
	}
	
	public static Canvas getLuminance(final Canvas canvas) throws Exception {
		Canvas grey = new Canvas(canvas);
		for (int i = 0; i < grey.getWidth(); i++) {
			for (int j = 0; j < grey.getHeight(); j++) {
				grey.set(i, j, new RGBPixel(true,
											grey.get(i, j).getLuminance(),
											grey.get(i, j).getLuminance(),
											grey.get(i, j).getLuminance()));
			}
		}
		return grey;
	}
	
	public IntegerHistogram getLuminanceHistogram(final Collection<Coordinate> coordinates) throws Exception {
		IntegerHistogram histogram = new IntegerHistogram(0, Byte.MAX_VALUE.getValue(), 8);
		
		for (Coordinate coordinate : coordinates) {
			histogram.increment(get(coordinate).getLuminance().getValue());
		}
		return histogram;
	}
	
	/**
	 * Returns whether or not a random sampling of averaged points indicates
	 * this image may be the same as another, ignoring scaling.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public boolean isSimilar(final Canvas other) throws Exception {
		if (!aspectRatioMatches(other, 2)) {
			return false;
		}
		
		Canvas this_scaled = scaleTo(this, SIMILAR_SCALE_WIDTH, null);
		Canvas other_scaled = scaleTo(other, SIMILAR_SCALE_WIDTH, null);
		for (Coordinate value : this_scaled.getBounds().getRandom(SIMILAR_SAMPLES)) {
			RGBPixel this_average = RGBPixel.getAverage(this_scaled.getBox(value, 3));

			// Aspect ratio was close, but we found a pixel out of bounds.
			if (!other_scaled.getBounds().contains(value)) {
				continue;
			}
			RGBPixel other_average = RGBPixel.getAverage(other_scaled.getBox(value, 3));
			
			if (this_average.getDelta(other_average) > SIMILAR_THRESHOLD) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Adds white noise to each pixel.
	 * @param max
	 * @throws Exception
	 */
	public void addNoise(final Byte max) throws Exception {
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				pixels[i][j].addNoise(max);
			}
		}
	}
	
	/**
	 * Returns the canvas bounds.
	 * @return
	 * @throws Exception
	 */
	public Box getBounds() throws Exception {
		return new Box(0, getWidth(), 0, getHeight());
	}
	
	/**
	 * Returns the values of a given vertical column.
	 * @param column
	 * @return
	 * @throws Exception
	 */
	public RGBPixel[] getColumn(final int column) throws Exception {
		CheckUtils.checkRange(column, 0, pixels.length - 1);
		return pixels[column];
	}
	
	public void setColumn(final RGBPixel column[], 
						  final int index) throws Exception {
		if (column.length != pixels[0].length) {
			throw new Exception("Mismatched column lengths: " + 
								column.length + " / " + pixels[0].length + ".");
		}
		CheckUtils.checkRange(index, 0, pixels.length - 1);
		System.arraycopy(column, 0, pixels[index], 0, column.length);
	}
	
	public List<RGBPixel> getBox(final Coordinate center, final int radius) throws Exception {
		List<RGBPixel> values = new ArrayList<RGBPixel>();
		for (int i = Integer.max(0, center.getX() - radius);
			 i < Integer.min(pixels.length, center.getX() + radius);
			 i++) {
			for (int j = Integer.max(0, center.getY() - radius);
					 j < Integer.min(pixels[0].length, center.getY() + radius);
					 j++) {
				values.add(get(i, j));
			}
		}
		return values;
	}
	
	/**
	 * Returns a copy of the canvas specified by the bounding box.	
	 * @param box
	 * @return
	 * @throws Exception
	 */
	public Canvas get(final Box box) throws Exception {
		if (!box.isContained(getBounds())) {
			throw new Exception("Selection " + box + " exceeds canvas " + 
								getBounds() + ".");
		}
		Canvas canvas = new Canvas(box.getWidth(), box.getHeight());
		for (int i = 0; i < box.getWidth(); i++) {
			for (int j = 0; j < box.getHeight(); j++) {
				canvas.set(i, j, new RGBPixel(get(i + box.getXMin(), j + box.getYMin())));
			}
		}
		return canvas;
	}
	
	/**
	 * Returns the RGB values at the specified coordinates.
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
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
	
	public void toFile(final File file) throws Exception {
		GraphicsUtils.writePNG(toBufferedImage(), file);
	}

	public void toPNG(final File file) throws Exception {
		GraphicsUtils.writePNG(toBufferedImage(), file);
	}

	public void toJPG(final File file) throws Exception {
		GraphicsUtils.writeJPG(toBufferedImage(), file);
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

	public static Canvas getRandomRotateMirror(final Canvas canvas) throws Exception {
		int random = RandomUtils.randomInt(0, 3);
		Canvas changed;
		if (random == 0) {
			changed = new Canvas(canvas);
		}
		else if (random == 1) {
			changed = rotate(canvas, GraphicsUtils.Rotation.CLOCKWISE);
		}
		else if (random == 2) {
			changed = rotate(canvas, GraphicsUtils.Rotation.ONE_EIGHTY);
		}
		else {
			changed = rotate(canvas, GraphicsUtils.Rotation.COUNTERCLOCKWISE);
		}
		random = RandomUtils.randomInt(0, 2);
		if (random == 0) {
		}
		else if (random == 1) {
			changed = mirror(changed, false);
		}
		else {
			changed = mirror(changed, true);
		}
		return changed;
	}
	
	public static Canvas rotate(final Canvas image, 
								final GraphicsUtils.Rotation rotation) throws Exception {
		if (rotation == Rotation.CLOCKWISE) {
			Canvas output = new Canvas(image.getHeight(), image.getWidth());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.set(output.getWidth() - j - 1, i, image.get(i, j));
				}
			}
			return output;
		}
		else if (rotation == Rotation.COUNTERCLOCKWISE) {
			Canvas output = new Canvas(image.getHeight(), image.getWidth());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.set(j, output.getHeight() - i - 1, image.get(i, j));
				}
			}
			return output;			
		}
		else if (rotation == Rotation.ONE_EIGHTY) {
			Canvas output = new Canvas(image.getWidth(), image.getHeight());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.set(output.getWidth() - i - 1, output.getHeight() - j - 1, image.get(i, j));
				}
			}
			return output;
		}
		else {
			throw new Exception("Unsupported rotation: " + rotation + ".");
		}
	}

	public static Canvas mirror(final Canvas image, 
								final boolean horizontal) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();

		Canvas output = new Canvas(image.getHeight(), image.getWidth());
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (horizontal) {
					output.set(width - i - 1, j, image.get(i, j));
				}
				else {
					output.set(i, height - j - 1, image.get(i, j));
				}
			}
		}
		return output;
	}
	
	/**
	 * Draws the specified values of the canvas from the top left x, y.
	 * @param topLeft
	 * @param values
	 * @throws Exception
	 */
	public void set(final Coordinate topLeft, final Canvas values) throws Exception {
		for (int i = 0; i < values.getWidth(); i++) {
			TypeUtils.copy(values.getColumn(i), 
					       0, 
					       pixels[i + topLeft.getX()], 
					       topLeft.getY(), 
					       values.getHeight());
		}
	}

	/**
	 * Draws an empty one-pixel box with the specified values.
	 * @param box
	 * @param value
	 * @throws Exception
	 */
	public void draw(final Box box, final RGBPixel value) throws Exception {
		if (!box.isContained(getBounds())) {
			throw new Exception("Box: " + box + " outside of " + getBounds() + ".");
		}
		
		for (int i = box.getXMin(); i <= box.getXMax(); i++) {
			pixels[i][box.getYMin()] = new RGBPixel(value);
			pixels[i][box.getYMax()] = new RGBPixel(value);
		}
		for (int j = box.getYMin(); j <= box.getYMax(); j++) {
			pixels[box.getXMin()][j] = new RGBPixel(value);
			pixels[box.getXMax()][j] = new RGBPixel(value);			
		}
		
	}
	
	/**
	 * Fills a box with the specified pixel values.
	 * @param box
	 * @param value
	 * @throws Exception
	 */
	public void fill(final Box box, final RGBPixel value) throws Exception {
		if (!box.isContained(getBounds())) {
			throw new Exception("Box: " + box + " outside of " + getBounds() + ".");
		}
		for (int i = box.getXMin(); i <= box.getXMax(); i++) {
			for (int j = box.getYMin(); j <= box.getYMax(); j++) {
				pixels[i][j] = new RGBPixel(value);
			}
		}
	}
	
	public void set(final Coordinate topLeft, 
				    final Canvas values, 
				    final int feather) throws Exception {
		Box patch_bounds = values.getBounds();

		for (int i = 0; i < values.getWidth(); i++) {
			for (int j = 0; j < values.getHeight(); j++) {
				int distance_from_edge = patch_bounds.getRiseRunToEdge(new Coordinate(i, j));
				if (distance_from_edge < feather) {
					Percent percent = new Percent(((distance_from_edge + 1) * 100) / feather);
					pixels[i + topLeft.getX()][topLeft.getY() + j] = 
							new RGBPixel(pixels[i + topLeft.getX()][topLeft.getY() + j],
									     values.get(i, j),
									     percent);
				}
				else {
					pixels[i + topLeft.getX()][topLeft.getY() + j] = values.get(i, j);
				}
			}
		}
	}
	
	/**
	 * Sets each pixel in the canvas to the average of the values in the 
	 * box specified by radius.
	 * @param radius
	 * @throws Exception
	 */
	public void smooth(final int radius) throws Exception {
		for (int i = 0; i < radius; i++) {
			for (int x = 0; x < getWidth(); x++) {
				for (int y = 0; y < getHeight(); y++) {
					set(x, y, RGBPixel.getAverage(getBox(new Coordinate(x, y), radius)));
				}
			}
		}
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
	
	public Orientation getOrientation() {
		if (getWidth() == getHeight()) {
			return Orientation.SQUARE;
		}
		else if (getWidth() > getHeight()) {
			return Orientation.LANDSCAPE;
		}
		else {
			return Orientation.PORTRAIT;
		}
	}

	/**
	 * Returns a random new subcanvas.
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Canvas getRandom(final int width, final int height) throws Exception {
		return get(getRandomBox(width, height));
	}

	/**
	 * Returns a random box from the canvas with the specified height and 
	 * width.
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Box getRandomBox(final int sideLength) throws Exception {
		return getRandomBox(sideLength, sideLength);
	}
	
	/**
	 * Returns a random box from the canvas with the specified height and 
	 * width.
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Box getRandomBox(final int width, final int height) throws Exception {
		CheckUtils.checkRange(width, 1, getWidth());
		CheckUtils.checkRange(height, 1, getHeight());
		int x = 0;
		int y = 0;

		if (getWidth() > width) {
			x = RandomUtils.randomInt(0, getWidth() - width - 1);
		}
		if (getHeight() > height) {
			y = RandomUtils.randomInt(0, getHeight() - height - 1);
		}
		return new Box(x, width, y, height);
	}
	
	/**
	 * Creates a set of boxes that covers the canvas based on random start
	 * points.  The boxes will overlap around the border.
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Set<Box> getRandomCoverage(final int width, final int height, final int overlap) throws Exception {
		Set<Box> set = new HashSet<Box>();

		// Determine the top edge of all rows.
		List<Integer> rows = new ArrayList<Integer>();	
		rows.add(0);
		rows.add(getHeight() - height - 1);
		int y = RandomUtils.randomInt(0, getHeight() - height - 1);
		rows.add(y);
		// Go up until you go off map.
		for (int j = y; j > 0; j -= (height - overlap)) {
			rows.add(j);
		}
		// Go up until you go off map.
		for (int j = y + height; j < getHeight() - height - 1; j += (height - overlap)) {
			rows.add(j);
		}
		
		// Rows determined, for each row, go left and right.
		for (Integer j : rows) {
			// Add the sides.
			set.add(new Box(0, width, j, height));
			set.add(new Box(getWidth() - 1 - width, getWidth(), j, height));
			
			int x = RandomUtils.randomInt(0, getWidth() - width - 1);
			// Go left.
			for (int i = x; i > 0; i -= (width - overlap)) {
				set.add(new Box(i, width, j, height));
			}
			// Go right.
			for (int i = x + width; i < getWidth() - width - 1; i += (width - overlap)) {
				set.add(new Box(i, i + width, j, j + height));
			}			
		}
		return set;
	}
	

	public Set<Box> getTiles(final int sideLength) throws Exception {
		return getTiles(sideLength, sideLength);
	} 
	
	/**
	 * Returns a set of equal-size tiles starting with the top left.  Note this
	 * will trim to the bottom/right when sizes don't line up.
	 * @return
	 * @throws Exception
	 */
	public Set<Box> getTiles(final int width, final int height) throws Exception {
		Set<Box> tiles = new HashSet<Box>();
		for (int i = 0; i < getWidth() - width; i += width) {
			for (int j = 0; j < getHeight() - width; j += height) {
				tiles.add(new Box(i, width, j, height));
			}
		}
		return tiles;
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
	
	/**
	 * Writes the canvas as an argb image.
	 * @return
	 * @throws Exception
	 */
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
	
	/**
	 * Resizes the canvas so the smaller dimensions match, then center
	 * crops the longer dimension.
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public static Canvas fill(final Canvas source,
							  final int width,
							  final int height) throws Exception {
		if (source.getWidth() == width && source.getHeight() == height) {
			return source;
		}
		
		return new Canvas(GraphicsUtils.fill(source.toBufferedImage(), width, height));
	}

	public Canvas getScaled(final Integer maxWidth,
			  final Integer maxHeight) throws Exception {
		return scaleTo(this, maxWidth, maxHeight);
	}
	
	/**
	 * Scales the canvas until the width or height (if specified) reaches max.
	 * @param canvas
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 * @throws Exception
	 */
	public static Canvas scaleTo(final Canvas canvas,
								  final Integer maxWidth,
								  final Integer maxHeight) throws Exception {
		return new Canvas(GraphicsUtils.getScaled(canvas.toBufferedImage(), maxWidth, maxHeight));
	}
	
	public static Canvas join(final Canvas... canvases) throws Exception {
		int width = 0;
		int height = 0;
		for (Canvas canvas : canvases) {
			width += canvas.getWidth();
			height = Math.max(height, canvas.getHeight());
		}
		Canvas new_canvas = new Canvas(width, height);
		int x_start = 0;
		
		for (Canvas canvas : canvases) {
			new_canvas.set(new Coordinate(x_start, 0), canvas);
			x_start += canvas.getWidth();
		}
		return new_canvas;
	}
	
	/**
	 * Joins two canvases together, left-to-right or top-to-bottom where the 
	 * joined dimensions must match.  If margin > 0, treats that number of 
	 * pixels as overlap where a heuristic decides how to blend them.
	 * @param topLeft
	 * @param bottomRight
	 * @param margin
	 * @param horizontal
	 * @return
	 * @throws Exception
	 */
	public static Canvas join(final Canvas topLeft, 
							  final Canvas bottomRight,
							  final int margin,
							  final boolean horizontal) throws Exception {
		Canvas canvas;
		if (horizontal) {
			CheckUtils.checkEquals(topLeft.getHeight(), bottomRight.getHeight(), "Height");
			canvas = new Canvas(topLeft.getWidth() + bottomRight.getWidth() - margin,
							    topLeft.getHeight());
			// Copy non-margin left side.
			for (int i = 0; i < topLeft.getWidth() - margin; i++) {
				canvas.setColumn(topLeft.getColumn(i), i);
			}
			// Copy non-margin right side.
			for (int i = margin; i < bottomRight.getWidth(); i++) {
				canvas.setColumn(bottomRight.getColumn(i), topLeft.getWidth() + i - margin);
			}

			if (margin > 0) {
				// Blend each row, left to right.
				double increment = 1.0 / (double)(margin - 1);
				for (int i = 0; i < topLeft.getHeight(); i++) {
					double percent_left = 1;
					for (int j = 0; j < margin; j++) {
						RGBPixel blend = new RGBPixel(topLeft.get(topLeft.getWidth() - margin + j, i),
													  bottomRight.get(j, i),
													  new Percent(percent_left),
													  new Percent(90));
						canvas.set(topLeft.getWidth() - margin + j, i, blend);
						percent_left -= increment;
					}
				}
			}
		}
		else {
			CheckUtils.checkEquals(topLeft.getWidth(), bottomRight.getWidth(), "Width");
			canvas = new Canvas(topLeft.getWidth(),
							    topLeft.getHeight() + bottomRight.getHeight() - margin);
			// Copy non-margin top side.
			for (int i = 0; i < topLeft.getWidth(); i++) {
				// Copy top.
				TypeUtils.copy(topLeft.getColumn(i), 0, canvas.getColumn(i), 0, topLeft.getColumn(i).length - margin);
				
				// Copy bottom.
				TypeUtils.copy(bottomRight.getColumn(i), 
								 margin, 
								 canvas.getColumn(i), 
								 topLeft.getColumn(i).length, 
								 bottomRight.getColumn(i).length - margin);

				if (margin > 0) {
					double increment = 1.0 / (double) (margin - 1);
					double percent_top = 1;
					for (int j = 0; j < margin; j++) {

						RGBPixel blend = new RGBPixel(topLeft.get(i, topLeft.getHeight() - margin + j),
													  bottomRight.get(i, j),
													  new Percent(percent_top),
													  new Percent(90));
						canvas.set(i, topLeft.getHeight() - margin + j, blend);
						percent_top -= increment;
					}
				}
				
			}
		}
		
		return canvas;
	}
	

	public static Canvas createMottled(final Coordinate dimensions,
									   final RGBPixel bg, 
									   final RGBPixel fg) throws Exception {
		return createMottled(dimensions, bg, fg, 2);
	}
	
	public static Canvas createMottled(final Coordinate dimensions,
									   final RGBPixel bg, 
									   final RGBPixel fg, 
									   final int smooth) throws Exception {
		Canvas canvas = new Canvas(dimensions.getX(), 
								   dimensions.getY(),
								   bg,
								   fg,
								   new Percent(70));
		
		for (int i = 0; i < smooth; i++) {
			canvas.smooth(2);
		}
		return canvas;
	}

	public static Canvas createMottled(final Coordinate dimensions,
										final int smooth,
										final RGBPixel... colors) throws Exception {
		Canvas canvas = new Canvas(dimensions.getX(), 
				dimensions.getY(),
				colors);

		for (int i = 0; i < smooth; i++) {
			canvas.smooth(2);
		}
		return canvas;
	}
	
	public static Canvas createMottled(final Coordinate dimensions,
									final int smooth,
									final Color... colors) throws Exception {
		RGBPixel pixels[] = new RGBPixel[colors.length];
		for (int i = 0; i < colors.length; i++) {
			pixels[i] = new RGBPixel(colors[i]);
		}
		Canvas canvas = new Canvas(dimensions.getX(), 
				dimensions.getY(),
				pixels);

		for (int i = 0; i < smooth; i++) {
			canvas.smooth(2);
		}
		return canvas;
	}
	
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			File file = GUIUtils.chooseFile(frame, new File("~"));
			if (file == null) {
				System.exit(1);
			}
			final Canvas canvas = new Canvas(file);
			Set<Box> boxes = canvas.getRandomCoverage(30, 30, 2);
			for (Box box : boxes) {
				canvas.fill(box, RGBPixel.getRandomOpaque());
			}
			
			final ImageComponent component = new ImageComponent(canvas);
			JPanel panel = new JPanel();
			panel.add(component);
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
			
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					for (int i = 0; i < 5; i++) {
						Thread.sleep(5000);
						int size = RandomUtils.randomInt(25, 40);
						
						Set<Box> boxes = canvas.getRandomCoverage(size, size, 2);
						for (Box box : boxes) {
							canvas.fill(box, RGBPixel.getRandomOpaque());
						}
						component.update(canvas);
					}
				}
				
			};
			worker.start();
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

class Modification {
	enum Mirror {
		NONE,
		X,
		Y
	}
	private final Rotation rotation;
	private final Mirror mirror;
	
	public Modification(final Rotation rotation, final Mirror mirror) {
		this.rotation = rotation;
		this.mirror = mirror;
	}
	
	public Rotation getRotation() {
		return rotation;
	}
	
	public Mirror getMirror() {
		return mirror;
	}
}