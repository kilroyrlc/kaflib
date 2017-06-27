package kaflib.graphics;

import kaflib.types.Coordinate;

/**
 * Defines a rectangle type.
 */
public class Rectangle extends Shape {
	private final int top;
	private final int bottom;
	private final int left;
	private final int right;
	
	/**
	 * Creates the rectangle with the specified extremities.
	 * @param c0
	 * @param c1
	 * @throws Exception
	 */
	public Rectangle(final Coordinate c0, final Coordinate c1) throws Exception {
		top = Math.max(c0.getY(), c1.getY());
		bottom = Math.min(c0.getY(), c1.getY());
		left = Math.min(c0.getX(), c1.getX());
		right = Math.max(c0.getX(), c1.getX());
		
		if (top == bottom || left == right) {
			throw new Exception("Invalid rectangle: " + c0 + ", " + c1 + ".");
		}
		
	}

	/**
	 * Returns whether or not the given point lies within this rectangle 
	 * (inclusive).
	 */
	public boolean contains(final Coordinate point) throws Exception {
		return contains(point.getX(), point.getY());
	}
	
	/**
	 * 	Returns whether or not the given point lies within this rectangle 
	 * (inclusive).
	 */
	public boolean contains(final int x, final int y) throws Exception {
		if (x >= left && x <= right && y >= bottom && y <= top) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Provides a string representation of the rectangle.
	 */
	public String toString() {
		return "Rectangle: top = " + top + " bottom = " + bottom + " left = " + left + " right = " + right;
	}
}
