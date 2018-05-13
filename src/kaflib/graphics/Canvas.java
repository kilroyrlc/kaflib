package kaflib.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import kaflib.gui.ImageCascade;
import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.Percent;
import kaflib.utils.CheckUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a raster canvas of pixels (argb values).  Uninitialized values start
 * as transparent black.  This is 
 */
public class Canvas {
	public enum Orientation {
		SQUARE,
		LANDSCAPE,
		PORTRAIT
	}
	
	private final RGBPixel pixels[][];

	public Canvas(final File file) throws Exception {
		this(GraphicsUtils.read(file));
	}
	
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
	
	public Box getBounds() throws Exception {
		return new Box(0, getWidth(), 0, getHeight());
	}
	
	public RGBPixel[] getColumn(final int index) throws Exception {
		CheckUtils.checkRange(index, 0, pixels.length - 1);
		return pixels[index];
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
	
	public static Canvas fill(final Canvas source,
							  final int width,
							  final int height) throws Exception {
		return new Canvas(GraphicsUtils.fill(source.toBufferedImage(), width, height));
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
			CheckUtils.checkEquals(topLeft.getHeight(), bottomRight.getHeight(), "Height");
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
	
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			File file = GUIUtils.chooseFile(frame, new File("~"));
			if (file == null) {
				System.exit(1);
			}
			Canvas canvas = new Canvas(file);
			Canvas horizontal = Canvas.join(canvas, canvas, 0, true);
			Canvas vertical = Canvas.join(canvas, canvas, 0, false);
			Canvas blendedh = Canvas.join(vertical, vertical, 10, true);
			Canvas blendedv = Canvas.join(horizontal, horizontal, 10, false);
			ImageCascade cascade = new ImageCascade(canvas, horizontal, vertical, blendedh, blendedv);
			frame.setContentPane(cascade);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
