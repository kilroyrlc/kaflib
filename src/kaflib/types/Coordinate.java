package kaflib.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;

public class Coordinate {

	private final boolean positive_domain;
	private final int x;
	private final int y;
	private final int hash_code;
	
	/**
	 * Creates a coordinate.
	 * @param x
	 * @param y
	 */
	public Coordinate(final int x, final int y) {
		this.x = x;
		this.y = y;
		this.positive_domain = !(x < 0 | y < 0);
		hash_code = toString().hashCode();
	}
	
	
	public Coordinate(final int x, final int y, final boolean positive) throws Exception {
		this.positive_domain = positive;

		if (positive_domain) {
			CheckUtils.checkNonNegative(x, "x");
			CheckUtils.checkNonNegative(x, "y");
		}
		
		this.x = x;
		this.y = y;

		hash_code = toString().hashCode();
	}
	

	/**
	 * Creates a coordinate copy.
	 * @param coordinate
	 */
	public Coordinate(final Coordinate coordinate) {
		this.x = coordinate.getX();
		this.y = coordinate.getY();
		this.positive_domain = coordinate.getPositiveDomain();
		hash_code = toString().hashCode();
	}
	
	/**
	 * Parses the coordinate out of a serialized string "(x, y)".
	 * @param serial
	 * @throws Exception
	 */
	public Coordinate(final String serial) throws Exception {
		Pattern pattern = Pattern.compile("^\\s*\\((\\d+)\\s*\\,\\s*(\\d+)\\)\\s*$");
		Matcher matcher = pattern.matcher(serial);
		if (!matcher.matches()) {
			throw new Exception("Could not parse (x, y) out of: " + serial + ".");
		}
		this.x = Integer.valueOf(matcher.group(1));
		this.y = Integer.valueOf(matcher.group(2));
		this.positive_domain = !(x < 0 | y < 0);
		hash_code = toString().hashCode();
	}
	
