package kaflib.graphics.transform;


import java.util.List;

import kaflib.graphics.Canvas;
import kaflib.graphics.Pixel;
import kaflib.graphics.Selection;
import kaflib.graphics.IndependentTransform;
import kaflib.types.Coordinate;
import kaflib.utils.CheckUtils;

public class AverageFilter extends IndependentTransform {
	public static final int DELTA_VERY_LOW = 20;
	public static final int DELTA_LOW = 45;
	public static final int DELTA_MED = 80;
	public static final int DELTA_HIGH = 120;
	public static final int DELTA_VERY_HIGH = 150;
	private final int size;
	private final Integer delta;
	
	public AverageFilter(final Canvas input,
		  	 final int size) throws Exception {
		this(input, size, null);
	}
	
	public AverageFilter(final Canvas input,
					  	 final int size,
					  	 final Integer delta) throws Exception {
		super(input);
		CheckUtils.checkPositive(size, "size");
		this.size = size;
		this.delta = delta;
	}

	@Override
	protected void visit(Coordinate coordinate) throws Exception {
		Selection selection = Selection.getStar(coordinate, size);
		Pixel value;
		if (delta != null && delta > 0) {
			List<Pixel> pixels = selection.getWithin(getInput(), getInput().get(coordinate), delta);
			value = Pixel.getAverage(pixels);
		}
		else {
			value = selection.getAverage(getInput());
		}
		getOutput().set(coordinate, value);
	}

}
