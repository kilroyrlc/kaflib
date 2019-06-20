package kaflib.graphics.transform;


import kaflib.graphics.Canvas;
import kaflib.graphics.Filter;
import kaflib.graphics.RGBPixel;
import kaflib.graphics.Selection;
import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;

/**
 * Defines a star (square + diamond)-shaped transform to trace edges and
 * optionally leave the rest intact or remove it.
 */
public class EdgeFilter extends Filter {
	public static final int DELTA_VERY_LOW = 20;
	public static final int DELTA_LOW = 45;
	public static final int DELTA_MED = 80;
	public static final int DELTA_HIGH = 120;
	public static final int DELTA_VERY_HIGH = 150;
	
	private final int size;
	private final int delta;
	private final boolean edges_only;
	private final RGBPixel edge_color;
	
	public EdgeFilter(final RGBPixel color,
					  final int size,
					  final int delta,
					  final boolean edgesOnly) throws Exception {
		
		CheckUtils.checkPositive(size, "size");
		this.size = size;
		this.delta = delta;
		this.edge_color = color;
		this.edges_only = edgesOnly;
	}

	public Canvas apply(final Canvas canvas) throws Exception {
		Canvas output = new Canvas(canvas.getWidth(), canvas.getHeight());
		
		for (int i = 0; i < canvas.getWidth(); i++) {
			for (int j = 0; j < canvas.getHeight(); j++) {
				Selection selection = Selection.getStar(new Coordinate(i, j), size);
				int selection_delta = selection.getAverageDelta(canvas);
				if (selection_delta >= delta) {
					output.set(i, j, edge_color);
				}
				else {
					if (!edges_only) {
						output.set(i, j, canvas.get(i, j));
					}
				}

			}
		}
		return output;
	}

	@Override
	public void applyInPlace(Canvas canvas) throws Exception {
		canvas = apply(canvas);
	}
}
