package kaflib.types;

import java.util.HashSet;
import java.util.Set;

import kaflib.utils.CheckUtils;

public class Coordinate {

	private int x;
	private int y;
	
	public Coordinate(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getRow() {
		return y;
	}
	
	public int getColumn() {
		return x;
	}
	
	/**
	 * Returns the delta between coordinates, the other minus this.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public Coordinate subtractFrom(final Coordinate other) throws Exception {
		CheckUtils.check(other, "other coordinate");
		return new Coordinate(other.getX() - getX(), other.getY() - getY());
	}
	
	/**
	 * Returns the NSEW direction from this coordinate to the other.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public Direction.Cardinal getNSEWDirection(final Coordinate other) throws Exception {
		Coordinate delta = subtractFrom(other);
		if (Math.abs(delta.getY()) >= Math.abs(delta.getX())) {
			if (delta.getY() >= 0) {
				return Direction.Cardinal.NORTH;
			}
			else {
				return Direction.Cardinal.SOUTH;
			}
		}
		else {
			if (delta.getX() >=0) {
				return Direction.Cardinal.EAST;
			}
			else {
				return Direction.Cardinal.WEST;
			}
		}
	}
	
	public Set<Coordinate> getNeighbors() {
		Set<Coordinate> neighbors = new HashSet<Coordinate>();
		neighbors.add(new Coordinate(getX() - 1, getY() - 1));
		neighbors.add(new Coordinate(getX() - 1, getY() + 1));
		neighbors.add(new Coordinate(getX() + 1, getY() - 1));
		neighbors.add(new Coordinate(getX() + 1, getY() + 1));
		neighbors.add(new Coordinate(getX() - 1, getY()));
		neighbors.add(new Coordinate(getX() + 1, getY()));
		neighbors.add(new Coordinate(getX(), getY() - 1));
		neighbors.add(new Coordinate(getX(), getY() + 1));
		return neighbors;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof Coordinate) {
			return equals((Coordinate) o);
		}
		else {
			return false;
		}
	}
	
	public boolean equals(Coordinate c) {
		return x == getX() && y == getY();
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
}