	/**
	 * Get distance to another coordinate.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public int getDistance(final Coordinate other) throws Exception {
		CheckUtils.check(other, "other coord");
		
		return (int) Math.sqrt(getDistanceSquared(other));
	}
	
	public boolean getPositiveDomain() {
		return positive_domain;
	}
	
	/**
	 * Returns (x2 - x1)^2 | (y2 - y1)^2.  So, distance without the final
	 * square root.  I want to say this is good enough for a relative distance
	 * calculation because if root(a) < root(b), a < b.  Right?
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public int getDistanceSquared(final Coordinate other) {
		
		return ((other.getX() - x) * (other.getX() - x)) +
 			   ((other.getY() - y) * (other.getY() - y));
	}
	
	public int getRisePlusRun(final Coordinate other) throws Exception {
		CheckUtils.check(other, "other coord");
		return Math.abs(other.getX() - x) + Math.abs(other.getY() - y);
	}
	
	public Coordinate getNorth() throws Exception {
		return new Coordinate(x, y - 1);
	}

	public Coordinate getSouth() throws Exception {
		return new Coordinate(x, y + 1);
	}

	public Coordinate getWest() throws Exception {
		return new Coordinate(x - 1, y);
	}

	public Coordinate getEast() throws Exception {
		return new Coordinate(x + 1, y);
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
	public Direction getNSEWDirection(final Coordinate other) throws Exception {
		Coordinate delta = subtractFrom(other);
		if (Math.abs(delta.getY()) >= Math.abs(delta.getX())) {
			if (delta.getY() >= 0) {
				return Direction.SOUTH;
			}
			else {
				return Direction.NORTH;
			}
		}
		else {
			if (delta.getX() >=0) {
				return Direction.EAST;
			}
			else {
				return Direction.WEST;
			}
		}
	}

	public Pair<Direction, Direction> getNSEWDirections(final Coordinate other) throws Exception {
		Pair<Direction, Direction> directions = new Pair<Direction, Direction>();
		Coordinate delta = subtractFrom(other);
		if (delta.getY() >= 0) {
			directions.setKey(Direction.SOUTH);
		}
		else {
			directions.setKey(Direction.NORTH);
		}
		if (delta.getX() >=0) {
			directions.setValue(Direction.EAST);
		}
		else {
			directions.setValue(Direction.WEST);
		}
		return directions;
	}
	
	public boolean isWithin(final int width, final int height) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void checkWithin(final int width, final int height) throws Exception {
		if (!isWithin(width, height)) {
			throw new Exception("Invalid coordinate " + toString() + 
								" for w: " + width + " and h: " + height + ".");
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

	public Coordinate add(final Coordinate other) {
		return new Coordinate(x + other.getX(), y + other.getY());
	}
	
	/**
	 * Returns a new coordinate as close as possible to a box from 0-max.
	 * @param value
	 * @param maxX
	 * @param maxY
	 * @return
	 * @throws Exception
	 */
	public Coordinate bound(final int maxX, final int maxY) throws Exception {
		CheckUtils.checkPositive(maxX, "x");
		CheckUtils.checkPositive(maxY, "y");
		int x = Math.max(getX(), 0);
		int y = Math.max(getY(), 0);
		x = Math.min(x, maxX);
		y = Math.min(y, maxY);
		
		return new Coordinate(x, y);
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
	
	public boolean isAdjacent(final Coordinate coordinate) throws Exception {
		if (getNeighbors().contains(coordinate)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Coordinate getRandomAdjacent() throws Exception {
		int new_x = x + RandomUtils.randomInt(-1, 1);
		int new_y = y + RandomUtils.randomInt(-1, 1);
		while (new_x == x && new_y == y) {
			new_x = x + RandomUtils.randomInt(-1, 1);
			new_y = y + RandomUtils.randomInt(-1, 1);			
		}
		
		return new Coordinate(new_x, new_y);
	}

	public Set<Coordinate> getNeighbors() throws Exception {
		return getNeighbors(1, 1);
	}
	
	public Coordinate getNeighbor(final Direction direction) throws Exception {
		return new Coordinate(x + Direction.getOffset(direction).getX(),
							  y + Direction.getOffset(direction).getY());
	}

	/**
	 * Returns the neighbors of the this point with a square dx/dy distance.
	 * @param dx
	 * @param dy
	 * @return
	 * @throws Exception
	 */
	public Set<Coordinate> getNeighbors(final int dx, final int dy) throws Exception {
		Set<Coordinate> neighbors = new HashSet<Coordinate>((dx + 2) * (dy + 2));
		int max = Integer.MAX_VALUE;
		int min = Integer.MIN_VALUE;
		if (positive_domain) {
			min = 0;
		}
		int startx = Math.max(min, getX() - dx);
		int starty = Math.max(min, getY() - dy);
		int endx = Math.min(max, getX() + dx);
		int endy = Math.min(max, getY() + dy);
		
		for (int i = startx; i <= endx; i++) {
			for (int j = starty; j <= endy; j++) {
				if (i == getX() && j == getY()) {
					continue;
				}
				neighbors.add(new Coordinate(i, j, positive_domain));
			}
		}
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
		return hash_code;
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

	public String toSerial() {
		return "(" + x + ", " + y + ")";
	}
	
	public String toString() {
		return toSerial();
	}
	
	
	/**
	 * Returns the average x and y.
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public static Coordinate getCentroid(final Collection<Coordinate> coordinates) throws Exception {
		int x = 0;
		int y = 0;
		
		for (Coordinate coordinate : coordinates) {
			CheckUtils.checkAddOverflow(coordinate.getX(), x);
			CheckUtils.checkAddOverflow(coordinate.getY(), y);
			
			x += coordinate.getX();
			y += coordinate.getY();
		}
		return new Coordinate(x / coordinates.size(), y / coordinates.size());
	}
	
	public static int getMinX(final Collection<Coordinate> coordinates) {
		int x = Integer.MAX_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getX() < x) {
				x = coordinate.getX();
			}
		}
		return x;
	}
	
	public static int getMaxX(final Collection<Coordinate> coordinates) {
		int x = Integer.MIN_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getX() > x) {
				x = coordinate.getX();
			}
		}
		return x;
	}
	
	public static int getMinY(final Collection<Coordinate> coordinates) {
		int y = Integer.MAX_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getY() < y) {
				y = coordinate.getY();
			}
		}
		return y;
	}
	
	public static int getMaxY(final Collection<Coordinate> coordinates) {
		int y = Integer.MIN_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getY() > y) {
				y = coordinate.getY();
			}
		}
		return y;
	}
	
	public Collection<Coordinate> getBox(final int width, final int height) throws Exception {
		CheckUtils.checkPositive(width);
		CheckUtils.checkPositive(height);

		List<Coordinate> box = new ArrayList<Coordinate>();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				box.add(new Coordinate(getX() + i, getY() + j));
			}
		}
		return box;
	}

	public static Coordinate getRandom(final Box box) throws Exception {
		return getRandom(box.getXMin(), box.getXMax(), box.getYMin(), box.getYMax());
	}
	
	public static Coordinate getRandom(final int xMin, final int xMax, 
									   final int yMin, final int yMax) throws Exception {
		return new Coordinate(RandomUtils.randomInt(xMin, xMax),
							  RandomUtils.randomInt(yMin, yMax));
	}

	public static Set<Coordinate> getRandom(final int count, final int xMin, final int xMax, 
			   						   final int yMin, final int yMax) throws Exception {
		Set<Coordinate> coordinates = new HashSet<Coordinate>();
		if (count > (xMax - xMin) * (yMax - yMin) / 2) {
			throw new Exception("Suboptimal sampling quantity.");
		}
		
		while (coordinates.size() < count) {
			coordinates.add(getRandom(xMin, xMax, yMin, yMax));
		}
		
		return coordinates;
	}
	
}
