package kaflib.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Defines a swing component with a bufferedimage.
 */
public class ImageComponent extends Component {

	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private float scaling;
	
	private int width;
	private int height;

	public ImageComponent() throws Exception {
		scaling = 1;
	}
	
	public ImageComponent(final File file) throws Exception {
		image = ImageIO.read(file);
		scaling = 1;
		width = image.getWidth();
		height = image.getHeight();
	}

	public ImageComponent(final int width, final int height) throws Exception {
		this.scaling = 1;
		this.width = width;
		this.height = height;
	}
	
	public ImageComponent(final int width, final int height, final float scaling) throws Exception {
		this.scaling = scaling;
		this.width = width;
		this.height = height;
	}
	
	public ImageComponent(final File file, final float scaling) throws Exception {
		image = ImageIO.read(file);
		this.scaling = scaling;
		
		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
	}

	public BufferedImage getImage() throws Exception {
		return image;
	}
	
	public void update(final BufferedImage image) throws Exception {
		this.image = image;
		
		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
		
		invalidate();
		repaint();
		revalidate();
	}
	
	public void update(final File file) throws Exception {
		image = ImageIO.read(file);

		width = (int)(image.getWidth() * scaling);
		height = (int)(image.getHeight() * scaling);
		
		invalidate();
		repaint();
		revalidate();
	}
	
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
