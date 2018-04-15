package kaflib.graphics;

import java.util.ArrayList;
import java.util.List;

import kaflib.types.Coordinate;

/**
 * Defines a parent type to iterate over a canvas and return a list of
 * selections that covers the canvas.  Child classes define how the selections
 * are made.
 */
public abstract class SelectionSet implements Traversable {

	public static final ImageTraverser.Order DEFAULT_ORDER = ImageTraverser.Order.RANDOMLY;

	private final Canvas canvas;
	private final ImageTraverser traverser;
	private final List<Selection> selections;

	public SelectionSet(final Canvas sourceCanvas) throws Exception {
		this(sourceCanvas, DEFAULT_ORDER);
	}

	public SelectionSet(final Canvas sourceCanvas, final ImageTraverser.Order order) throws Exception {
		canvas = new Canvas(sourceCanvas);
		selections = new ArrayList<Selection>();
		traverser = new ImageTraverser(this, canvas.getWidth(), canvas.getHeight(), order);
	}
	
	public synchronized void start() throws Exception {
		if (traverser.isAlive() || traverser.isDone()) {
			return;
		}
		traverser.start();
	}
	
	public List<Selection> getSelections() throws Exception {
		if (!traverser.isDone()) {
			throw new Exception("Traverser not done.");
		}
		return selections;
	}
	
	public void blockUntilDone(final Long timeout) throws Exception {
		traverser.blockUntilDone(timeout);
	}
	
	@Override
	public void visit(int x, int y) throws Exception {
		// Already part of a selection.
		if (canvas.isNull(x, y)) {
			return;
		}
		
		Selection selection = createSelection(canvas, new Coordinate(x, y));
		selections.add(selection);
		
		// Set processed pixels to null.
		canvas.set(selection.getCoordinates(), null);
	}

	public abstract Selection createSelection(final Canvas canvas,
											  final Coordinate startingPoint) throws Exception;

}
