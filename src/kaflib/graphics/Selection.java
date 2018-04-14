package kaflib.graphics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.types.Coordinate;
import kaflib.utils.TypeUtils;

/**
 * Contains a set of coordinates comprising a selection area.
 */
public class Selection {

	private final Set<Coordinate> coordinates;
	
	public Selection() {
		coordinates = new HashSet<Coordinate>();
	}

	public Selection(final Coordinate coordinate) {
		this();
		coordinates.add(coordinate);
	}
	
	public Selection(final Collection<Coordinate> coordinates) {
		this();
		this.coordinates.addAll(coordinates);
	}
	
	public void add(final Coordinate coordinate) {
		coordinates.add(coordinate);
	}
	
	public void add(final Collection<Coordinate> coordinates) {
		for (Coordinate coordinate : coordinates) {
			add(coordinate);
		}
	}
	
	public Set<Coordinate> getCoordinates() {
		return coordinates;
	}
	
	public int getAverageDelta(final Canvas canvas) throws Exception {
		return canvas.getAverageDelta(coordinates);
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
		for (Coordinate coordinate : coordinates) {
			if (canvas.isValid(coordinate) && 
				canvas.get(coordinate).getDelta(value) < delta) {
				pixels.add(canvas.get(coordinate));
			}
		}
		return pixels;
	}
	
	public Pixel getAverage(final Canvas canvas) throws Exception {
		return canvas.getAverage(coordinates);
	}
	
	public String toString() {
		return new String("Selection of " + coordinates.size() + " pixels.");
	}
	
	public int opaquePixels(final Canvas canvas) throws Exception {
		int count = 0;
		for (Coordinate coordinate : coordinates) {
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
	public void addAllConnected(final Canvas canvas,
								final Opacity threshold) throws Exception {
		Set<Coordinate> processed = new HashSet<Coordinate>();
		Set<Coordinate> process = new HashSet<Coordinate>();
		process.addAll(coordinates);
		
		while (process.size() > 0) {
			Coordinate c = TypeUtils.getItem(process);
			processed.add(c);
			process.remove(c);
			
			for (Coordinate d : c.getNeighbors()) {
				if (process.contains(d) || processed.contains(d) ||
					!canvas.isValid(d)) {
					continue;
				}
				if (canvas.get(d).getOpacity().compareTo(threshold) >= 0) {
					add(d);
				}
				processed.add(d);
			}
		}
	}
	
	public Selection getBorder() throws Exception {
		Set<Coordinate> border = new HashSet<Coordinate>();
		for (Coordinate coordinate : coordinates) {
			for (Coordinate n : coordinate.getNeighbors()) {
				if (!coordinates.contains(n)) {
					border.add(coordinate);
					break;
				}
			}
		}
		return new Selection(border);
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
				if (point.getDistance(center) <= radius) {
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
				selection.addAllConnected(canvas, threshold);
				list.add(selection);
				traversed.addAll(selection.getCoordinates());
			}
		}
		return list;
	}
	
}


