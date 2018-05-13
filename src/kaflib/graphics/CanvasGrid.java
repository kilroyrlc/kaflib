package kaflib.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaflib.graphics.Canvas.Orientation;
import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.Directory;
import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;

public class CanvasGrid {
	public enum AspectPreset {
		NONE,
		THREE_BY_THREE,
		THREE_BY_FOUR,
		THREE_BY_FIVE
	}
	
	private final Canvas scaled;
	private final Canvas source;
	private final Canvas grid[][];
	private final int width;
	private final int height;
	private final int element_width;
	private final int element_height;
	private final int margin;
	private final AspectPreset aspect;
	
	
//	public CanvasGrid(final Canvas source,
//					  final int horizontalSections,
//					  final int verticalSections,
//					  final int margin) throws Exception {
//		
//	}

	public CanvasGrid(final Canvas source,
					  final int xSize,
					  final int ySize,
					  final int margin) throws Exception {
		CheckUtils.check(source, "source");
		
		this.source = source;
		element_width = xSize;
		element_height = ySize;
		this.margin = margin;
		
		double ratio = ((double) Math.max(source.getWidth(), source.getHeight())) /
					   ((double) Math.min(source.getWidth(), source.getHeight()));

		if (ratio < 1.0) {
			throw new Exception("Didn't do aspect ratio right.");
		}
		else if (ratio < 1.17) {
			aspect = AspectPreset.THREE_BY_THREE;
			grid = new Canvas[3][3];
			width = xSize * 3 - margin * 2;
			height = ySize * 3 - margin * 2;
			
		}
		else if (ratio < 1.50) {
			aspect = AspectPreset.THREE_BY_FOUR;
			if (source.getOrientation() == Orientation.LANDSCAPE) {
				grid = new Canvas[4][3];
				width = xSize * 4 - margin * 3;
				height = ySize * 3 - margin * 2;
			}
			else {
				grid = new Canvas[3][4];
				width = xSize * 3 - margin * 2;
				height = ySize * 4 - margin * 3;
			}
		}
		else {
			aspect = AspectPreset.THREE_BY_FIVE;
			if (source.getOrientation() == Orientation.LANDSCAPE) {
				grid = new Canvas[5][3];
				width = xSize * 5 - margin * 4;
				height = ySize * 3 - margin * 2;
			}
			else {
				grid = new Canvas[3][5];
				width = xSize * 3 - margin * 2;
				height = ySize * 5 - margin * 4;
			}		
		}
		scaled = Canvas.fill(source, width, height);
		for (int i = 0; i < grid.length; i++) {
			int xstart = Math.max(0, i * xSize - (i * margin));
			for (int j = 0; j < grid[i].length; j++) {
				int ystart = Math.max(0, j * ySize - (j * margin));
				grid[i][j] = scaled.get(new Box(xstart, xstart + xSize, 
								                ystart, ystart + ySize));
			}
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
				Canvas.join(column, grid[i][j], margin, false);
			}
column.toFile(new File("column_" + i + ".png"));
			if (i == 0) {
				canvas = column;
			}
			else {
				canvas = Canvas.join(canvas, column, margin, true);
			}
		}
		return canvas;
	}
	
	public void write(final Directory directory, final String prefix) throws Exception {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				File file = new File(directory, prefix + "_" + i + "_" + j + ".png");
				grid[i][j].toFile(file);
			}
		}
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
	 * Returns an ordered list of subcanvases in the grid.  The order is preset
	 * starting with the center elements and working outward.
	 * @return
	 * @throws Exception
	 */
	public List<Canvas> getOrdered() throws Exception {
		List<Canvas> list = new ArrayList<Canvas>();
		if (aspect == AspectPreset.THREE_BY_THREE) {
			for (Coordinate coordinate : ORDER_3_3) {
				list.add(get(coordinate));
			}
		}
		else if (aspect == AspectPreset.THREE_BY_FOUR) {
			for (Coordinate coordinate : ORDER_3_4) {
				list.add(get(coordinate));
			}
		}
		else if (aspect == AspectPreset.THREE_BY_FIVE) {
			for (Coordinate coordinate : ORDER_3_5) {
				list.add(get(coordinate));
			}		
		}
		else {
			throw new Exception("Not yet implemented.");
		}
		return list;
	}
	
	private static final Coordinate ORDER_3_3[] = 
		{
			new Coordinate(1, 1),
			new Coordinate(2, 1),
			new Coordinate(1, 0),
			new Coordinate(0, 1),
			new Coordinate(1, 2),
			new Coordinate(2, 0),
			new Coordinate(0, 2),
			new Coordinate(0, 0),
			new Coordinate(2, 2)
		};
	private static final Coordinate ORDER_3_4[] = 
		{
			new Coordinate(2, 1),
			new Coordinate(1, 1),
			new Coordinate(2, 0),
			new Coordinate(1, 2),
			new Coordinate(1, 0),
			new Coordinate(2, 2),
			new Coordinate(0, 1),
			new Coordinate(3, 1),
			new Coordinate(0, 2),
			new Coordinate(3, 0),
			new Coordinate(3, 2),
			new Coordinate(0, 0)
		};
	private static final Coordinate ORDER_3_5[] = 
		{
			new Coordinate(2, 1),
			new Coordinate(3, 1),
			new Coordinate(1, 1),
			new Coordinate(2, 0),
			new Coordinate(2, 2),
			new Coordinate(1, 0),
			new Coordinate(3, 2),
			new Coordinate(1, 2),
			new Coordinate(3, 0),
			new Coordinate(0, 1),
			new Coordinate(4, 1),
			new Coordinate(0, 2),
			new Coordinate(4, 0),
			new Coordinate(0, 0),
			new Coordinate(4, 2)
		};
	
}
