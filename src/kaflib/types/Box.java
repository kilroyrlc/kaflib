package kaflib.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.utils.CheckUtils;
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
	

	public Box(final int xMin, final int xMax,
			   final int yMin, final int yMax) throws Exception {
		x_min = xMin;
		x_max = xMax;
		y_min = yMin;
		y_max = yMax;
		
		top_left = new Coordinate(x_min, y_min);
		top_right = new Coordinate(x_max, y_min);
		bottom_left = new Coordinate(x_min, y_max);
		bottom_right = new Coordinate(x_max, y_max);
		
		if (x_min == x_max || y_min == y_max) {
			throw new Exception("Invalid box bounds.");
		}
	}
	
	/**
	 * Returns the box defined by the given coordinates.
	 * @param c0
	 * @param c1
	 * @throws Exception
	 */
	public Box(final Coordinate c0, final Coordinate c1) throws Exception {
		List<Coordinate> coordinates = TypeUtils.getList(c0, c1);
		
		x_min = Coordinate.getMinX(coordinates);
		x_max = Coordinate.getMaxX(coordinates);
		y_min = Coordinate.getMinY(coordinates);
		y_max = Coordinate.getMaxY(coordinates);
		
		top_left = new Coordinate(x_min, y_min);
		top_right = new Coordinate(x_max, y_min);
		bottom_left = new Coordinate(x_min, y_max);
		bottom_right = new Coordinate(x_max, y_max);
		
		if (x_min == x_max || y_min == y_max) {
			throw new Exception("Invalid box coordinates: " + 
						        StringUtils.concatenate(coordinates, " ", true) + ".");
		}
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
	
	public boolean containsOrAdjacent(final Coordinate coordinate) throws Exception {
		return getBorderBox().contains(coordinate);
	}
	
	public Box getBorderBox() throws Exception {
		CheckUtils.checkSubOverflow(x_min, 1);
		CheckUtils.checkAddOverflow(x_max, 1);
		CheckUtils.checkSubOverflow(y_min, 1);
		CheckUtils.checkAddOverflow(y_max, 1);
		return new Box(x_min - 1, x_max + 1, y_min - 1, y_max + 1);
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
		if (x_min == x_max || y_min == y_max) {
			return null;
		}
		
		return new Box(x_min, x_max, y_min, y_max);
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
		return new Box(x_min, x_max, y_min, y_max);
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
}
