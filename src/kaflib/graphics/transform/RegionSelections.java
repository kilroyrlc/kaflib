package kaflib.graphics.transform;

import kaflib.graphics.Canvas;
import kaflib.graphics.ImageTraverser;
import kaflib.graphics.Selection;
import kaflib.graphics.SelectionSet;
import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;

public class RegionSelections extends SelectionSet {
	public static final int DELTA_VERY_LOW = 20;
	public static final int DELTA_LOW = 45;
	public static final int DELTA_MED = 80;
	public static final int DELTA_HIGH = 120;
	public static final int DELTA_VERY_HIGH = 150;
	
	private final int min_size;
	private final int max_size;
	private final int round_every;
	private final int delta_threshold;
	
	public RegionSelections(final Canvas sourceCanvas,
							final int minSize,
							final int maxSize,
							final int roundEvery,
							final int deltaThreshold) throws Exception {
		super(sourceCanvas, ImageTraverser.Order.RANDOMLY);
		
		CheckUtils.checkNonNegative(maxSize - minSize, "max/min size");
		
		min_size = minSize;
		max_size = maxSize;
		round_every = roundEvery;
		delta_threshold = deltaThreshold;
	}

	@Override
	public Selection createSelection(final Canvas canvas,
									 final Coordinate startingPoint) throws Exception {
		Selection selection = new Selection(startingPoint);
		int steps;
		if (min_size == max_size) {
			steps = min_size;
		}
		else {
			steps = RandomUtils.randomInt(min_size, max_size);
		}
		
		for (int i = 0; i < steps; i++) {
			Coordinate coordinate;
			if (i > 0 && round_every > 0 && i % round_every == 0) { 
				coordinate = selection.getRoundestNeighbor(canvas, delta_threshold);
			}
			else {
				coordinate = selection.getClosestRGBNeighbor(canvas, delta_threshold);
			}
			if (coordinate == null) {
				break;
			}
			else {
				selection.add(coordinate);
			}			
		}
		return selection;
	}

}
