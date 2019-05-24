package kaflib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.types.Coordinate;
import kaflib.types.IntegerHistogram;
import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Contains a set of coordinates comprising a selection area.
 */
public class Selection extends SelectionCore {

	public enum Approach {
		RGB,
		OPACITY,
		LUMINANCE
	}
	
	/**
	 * Creates an empty selection.
	 */
	public Selection() {
		super();
	}

	/**
	 * Creates a selection with a single starting coordinate.
	 * @param coordinate
	 * @throws Exception
	 */
	public Selection(final Coordinate coordinate) throws Exception {
		super(coordinate);
	}
	
	/**
	 * Creates a selection with a collection of starting coordinates.
	 * @param coordinates
	 * @throws Exception
	 */
	public Selection(final Collection<Coordinate> coordinates) throws Exception {
		this();
		add(coordinates);
	}
	
	/**
	 * Returns the average difference between each pixel and the average pixel
	 * value.
	 * @param canvas
	 * @return
	 * @throws Exception
	 */
	public int getAverageDelta(final Canvas canvas) throws Exception {
		return canvas.getAverageDelta(getCoordinates());
	}
	
	
	/**
	 * Gets all pixels within delta of value.
	 * @param canvas
	 * @param delta
	 * @return
	 * @throws Exception
	 */
	public List<RGBPixel> getWithin(final Canvas canvas,
								 final RGBPixel value,
								 final Integer delta) throws Exception {
		List<RGBPixel> pixels = new ArrayList<RGBPixel>();
		for (Coordinate coordinate : getCoordinates()) {
			if (canvas.isValid(coordinate) && 
				canvas.get(coordinate).getDelta(value) < delta) {
				pixels.add(canvas.get(coordinate));
			}
		}
		return pixels;
	}
	
	/**
	 * Returns whether or not the selection contains the specified coordinate.
	 * @param coordinate
	 * @return
	 */
	public boolean contains(final Coordinate coordinate) {
		return getCoordinates().contains(coordinate);
	}

//	public int getValue
//	if (approach == Approach.RGB) {
//		
//	}
//	else if (approach == Approach.OPACITY) {
//		
//	}
//	else if (approach == Approach.LUMINANCE) {
//		
//	}
//	else {
//		throw new Exception("Approach not implemented: " + approach + ".");
//	}
	
