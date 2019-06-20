package kaflib.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.IntegerHistogram;
import kaflib.types.Pair;
import kaflib.types.Percent;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.TypeUtils;

public class ThumbnailFinder extends Worker {

	private final Canvas canvas;
	private final int radius;
	private final Area[][] areas;

	public ThumbnailFinder(final Canvas canvas, final int smallSideSections) throws Exception {
		this(canvas, getXY(canvas, smallSideSections).getFirst(), getXY(canvas, smallSideSections).getSecond());
	}
	
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
	 * Returns a subsection of the canvas with minimum width and height that 
	 * maximizes the interest value.
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Canvas getMaxSelection(final int width, final int height) throws Exception {
		if (!isDone()) {
			throw new Exception("Area computation not yet complete.");
		}
		if (width > canvas.getWidth() || height > canvas.getHeight()) {
			Canvas enlarged = canvas.getScaledUp(width, height);
			Box box = new Box(new Coordinate(canvas.getWidth() / 2, canvas.getHeight() / 2), width, height);
			box = Box.slideInbounds(box, enlarged.getBounds());
			return enlarged.get(box);
		}
		
		int best = Integer.MIN_VALUE;
		Box best_box = null;
		
		int x_max = areas.length;
		int y_max = areas[0].length;
		
		// Iterate over all starting top/lefts.
		for (int i = 0; i < x_max; i++) {
			for (int j = 0; j < y_max; j++) {
				List<Integer> percentages = new ArrayList<Integer>();
				
				for (int k = i; k < x_max; k++) {
					for (int l = j; l < y_max; l++) {
						Box box = new Box(areas[i][j].getTopLeft(), areas[k][l].getBottomRight());
						percentages.add(areas[k][l].getNormalizedInterest().get());
						if (box.getWidth() > width && box.getHeight() > height) {
							int average = MathUtils.average(percentages);
							if (average > best) {
								best = average;
								best_box = box;
							}
						}
					}
				}
			}
		}
		if (best_box == null) {
			best_box = new Box(new Coordinate(canvas.getWidth() / 2, canvas.getHeight() / 2), width, height);
			best_box = Box.slideInbounds(best_box, canvas.getBounds());
		}
		
		Canvas thumbnail = canvas.get(best_box);
		if (thumbnail.getWidth() != width || thumbnail.getHeight() != height) {
			return thumbnail.getCropped(width, height);
		}
		else {
			return thumbnail;
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
	
	/**
	 * Overlays the interest values on the canvas.
	 * @return
	 * @throws Exception
	 */
	public Canvas drawInterest() throws Exception {
		if (!isDone()) {
			throw new Exception("Area computation not yet complete.");
		}
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
	
	public List<Pair<Coordinate, Percent>> getRanked() throws Exception {
		if (!isDone()) {
			throw new Exception("Area computation not yet complete.");
		}
		List<Pair<Coordinate, Percent>> coordinates = new ArrayList<Pair<Coordinate, Percent>>();
		for (int i = 0; i < areas.length; i++) {
			for (int j = 0; j < areas[i].length; j++) {
				int index = 0;
				for (index = 0; index < coordinates.size(); index++) {
					int interest = areas[i][j].getNormalizedInterest().get();
					if (interest > coordinates.get(index).getSecond().get()) {
						break;
					}
				}
				coordinates.add(index, 
						new Pair<Coordinate, Percent>(areas[i][j].getCoordinate(), areas[i][j].getNormalizedInterest()));
			}
		}
		return coordinates;
	}

	@Override
	protected void process() throws Exception {
		Set<Area> set = new HashSet<Area>(areas.length * areas[0].length);
		for (int i = 0; i < areas.length; i++) {
			for (int j = 0; j < areas[i].length; j++) {
				set.add(areas[i][j]);
			}
		}
		
		set.parallelStream()
				.forEach(w -> w.setInterest(canvas));
		
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Area area : set) {
			min = Math.min(min, area.getAbsoluteInterest());
			max = Math.max(max, area.getAbsoluteInterest());
		}
		
		for (Area area : set) {
			area.normalizeInterest(min, max);
		}
		
	}
	
	private static Pair<Integer, Integer> getXY(final Canvas canvas, final int smallSideSections) {
		double aspect = canvas.getAspectRatio();
		int x_sections;
		int y_sections;

		if (aspect == 1.0) {
			return new Pair<Integer, Integer>(smallSideSections, smallSideSections);
		}
		else if (aspect > 1.0) {
			y_sections = smallSideSections;
			x_sections = (int)(((double) smallSideSections) * aspect);
			return new Pair<Integer, Integer>(x_sections, y_sections);
		}
		else {
			y_sections = smallSideSections;
			x_sections = (int)(((double) smallSideSections) * aspect);
			return new Pair<Integer, Integer>(x_sections, y_sections);
		}
		
	}

	public static Box getThumbnail(final Canvas canvas, final int minSections) throws Exception {
		ThumbnailFinder thumbnailer = new ThumbnailFinder(canvas, 5);
		thumbnailer.start();
		thumbnailer.blockUntilDone(null);
		int max = Math.min(minSections / 3, 2);
		return thumbnailer.getMaxSelection(2, max, 2, max);
	}
	
	public static void main(String args[]) {
		try {
			Canvas canvas = new Canvas(new File(new File("data"), "flag_medium.jpg"));
			ThumbnailFinder thumbnailer = new ThumbnailFinder(canvas, 5);
			thumbnailer.start();
			thumbnailer.blockUntilDone(null);
//			for (Pair<Coordinate, Percent> value : thumbnailer.getRanked()) {
//				System.out.println(value.toString());
//			}
		}
		catch (Exception e) {
			e.printStackTrace();
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

	
	public void setInterest(final Canvas canvas) {
		try {
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
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void normalizeInterest(final int min, final int max) throws Exception {
		CheckUtils.check(interest_absolute, "absolute interest");
		interest_relative = Percent.getRelative(interest_absolute, min, max);
	}
	
	public boolean equals(final Object o) {
		if (o instanceof Area) {
			return equals((Area) o);
		}
		else {
			return false;
		}
	}
	
	public boolean equals(final Area area) {
		return area.getCoordinate().equals(coordinate);
	}
	
	public int hashCode() {
		return coordinate.hashCode();
	}
	
}