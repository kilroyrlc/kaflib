package kaflib.graphics.transform;

import kaflib.graphics.Canvas;
import kaflib.graphics.DependentTransform;
import kaflib.graphics.Pixel;
import kaflib.graphics.Selection;
import kaflib.types.Coordinate;

/**
 * Creates a brush effect by ensuring every opaque pixel has a certain number 
 * of neighbors.
 */
public class BrushTransform extends DependentTransform {

	private final int radius;
	private final Pixel color;
	
	protected BrushTransform(final Canvas input, 
							 final int radius,
							 final Pixel color) throws Exception {
		super(input);
		this.radius = radius;
		this.color = color;
	}

	@Override
	protected void processImage() throws Exception {
		for (int i = 0; i < getOutput().getWidth(); i++) {
			for (int j = 0; j < getOutput().getHeight(); j++) {
				processPixel(new Coordinate(i, j));
			}
		}
	}

	private void processPixel(final Coordinate coordinate) throws Exception {
		Pixel pixel = getOutput().get(coordinate);
		// Ignore transparent.
		if (pixel.isTransparent()) {
			return;
		}
		// Semi-transparent, set to transparent.
		if (!pixel.isOpaque()) {
			getOutput().set(coordinate, new Pixel(Pixel.OPAQUE_BLACK));
		}
		
		Selection selection = Selection.getMostOpaqueStar(coordinate, radius, radius / 2, getOutput());
		getOutput().set(selection.getCoordinates(), color);
		
	}
	
}
