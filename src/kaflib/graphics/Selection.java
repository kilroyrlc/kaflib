package kaflib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.types.Coordinate;

/**
 * Contains a set of coordinates comprising a selection area.
 */
public class Selection extends SelectionCore {

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
	public List<Pixel> getWithin(final Canvas canvas,
								 final Pixel value,
								 final Integer delta) throws Exception {
		List<Pixel> pixels = new ArrayList<Pixel>();
		for (Coordinate coordinate : getCoordinates()) {
			if (canvas.isValid(coordinate) && 
				canvas.get(coordinate).getDelta(value) < delta) {
				pixels.add(canvas.get(coordinate));
			}
		}
		return pixels;
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
		Pixel average = getAverage(canvas);
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
		for (int i = center.getX() - radius; i < center.getX() + radius; i++) {
			for (int j = center.getY() - radius; j < center.getY() + radius; j++) {
				Coordinate point = new Coordinate(i, j);
				if (point.getDistanceSquared(center) <= radius) {
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
				Pixel pixel = canvas.get(coordinate);
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
	
}


