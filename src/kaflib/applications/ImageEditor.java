package kaflib.applications;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;

import kaflib.graphics.MedianFilter;
import kaflib.graphics.RegionFillFilter;
import kaflib.gui.ImageComponent;
import kaflib.types.Percent;

public class ImageEditor {
	 
	private ImageComponent inputImage;
	private ImageComponent outputImage;
	private JFrame inputFrame;
	private JFrame outputFrame;
	
	
	public ImageEditor(final File image) throws Exception {
		inputFrame = new JFrame();
		outputFrame = new JFrame();
		
		inputImage = new ImageComponent(image);
		outputImage = new ImageComponent(image);
		
		inputFrame.add(inputImage);
		outputFrame.add(outputImage);
		inputFrame.pack();
		inputFrame.setVisible(true);
		outputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		outputFrame.pack();
		outputFrame.setVisible(true);
	}
	
	public void process() throws Exception {
		BufferedImage image = inputImage.getImage();
		//MedianFilter filter = new MedianFilter(image, 5, new Percent(40));
		RegionFillFilter filter = new RegionFillFilter(image, 35, 1, new Percent(100));
		outputImage.update(filter.apply());
	}
	
	public static void main(String args[]) {
		try {
			ImageEditor editor = new ImageEditor(new File("flag.jpg"));
			editor.process();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
