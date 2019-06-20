package kaflib.graphics.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaflib.types.Byte;
import kaflib.graphics.Canvas;
import kaflib.graphics.CanvasTransform;
import kaflib.graphics.Opacity;
import kaflib.graphics.RGBPixel;
import kaflib.types.Direction;
import kaflib.types.Pair;
import kaflib.utils.CheckUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.RandomUtils;

public class DabTransform implements CanvasTransform {
	private final double scaling;
	private static final Byte LIGHTNESS_ADJUST = new Byte(0x55);

	
	public DabTransform() throws Exception {
		this(1.0);
	}
	
	public DabTransform(final double scaling) throws Exception {
		this.scaling = scaling;
	}
	
	@Override
	public void apply(RGBPixel[][] pixels) throws Exception {
		// Determine a baseline radius that scales with the max image 
		// dimensions, then apply scaling factor parameter.
		int radius = (int)(((double) Math.max(pixels.length, pixels[0].length) * scaling) / 60);
		int step_min = radius / 2;
		int step_max = radius * 2;
		
		RGBPixel result[][] = new RGBPixel[pixels.length][pixels[0].length];

		for (int i = 0; i < pixels.length; i += RandomUtils.randomInt(step_min, step_max)) {
			for (int j = 0; j < pixels[0].length; j += RandomUtils.randomInt(step_min, step_max)) {
				int start_x = Math.max(0, i - radius);
				int end_x = Math.min(pixels.length, i + radius);
				int start_y = Math.max(0, j - radius);
				int end_y = Math.min(pixels[0].length, j + radius);
				for (int x = start_x; x < end_x; x++) {
					for (int y = start_y; y < end_y; y++) {
						int distance = MathUtils.getDistance(new Pair<Integer, Integer>(i - x, j - y));
						RGBPixel value = null;
						if (distance > radius) {
						}
						else if (distance > radius - 2 && RandomUtils.randomBoolean()) {
						}
						else if (distance > radius - 2) {
							if (x > i && y > j) {
								value = new RGBPixel(pixels[x][y]);
								value.lighten(LIGHTNESS_ADJUST);
							}
							else {
								value = new RGBPixel(pixels[x][y]);
								value.darken(LIGHTNESS_ADJUST);
							}
						}
						else {
							value = new RGBPixel(pixels[x][y]);
						}
						if (value != null) {
							result[x][y] = value;
						}
					}
				}
			}
		}		
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				pixels[i][j] = result[i][j];
			}
		}
	}
	
}
