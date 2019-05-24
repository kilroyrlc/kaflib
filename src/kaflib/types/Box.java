package kaflib.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.StringUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a box.
 */
public class Box {
	protected final Coordinate top_left;
	protected final Coordinate top_right;
	protected final Coordinate bottom_left;
	protected final Coordinate bottom_right;
	protected final int x_min;
	protected final int y_min;
	protected final int x_max;
	protected final int y_max;
	protected final int width;
	protected final int height;
	
	/**
	 * Creates a box of specified x/width, y/height.
	 * @param xMin
	 * @param width
	 * @param yMin
	 * @param height
	 * @throws Exception
	 */
	public Box(final int xMin, final int width,
			   final int yMin, final int height) throws Exception {
		x_min = xMin;
		x_max = xMin + width;
		y_min = yMin;
		y_max = yMin + height;
		this.width = width;
		this.height = height;
		
		top_left = new Coordinate(x_min, y_min);
		top_right = new Coordinate(x_max, y_min);
		bottom_left = new Coordinate(x_min, y_max);
		bottom_right = new Coordinate(x_max, y_max);
		
		if (x_min == x_max || y_min == y_max) {
			throw new Exception("Invalid box bounds: " + x_min + "-" + x_max + 
								" by " + y_min + "-" + y_max + ".");
		}
	}
	
	/**
	 * Returns the box defined by the given coordinates.
	 * @param c0
	 * @param c1
	 * @throws Exception
	 */
	public Box(final Coordinate c0, final Coordinate c1) throws Exception {
		if (!isBox(c0, c1)) {
			throw new Exception("Invalid box coordinates: " + c0 + ", " + c1 + ".");
		}
		
		List<Coordinate> coordinates = TypeUtils.getList(c0, c1);
		
		x_min = Coordinate.getMinX(coordinates);
		x_max = Coordinate.getMaxX(coordinates);
		y_min = Coordinate.getMinY(coordinates);
		y_max = Coordinate.getMaxY(coordinates);
		width = x_max - x_min + 1;
		height = y_max - y_min + 1;
		
		top_left = new Coordinate(x_min, y_min);
		top_right = new Coordinate(x_max, y_min);
		bottom_left = new Coordinate(x_min, y_max);
		bottom_right = new Coordinate(x_max, y_max);
	}

	/**
	 * Creates a box with the given center point and dimensions.
	 * @param center
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public Box(final Coordinate center, final int width, final int height) throws Exception {
		x_min = center.getX() - (width / 2);
		x_max = x_min + width;
		y_min = center.getY() - (height / 2);
		y_max = y_min + height;
		this.width = width;
		this.height = height;
		
		top_left = new Coordinate(x_min, y_min);
		top_right = new Coordinate(x_max, y_min);
		bottom_left = new Coordinate(x_min, y_max);
		bottom_right = new Coordinate(x_max, y_max);
		
	}
	
	public Box(final String serialized) throws Exception {
		String tokens[] = serialized.split("\\:");
		if (tokens.length != 2) {
			throw new Exception("Could not parse coordinate:coordinate from " + serialized + ".");
		}
		Coordinate c0 = new Coordinate(tokens[0]);
		Coordinate c1 = new Coordinate(tokens[1]);
		if (!isBox(c0, c1)) {
			throw new Exception("Invalid box coordinates: " + c0 + ", " + c1 + ".");
		}
		
		List<Coordinate> coordinates = TypeUtils.getList(c0, c1);
		
		x_min = Coordinate.getMinX(coordinates);
		x_max = Coordinate.getMaxX(coordinates);
		y_min = Coordinate.getMinY(coordinates);
		y_max = Coordinate.getMaxY(coordinates);
		width = x_max - x_min + 1;
		height = y_max - y_min + 1;
		
		top_left = new Coordinate(x_min, y_min);
		top_right = new Coordinate(x_max, y_min);
		bottom_left = new Coordinate(x_min, y_max);
		bottom_right = new Coordinate(x_max, y_max);
	}
	
	
	/**
	 * Returns whether or not the boxes overlap, inclusive.
	 * @param box
	 * @return
	 */
	public boolean overlaps(final Box box) throws Exception {
		CheckUtils.check(box, "box");
		
		if (x_max < box.getXMin()) {
			return false;
		}
		if (x_min > box.getXMax()) {
			return false;
		}
		if (y_max < box.getYMin()) {
			return false;
		}
		if (y_min > box.getYMax()) {
			return false;
		}
		
		return true;
	}
	
	public boolean isAdjacentOrOverlapping(final Box box) throws Exception {
		return getBorderBox().overlaps(box);
	}

