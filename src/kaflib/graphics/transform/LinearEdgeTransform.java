package kaflib.graphics.transform;

import java.util.ArrayList;
import java.util.List;

import kaflib.graphics.Canvas;
import kaflib.graphics.CanvasTransform;
import kaflib.graphics.Opacity;
import kaflib.graphics.RGBPixel;
import kaflib.types.Direction;
import kaflib.utils.CheckUtils;
import kaflib.utils.MathUtils;

public class LinearEdgeTransform implements CanvasTransform {
	public static final int DELTA_VERY_LOW = 20;
	public static final int DELTA_LOW = 45;
	public static final int DELTA_MED = 80;
	public static final int DELTA_HIGH = 120;
	public static final int DELTA_VERY_HIGH = 150;
	
	private final int distance;
	private final int delta;
	
	public LinearEdgeTransform(final int distance,
					     final int delta) throws Exception {

		CheckUtils.checkPositive(delta, "delta");
		CheckUtils.checkPositive(distance, "distance");
		this.distance = distance;
		this.delta = delta;
	};
	
	@Override
	public void apply(RGBPixel[][] pixels) throws Exception {
		RGBPixel edges[][] = new RGBPixel[pixels.length][pixels[0].length];
		
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				if (edges[i][j] == null) {
					if (look(Direction.NORTH, i, j, pixels)) {
						for (int k = j; k > Math.max(0, j - distance); k--) {
							edges[i][k] = RGBPixel.OPAQUE_BLACK;
						}
						continue;
					}
					if (look(Direction.SOUTH, i, j, pixels)) {
						for (int k = j; k < Math.min(pixels[0].length, j + distance); k++) {
							edges[i][k] = RGBPixel.OPAQUE_BLACK;
						}
						continue;
					}		
					if (look(Direction.EAST, i, j, pixels)) {
						for (int k = i; k > Math.max(0, i - distance); k--) {
							edges[k][j] = RGBPixel.OPAQUE_BLACK;
						}
						continue;
					}
					if (look(Direction.WEST, i, j, pixels)) {
						for (int k = i; k < Math.min(pixels.length, i + distance); k++) {
							edges[k][j] = RGBPixel.OPAQUE_BLACK;
						}
						continue;
					}	
					edges[i][j] = RGBPixel.TRANSPARENT_BLACK;
				}
			}
		}		
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				pixels[i][j] = edges[i][j];
			}
		}
	}
	
	private boolean look(final Direction direction, final int x, final int y, final RGBPixel canvas[][]) throws Exception {
		List<Integer> deltas = new ArrayList<Integer>();
		
		if (direction == Direction.NORTH) {
			for (int i = y; i > Math.max(1, y - distance); i--) {
				deltas.add(canvas[x][i].getDelta(canvas[x][i - 1]));
			}
		}
		else if (direction == Direction.SOUTH) {
			for (int i = y; i < Math.min(canvas[0].length - 1, y + distance); i++) {
				deltas.add(canvas[x][i].getDelta(canvas[x][i + 1]));
			}		
		}
		else if (direction == Direction.EAST) {
			for (int i = x; i > Math.max(1, x - distance); i--) {
				deltas.add(canvas[i][y].getDelta(canvas[i - 1][y]));
			}
		}
		else if (direction == Direction.WEST) {
			for (int i = x; i < Math.min(canvas.length - 1, x + distance); i++) {
				deltas.add(canvas[i][y].getDelta(canvas[i + 1][y]));
			}		
		}
		else {
			throw new Exception("Unsupported direction: " + direction + ".");
		}
		
		
		if (deltas.size() == 0) {
			return false;
		}
		else if (MathUtils.average(deltas) > delta) {
			return true;
		}
		else {
			return false;
		}
	}

}
