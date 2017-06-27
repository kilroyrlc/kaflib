package kaflib.graphics;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import kaflib.graphics.ImageTraverser.Order;
import kaflib.types.Coordinate;
import kaflib.types.Percent;
import kaflib.utils.CheckUtils;

/**
 * Performs a median filter.
 */
public class RegionFillFilter extends Filter implements Traversable {

	private final int tolerance;
	private final int softness;
	private final Percent opacity;
	private BufferedImage output;
	private final ImageTraverser traverser;
	
	/**
	 * Creates the filter.
	 * @param image
	 * @param size
	 * @param opacity
	 * @throws Exception
	 */
	public RegionFillFilter(final BufferedImage image, 
							final int tolerance,
							final int softness,
							final Percent opacity) throws Exception {
		super(image);
		CheckUtils.checkPositive(tolerance, "tolerance");
		this.tolerance = tolerance;
		CheckUtils.checkPositive(softness, "softness");
		this.softness = softness;
		this.opacity = opacity;

		traverser = new ImageTraverser(this, getInput(), Order.RANDOMLY);
		
		output = GraphicsUtils.copy(image);
	}

	@Override
	public void visit(final int x, final int y) throws Exception {
		int average = getInput().getRGB(x, y);

		Set<Coordinate> color = new HashSet<Coordinate>();
		Set<Coordinate> visited = new HashSet<Coordinate>();
		Set<Coordinate> visit = new HashSet<Coordinate>();
		visit.add(new Coordinate(x, y));
		
		while (visit.size() > 0) {
			Coordinate c = visit.iterator().next();
			visit.remove(c);
			visited.add(c);
			
			if (c.getX() >= 0 && c.getX() < getInput().getWidth() &&
				c.getY() >= 0 && c.getY() < getInput().getHeight() &&
				GraphicsUtils.getDistance(average, getInput().getRGB(c.getX(), c.getY())) <= tolerance) {
				
				traverser.visited(c.getX(), c.getY());
				color.add(c);
				
				Set<Coordinate> neighbors = c.getNeighbors();
				neighbors.removeAll(visited);
				visit.addAll(neighbors);
				average = GraphicsUtils.averageRGB(average, getInput().getRGB(c.getX(), c.getY()));
			}
		}
		
		if (opacity.get() == 100) {
			for (Coordinate c : color) {
				output.setRGB(c.getX(), c.getY(), average);				
			}
		}
		else if (opacity.get() == 0) {
			// Nothing.
		}
		else {
			for (Coordinate c : color) {
				output.setRGB(c.getX(), c.getY(), GraphicsUtils.blendRGB(average,
													  opacity,
													  getInput().getRGB(c.getX(), c.getY())));
			}
		}
	}

	/**
	 * Gets softness.
	 * @return
	 */
	public int getSoftness() {
		return softness;
	}
	
	@Override
	public BufferedImage apply() throws Exception {
		traverser.start();
		traverser.blockUntilDone(-1);
		return output;
	}

}
