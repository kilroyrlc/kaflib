package kaflib.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import kaflib.utils.CheckUtils;

public class Coordinate {

	private int x;
	private int y;
	
	public Coordinate(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	

	public Coordinate(final Coordinate coordinate) {
		this.x = coordinate.getX();
		this.y = coordinate.getY();
	}
	
	/**
	 * Get distance to another coordinate.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public int getDistance(Coordinate other) throws Exception {
		CheckUtils.check(other, "other coord");
		
		return (int) Math.sqrt(((other.getX() - x) * (other.getX() - x)) +
							  ((other.getY() - y) * (other.getY() - y)));
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
	
	/**
	 * Return the nearest coordinate from the collection.
	 * @param others
	 * @return
	 * @throws Exception
	 */
	public Coordinate getNearest(Collection<Coordinate> others) throws Exception {
		CheckUtils.check(others, "coords");
		Coordinate min = null;
		int min_distance = Integer.MAX_VALUE;
		
		Iterator<Coordinate> i = others.iterator();
		while (i.hasNext()) {
			Coordinate c = i.next();
			int d = getDistance(c);
			if (d < min_distance) {
				min = c;
				min_distance = d;
			}
		}
		return min;
	}
	
	/**
	 * Returns the midpoint between the two points.
	 * @param endpoint
	 * @return
	 * @throws Exception
	 */
	public Coordinate getMidpoint(Coordinate endpoint) throws Exception {
		CheckUtils.check(endpoint, "endpoint");
		
		int dx = (endpoint.getX() - x) / 2;
		int dy = (endpoint.getY() - y) / 2;
		
		if (dx == 0 && dy == 0) {
			return new Coordinate(this);
		}
		
		return new Coordinate(x + dx, y + dy);
	}
	
	/**
	 * Gets a point between coordinates that is approximately 'distance' from 
	 * this.  Does a binary search, there may be a mathy way.
	 * @param end
	 * @param distance
	 * @return
	 * @throws Exception
	 */
	public Coordinate getColinear(Coordinate endpoint, int distance) throws Exception {
		return getColinear(endpoint, distance, 0);
	}
	
	private Coordinate getColinear(Coordinate endpoint, int distance, int count) throws Exception {
		CheckUtils.check(endpoint, "endpoint");
		CheckUtils.checkNonNegative(distance);

		if (distance == 0 || this.equals(endpoint)) {
			return new Coordinate(this);
		}
		
		Coordinate midpoint = getMidpoint(endpoint);
		int d = getDistance(midpoint);

		if (d > distance) {
			return getColinear(midpoint, distance, count + 1);
		}
		else if (d < distance) {
			return midpoint.getColinear(endpoint, distance - d, count + 1);
		}
		else {
			return midpoint;
		}
		
	}
	
	/**
	 * Get a randomly-placed point with 'radius' of this point.
	 * @param radius
	 * @return
	 */
	public Coordinate getRandomNeighbor(int radius) throws Exception {
		CheckUtils.checkNonNegative(radius);
		
		if (radius == 0) {
			return new Coordinate(this);
		}
		
		Random random = new Random();

		// Add a random dx/dy to the point.  Check the distance, if it's
		// acceptable, return, otherwise try again and decrease the possible
		// dx/dy values.
		for (int i = radius; i > 0; i--) {
			int dx = random.nextInt(i);
			int dy = random.nextInt(i);
			if (random.nextBoolean()) {
				dx *= -1;
			}
			if (random.nextBoolean()) {
				dy *= -1;
			}
			Coordinate c = new Coordinate(x + dx, y + dy);
			
			if (this.getDistance(c) < radius) {
				return c;
			}
		}
		return new Coordinate(this);
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
	
	public enum BoxValues {
		X,
		Y,
		WIDTH,
		HEIGHT
	}
	
	/**
	 * Returns an x/y width/height where width and height are positive.
	 * @param a
	 * @param b
	 * @return
	 */
	public static Map<BoxValues, Integer> getXYWH(final Coordinate a, 
										          final Coordinate b) {
		Map<BoxValues, Integer> values = new HashMap<BoxValues, Integer>();
		values.put(BoxValues.X, Math.min(a.getX(), b.getX()));
		values.put(BoxValues.Y, Math.min(a.getY(), b.getY()));
		values.put(BoxValues.WIDTH, Math.abs(a.getX() - b.getX()));
		values.put(BoxValues.HEIGHT, Math.abs(a.getY() - b.getY()));
		return values;
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
