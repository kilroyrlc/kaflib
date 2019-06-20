package kaflib.graphics.mask;

import java.lang.reflect.Array;

import kaflib.graphics.Canvas;
import kaflib.types.Worker;

/**
 * Defines an grid of values that can be applied to a Canvas in user-defined
 * ways.  Grid type is defined in the subclass, e.g. boolean for a thing that
 * might be applied to specific pixels, integer to modify them, etc.
 * @param <T>
 */
public abstract class Mask<T> {

	protected T mask[][];
	private boolean processed;
	
	@SuppressWarnings("unchecked")
	protected Mask(Class<? extends T> cls, final int width, final int height) {
		mask = (T[][]) Array.newInstance(cls, width, height);
		processed = false;
	}
	
	public synchronized void processSerial(final Canvas canvas) throws Exception {
		if (processed == true) {
			return;
		}
		canvas.getCoordinates().parallelStream()
					.forEach(c -> compute(canvas, c.getX(), c.getY()));
		
		processed = true;
	}
	
	public void processAsync(final Canvas canvas) throws Exception {
		if (processed == true) {
			return;
		}
		Worker worker = new Worker() {
			@Override
			protected void process() throws Exception {
				processSerial(canvas);
			}
		};
		worker.start();
	}
	
	/**
	 * Subclass-defined computation for pixel at x, y.
	 * @param x
	 * @param y
	 * @return
	 */
	protected abstract T compute(final Canvas canvas, final int x, final int y);
	
	/**
	 * Returns a raster representation of the mask.
	 * @return
	 */
	protected abstract Canvas toRaster();
	
}
