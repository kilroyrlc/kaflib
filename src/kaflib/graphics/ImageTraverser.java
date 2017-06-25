package kaflib.graphics;

import java.awt.image.BufferedImage;

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
	
	private final Traversable client;
	private final BufferedImage image;
	private final Order order;
	private Matrix<Boolean> visited;
	
	public ImageTraverser(final Traversable client,
						  final BufferedImage image, 
			              final Order order) throws Exception {
		this.client = client;
		this.image = image;
		this.order = order;
		
		visited = new Matrix<Boolean>();
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
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
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				if (!visited.get(i, j)) {
					client.visit(j, i);
					visited.set(i, j, true);
				}
			}
		}
	}
	
	private void traverseRandomly() throws Exception {
		RandomStack<Coordinate> stack = new RandomStack<Coordinate>();
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
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
	protected void visited(final int x, final int y) throws Exception {
		visited.set(y, x, true);
	}
	
}
