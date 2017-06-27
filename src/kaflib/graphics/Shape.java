package kaflib.graphics;

import kaflib.types.Coordinate;

/**
 * Defines a generic type to represent a shape.  Presently it just defines an
 * interface for determining if a given point is inside it.
 */
public abstract class Shape {
	public abstract boolean contains(final Coordinate coordinate) throws Exception;
	public abstract boolean contains(final int x, final int y) throws Exception;

}
