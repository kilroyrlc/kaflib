package kaflib.graphics;

import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;

/**
 * Defines a circle type.
 */
public class Circle extends Shape {
	private final Coordinate center;
	private final int radius;

	/**
	 * Creates the circle with the given center and radius.
	 * @param center
	 * @param radius
	 * @throws Exception
	 */
	public Circle(final Coordinate center, final int radius) throws Exception {
		CheckUtils.checkNonNegative(radius);
		
		this.center = new Coordinate(center);
		this.radius = radius;
	}
	
	/**
	 * Creates the circle with the given center and radius.
	 * @param x
	 * @param y
	 * @param radius
	 * @throws Exception
	 */
	public Circle(final int x, final int y, final int radius) throws Exception {
		CheckUtils.checkNonNegative(radius);
		
		center = new Coordinate(x, y);
		this.radius = radius;
	}
	
	/**
	 * Returns whether or not the point lies in the circle (inclusive).
	 */
	public boolean contains(final Coordinate point) throws Exception {
		if (point.getDistance(center) <= radius) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns whether or not the point lies in the circle (inclusive).
	 */
	public boolean contains(final int x, final int y) throws Exception {
		return contains(new Coordinate(x, y));
	}
	
	/**
	 * Provides a string representation of the circle.
	 */
	public String toString() {
		return "Circle: center = " + center + " radius = " + radius;
	}
}
