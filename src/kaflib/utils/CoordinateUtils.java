package kaflib.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.MRUCache;

/**
 * Contains a collection of static functions used to perform operations
 * on the Coordinate type.
 */
public class CoordinateUtils {
	
	private static final int CIRCLES_CACHE_SIZE = 16;
	private static MRUCache<Integer, Set<Coordinate>> circles;
	

	public static Set<Coordinate> getCircle(final int radius) throws Exception {
		return getCircle(radius, null, null);
	}
	
	public static Set<Coordinate> getCircle(final int radius, final Box boundary) throws Exception {
		return getCircle(radius, null, boundary);
	}

	
	public static Set<Coordinate> getCircle(final int radius, final Coordinate center) throws Exception {
		return getCircle(radius, center, null);
	}
	
	public static List<Coordinate> getCircleOrdered(final int radius, final Coordinate center, final Box boundary) throws Exception {
		List<Coordinate> list = new ArrayList<Coordinate>();
		list.addAll(getCircle(radius, center, boundary));
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Returns a circle of coordinates within the given radius, biased from the
	 * center (if specified) and cropped to the bounding box (if specified).
	 * Uses a static cache of most recently used values.
	 * @param radius
	 * @param center
	 * @param boundary
	 * @return
	 * @throws Exception
	 */
	public static Set<Coordinate> getCircle(final int radius, final Coordinate center, final Box boundary) throws Exception {
		if (circles == null) {
			circles = new MRUCache<Integer, Set<Coordinate>>(CIRCLES_CACHE_SIZE);
		}
		Set<Coordinate> relative_circle = circles.lookup(radius);
		if (relative_circle == null) {
			relative_circle = getNewCircle(radius);
			circles.insert(radius, relative_circle);
		}
		if (center == null || center.equals(Coordinate.ORIGIN)) {
			if (boundary == null) {
				return relative_circle;
			}
			else {
				Set<Coordinate> absolute_circle = new HashSet<Coordinate>(relative_circle.size());
				absolute_circle.addAll(boundary.intersection(relative_circle));
				return absolute_circle;
			}
		}
		else {
			Set<Coordinate> absolute_circle = new HashSet<Coordinate>(relative_circle.size());
			for (Coordinate coordinate : relative_circle) {
				absolute_circle.add(new Coordinate(coordinate.getX() + center.getX(), coordinate.getY() + center.getY()));
			}
			if (boundary == null) {
				return absolute_circle;
			}
			else {
				// Jankily reusing relative circle to mule absolutes to the
				// return.
				relative_circle.clear();
				relative_circle.addAll(boundary.intersection(absolute_circle));
				return relative_circle;
			}
		}
	}

	/**
	 * Returns a set of coordinates composing a circle.  Visibility is private
	 * so users invoke the cached version of this function.
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	private static Set<Coordinate> getNewCircle(final int radius) throws Exception {
		CheckUtils.checkPositive(radius, "radius");
		Set<Coordinate> coordinates = new HashSet<Coordinate>();
		for (int i = -1 * radius; i <= radius; i++) {
			for (int j = -1 * radius; j <= radius; j++) {
				Coordinate point = new Coordinate(i, j);
				if (point.getDistance(Coordinate.ORIGIN) <= radius) {
					coordinates.add(point);
				}
			}
		}
		return coordinates;
	}
	
	public static Set<Coordinate> getRandom(final int count, final int xMin, final int xMax, 
			   						   final int yMin, final int yMax) throws Exception {
		Set<Coordinate> coordinates = new HashSet<Coordinate>();
		if (count > (xMax - xMin) * (yMax - yMin) / 2) {
			throw new Exception("Suboptimal sampling quantity.");
		}
		
		while (coordinates.size() < count) {
			coordinates.add(CoordinateUtils.getRandom(xMin, xMax, yMin, yMax));
		}
		
		return coordinates;
	}

	public static Coordinate getRandom(final Box box) throws Exception {
		return CoordinateUtils.getRandom(box.getXMin(), box.getXMax(), box.getYMin(), box.getYMax());
	}

	public static Coordinate getRandom(final int xMin, final int xMax, 
									   final int yMin, final int yMax) throws Exception {
		return new Coordinate(RandomUtils.randomInt(xMin, xMax),
							  RandomUtils.randomInt(yMin, yMax));
	}

	public static Coordinate getMaxYCoordinate(final Collection<Coordinate> coordinates) {
		Coordinate max = null;
		int y = Integer.MIN_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getY() > y) {
				y = coordinate.getY();
				max = coordinate;
			}
		}
		return max;
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

	public static int getMinY(final Collection<Coordinate> coordinates) {
		int y = Integer.MAX_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getY() < y) {
				y = coordinate.getY();
			}
		}
		return y;
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

	public static Coordinate getMinXCoordinate(final Collection<Coordinate> coordinates) {
		Coordinate min = null;
		int x = Integer.MAX_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getX() < x) {
				x = coordinate.getX();
				min = coordinate;
			}
		}
		return min;
	}

	public static Coordinate getMaxXCoordinate(final Collection<Coordinate> coordinates) {
		int x = Integer.MIN_VALUE;
		Coordinate max = null;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getX() > x) {
				x = coordinate.getX();
				max = coordinate;
			}
		}
		return max;
	}

	public static Coordinate getMinYCoordinate(final Collection<Coordinate> coordinates) {
		Coordinate min = null;
		int y = Integer.MAX_VALUE;
		for (Coordinate coordinate : coordinates) {
			if (coordinate.getY() < y) {
				y = coordinate.getY();
				min = coordinate;
			}
		}
		return min;
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


}
