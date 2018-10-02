package kaflib.graphics;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kaflib.graphics.transform.CompositeTransforms;
import kaflib.gui.ImageComponent;

public class FilterTest {
	
	private ImageComponent inputImage;
	private ImageComponent outputImage;
	private JFrame frame;
	
	public FilterTest(final File image) throws Exception {
		frame = new JFrame();
		JPanel panel = new JPanel(new GridLayout(1, 2));
		inputImage = new ImageComponent(image);
		outputImage = new ImageComponent(image);
		
		panel.add(inputImage);
		panel.add(outputImage);
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void process() throws Exception {
		System.out.println("Initializing canvas.");
		Canvas canvas = new Canvas(inputImage.getImage());
		System.out.println("Creating filter.");
			
		//MedianFilter filter = new MedianFilter(image, 3, new Percent(40));
		//RegionFillFilter filter = new RegionFillFilter(image, 35, 1, new Percent(100));

//		AverageFilter filter = new AverageFilter(canvas, 13, AverageFilter.DELTA_MED);
//		EdgeFilter filter = new EdgeFilter(canvas, 
//										   new Pixel(Pixel.OPAQUE_BLACK), 
//										   7, 
//										   new Percent(400));
//	
//		System.out.println("Applying filter.");
//		filter.start();
//		Transform.Status status = filter.waitUntilFinished(null);
//		if (status != Transform.Status.SUCCESS) {
//			System.out.println("Filter failure.");
//			System.out.println(filter.getMessages());
//		}
//		else {
//			System.out.println("Filter success.");
//			outputImage.update(filter.getResult().toBufferedImage());
//		}
		canvas = CompositeTransforms.temp(canvas);
		if (canvas != null) {
			outputImage.update(canvas.toBufferedImage());
		}
		
		System.out.println("Done applying filter.");

	}
	
	public static void main(String args[]) {
		try {
			//FilterTest editor = new FilterTest(new File("flag.jpg"));
			FilterTest editor = new FilterTest(new File(new File("data"), "flag_medium.jpg"));
			//FilterTest editor = new FilterTest(new File("flag_small.jpg"));
			editor.process();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
