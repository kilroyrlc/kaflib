package kaflib.graphics;

import kaflib.types.Coordinate;
import kaflib.types.Matrix;
import kaflib.types.RandomStack;
import kaflib.types.Worker;

/**
 * Defines a class to traverse all pixels of an image.
 */
public class ImageTraverser extends Worker {

	public enum Order {LINEARLY,
		               RANDOMLY
					  };
	
	private final int width;
	private final int height;
	private final Traversable client;
	private final Order order;
	private Matrix<Boolean> visited;
	
	public ImageTraverser(final Traversable client,
						  final int width,
						  final int height,
			              final Order order) throws Exception {
		this.client = client;
		this.width = width;
		this.height = height;
		this.order = order;
		
		visited = new Matrix<Boolean>();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				visited.set(i, j, false);
			}
		}	
	}
	
	
	public void process() throws Exception {
		if (order == Order.LINEARLY) {
			traverseLinearly();
		}
		else if (order == Order.RANDOMLY) {
			traverseRandomly();
		}
		else {
			throw new Exception("Unsupported traversal: " + order + ".");
		}
	}

	private void traverseLinearly() throws Exception {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (!visited.get(i, j)) {
					client.visit(j, i);
					visited.set(i, j, true);
				}
			}
		}
	}
	
	private void traverseRandomly() throws Exception {
		RandomStack<Coordinate> stack = new RandomStack<Coordinate>();
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				stack.push(new Coordinate(j, i));
			}
		}
		
		while (stack.size() > 0) {
			Coordinate coordinate = stack.pop();
			if (!visited.get(coordinate.getY(), coordinate.getX())) {
				client.visit(coordinate.getX(), coordinate.getY());
				visited.set(coordinate.getY(), coordinate.getX(), true);
			}
		}
	}
	
	/**
	 * Called by subclass to indicate it visited a coordinate of its own 
	 * volition.
	 * @param x
	 * @param y
	 * @throws Exception
	 */
	public void visited(final int x, final int y) throws Exception {
		visited.set(y, x, true);
	}
	
}
