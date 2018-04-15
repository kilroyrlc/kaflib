package kaflib.graphics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Defines core functionality of the selection class.  This is focused on
 * caching ensuring coherence.
 */
public abstract class SelectionCore {
	private static final int DEFAULT_MAP_SIZE = 24;
	private final Set<Coordinate> coordinates;
	private final Set<Coordinate> border;
	private final Set<Coordinate> neighbors;
	private final Coordinate start;
	private Coordinate centroid;
	
	public SelectionCore() {
		coordinates = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		border = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		neighbors = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		centroid = null;
		start = null;
	}
	
	public SelectionCore(final Coordinate coordinate) throws Exception {
		coordinates = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		border = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		neighbors = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		centroid = null;
		start = coordinate;
		add(coordinate);
	}
	
	/**
	 * Adds the coordinate and updates cached information.
	 * @param coordinate
	 */
	public final void add(final Coordinate coordinate) throws Exception  {
		add(coordinate, true);
	}
	
	private final void add(final Coordinate coordinate, 
						   final boolean computeCentroid) throws Exception  {
		// Add the coordinate to the set.
		coordinates.add(coordinate);

		// Add to the border cache if this touches non-selection points.
		if (isBorder(coordinate, coordinates)) {
			border.add(coordinate);
		}
		
		// If this coordinate was listed as a neighbor, it no longer is.
		if (neighbors.contains(coordinate)) {
			neighbors.remove(coordinate);
		}
		
		// Add is coordinate's neighbors to the neighbor list, as long as they
		// aren't in the selection.
		for (Coordinate c : coordinate.getNeighbors()) {
			// If the neighbor is not part of the selection, add it as a 
			// neighbor.
			if (!coordinates.contains(c)) {
				neighbors.add(c);
			}
			// If the neighbor is part of the selection, update the border
			// status.
			else {
				if (isBorder(c, coordinates)) {
					border.add(c);
				}
				else {
					border.remove(c);
				}
			}
		}
		
		// Recompute the centroid.  This must be done, however it can be
		// flagged off for bulk operations.  They must do it once all values
		// have been added.
		if (computeCentroid) {
			computeCentroid();
		}
	}
	
	public final void add(final Collection<Coordinate> coordinates) throws Exception  {
		for (Coordinate coordinate : coordinates) {
			add(coordinate, false);
		}
		computeCentroid();
	}
	
	public final Coordinate getCentroid() {
		return centroid;
	}
	
	/**
	 * Updates the centroid value.  For bulk operations, this is best left to
	 * the end since it doesn't reuse and cached values.
	 * @throws Exception
	 */
	private final void computeCentroid() throws Exception {
		centroid = Coordinate.getCentroid(coordinates);
	}
	
	/**
	 * Returns the start coordinate, or null if there was none or there were
	 * multiple.
	 * @return
	 */
	public final Coordinate getStart() {
		return start;
	}
	
	public final int adjacent(final Coordinate coordinate) throws Exception {
		int adjacent = 0;
		for (Coordinate c : coordinate.getNeighbors()) {
			if (coordinates.contains(c)) { 
				adjacent++;
			}
		}
		return adjacent;
	}
	
	public Pixel getAverage(final Canvas canvas) throws Exception {
		return canvas.getAverage(getCoordinates());
	}

	/**
	 * If there was a start coord, returns the neighbor closest to it.
	 * @return
	 * @throws Exception
	 */
	public final Coordinate getRoundestNeighbor() throws Exception {
		return getRoundestNeighbor(null, null);
	}

	/**
	 * Returns the neighbor that would make this selection the roundest.
	 * @param canvas
	 * @param deltaThreshold
	 * @return
	 * @throws Exception
	 */
	public final Coordinate getRoundestNeighbor(final Canvas canvas, 
												final Integer deltaThreshold) throws Exception {
		Pixel average = null;
		if (canvas != null && deltaThreshold != null) {
			average = getAverage(canvas);
		}
		Coordinate candidate = null;
		int value = 0;
		for (Coordinate coordinate : neighbors) {
			if (canvas != null) {
				// If we have a raster available, skip invalid and transparent
				// coordinates.
				if (!canvas.isValid(coordinate) ||
					canvas.get(coordinate).isTransparent()) {
					continue;
				}
				// If there is a threshold and the delta is greater, skip.
				if (deltaThreshold != null && 
					average.getDelta(canvas.get(coordinate)) > deltaThreshold) {
					continue;
				}
			}
			if (candidate == null || start.getDistanceSquared(coordinate) < value) {
				candidate = coordinate;
				value = start.getDistanceSquared(coordinate);
			}
		}
		return candidate;
	}
	
	public final int size() {
		return coordinates.size();
	}
	
	public final Set<Coordinate> getBorder() {
		return border;
	}
	
	public final Set<Coordinate> getNeighbors() { 
		return neighbors;
	}
	
	public final Set<Coordinate> getCoordinates() {
		return coordinates;
	}
	
	public String toString() {
		if (coordinates.size() == 0) {
			return new String("Uninitialized selection.");
		}
		try {
			return "Coords: " + StringUtils.concatenate(coordinates, " ", true) + 
				   "\nBorder: " + StringUtils.concatenate(border, " ", true) + 
				   "\nNghbrs: " + StringUtils.concatenate(neighbors, " ", true); 
		}
		catch (Exception e) {
			return new String("Unable to create string: " + e.getMessage());
		}
	}
	
	public String toMatrix() throws Exception {
		int x_start = Coordinate.getMinX(coordinates);
		int y_start = Coordinate.getMinY(coordinates);
		int x_size = Math.max(Coordinate.getMaxX(coordinates) - x_start, 1) + 4;
		int y_size = Math.max(Coordinate.getMaxY(coordinates) - y_start, 1) + 4;
		CheckUtils.checkPositive(x_start, "x start");
		CheckUtils.checkPositive(y_start, "y start");
		CheckUtils.checkPositive(x_size, "x size");
		CheckUtils.checkPositive(y_size, "y size");
		
		char values[][] = new char[x_size][y_size];
		
		for (int i = 0; i < x_size; i++) {
			for (int j = 0; j < y_size; j++) {
				values[i][j] = ' ';
			}
		}
		for (Coordinate coordinate : coordinates) {
			values[coordinate.getX() - x_start + 2][coordinate.getY() - y_start + 2] = 'o';
		}
		for (Coordinate coordinate : border) {
			values[coordinate.getX() - x_start + 2][coordinate.getY() - y_start + 2] = 'x';
		}
		for (Coordinate coordinate : neighbors) {
			values[coordinate.getX() - x_start + 2][coordinate.getY() - y_start + 2] = '*';
		}
		if (start != null) {
			values[start.getX() - x_start + 2][start.getY() - y_start + 2] = '%';
		}

		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < x_size; i++) {
			for (int j = 0; j < y_size; j++) {
				buffer.append(values[i][j] + " ");
			}
			buffer.append("\n");
		}
		return new String(buffer);
	}
	
	/**
	 * Returns whether or not the given coordinate is in the border of the set.
	 * Specifically, does the coordinate have a neighbor that is not in the
	 * set.
	 * @param coordinate
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public static boolean isBorder(final Coordinate coordinate, 
								   final Set<Coordinate> coordinates) throws Exception {
		
		for (Coordinate n : coordinate.getNeighbors()) {
			if (!coordinates.contains(n)) {
				return true;
			}
		}
		return false;
	}
	
}
