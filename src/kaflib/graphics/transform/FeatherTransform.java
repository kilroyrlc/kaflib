package kaflib.graphics.transform;

import kaflib.graphics.CanvasTransform;
import kaflib.graphics.CanvasUtils;
import kaflib.graphics.Opacity;
import kaflib.graphics.RGBPixel;

/**
 * Feathers pixel opacity based on the adjacent values.  NSEW pixels are
 * counted at > 1/2 opaque, diagonals must be greather than 3/4 opaque.
 */
public class FeatherTransform implements CanvasTransform {

	@Override
	public void apply(RGBPixel[][] pixels) throws Exception {
		RGBPixel previous[][] = CanvasUtils.copy(pixels);
		
		for (int i = 1; i < previous.length - 1; i++) {
			for (int j = 1; j < previous[0].length - 1; j++) {
				int opaque = 0;
				if (previous[i-1][j].getOpacity().greaterThanHalf()) {
					opaque++;
				}
				if (previous[i+1][j].getOpacity().greaterThanHalf()) {
					opaque++;
				}
				if (previous[i][j-1].getOpacity().greaterThanHalf()) {
					opaque++;
				}
				if (previous[i][j+1].getOpacity().greaterThanHalf()) {
					opaque++;
				}
				if (previous[i][j].getOpacity().greaterThanHalf()) {
					opaque++;
				}
				if (previous[i-1][j-1].getOpacity().greaterThanThreeQuarter()) {
					opaque++;
				}
				if (previous[i-1][j+1].getOpacity().greaterThanThreeQuarter()) {
					opaque++;
				}
				if (previous[i+1][j-1].getOpacity().greaterThanThreeQuarter()) {
					opaque++;
				}
				if (previous[i+1][j-1].getOpacity().greaterThanThreeQuarter()) {
					opaque++;
				}
				
				if (opaque < 3) {
					pixels[i][j].setOpacity(Opacity.TRANSPARENT);
				}
				else if (opaque < 4) {
					pixels[i][j].setOpacity(Opacity.QUARTER);
				}
				else if (opaque < 5) {
					pixels[i][j].setOpacity(Opacity.HALF);
				}
				else if (opaque < 7) {
					pixels[i][j].setOpacity(Opacity.THREE_QUARTER);
				}
				else {
					pixels[i][j].setOpacity(Opacity.OPAQUE);
				}
			}
		}		
	}

}
