//package kaflib.graphics.transform;
//
//import kaflib.graphics.Canvas;
//import kaflib.graphics.RGBPixel;
//import kaflib.types.Byte;
//import kaflib.graphics.Selection;
//import kaflib.graphics.Transform;
//import kaflib.graphics.IndependentTransform;
//
///**
// * A set of transform combinations that have an interesting result.
// */
//public class CompositeTransforms {
//
//	public static Canvas averageFilterRepeated(final Canvas input) throws Exception {
//		Canvas canvas = input;
//		AverageFilter filter = new AverageFilter(canvas, 17, AverageFilter.DELTA_VERY_LOW);
//		filter.start();
//		IndependentTransform.Status status = filter.waitUntilFinished(null);
//		if (status != IndependentTransform.Status.SUCCESS) {
//			System.out.println("Filter failure.");
//			System.out.println(filter.getMessages());
//			return null;
//		}
//		canvas = filter.getResult();
//		
//		filter = new AverageFilter(canvas, 7, AverageFilter.DELTA_MED);
//		filter.start();
//		status = filter.waitUntilFinished(null);
//		if (status != IndependentTransform.Status.SUCCESS) {
//			System.out.println("Filter failure.");
//			System.out.println(filter.getMessages());
//			return null;
//		}
//		canvas = filter.getResult();
//		
//		return canvas;
//	}
//	
//	public static Canvas blendThenEdge(final Canvas input) throws Exception {
//		Canvas canvas = input;
//		Transform filter = new AverageFilter(canvas, 7, AverageFilter.DELTA_MED);
//		filter.start();
//		IndependentTransform.Status status = filter.waitUntilFinished(null);
//		if (status != IndependentTransform.Status.SUCCESS) {
//			System.out.println("Filter failure.");
//			System.out.println(filter.getMessages());
//			return null;
//		}
//		canvas = filter.getResult();
//		
//		filter = new EdgeFilter(canvas, new RGBPixel(RGBPixel.OPAQUE_BLACK), 3, EdgeFilter.DELTA_LOW, true);
//		filter.start();
//		status = filter.waitUntilFinished(null);
//		if (status != IndependentTransform.Status.SUCCESS) {
//			System.out.println("Filter failure.");
//			System.out.println(filter.getMessages());
//			return null;
//		}
//		canvas = filter.getResult();
//
//		filter = new BrushTransform(canvas, 3, new RGBPixel(RGBPixel.OPAQUE_BLACK));
//		filter.start();
//		status = filter.waitUntilFinished(null);
//		if (status != IndependentTransform.Status.SUCCESS) {
//			System.out.println("Filter failure.");
//			System.out.println(filter.getMessages());
//			return null;
//		}
//		canvas = filter.getResult();
//		
//		
//		return canvas;
//	}
//	
//	public static Canvas temp(final Canvas input) throws Exception {
//		Canvas canvas = input;
////		Transform filter = new AverageFilter(canvas, 7, AverageFilter.DELTA_MED);
////		filter.start();
////		IndependentTransform.Status status = filter.waitUntilFinished(null);
////		if (status != IndependentTransform.Status.SUCCESS) {
////			System.out.println("Filter failure.");
////			System.out.println(filter.getMessages());
////			return null;
////		}
////		canvas = filter.getResult();
//		
//		RegionSelections selections = new RegionSelections(canvas, 60, 75, 1, RegionSelections.DELTA_MED);
//		selections.start();
//		selections.blockUntilDone(null);
//
//		for (Selection selection : selections.getSelections()) {
//			RGBPixel average = selection.getAverage(canvas);
//			canvas.set(selection.getCoordinates(), average);
//			if (selection.size() > 30) {
//				RGBPixel border = new RGBPixel(average);
//				border.darken(new Byte(0x10));
//				canvas.set(selection.getBorder(), border);
//			}
//		}
//
//		return canvas;
//	}
//	
//}
