package kaflib.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;

import kaflib.graphics.GraphicsUtils.Rotation;
import kaflib.types.Coordinate;
import kaflib.types.Percent;
import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.TypeUtils;

public class CanvasUtils {

	public static Canvas getRandomRotateMirror(final Canvas canvas) throws Exception {
		int random = RandomUtils.randomInt(0, 3);
		Canvas changed;
		if (random == 0) {
			changed = new Canvas(canvas);
		}
		else if (random == 1) {
			changed = CanvasUtils.rotate(canvas, GraphicsUtils.Rotation.CLOCKWISE);
		}
		else if (random == 2) {
			changed = CanvasUtils.rotate(canvas, GraphicsUtils.Rotation.ONE_EIGHTY);
		}
		else {
			changed = CanvasUtils.rotate(canvas, GraphicsUtils.Rotation.COUNTERCLOCKWISE);
		}
		random = RandomUtils.randomInt(0, 2);
		if (random == 0) {
		}
		else if (random == 1) {
			changed = CanvasUtils.mirror(changed, false);
		}
		else {
			changed = CanvasUtils.mirror(changed, true);
		}
		return changed;
	}

	public static Canvas rotate(final Canvas image, 
								final GraphicsUtils.Rotation rotation) throws Exception {
		if (rotation == Rotation.CLOCKWISE) {
			Canvas output = new Canvas(image.getHeight(), image.getWidth());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.set(output.getWidth() - j - 1, i, image.get(i, j));
				}
			}
			return output;
		}
		else if (rotation == Rotation.COUNTERCLOCKWISE) {
			Canvas output = new Canvas(image.getHeight(), image.getWidth());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.set(j, output.getHeight() - i - 1, image.get(i, j));
				}
			}
			return output;			
		}
		else if (rotation == Rotation.ONE_EIGHTY) {
			Canvas output = new Canvas(image.getWidth(), image.getHeight());
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					output.set(output.getWidth() - i - 1, output.getHeight() - j - 1, image.get(i, j));
				}
			}
			return output;
		}
		else {
			throw new Exception("Unsupported rotation: " + rotation + ".");
		}
	}

	public static Canvas mirror(final Canvas image, 
								final boolean horizontal) throws Exception {
		int width = image.getWidth();
		int height = image.getHeight();
	
		Canvas output = new Canvas(image.getHeight(), image.getWidth());
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (horizontal) {
					output.set(width - i - 1, j, image.get(i, j));
				}
				else {
					output.set(i, height - j - 1, image.get(i, j));
				}
			}
		}
		return output;
	}

	/**
	 * Joins two canvases together, left-to-right or top-to-bottom where the 
	 * joined dimensions must match.  If margin > 0, treats that number of 
	 * pixels as overlap where a heuristic decides how to blend them.
	 * @param topLeft
	 * @param bottomRight
	 * @param margin
	 * @param horizontal
	 * @return
	 * @throws Exception
	 */
	public static Canvas join(final Canvas topLeft, 
							  final Canvas bottomRight,
							  final int margin,
							  final boolean horizontal) throws Exception {
		Canvas canvas;
		if (horizontal) {
			CheckUtils.checkEquals(topLeft.getHeight(), bottomRight.getHeight(), "Height");
			canvas = new Canvas(topLeft.getWidth() + bottomRight.getWidth() - margin,
							    topLeft.getHeight());
			// Copy non-margin left side.
			for (int i = 0; i < topLeft.getWidth() - margin; i++) {
				canvas.setColumn(topLeft.getColumn(i), i);
			}
			// Copy non-margin right side.
			for (int i = margin; i < bottomRight.getWidth(); i++) {
				canvas.setColumn(bottomRight.getColumn(i), topLeft.getWidth() + i - margin);
			}
	
			if (margin > 0) {
				// Blend each row, left to right.
				double increment = 1.0 / (double)(margin - 1);
				for (int i = 0; i < topLeft.getHeight(); i++) {
					double percent_left = 1;
					for (int j = 0; j < margin; j++) {
						RGBPixel blend = new RGBPixel(topLeft.get(topLeft.getWidth() - margin + j, i),
													  bottomRight.get(j, i),
													  new Percent(percent_left),
													  new Percent(90));
						canvas.set(topLeft.getWidth() - margin + j, i, blend);
						percent_left -= increment;
					}
				}
			}
		}
		else {
			CheckUtils.checkEquals(topLeft.getWidth(), bottomRight.getWidth(), "Width");
			canvas = new Canvas(topLeft.getWidth(),
							    topLeft.getHeight() + bottomRight.getHeight() - margin);
			// Copy non-margin top side.
			for (int i = 0; i < topLeft.getWidth(); i++) {
				// Copy top.
				TypeUtils.copy(topLeft.getColumn(i), 0, canvas.getColumn(i), 0, topLeft.getColumn(i).length - margin);
				
				// Copy bottom.
				TypeUtils.copy(bottomRight.getColumn(i), 
								 margin, 
								 canvas.getColumn(i), 
								 topLeft.getColumn(i).length, 
								 bottomRight.getColumn(i).length - margin);
	
				if (margin > 0) {
					double increment = 1.0 / (double) (margin - 1);
					double percent_top = 1;
					for (int j = 0; j < margin; j++) {
	
						RGBPixel blend = new RGBPixel(topLeft.get(i, topLeft.getHeight() - margin + j),
													  bottomRight.get(i, j),
													  new Percent(percent_top),
													  new Percent(90));
						canvas.set(i, topLeft.getHeight() - margin + j, blend);
						percent_top -= increment;
					}
				}
				
			}
		}
		
		return canvas;
	}

	public static Canvas createMottled(final Coordinate dimensions,
									   final RGBPixel bg, 
									   final RGBPixel fg) throws Exception {
		return CanvasUtils.createMottled(dimensions, bg, fg, 2);
	}

	public static Canvas createMottled(final Coordinate dimensions,
									   final RGBPixel bg, 
									   final RGBPixel fg, 
									   final int smooth) throws Exception {
		Canvas canvas = new Canvas(dimensions.getX(), 
								   dimensions.getY(),
								   bg,
								   fg,
								   new Percent(70));
		
		for (int i = 0; i < smooth; i++) {
			canvas.smooth(2);
		}
		return canvas;
	}

	public static Canvas createMottled(final Coordinate dimensions,
			final int smooth,
			final RGBPixel... colors) throws Exception {
		Canvas canvas = new Canvas(dimensions.getX(), 
				dimensions.getY(),
				colors);

		for (int i = 0; i < smooth; i++) {
			canvas.smooth(2);
		}
		return canvas;
	}

	public static Canvas createMottled(final Coordinate dimensions,
			final int smooth,
			final Color... colors) throws Exception {
		RGBPixel pixels[] = new RGBPixel[colors.length];
		for (int i = 0; i < colors.length; i++) {
			pixels[i] = new RGBPixel(colors[i]);
		}
		Canvas canvas = new Canvas(dimensions.getX(), 
				dimensions.getY(),
				pixels);

		for (int i = 0; i < smooth; i++) {
			canvas.smooth(2);
		}
		return canvas;
	}

	public static BufferedImage getThumbnail(final Canvas canvas, final int width, final int height, final int segments) throws Exception {
		return CanvasUtils.getThumbnailCanvas(canvas, width, height, segments).toBufferedImage();
	}

	public static Canvas getThumbnailCanvas(final Canvas canvas, final int width, final int height, final int segments) throws Exception {
		ThumbnailFinder thumbnailer = new ThumbnailFinder(canvas, segments);
		thumbnailer.start();
		thumbnailer.blockUntilDone(null);
		return thumbnailer.getMaxSelection(width, height);
	}

	/**
	 * Resizes the canvas so the smaller dimensions match, then center
	 * crops the longer dimension.
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public static Canvas fill(final Canvas source,
							  final int width,
							  final int height) throws Exception {
		if (source.getWidth() == width && source.getHeight() == height) {
			return source;
		}
		
		return new Canvas(GraphicsUtils.fill(source.toBufferedImage(), width, height));
	}

	/**
	 * Scales the canvas until the width or height (if specified) reaches max.
	 * @param canvas
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 * @throws Exception
	 */
	public static Canvas scaleTo(final Canvas canvas,
								  final Integer maxWidth,
								  final Integer maxHeight) throws Exception {
		return new Canvas(GraphicsUtils.getScaled(canvas.toBufferedImage(), maxWidth, maxHeight));
	}

	public static Canvas scaleToAtLeast(final Canvas canvas,
			  final Integer minWidth,
			  final Integer minHeight) throws Exception {
		return new Canvas(GraphicsUtils.scaleTo(canvas.toBufferedImage(), minWidth, minHeight));
	}

	public static Canvas join(final Canvas... canvases) throws Exception {
		int width = 0;
		int height = 0;
		for (Canvas canvas : canvases) {
			width += canvas.getWidth();
			height = Math.max(height, canvas.getHeight());
		}
		Canvas new_canvas = new Canvas(width, height);
		int x_start = 0;
		
		for (Canvas canvas : canvases) {
			new_canvas.set(new Coordinate(x_start, 0), canvas);
			x_start += canvas.getWidth();
		}
		return new_canvas;
	}

	public static Canvas getLuminance(final Canvas canvas) throws Exception {
		Canvas grey = new Canvas(canvas);
		for (int i = 0; i < grey.getWidth(); i++) {
			for (int j = 0; j < grey.getHeight(); j++) {
				grey.set(i, j, new RGBPixel(true,
											grey.get(i, j).getLuminance(),
											grey.get(i, j).getLuminance(),
											grey.get(i, j).getLuminance()));
			}
		}
		return grey;
	}

	public static RGBPixel[][] copy(final RGBPixel source[][]) {
		RGBPixel copy[][] = new RGBPixel[source.length][source[0].length];
		for (int i = 0; i < source.length; i++) {
			for (int j = 0; j < source[0].length; j++) {
				copy[i][j] = new RGBPixel(source[i][j]);
			}
		}
		return copy;
	}
	
}
