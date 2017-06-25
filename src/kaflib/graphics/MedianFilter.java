package kaflib.graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaflib.graphics.ImageTraverser.Order;
import kaflib.types.Percent;
import kaflib.utils.CheckUtils;

/**
 * Performs a median filter.
 */
public class MedianFilter extends Filter implements Traversable {

	private final int size;
	private final Percent opacity;
	private BufferedImage output;
	
	/**
	 * Creates the filter.
	 * @param image
	 * @param size
	 * @param opacity
	 * @throws Exception
	 */
	public MedianFilter(final BufferedImage image, 
						final int size,
						final Percent opacity) throws Exception {
		super(image);
		CheckUtils.checkPositive(size, "size");
		this.size = size;
		this.opacity = opacity;
		output = GraphicsUtils.copy(image);
	}

	@Override
	public void visit(final int x, final int y) throws Exception {
		List<Integer> red = new ArrayList<Integer>();
		List<Integer> green = new ArrayList<Integer>();
		List<Integer> blue = new ArrayList<Integer>();
		
		int startX = Math.max(x - size, 0);
		int startY = Math.max(y - size, 0);
		int endX = Math.min(x + size, getInput().getWidth());
		int endY = Math.min(y + size, getInput().getHeight());

		if (startX >= endX || startY >= endY) {
			throw new Exception("( " + x + ", " + y + "): (" + startX + " - " + endX + ", " + startY + " - " + endY + ").");
		}
		
		for (int i = startY; i < endY; i++) {
			for (int j = startX; j < endX; j++) {
				int rgb = getInput().getRGB(j, i);
				red.add(GraphicsUtils.getRed(rgb));
				green.add(GraphicsUtils.getGreen(rgb));
				blue.add(GraphicsUtils.getBlue(rgb));
			}
		}
		
		Collections.sort(red);
		Collections.sort(green);
		Collections.sort(blue);
				
		if (opacity.get() == 100) {
			output.setRGB(x, y, GraphicsUtils.getRGB(red.get(red.size() / 2), 
														  green.get(green.size() / 2), 
														  blue.get(blue.size() / 2)));
		}
		else if (opacity.get() == 0) {
			// Nothing.
		}
		else {
			output.setRGB(x, y, GraphicsUtils.blendRGB(red.get(red.size() / 2), 
													  green.get(green.size() / 2), 
													  blue.get(blue.size() / 2),
													  opacity,
													  getInput().getRGB(x, y)));
										
		}
	}

	@Override
	public BufferedImage apply() throws Exception {
		ImageTraverser traverser = new ImageTraverser(this, getInput(), Order.LINEARLY);
		traverser.start();
		traverser.blockUntilDone(-1);
		return output;
	}

}
