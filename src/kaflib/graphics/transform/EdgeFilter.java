package kaflib.graphics.transform;


import kaflib.graphics.Canvas;
import kaflib.graphics.RGBPixel;
import kaflib.graphics.Selection;
import kaflib.graphics.IndependentTransform;
import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;

/**
 * Defines a star (square + diamond)-shaped transform to trace edges and
 * optionally leave the rest intact or remove it.
 */
public class EdgeFilter extends IndependentTransform {
	public static final int DELTA_VERY_LOW = 20;
	public static final int DELTA_LOW = 45;
	public static final int DELTA_MED = 80;
	public static final int DELTA_HIGH = 120;
	public static final int DELTA_VERY_HIGH = 150;
	
	private final int size;
	private final int delta;
	private final boolean edges_only;
	private final RGBPixel edge_color;
	
	public EdgeFilter(final Canvas input,
					  final RGBPixel color,
					  final int size,
					  final int delta,
					  final boolean edgesOnly) throws Exception {
		super(input);
		
		CheckUtils.checkPositive(size, "size");
		this.size = size;
		this.delta = delta;
		this.edge_color = color;
		this.edges_only = edgesOnly;
	}

	@Override
	protected void visit(Coordinate coordinate) throws Exception {
		Selection selection = Selection.getStar(coordinate, size);
		int selection_delta = selection.getAverageDelta(getInput());
		if (selection_delta >= delta) {
			getOutput().set(coordinate, edge_color);
		}
		else {
			if (!edges_only) {
				getOutput().set(coordinate, getInput().get(coordinate));
			}
		}
	}

}
