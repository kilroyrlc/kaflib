package kaflib.graphics;

import java.util.HashSet;
import java.util.Set;

import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.Percent;
import kaflib.utils.RandomUtils;

/**
 * Defines a boolean mask for a canvas.
 */
public class CanvasMask {

	private final Set<Coordinate> covered;
	private final Set<Coordinate> uncovered;

	public CanvasMask(final Canvas canvas) {
		covered = new HashSet<Coordinate>();
		uncovered = new HashSet<Coordinate>();
		for (int i = 0; i < canvas.getWidth(); i++) {
			for (int j = 0; j < canvas.getHeight(); j++) {
				uncovered.add(new Coordinate(i, j));
			}
		}
	}
	
	/**
	 * Masks the given box, ignores values out of range.
	 * @param box
	 * @return
	 * @throws Exception
	 */
	public void add(final Box box) throws Exception {
		uncovered.removeAll(box.getCoordinates());
		covered.addAll(box.getCoordinates());
	}
	
	/**
	 * Returns the percent overlap this box has to the mask.
	 * @param box
	 * @return
	 * @throws Exception
	 */
	public Percent overlap(final Box box) throws Exception {
		int count = 0;
		Set<Coordinate> coordinates = box.getCoordinates();
		for (Coordinate coordinate : coordinates) {
			if (covered.contains(coordinate) && !uncovered.contains(coordinate)) {
				count++;
			}
			else if (!covered.contains(coordinate) && uncovered.contains(coordinate)) {
				
			}
			else {
				throw new Exception("In both covered and uncovered: " + coordinate + ".");
			}
		}
		return new Percent(count, 0, coordinates.size());
	}
	
	public Coordinate getRandomUncovered() throws Exception {
		if (uncovered.size() == 0) {
			return null;
		}
		
		return RandomUtils.getRandom(uncovered);
	}
	
	public Percent percentUncovered() throws Exception {
		return new Percent(uncovered.size(), 0, uncovered.size() + covered.size());
	}
	
	public boolean covered() {
		return uncovered.size() == 0;
	}
}
