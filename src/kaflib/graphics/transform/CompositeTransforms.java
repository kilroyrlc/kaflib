package kaflib.graphics.transform;

import kaflib.graphics.Canvas;
import kaflib.graphics.Pixel;
import kaflib.graphics.Selection;
import kaflib.graphics.Transform;
import kaflib.graphics.IndependentTransform;
import kaflib.graphics.Opacity;

/**
 * A set of transform combinations that have an interesting result.
 */
public class CompositeTransforms {

	public static Canvas averageFilterRepeated(final Canvas input) throws Exception {
		Canvas canvas = input;
		AverageFilter filter = new AverageFilter(canvas, 17, AverageFilter.DELTA_VERY_LOW);
		filter.start();
		IndependentTransform.Status status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();
		
		filter = new AverageFilter(canvas, 7, AverageFilter.DELTA_MED);
		filter.start();
		status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();
		
		return canvas;
	}
	
	public static Canvas blendThenEdge(final Canvas input) throws Exception {
		Canvas canvas = input;
		Transform filter = new AverageFilter(canvas, 7, AverageFilter.DELTA_MED);
		filter.start();
		IndependentTransform.Status status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();
		
		filter = new EdgeFilter(canvas, new Pixel(Pixel.OPAQUE_BLACK), 3, EdgeFilter.DELTA_LOW, true);
		filter.start();
		status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();

		filter = new BrushTransform(canvas, 3, new Pixel(Pixel.OPAQUE_BLACK));
		filter.start();
		status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();
		
		
		return canvas;
	}
	
	public static Canvas temp(final Canvas input) throws Exception {
		Canvas canvas = input;
		Transform filter = new AverageFilter(canvas, 7, AverageFilter.DELTA_MED);
		filter.start();
		IndependentTransform.Status status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();
		
		filter = new EdgeFilter(canvas, new Pixel(Pixel.OPAQUE_BLACK), 3, EdgeFilter.DELTA_LOW, true);
		filter.start();
		status = filter.waitUntilFinished(null);
		if (status != IndependentTransform.Status.SUCCESS) {
			System.out.println("Filter failure.");
			System.out.println(filter.getMessages());
			return null;
		}
		canvas = filter.getResult();

		for (Selection selection : Selection.getAllSelections(canvas, new Opacity(Opacity.OPAQUE))) {
			canvas.set(selection.getCoordinates(), new Pixel(Pixel.OPAQUE_BLUE));
			Selection border = selection.getBorder();
			canvas.set(border.getCoordinates(), new Pixel(Pixel.OPAQUE_RED));
		}
		
		return canvas;
	}
	
}
