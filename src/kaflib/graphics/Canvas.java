package kaflib.graphics;

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
import kaflib.gui.components.DownscaledImageComponent;
import kaflib.types.Box;
import kaflib.types.Byte;
import kaflib.types.Coordinate;
import kaflib.types.IntegerHistogram;
import kaflib.types.Percent;
import kaflib.utils.CheckUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a raster canvas of pixels (argb values).  Uninitialized values start
 * as transparent black.  Canvas dimensions are immutable, but values can be
 * modified.
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
	private Map<Coordinate, RGBPixel> map;

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
	
	public Canvas(final RGBPixel canvas[][]) throws Exception {
		pixels = new RGBPixel[canvas.length][canvas[0].length];
		for (int i = 0; i < canvas.length; i++) {
			setColumn(canvas[i], i);
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

	public void applyTransform(final CanvasTransform transform) throws Exception {
		CheckUtils.check(pixels, "image");
		CheckUtils.check(transform, "transform");
		transform.apply(pixels);
	}

	/**
	 * Returns width / height.
	 * @return
	 */
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
		
		Canvas this_scaled = CanvasUtils.scaleTo(this, SIMILAR_SCALE_WIDTH, null);
		Canvas other_scaled = CanvasUtils.scaleTo(other, SIMILAR_SCALE_WIDTH, null);
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
	
	public final void setColumn(final RGBPixel column[], 
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
				try {
					canvas.set(i, j, new RGBPixel(get(i + box.getXMin(), j + box.getYMin())));
				}
				catch (Exception e) {
					System.err.println("Error writing " + i + ", " + j + " from " + 
									   (i + box.getXMin() + ", " + (j + box.getYMin() + ".")));
					throw e;
				}
			}
		}
		return canvas;
	}
	
	/**
	 * Returns the pixel array.
	 * @return
	 */
	public RGBPixel[][] get() {
		return pixels;
	}
	
	public RGBPixel[][] getCopy() {
		return CanvasUtils.copy(pixels);
	}
	
	public void set(final Map<Coordinate, RGBPixel> values) throws Exception {
		for (Coordinate coordinate : values.keySet()) {
			set(coordinate, values.get(coordinate));
		}
	}
	
	public void set(final RGBPixel values[][]) throws Exception {
		if (values.length != pixels.length || values[0].length != pixels[0].length) {
			throw new Exception("Mismatched canvas sizes.");
		}
		for (int i = 0; i < values.length; i++) {
			System.arraycopy(values[i], 0, pixels[i], 0, pixels[i].length);
		}
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
	 * Adds a border of the specified number of pixels.
	 * @param width
	 * @param color
	 * @throws Exception
	 */
	public void addBorder(final int width, final RGBPixel color) throws Exception {
		CheckUtils.checkPositive(width, "width");
		CheckUtils.check(color, "color");
		int height = pixels[0].length;
		
		// Top/bottom.
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < Math.min(width, height); j++) {
				pixels[i][j] = new RGBPixel(color);
				pixels[i][height - 1 - j] = new RGBPixel(color);
			}
		}
		
		// Left/right.
		for (int i = 0; i < Math.min(width, pixels.length); i++) {
			for (int j = 0; j < height; j++) {
				pixels[i][j] = new RGBPixel(color);
				pixels[pixels.length - 1 - i][j] = new RGBPixel(color);
			}
		}
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
	
	public Coordinate getCenter() {
		return new Coordinate(getWidth() / 2, getHeight() / 2);
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
	
	public RGBPixel get(final Coordinate location) {
		return get(location.getX(), location.getY());
	}
	
	public RGBPixel get(int x, int y) {
		if (x < 0 || x >= pixels.length || y < 0 || y >= pixels[0].length) {
			return null;
		}
		if (pixels[x][y] == null) {
			return new RGBPixel(RGBPixel.TRANSPARENT_BLACK);
		}
		
		return pixels[x][y];
	}
	
	public Coordinate getRandomLocation() throws Exception {
		return new Coordinate(RandomUtils.randomInt(getWidth()), 
							  RandomUtils.randomInt(getHeight()));
	}
	
	/**
	 * Returns a map of the contents of this Canvas.
	 * @return
	 */
	public Map<Coordinate, RGBPixel> getMap() {
		if (map == null) {
			map = new HashMap<Coordinate, RGBPixel>(pixels.length * pixels[0].length);
			for (int i = 0; i < pixels.length; i++) {
				for (int j = 0; j < pixels[i].length; j++) {
					map.put(new Coordinate(i, j), pixels[i][j]);
				}
			}
		}
		return map;
	}
	
	/**
	 * Returns a set of all coordinates in this Canvas.
	 * @return
	 */
	public Set<Coordinate> getCoordinates() {
		return getMap().keySet();
	}
	
	/**
	 * Returns a collection of the pixels in this Canvas.
	 * @return
	 */
	public Collection<RGBPixel> getPixels() {
		return getMap().values();
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

	public void uncheckedSet(final Coordinate coordinate, final RGBPixel value) {
		pixels[coordinate.getX()][coordinate.getY()] = value;
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
	
	public Canvas getCropped(final int width,
			   				 final int height) throws Exception {
		CheckUtils.checkPositive(width);
		CheckUtils.checkPositive(height);
		int dx = getWidth() - width;
		int dy = getHeight() - height;
		if (dx < 0 || dy < 0) {
			throw new Exception("Attempting to crop to larger area: " + getWidth() + 
					" -> " + width + " by " + getHeight() + " -> " + height + ".");
		}
		return getCropped(dx / 2, dy / 2, width, height);
	}

	public Canvas getCropped(final int x, 
							final int y, 
							final int width, 
							final int height) throws Exception {
		CheckUtils.checkNonNegative(x);
		CheckUtils.checkNonNegative(y);
		CheckUtils.checkPositive(width);
		CheckUtils.checkPositive(height);

		if (x + width > getWidth() || y + height > getHeight()) {
			throw new Exception("Invalid crop (" + x + ", " + y + ") " + width + "x" + height + 
					" for " + getWidth() + "x" + getHeight());
		}
		
		Canvas cropped = new Canvas(width, height);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cropped.set(i, j, get(i + x, j + y));
			}
		}
		return cropped;
	}
	
	public Canvas getScaled(final int longestSide) throws Exception {
		if (getOrientation() == Orientation.LANDSCAPE) {
			return getScaled(longestSide, null);
		}
		else {
			return getScaled(null, longestSide);
		}
	}

	public Canvas getScaledToShortestSide(final int shortestSide) throws Exception {
		if (getOrientation() == Orientation.LANDSCAPE) {
			return getScaled(null, shortestSide);
		}
		else {
			return getScaled(shortestSide, null);
		}
	}
	
	public Canvas getScaled(final Integer maxWidth,
			  				final Integer maxHeight) throws Exception {
		return CanvasUtils.scaleTo(this, maxWidth, maxHeight);
	}
	
	public Canvas getScaledUp(final Integer minWidth,
				final Integer minHeight) throws Exception {
		return CanvasUtils.scaleToAtLeast(this, minWidth, minHeight);
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
			canvas.addBorder(5, RGBPixel.OPAQUE_BLUE);
			
			final DownscaledImageComponent component = new DownscaledImageComponent(canvas);
			JPanel panel = new JPanel();
			panel.add(component);
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
	
			
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