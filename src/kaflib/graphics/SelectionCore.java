package kaflib.graphics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.utils.CoordinateUtils;

/**
 * Defines core functionality of the selection class.  This may be bad design
 * but it's meant to bottleneck any mutators to this class so that the 
 * bookkeeping involved with keeping the border/neighbor/centroid caches
 * from getting messed up.
 */
public abstract class SelectionCore {
	private static final int DEFAULT_MAP_SIZE = 24;
	private final Set<Coordinate> coordinates;
	private final Set<Coordinate> border;
	private final Set<Coordinate> neighbors;
	private final Coordinate start;
	private Coordinate centroid;
	private Box bounding_box;
	
	public SelectionCore() {
		coordinates = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		border = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		neighbors = new HashSet<Coordinate>(DEFAULT_MAP_SIZE);
		centroid = null;
		start = null;
		bounding_box = null;
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
/*	
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
		
		if (coordinates.size() > 1) {
			if (bounding_box == null) {
				bounding_box = Box.getBoundingBoxIf(coordinates);
			}
			else {
				bounding_box = Box.getBoundingBox(bounding_box, coordinate);
			}
		}
	}
	*/
	protected final void add(final Coordinate coordinate, 
			final boolean computeCentroid) throws Exception  {
		// Add the coordinate to the set.
		coordinates.add(coordinate);

		for (Coordinate c : coordinate.getNeighbors()) {
			refresh(c);
		}

		// Recompute the centroid.  This must be done, however it can be
		// flagged off for bulk operations.  They must do it once all values
		// have been added.
		if (computeCentroid) {
			computeCentroid();
		}

		updateBoundingBox(coordinate, true);
	}
	
	private final void updateBoundingBox(final Coordinate coordinate, final boolean add) throws Exception {
		if (coordinates.size() > 1) {
			if (bounding_box == null) {
				bounding_box = Box.getBoundingBoxIf(coordinates);
			}
			else {
				if (add) {
					bounding_box = Box.addToBoundingBox(bounding_box, coordinate);
				}
				else {
					bounding_box = Box.removeFromBoundingBox(bounding_box, coordinates, coordinate);
				}
			}
		}
	}
	
	protected final void remove(final Coordinate coordinate, 
							  final boolean computeCentroid) throws Exception  {
		// Add the coordinate to the set.
		coordinates.remove(coordinate);

		for (Coordinate c : coordinate.getNeighbors()) {
			refresh(c);
		}

		// Recompute the centroid.  This must be done, however it can be
		// flagged off for bulk operations.  They must do it once all values
		// have been added.
		if (computeCentroid) {
			computeCentroid();
		}

		updateBoundingBox(coordinate, false);
	}
	
	/**
	 * Refreshes the coordinate lists for the specified point.
	 * @param coordinate
	 * @throws Exception
	 */
	private final void refresh(final Coordinate coordinate) throws Exception {
		// If this coordinate is in the selection.
		if (coordinates.contains(coordinate)) {
			boolean border = isBorder(coordinate, coordinates);
			
			for (Coordinate c : coordinate.getNeighbors()) {
				if (!coordinates.contains(c)) {
					neighbors.add(c);
				}
			}
			
			if (border) {
				this.border.add(coordinate);
			}
			else {
				this.border.remove(coordinate);
			}
		}
		// This coordinate is not in the selection.
		else {
			boolean neighbor = isNeighbor(coordinate, coordinates);

			if (neighbor == true) { 
				neighbors.add(coordinate);
				for (Coordinate c : coordinate.getNeighbors()) {
					if (coordinates.contains(c)) {
						border.add(c);
					}
				}
			}
			else {
				neighbors.remove(coordinate);
			}
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
		centroid = CoordinateUtils.getCentroid(coordinates);
	}
	
	/**
	 * Returns the start coordinate, or null if there was none or there were
	 * multiple.
	 * @return
	 */
	public final Coordinate getStart() {
		return start;
	}
	
	
	/**
	 * Returns the selection's bounding box.
	 * @return
	 */
	public final Box getBoundingBox() {
		return bounding_box;
	}
	
	public final int adjacent(final Coordinate coordinate) throws Exception {
		int adjacent = 0;
		for (Coordinate c : coordinate.getNeighbors()) {
			if (getCoordinates().contains(c)) { 
				adjacent++;
			}
		}
		return adjacent;
	}
	
	public RGBPixel getAverage(final Canvas canvas) throws Exception {
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
		RGBPixel average = null;
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

	/**
	 * Returns whether or not the given coordinate is in the neighbors of this
	 * set.
	 * @param coordinate
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public static boolean isNeighbor(final Coordinate coordinate, 
									 final Set<Coordinate> coordinates) throws Exception {
		if (coordinates.contains(coordinate)) {
			throw new Exception("Coordinate cannot be in the set.");
		}
		
		for (Coordinate n : coordinate.getNeighbors()) {
			if (coordinates.contains(n)) {
				return true;
			}
		}
		return false;
	}
	
}
