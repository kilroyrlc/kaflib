package kaflib.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import kaflib.graphics.Canvas;
import kaflib.utils.CheckUtils;

/**
 * Defines a swing component with a bufferedimage.
 */
public class ImageComponent extends Component {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private float scaling;
	private int width;
	private int height;
	
	/**
	 * Creates the component.  Read-only spares the mouse listeners associated
	 * with the editing functions.
	 * @param readOnly
	 * @throws Exception
	 */
	public ImageComponent() throws Exception {
		scaling = 1;
	}
	
	public ImageComponent(final File file) throws Exception {
		this();
		image = ImageIO.read(file);
		width = image.getWidth();
		height = image.getHeight();
	}
	
	public ImageComponent(final int width, final int height) throws Exception {
		this();
		this.width = width;
		this.height = height;
	}
	
	public ImageComponent(final int width, final int height, final float scaling) throws Exception {
		this();
		this.scaling = scaling;
		this.width = width;
		this.height = height;
	}
	
	public ImageComponent(final File file, final float scaling) throws Exception {
		this();
		
		image = ImageIO.read(file);
		this.scaling = scaling;
		
		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
	}

	public ImageComponent(final Canvas image) throws Exception {
		this(image.toBufferedImage(), 1);
	}

	
	public ImageComponent(final BufferedImage image) throws Exception {
		this(image, 1);
	}

	public ImageComponent(final BufferedImage image, final boolean readOnly) throws Exception {
		this(image, 1, readOnly);
	}
	
	public ImageComponent(final BufferedImage image, final float scaling) throws Exception {
		this();
		this.image = image;
		this.scaling = scaling;
		
		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
	}
	
	public ImageComponent(final BufferedImage image, 
						  final float scaling,
						  final boolean readOnly) throws Exception {
		this();
		this.image = image;
		this.scaling = scaling;
		
		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
	}
	
	public BufferedImage getImage() {
		return image;
	}
		
	/**
	 * Sets the x:y aspect ratio of the thumbnail.
	 * @param aspect
	 * @throws Exception
	 */
	public void setThumbnailAspect(final float aspect) throws Exception {
		CheckUtils.checkPositive(aspect, "aspect");
	}
	
	/**
	 * Sets the x:y aspect ratio of the thumbnail.
	 * @param aspect
	 * @throws Exception
	 */
	public void setThumbnailScaling(final int scaling) throws Exception {
		CheckUtils.checkPositive(scaling, "scaling");
	}
	
	
	/**
	 * Updates the image.
	 * @param image
	 * @throws Exception
	 */
	public void update(final BufferedImage image) throws Exception {
		CheckUtils.check(image, "image");
		this.image = image;
		
		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
		
		invalidate();
		repaint();
		revalidate();
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
		
		image = ImageIO.read(file);

		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
		
		invalidate();
		repaint();
		revalidate();
	}
	
	/**
	 * Repaints the component.
	 */
    public void paint(Graphics g) {
    	if (image == null) {
    		return;
    	}
    	if (scaling == 1) {
            g.drawImage(image, 0, 0, null);
    	}
    	else {
        	g.drawImage(image, 0, 0, 
        				(int) (image.getWidth() * scaling), 
        				(int) (image.getHeight() * scaling), null);
    		
    	}
    }

    public Dimension getPreferredSize() {
    	return new Dimension(width, height);
    }
	
	
}