	/**
	 * Returns the rise or run to get to the nearest edge.
	 * @param coordinate
	 * @return
	 * @throws Exception
	 */
	public int getRiseRunToEdge(final Coordinate coordinate) throws Exception {
		int x = Math.min(Math.abs(coordinate.getX() - x_min), Math.abs(coordinate.getX() - x_max));
		int y = Math.min(Math.abs(coordinate.getY() - y_min), Math.abs(coordinate.getY() - y_max));
		return Math.min(x, y);
	}
	
	/**
	 * Returns whether or not the box contains the specified coordinate, 
	 * inclusive.
	 * @param coordinate
	 * @return
	 */
	public boolean contains(final Coordinate coordinate) {
		if (x_max >= coordinate.getX() &&
			x_min <= coordinate.getX() &&
			y_max >= coordinate.getY() &&
			y_min <= coordinate.getY()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Coordinate getCenter() {
		return new Coordinate(x_min + width / 2, y_min + height / 2);
	}
	
	public boolean isContained(final Box other) {
		if (x_min >= other.getXMin() &&
			x_max <= other.getXMax() &&
			y_min >= other.getYMin() &&
			y_max <= other.getYMax()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns width / height.
	 * @return
	 */
	public double getAspectRatio() {
		return width / height;
	}
	
	public boolean containsOrAdjacent(final Coordinate coordinate) throws Exception {
		return getBorderBox().contains(coordinate);
	}
	
	public Box getBorderBox() throws Exception {
		CheckUtils.checkSubOverflow(x_min, 1);
		CheckUtils.checkAddOverflow(x_max, 1);
		CheckUtils.checkSubOverflow(y_min, 1);
		CheckUtils.checkAddOverflow(y_max, 1);
		return new Box(x_min - 1, x_max - x_min + 1, y_min - 1, y_max - y_min + 1);
	}
	
	/**
	 * @return the top_left
	 */
	public Coordinate getTopLeft() {
		return top_left;
	}

	/**
	 * @return the top_right
	 */
	public Coordinate getTopRight() {
		return top_right;
	}

	/**
	 * @return the bottom_left
	 */
	public Coordinate getBottomLeft() {
		return bottom_left;
	}

	/**
	 * @return the bottom_right
	 */
	public Coordinate getBottomRight() {
		return bottom_right;
	}

	/**
	 * @return the x_min
	 */
	public int getXMin() {
		return x_min;
	}

	/**
	 * @return the y_min
	 */
	public int getYMin() {
		return y_min;
	}

	/**
	 * @return the x_max
	 */
	public int getXMax() {
		return x_max;
	}

	/**
	 * @return the y_max
	 */
	public int getYMax() {
		return y_max;
	}
	
	public String toSerial() {
		return getTopLeft().toSerial() + ":" + getBottomRight().toSerial();
	}
	
	public String toString() {
		return "Box: " + getTopLeft() + getBottomRight();
	}
	
	public Set<Coordinate> getCoordinates() {
		Set<Coordinate> set = new HashSet<Coordinate>();
		for (int i = x_min; i <= x_max; i++) {
			for (int j = y_min; j <= y_max; j++) {
				set.add(new Coordinate(i, j));
			}
		}
		return set;
	}
	
	public Coordinate getRandom() throws Exception {
		return new Coordinate(RandomUtils.randomInt(x_min, x_max),
							  RandomUtils.randomInt(y_min, y_max));
	}

	public Set<Coordinate> getRandom(final int count) throws Exception {
		Set<Coordinate> coordinates = new HashSet<Coordinate>();
		if (count > (x_max - x_min) * (y_max - y_min) / 2) {
			throw new Exception("Suboptimal sampling quantity.");
		}

		while (coordinates.size() < count) {
			coordinates.add(getRandom());
		}

		return coordinates;
	}
	
	/**
	 * Return if the coordinate is in one of the bounds.
	 * @param coordinate
	 * @return
	 */
	public boolean isBound(final Coordinate coordinate) {
		if (getXMin() == coordinate.getX() ||
			getXMax() == coordinate.getX() ||
			getYMin() == coordinate.getY() ||
			getYMax() == coordinate.getY()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns a new box that slides within the boundary if it exceeds it.
	 * @param box
	 * @param boundary
	 * @return
	 * @throws Exception
	 */
	public static final Box slideInbounds(final Box box, final Box boundary) throws Exception {
		if (box.getWidth() > boundary.getWidth() || box.getHeight() > boundary.getHeight()) {
			throw new Exception("Box " + box + " larger than boundary " + boundary + ".");
		}
		int x_min;
		int y_min;

		if (box.getXMin() < boundary.getXMin()) {
			x_min = boundary.getXMin();
		}
		else if (box.getXMax() > boundary.getXMax()) {
			x_min = boundary.getXMax() - box.getWidth();
		}
		else {
			x_min = box.getXMin();
		}

		if (box.getYMin() < boundary.getYMin()) {
			y_min = boundary.getYMin();
		}
		else if (box.getYMax() > boundary.getYMax()) {
			y_min = boundary.getYMax() - box.getWidth();
		}
		else {
			y_min = box.getYMin();
		}
		return new Box(x_min, box.getWidth(), y_min, box.getHeight());
	}
	
	/**
	 * Returns a box bounding all coordinates.
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public static final Box getBoundingBox(final Collection<Coordinate> coordinates) throws Exception {
		Box box = getBoundingBoxIf(coordinates);
		if (box == null) {
			throw new Exception("Invalid box coordinates: " + 
			        StringUtils.concatenate(coordinates, " ", true) + ".");
		}
		return box;
	}
	
	/**
	 * Returns a box bounding all coordinates if the coordinates aren't all 
	 * colinear.
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public static final Box getBoundingBoxIf(final Collection<Coordinate> coordinates) throws Exception {
		CheckUtils.checkSize(coordinates, 2);
		int x_min = Coordinate.getMinX(coordinates);
		int x_max = Coordinate.getMaxX(coordinates);
		int y_min = Coordinate.getMinY(coordinates);
		int y_max = Coordinate.getMaxY(coordinates);
		if (x_min >= x_max || y_min >= y_max) {
			return null;
		}
		try {
			return new Box(x_min, x_max - x_min, y_min, y_max - y_min);
		}
		catch (Exception e) {
			System.err.println("Bounding box for coordinates: " + StringUtils.concatenate(coordinates, " ", true) + ".");
			throw e;
		}
	}
	
	/**
	 * Returns a bounding box that is expanded if this coordinate is outside.
	 * @param box
	 * @param coordinate
	 * @return
	 * @throws Exception
	 */
	public static final Box addToBoundingBox(final Box box, final Coordinate coordinate) throws Exception {
		int x_min = Math.min(box.getXMin(), coordinate.getX());
		int x_max = Math.max(box.getXMax(), coordinate.getX());
		int y_min = Math.min(box.getYMin(), coordinate.getY());
		int y_max = Math.max(box.getYMax(), coordinate.getY());
		return new Box(x_min, x_max - x_min, y_min, y_max - y_min);
	}

	public static final Box getScaledUp(final Box box, final Integer scalingFactor) throws Exception {
		CheckUtils.check(box, "box");
		if (scalingFactor == null || scalingFactor == 1) {
			return box;
		}
		
		int x_min = box.getXMin() * scalingFactor;
		int width = box.getWidth() * scalingFactor;
		int y_min = box.getYMin() * scalingFactor;
		int height = box.getHeight() * scalingFactor;
		return new Box(x_min, width, y_min, height);		
	}
	
	public static final Box getScaledDown(final Box box, final Integer scalingFactor) throws Exception {
		CheckUtils.check(box, "box");
		if (scalingFactor == null || scalingFactor == 1) {
			return box;
		}
		
		int x_min = box.getXMin() / scalingFactor;
		int width = box.getWidth() / scalingFactor;
		int y_min = box.getYMin() / scalingFactor;
		int height = box.getHeight() / scalingFactor;
		return new Box(x_min, width, y_min, height);		

	}
	
	/**
	 * Returns a bounding box that is expanded if this coordinate is outside.
	 * @param box
	 * @param coordinate
	 * @return
	 * @throws Exception
	 */
	public static final Box removeFromBoundingBox(final Box box, 
												  final Set<Coordinate> coordinates,
												  final Coordinate coordinate) throws Exception {
	
		if (box.isBound(coordinate)) {
			return box;
		}
		else {
			Set<Coordinate> remaining;
			
			if (coordinates.contains(coordinate)) {
				remaining = new HashSet<Coordinate>();
				remaining.remove(coordinate);
			}
			else {
				remaining = coordinates;
			}
			if (remaining.size() < 3) {
				return null;
			}
			return getBoundingBoxIf(remaining);
		}
	}
	
	/**
	 * Returns whether or not the coordinates form a box, that is, whether or
	 * not either coordinate is colinear.
	 * @param c0
	 * @param c1
	 * @return
	 */
	public static boolean isBox(final Coordinate c0, final Coordinate c1) {
		if (c0.getX() != c1.getX() && c0.getY() != c1.getY()) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