	/**
	 * Returns whether this selection has shared coordinates with another.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public boolean overlaps(final Selection other) throws Exception {
		// No bounding box overlap, no need to check.
		if (getBoundingBox() != null &&
			other.getBoundingBox() != null &&
			!getBoundingBox().overlaps(other.getBoundingBox())) {
			return false;
		}
		// See if any coordinates are shared.
		for (Coordinate coordinate : getCoordinates()) {
			if (other.contains(coordinate)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns whether this selection is adjacent to another.  Overlapping
	 * selections do not prevent adjacency but do not constitute adjacency.
	 * @param other
	 * @return
	 * @throws Exception
	 */
	public boolean isAdjacent(final Selection other) throws Exception {
		CheckUtils.check(other, "other selection");
		
		// No bounding box adjacency, no need to check.
		if (getBoundingBox() != null &&
			other.getBoundingBox() != null &&
		    !getBoundingBox().isAdjacentOrOverlapping(other.getBoundingBox())) {
			return false;
		}
		
		// See if any border coordinates are adjacent to the other.
		for (Coordinate coordinate : getBorder()) {
			if (other.isAdjacent(coordinate)) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Checks if the coordinate is adjacent to the selection.
	 * @param coordinate
	 * @return
	 * @throws Exception
	 */
	public boolean isAdjacent(final Coordinate coordinate) throws Exception {
		return getNeighbors().contains(coordinate);
	}
	
	public int opaquePixels(final Canvas canvas) throws Exception {
		int count = 0;
		for (Coordinate coordinate : getCoordinates()) {
			if (canvas.isValid(coordinate) && 
				canvas.get(coordinate).isOpaque()) {
				count++;
			}
		}
		return count;
	}
	
	public List<Selection> getAdjacentSelections(final Collection<Selection> selections) throws Exception {
		List<Selection> neighbors = new ArrayList<Selection>();
		
		for (Selection selection : selections) {
			if (isAdjacent(selection)) {
				neighbors.add(selection);
			}
		}
		return neighbors;
	}
	
	/**
	 * Adds all connected points that meet the threshold capacity.
	 * @param threshold
	 * @throws Exception
	 */
	public void addNeighbors(final Canvas canvas,
							 final Opacity threshold) throws Exception {
		
		Set<Coordinate> add_list = new HashSet<Coordinate>();
		for (Coordinate c : getNeighbors()) {
			if (canvas.isValid(c) &&
				canvas.get(c).getOpacity().compareTo(threshold) >= 0) {
				add_list.add(c);
			}
		}
		add(add_list);
		
	}

	/**
	 * Returns the closest neighbor pixel by rgb average.
	 * @param canvas
	 * @return
	 * @throws Exception
	 */
	public Coordinate getClosestRGBNeighbor(final Canvas canvas, 
											final Integer deltaThreshold) throws Exception {
		RGBPixel average = getAverage(canvas);
		Coordinate coordinate = null;
		int delta = Integer.MAX_VALUE;
		for (Coordinate c : getNeighbors()) {
			if (!canvas.isValid(c)) {
				continue;
			}
			if (deltaThreshold != null && 
				average.getDelta(canvas.get(c)) > deltaThreshold) {
				continue;
			}
			if (average.getDelta(canvas.get(c)) < delta) {
				delta = average.getDelta(canvas.get(c));
				coordinate = c;
			}
		}
		return coordinate;
	}
	
	/**
	 * Displays a super cool text representation of the selection.
	 * @return
	 * @throws Exception
	 */
	public String toMatrix() throws Exception {
		Set<Coordinate> coordinates = getCoordinates();
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
		for (Coordinate coordinate : getBorder()) {
			values[coordinate.getX() - x_start + 2][coordinate.getY() - y_start + 2] = 'x';
		}
		for (Coordinate coordinate : getNeighbors()) {
			values[coordinate.getX() - x_start + 2][coordinate.getY() - y_start + 2] = '*';
		}
		if (getStart() != null) {
			values[getStart().getX() - x_start + 2][getStart().getY() - y_start + 2] = '%';
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
	

	
	public String toString() {
		if (getCoordinates().size() == 0) {
			return new String("Uninitialized selection.");
		}
		try {
			return "Coords: " + StringUtils.concatenate(getCoordinates(), " ", true) + 
				   "\nBorder: " + StringUtils.concatenate(getBorder(), " ", true) + 
				   "\nNghbrs: " + StringUtils.concatenate(getNeighbors(), " ", true); 
		}
		catch (Exception e) {
			return new String("Unable to create string: " + e.getMessage());
		}
	}
	
	
	/**
	 * Returns a selection that is a circle.
	 * @param image
	 * @param center
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	public static final Selection getCircle(final Coordinate center, 
											final int radius) throws Exception {
		Selection selection = new Selection();
		int radius_sq = radius * radius;
		for (int i = center.getX() - radius; i < center.getX() + radius; i++) {
			for (int j = center.getY() - radius; j < center.getY() + radius; j++) {
				Coordinate point = new Coordinate(i, j);
				if (point.getDistanceSquared(center) <= radius_sq) {
					selection.add(point);
				}
			}
		}
		return selection;
	}
	
	/**
	 * Returns a selection that is a box.
	 * @param image
	 * @param center
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	public static final Selection getBox(final Coordinate topLeft, 
											final int width,
											final int height) throws Exception {

		Selection selection = new Selection();
		for (int i = topLeft.getX(); i < width; i++) {
			for (int j = topLeft.getY(); j < height; j++) {
				Coordinate point = new Coordinate(i, j);
				selection.add(point);
			}
		}
		return selection;
	}

	public static final Selection getBox(final Coordinate center, 
										 final int radius) throws Exception {

		Selection selection = new Selection();
		for (int i = center.getX() - radius; i < center.getX() + radius; i++) {
			for (int j = center.getY() - radius; j < center.getY() + radius; j++) {
				selection.add(new Coordinate(i, j));
			}
		}
		return selection;
	}

	/**
	 * Returns a selection that is a box.
	 * @param image
	 * @param center
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	public static final Selection getDiamond(final Coordinate center, 
											 final int radius) throws Exception {

		Selection selection = new Selection();
		for (int i = center.getX() - radius; i < center.getX() + radius; i++) {
			for (int j = center.getY() - radius; j < center.getY() + radius; j++) {
				Coordinate point = new Coordinate(i, j);
				if (center.getRisePlusRun(point) <= radius) {
					selection.add(point);
				}
			}
		}
		return selection;
	}
	
	/**
	 * Returns a selection that is a star that is a mediocre stand in for a circle.
	 * @param image
	 * @param center
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	public static final Selection getStar(final Coordinate center, 
										  final int radius) throws Exception {
		Selection selection = getDiamond(center, radius);
		selection.add(getBox(center, radius * 3 / 4).getCoordinates());
		return selection;
	}
	
	
	/**
	 * Wiggles a star pattern around a radius of adjustment, returns the star
	 * with the greatest number of opaque pixels.
	 * @param starting
	 * @param radius
	 * @param adjustment
	 * @param canvas
	 * @return
	 * @throws Exception
	 */
	public static final Selection getMostOpaqueStar(final Coordinate starting,
													final int radius,
												 	final int adjustment,
												 	final Canvas canvas) throws Exception {
		Selection selection = getStar(starting, radius);
		Selection best_selection = selection;
		int opaque = selection.opaquePixels(canvas);
		int best_opacity = opaque;
		
		for (int i = starting.getX() - radius; i < starting.getX() + radius; i++) {
			for (int j = starting.getY() - radius; j < starting.getY() + radius; j++) {
				Coordinate point = new Coordinate(i, j);
				if (starting.getRisePlusRun(point) <= radius) {
					selection = getStar(point, radius);
					opaque = selection.opaquePixels(canvas);
					if (opaque > best_opacity) {
						best_selection = selection;
						best_opacity = opaque;					
					}					
				}
			}
		}
		return best_selection;
	}
	
	public static List<Selection> getAllSelections(final Canvas canvas,
										    	   final Opacity threshold) throws Exception {
		List<Selection> list = new ArrayList<Selection>();
		Set<Coordinate> traversed = new HashSet<Coordinate>();
		for (int i = 0; i < canvas.getWidth(); i++) {
			for (int j = 0; j < canvas.getHeight(); j++) {
				Coordinate coordinate = new Coordinate(i, j);
				RGBPixel pixel = canvas.get(coordinate);
				if (pixel == null ||
					traversed.contains(coordinate) ||
					pixel.getOpacity().compareTo(threshold) < 0) {
					continue;
				}
				Selection selection = new Selection(coordinate);
				selection.addNeighbors(canvas, threshold);
				list.add(selection);
				traversed.addAll(selection.getCoordinates());
			}
		}
		return list;
	}
	
	public IntegerHistogram getLuminanceHistogram(final Canvas canvas) throws Exception {
		return canvas.getLuminanceHistogram(getCoordinates());
	}
	
	/**
	 * Draws a line from the top/leftmost coordinate to the other side of the
	 * selection, returns the max rgb delta for a given window size of pixels.
	 * Possibly useful to find areas of focus + contrast.
	 * @param canvas
	 * @param window
	 * @param horizontal
	 * @return
	 * @throws Exception
	 */
	public int getMaxDelta(final Canvas canvas, 
							final int windowSize, 
							final boolean horizontal) throws Exception {
		Set<Coordinate> coordinates = getCoordinates();
		List<Coordinate> window = new ArrayList<Coordinate>();
		Coordinate start;
		Coordinate next;
		int value = 0;
		if (horizontal) {
			start = Coordinate.getMinXCoordinate(coordinates);
			next = start.getEast();
		}
		else {
			start = Coordinate.getMinYCoordinate(coordinates);
			next = start.getSouth();
		}
		window.add(start);
		window.add(next);

		if (!coordinates.contains(next)) {
			return 0;
		}
		value = Math.max(value, RGBPixel.getMaxDeltaFrom(canvas.get(window)));

		while (coordinates.contains(next)) {
			if (horizontal) {
				next = next.getEast();
			}
			else {
				next = next.getSouth();
			}
			window.add(next);
			while (window.size() > windowSize) {
				window.remove(0);
			}
			value = Math.max(value, RGBPixel.getMaxDeltaFrom(canvas.get(window)));
		}	
		return value;
	}
	
	/**
	 * Removes and reassigns each pixel in the selection.
	 * @param canvas
	 * @param selection
	 * @param selections
	 * @param approach
	 * @throws Exception
	 */
//	public static void dissolveLu(final Canvas canvas,
//								final Selection selection,
//								final Collection<Selection> selections,
//								final Approach approach) throws Exception {
//		List<Pair<Selection, Integer>> adjacent = new ArrayList<Pair<Selection, Integer>>();
//		
//		
//		for (Selection s : selection.getAdjacentSelections(selections)) {
//			
//			
//			//adjacent.add(new Pair<Selection, Integer>(s, value));
//		}
//		
//		
//		
//		while (selection.size() > 0) {
//			for (Coordinate coordinate : selection.getBorder()) {
//				
//			}
//		}
//	}
	
//	public static Selection findBestMatch(final Canvas canvas,
//										  final Coordinate coordinate,
//										  final Collection<Selection> selections,
//										  final Approach approach) throws Exception {
//		Selection choice = null;
//		Integer value = null;
//		for (Selection selection : selections) {
//			if (!selection.isAdjacent(coordinate)){
//				continue;
//			}
//			
//			if (approach == Approach.RGB) {
//				
//			}
//			else if (approach == Approach.OPACITY) {
//				
//			}
//			else if (approach == Approach.LUMINANCE) {
//				
//			}
//			else {
//				throw new Exception("Approach not implemented: " + approach + ".");
//			}
//		}
//		
//	}
	
}



