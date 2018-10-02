package kaflib.applications;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import kaflib.gui.ThumbCropComponent;

/**
 * Defines a type that shows a raster image and a client/child-specified 
 * toolbar.
 */
public class ImageEditor {
	 
	private ThumbCropComponent image;
	private JFrame frame;
	private JPanel panel;
	private JScrollPane input_pane;
	
	/**
	 * Create the editor with the specified image.
	 * @param image
	 * @param inputs
	 * @throws Exception
	 */
	public ImageEditor(final File image) throws Exception {
		this();
		this.image = new ThumbCropComponent(image);
	}
	
	/**
	 * Create the editor with the specified image.
	 * @param image
	 * @param inputs
	 * @throws Exception
	 */
	public ImageEditor(final BufferedImage image) throws Exception {
		this();
		this.image = new ThumbCropComponent(image);
	}

	/**
	 * Create the editor with the specified image.
	 * @param image
	 * @param inputs
	 * @throws Exception
	 */
	public ImageEditor() throws Exception {
		frame = new JFrame();
		this.image = new ThumbCropComponent();
	}
	
	protected ThumbCropComponent getComponent() {
		return image;
	}
	
	protected JFrame getFrame() {
		return frame;
	}
	
	/**
	 * Does the gui init.
	 * @throws Exception
	 */
	public void show() throws Exception {
		panel = new JPanel(new BorderLayout());
		
		this.image.setThumbnailAspect((float) 0.85);
		panel.add(this.image, BorderLayout.CENTER);
		
		input_pane = new JScrollPane(getInputPanel());
		panel.add(input_pane, BorderLayout.EAST);
		
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	/**
	 * Returns the input panel.  Meant to be overridden.
	 * @return
	 */
	protected JPanel getInputPanel() {
		return new JPanel();
	}
	
	/**
	 * Unit test function.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			JPanel panel = new JPanel(new GridLayout(3, 1));
			panel.add(new JLabel("Label a"));
			panel.add(new JLabel("Label b"));
			panel.add(new JLabel("Label c"));
			
			ImageEditor editor = new ImageEditor(new File("data/flag.jpg"));
			editor.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
