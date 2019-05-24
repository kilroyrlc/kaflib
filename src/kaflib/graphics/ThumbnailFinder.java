package kaflib.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.IntegerHistogram;
import kaflib.types.Percent;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.TypeUtils;

public class ThumbnailFinder extends Worker {

	private final Canvas canvas;
	private final int radius;
	private final Area[][] areas;
	
	public ThumbnailFinder(final Canvas canvas, final int xSections, final int ySections) throws Exception {
		super();
		this.canvas = canvas;
		int x_step = canvas.getWidth() / (xSections + 1);
		int y_step = canvas.getHeight() / (ySections + 1);
		radius = Math.min(x_step, y_step) / 2;
		areas = new Area[xSections][ySections];
		
		for (int i = 0; i < xSections; i++) {
			for (int j = 0; j < ySections; j++) {
				Coordinate coordinate = new Coordinate(x_step + (i * x_step), y_step + (j * y_step));
				areas[i][j] = new Area(coordinate, radius);
			}
		}
		
	}

	/**
	 * Returns a coordinate box with the maximized relative interest given the
	 * specified min/max number of points.
	 * @return
	 */
	public Box getMaxSelection(final int minX, final int maxX, final int minY, final int maxY) throws Exception {
		if (!isDone()) {
			throw new Exception("Area computation not yet complete.");
		}
		
		int best = Integer.MIN_VALUE;
		Box best_box = null;
		
		int x_max = areas.length;
		int y_max = areas[0].length;
		
		// Iterate over all starting top/lefts.
		for (int i = 0; i < x_max; i++) {
			for (int j = 0; j < y_max; j++) {
				List<Integer> percentages = new ArrayList<Integer>();
				
				for (int k = i; k < Math.min(i + maxX + 1, x_max); k++) {
					for (int l = j; l < Math.min(j + maxY + 1, y_max); l++) {
						percentages.add(areas[k][l].getNormalizedInterest().get());
						if (k - i + 1 >= minX && l - j + 1 >= minY) {
							int average = MathUtils.average(percentages);
							if (average > best) {
								best = average;
								best_box = new Box(areas[i][j].getTopLeft(), areas[k][l].getBottomRight());
							}
						}
					}
				}
			}
		}
		return best_box;
	}
	
	
	public Canvas drawInterest() throws Exception {
		BufferedImage overlay = canvas.toBufferedImage();
		for (int i = 0; i < areas.length; i++) {
			for (int j = 0; j < areas[0].length; j++) {		
				GraphicsUtils.drawCircle(overlay, areas[i][j].getCoordinate(), radius, Color.MAGENTA);
				GraphicsUtils.drawString(overlay,
									 areas[i][j].getNormalizedInterest().toString(),
									 Color.BLACK, 
									 Color.YELLOW, 
									 18, 
									 areas[i][j].getCoordinate());
			
			}
		}
		return new Canvas(overlay);
	}
	
	@Override
	protected void process() throws Exception {
		
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < areas.length; i++) {
			for (int j = 0; j < areas[i].length; j++) {
				areas[i][j].setInterest(canvas);
				min = Math.min(min, areas[i][j].getAbsoluteInterest());
				max = Math.max(max, areas[i][j].getAbsoluteInterest());
			}
		}

		for (int i = 0; i < areas.length; i++) {
			for (int j = 0; j < areas[i].length; j++) {
				areas[i][j].normalizeInterest(min, max);
			}
		}
	}

	
}

class Area {
	private final Coordinate coordinate;
	private final int radius;
	private Integer interest_absolute;
	private Percent interest_relative;
	
	public Area(final Coordinate coordinate, final int radius) {
		this.coordinate = coordinate;
		this.radius = radius;
		interest_absolute = null;
		interest_relative = null;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public int getAbsoluteInterest() throws Exception {
		CheckUtils.check(interest_absolute, "absolute interest");
		return interest_absolute;
	}
	
	public Percent getNormalizedInterest() throws Exception {
		CheckUtils.check(interest_relative, "normalized interest");
		return interest_relative;
	}
	
	public Coordinate getTopLeft() {
		return new Coordinate(coordinate.getX() - radius, coordinate.getY() - radius);
	}

	public Coordinate getBottomRight() {
		return new Coordinate(coordinate.getX() + radius, coordinate.getY() + radius);
	}

	
	public void setInterest(final Canvas canvas) throws Exception {
		int sharpness;
		int contrast;
		int midtone;
		Selection selection = Selection.getCircle(coordinate, radius);
		IntegerHistogram histogram = selection.getLuminanceHistogram(canvas);

		List<Integer> ranked = histogram.getRanked();
		if (ranked.size() > 1) {
			int rankmax = TypeUtils.max(ranked.subList(0, Math.min(ranked.size(), 4)));
			int rankmin = TypeUtils.min(ranked.subList(0, Math.min(ranked.size(), 4)));
			
			midtone = 0x7f - Math.abs(rankmax - 0x7f);  // Distance from center tone, so 0-127.
			contrast = Math.abs(rankmax - rankmin); // Distance from peaks.
		}
		else {
			midtone = 0x7f - Math.abs(histogram.getMostFrequent() - 0x7f);
			contrast = 0;
		}
		sharpness = (selection.getMaxDelta(canvas, 6, true) + selection.getMaxDelta(canvas, 6, false)) / 2;
		sharpness /= 3;
		
		interest_absolute = sharpness + contrast + midtone;
	}
	
	public void normalizeInterest(final int min, final int max) throws Exception {
		CheckUtils.check(interest_absolute, "absolute interest");
		interest_relative = Percent.getRelative(interest_absolute, min, max);
	}
	
}