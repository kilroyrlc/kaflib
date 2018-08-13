package kaflib.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.Directory;
import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines an x-by-y grid of canvas tiles that have an overlap margin used to
 * re-stitch them.  The tile dimensions are static, so the canvas is resized
 * to fit the best grid layout.
 */
public class CanvasGrid {
	private final Canvas scaled;
	private final Canvas grid[][];
	private final int width;
	private final int height;
	private final int tile_width;
	private final int tile_height;
	private final int margin;

	public CanvasGrid(final File source,
					final int tileWidth,
					final int tileHeight,
					final int margin,
					final int longestDimension) throws Exception {
		this(new Canvas(source), tileWidth, tileHeight, margin, longestDimension);
	}

	private static double getRatio(final int width, final int height) {
		return ((double) Math.max(width, height)) /
				((double) Math.min(width, height));
	}

	public CanvasGrid(final Canvas source,
					final int tileWidth,
					final int tileHeight,
					final int margin,
					final int longestDimension) throws Exception {
		CheckUtils.check(source, "source");

		tile_width = tileWidth;
		tile_height = tileHeight;
		this.margin = margin;
		
		Coordinate bounds = getGrid(source.getWidth(), 
						source.getHeight(),
						longestDimension);
		grid = new Canvas[bounds.getX()][bounds.getY()];
		width = (tileWidth * grid.length) - (margin * (grid.length - 1));
		height = (tileHeight * grid[0].length) - (margin * (grid[0].length - 1));
		
		scaled = Canvas.fill(source, width, height);
		for (int i = 0; i < grid.length; i++) {
			int xstart = Math.max(0, i * tileWidth - (i * margin));
			for (int j = 0; j < grid[i].length; j++) {
				int ystart = Math.max(0, j * tileHeight - (j * margin));
				grid[i][j] = scaled.get(new Box(xstart, tileWidth, 
												ystart, tileHeight));
			}
		}
	}
	
	/**
	 * Returns a grid configuration as a box - the x by y dimensions of
	 * the grid based on the image width and height.
	 * @param width
	 * @param height
	 * @param max
	 * @return
	 * @throws Exception
	 */
	private static Coordinate getGrid(final int width, 
								final int height,
								final int max) throws Exception {
		if (width == height) {
			return new Coordinate(max, max);
		}
		double true_ratio = getRatio(width, height);
		
		int long_side = max;
		int short_side;
		for (short_side = max - 1; short_side >= 0; short_side--) {
			double this_ratio = getRatio(long_side, short_side);
			if (this_ratio > true_ratio) {
				double last_ratio = getRatio(long_side, short_side + 1);
				if (Math.abs(true_ratio - this_ratio) < Math.abs(true_ratio - last_ratio)) {
					break;
				}
				else {
					short_side++;
					break;
				}
			}
		}
		if (short_side < 1) {
			throw new Exception("Somehow short side came out to: " + 
								short_side + ".");
		}

		if (width > height) {
			return new Coordinate(max, short_side);
		}
		else {
			return new Coordinate(short_side, max);
		}
	}
	
	public Canvas scaled() {
		return scaled;
	}
	
	/**
	 * Returns a random canvas in this grid.
	 * @return
	 */
	public Canvas getRandom() {
		int i = RandomUtils.randomInt(grid.length);
		int j = RandomUtils.randomInt(grid[0].length);
		return grid[i][j];
	}
	
	public void set(final Coordinate coordinate, final Canvas canvas) throws Exception {
		CheckUtils.checkRange(coordinate.getX(), 0, grid.length - 1);
		CheckUtils.checkRange(coordinate.getY(), 0, grid[0].length - 1);
		grid[coordinate.getX()][coordinate.getY()] = canvas;
	}
	
	public Canvas get(final Coordinate coordinate) throws Exception {
		CheckUtils.checkRange(coordinate.getX(), 0, grid.length - 1);
		CheckUtils.checkRange(coordinate.getY(), 0, grid[0].length - 1);
		return grid[coordinate.getX()][coordinate.getY()];
	}
	
	public void toFile(final File file) throws Exception {
		toCanvas().toFile(file);
	}
	
	public Canvas toCanvas() throws Exception {
		Canvas canvas = null;
		for (int i = 0; i < grid.length; i++){
			Canvas column = grid[i][0];
			for (int j = 1; j < grid[i].length; j++) {
				column = Canvas.join(column, grid[i][j], margin, false);
			}
			if (i == 0) {
				canvas = column;
			}
			else {
				canvas = Canvas.join(canvas, column, margin, true);
			}
		}
		return canvas;
	}
	
	/** 
	 * Returns the number of horizontal tiles.
	 * @return
	 */
	public int getWidth() {
		return grid.length;
	}
	
	/**
	 * Returns the number of vertical tiles.
	 * @return
	 */
	public int getHeight() {
		return grid[0].length;
	}
	
	public int getTotalWidth() {
		return getWidth() * tile_width;
	}
	
	public int getTotalHeight() {
		return getHeight() * tile_height;
	}
	
	public void write(final Directory directory, final String prefix) throws Exception {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				File file = new File(directory, prefix + "_" + i + "_" + j + ".png");
				grid[i][j].toFile(file);
			}
		}
	}
	
	public List<Coordinate> getIndices() {
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				coordinates.add(new Coordinate(i, j));
			}
		}
		return coordinates;
	}
	
	public int size() {
		return grid.length * grid[0].length;
	}
	
	public Map<Coordinate, Canvas> get() throws Exception {
		Map<Coordinate, Canvas> map = new HashMap<Coordinate, Canvas>();
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				map.put(new Coordinate(i, j), grid[i][j]);
			}
		}
		return map;
	}
	
	/**
	 * Returns the tiles ordered by distance to center.
	 * @return
	 * @throws Exception
	 */
	public List<Canvas> getOrdered() throws Exception {
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		final Coordinate mid = new Coordinate(grid.length / 2, grid[0].length / 2);
		
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				coordinates.add(new Coordinate(i, j));
			}
		}
		Collections.sort(coordinates, new Comparator<Coordinate>() {
			@Override
			public int compare(Coordinate o1, Coordinate o2) {
				int one = o1.getDistanceSquared(mid);
				int two = o2.getDistanceSquared(mid);
				
				if (one < two) {
					return -1;
				}
				else if (one > two) {
					return 1;
				}
				else {
					return 0;
				}
			}
		});
				
		List<Canvas> list = new ArrayList<Canvas>();
		for (Coordinate coordinate : coordinates) {
			list.add(grid[coordinate.getX()][coordinate.getY()]);
		}
		return list;
	}
}
