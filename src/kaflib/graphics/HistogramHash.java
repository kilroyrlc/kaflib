//package kaflib.graphics;
//
//import kaflib.types.Histogram;
//
///**
// * Defines a hashing function based on a histogram of r/g/b values.  To address
// * scaling, the values are stored as relative percentages of the image.
// */
//public class HistogramHash extends ImageHash {
//
//	private final int SCALED_WIDTH = 640;
//	private final int BUCKETS = 32;
//	
//	private final Histogram<Double> r;
//	private final Histogram<Double> g;
//	private final Histogram<Double> b;
//	
//	public HistogramHash(final Canvas canvas) throws Exception {
//		r = new Histogram<Double>();
//		g = new Histogram<Double>();
//		b = new Histogram<Double>();
//		
//		Histogram<Byte> r_nominal;
//		Histogram<Byte> g_nominal;
//		Histogram<Byte> b_nominal;
//		
//		Canvas image = Canvas.scaleTo(canvas, SCALED_WIDTH, null);
//		
//		
//	}
//	
//	@Override
//	protected String getSerial() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
