package kaflib.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;
import kaflib.utils.CheckUtils;

/**
 * Defines a swing component with a bufferedimage.
 */
public class ImageComponent extends Component {

	private static final long serialVersionUID = 1L;

	private Canvas image;
	private BufferedImage scaled_image; // The scaled image.
	private int scaling_factor; // Always down: 1/2 = 2, 1/4 = 4...
	private Integer max_width;
	private Integer max_height;
	
	/**
	 * Creates the component.  Read-only spares the mouse listeners associated
	 * with the editing functions.
	 * @param readOnly
	 * @throws Exception
	 */
	public ImageComponent() throws Exception {
		super();
		max_width = null;
		max_height = null;
	}
	
	/**
	 * Creates a component with the image contained in the specified file.
	 * @param file
	 * @throws Exception
	 */
	public ImageComponent(final File file) throws Exception {
		this();
		update(file);
	}
	
	/**
	 * Creates an empty component of the specified dimensions.
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public ImageComponent(final int maxWidth, final int maxHeight) throws Exception {
		this();
		max_width = maxWidth;
		max_height = maxHeight;
	}
	
	public ImageComponent(final Canvas image) throws Exception {
		this(image.toBufferedImage());
	}

	
	public ImageComponent(final BufferedImage image) throws Exception {
		update(image);

	}

	public Canvas getCanvas() {
		return image;
	}
	
	public BufferedImage getImage() throws Exception {
		return image.toBufferedImage();
	}
		
	public boolean scale() {
		return max_width != null || max_height != null;
	}
	
	private void updateScaledImage() throws Exception {
		scaled_image = image.toBufferedImage();
		scaling_factor = 1;
		while ((max_width != null && scaled_image.getWidth() > max_width) ||
		       (max_height != null && scaled_image.getHeight() > max_height)) {
			scaled_image = GraphicsUtils.getScaledDown(scaled_image, 2);
			scaling_factor *= 2;
		}
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				invalidate();
				repaint();
				revalidate();
			}
		});
	}
	
	protected BufferedImage getScaledImage() {
		return scaled_image;
	}
	
	/**
	 * Returns scaling factor as a 1/x integer.  So scaled by 1/4 = 4.
	 * @return
	 * @throws Exception
	 */
	protected int getScalingFactor() {
		return scaling_factor;
	}
	
	public void setMaxWidth(final Integer width) throws Exception {
		CheckUtils.checkPositive(width, "width");
		max_width = width;
		updateScaledImage();
	}
	
	public void setMaxHeight(final Integer height) throws Exception {
		CheckUtils.checkPositive(height, "height");
		max_height = height;
		updateScaledImage();
	}
	
	/**
	 * Updates the image.
	 * @param image
	 * @throws Exception
	 */
	public void update(final Canvas image) throws Exception {
		CheckUtils.check(image, "image");
		this.image = image;
		updateScaledImage();
	}
	
	/**
	 * Updates the image.
	 * @param image
	 * @throws Exception
	 */
	public void update(final BufferedImage image) throws Exception {
		CheckUtils.check(image, "image");
		this.image = new Canvas(image);
		updateScaledImage();

	}
	
	/**
	 * Updates the image.
	 * @param file
	 * @throws Exception
	 */
	public void update(final File file) throws Exception {
		if (file == null || !file.exists()) {
			return;
		}
		
		image = new Canvas(file);
		updateScaledImage();

	}
	
	/**
	 * Repaints the component.
	 */
    public void paint(Graphics g) {
    	if (image == null) {
    		return;
    	}
        g.drawImage(scaled_image, 0, 0, null);
    }

    /**
     * Returns the preferred component size, typically matching the content.
     */
    public Dimension getPreferredSize() {
    	Integer width = max_width;
    	Integer height = max_height;

    	if (image != null) {
        	if (width == null) {
        		width = image.getWidth();
        	}
        	if (height == null) {
        		height = image.getHeight();
        	}
    	}
    	if (width == null) {
    		width = 0;
    	}
    	if (height == null) {
    		height = 0;
    	}
    	return new Dimension(width, height);
    }
	
	public static void main(String args[]) {
		try {
			JPanel image_panel = new ImagePanel();
			JFrame image_frame = new JFrame();
			image_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			ImageComponent component = new ImageComponent(new File("data/flag.jpg"));
			image_panel.add(component);
			image_frame.setContentPane(image_panel);
			image_frame.pack();
			image_frame.setVisible(true);
			//component.update(new File("data/flag.jpg"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
