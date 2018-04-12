package kaflib.graphics;

import java.util.ArrayList;
import java.util.List;

import kaflib.types.Coordinate;
import kaflib.types.DistributedProcessor;
import kaflib.types.DistributedTask;
import kaflib.types.Pair;

/**
 * Defines an operation to modify a Canvas raster type where each pixel 
 * operation is independent of another.
 */
public abstract class IndependentTransform extends Transform implements DistributedTask<Pair<Integer, Integer>> {
	public static final int DEFAULT_THREADS = 4;

	
	private final DistributedProcessor<Pair<Integer, Integer>> processor;
	private final Canvas input;
	

	public IndependentTransform(final Canvas input) throws Exception {
		this(input, DEFAULT_THREADS);
	}
	
	public IndependentTransform(final Canvas input, final int threads) throws Exception {
		super(new Canvas(input.getWidth(), input.getHeight()));
		
		this.input = input;

		List<Pair<Integer, Integer>> bounds = new ArrayList<Pair<Integer, Integer>>();
		int step = (input.getWidth() / threads) + 1;
		int start = 0;
		for (int i = 0; i < threads; i++) {
			bounds.add(new Pair<Integer, Integer>(start, Math.min(start + step, input.getWidth())));
			start += step;
		}
		
		processor = new DistributedProcessor<Pair<Integer, Integer>>(this, bounds);
	}
	
	protected Canvas getInput() {
		return input;
	}
	
	public synchronized void start() throws Exception {
		super.start();
		processor.start();
	}
	
	public Status waitUntilFinished(final Long timeoutMS) throws Exception {
		processor.waitUntilFinished(timeoutMS);
		if (status == Status.PROCESSING) {
			status = Status.SUCCESS;
		}
		return status;
	}	

	
	public void process(Pair<Integer, Integer> xBounds) {
		try {
			for (int i = xBounds.getFirst(); i < xBounds.getSecond(); i++) {
				for (int j = 0; j < input.getHeight(); j++) {
					visit(new Coordinate(i, j));
				}
			}
		}
		catch (Exception e) {
			status = Status.FAILURE;
			messages.add(e.getMessage());
			e.printStackTrace();
		}
	}
	
	protected abstract void visit(final Coordinate coordinate) throws Exception;
	
}
