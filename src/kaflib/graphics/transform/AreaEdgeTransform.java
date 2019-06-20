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

public class AreaEdgeTransform implements CanvasTransform {
	public static final int DELTA_VERY_LOW = 20;
	public static final int DELTA_LOW = 45;
	public static final int DELTA_MED = 80;
	public static final int DELTA_HIGH = 120;
	public static final int DELTA_VERY_HIGH = 150;
	
	private final int distance;
	private final int delta;
	
	public AreaEdgeTransform(final int distance,
					     final int delta) throws Exception {

		CheckUtils.checkPositive(delta, "delta");
		CheckUtils.checkPositive(distance, "distance");
		this.distance = distance;
		this.delta = delta;
	};
	
	@Override
	public void apply(RGBPixel[][] pixels) throws Exception {
		RGBPixel edges[][] = new RGBPixel[pixels.length][pixels[0].length];
		List<Integer> list = new ArrayList<Integer>(distance * distance);
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				int start_x = Math.max(0, i - distance);
				int end_x = Math.min(pixels.length, i + distance);
				int start_y = Math.max(0, j - distance);
				int end_y = Math.min(pixels[0].length, j + distance);
				for (int x = start_x; x < end_x; x++) {
					for (int y = start_y; y < end_y; y++) {
						if (Math.abs(i - x) + Math.abs(j - y) < distance) { 
							list.add(pixels[x][y].getDelta(pixels[i][j]));
						}
					}
				}
				if (MathUtils.average(list) > delta) {
					edges[i][j] = RGBPixel.OPAQUE_BLACK;
				}
				else {
					edges[i][j] = RGBPixel.TRANSPARENT_BLACK;
				}
				
				list.clear();
			}
		}		
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				pixels[i][j] = edges[i][j];
			}
		}
	}
	
}
