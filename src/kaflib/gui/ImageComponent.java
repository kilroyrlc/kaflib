package kaflib.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import kaflib.graphics.GraphicsUtils;
import kaflib.types.Coordinate;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;

/**
 * Defines a swing component with a bufferedimage.
 */
public class ImageComponent extends Component implements MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * Defines image modes - read only, crop, select thumbnail.
	 */
	public enum Mode {
		VIEW,
		CROP,
		THUMBNAIL
	}
	
	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private float scaling;
	private int width;
	private int height;
	
	private int thumbnail_scaling;
	private float thumbnail_aspect;
	
	private Mode mode;
	private Coordinate mouse_location;
	private Coordinate down_click;
	private Coordinate up_click;

	public ImageComponent() throws Exception {
		scaling = 1;
		mode = Mode.VIEW;
		down_click = null;
		up_click = null;
		thumbnail_aspect = 1;
		thumbnail_scaling = 60;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	public ImageComponent(final File file) throws Exception {
		this();
		image = ImageIO.read(file);
		width = image.getWidth();
		height = image.getHeight();
	}

	public ImageComponent(final int width, final int height) throws Exception {
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
	
	/**
	 * Sets the x:y aspect ratio of the thumbnail.
	 * @param aspect
	 * @throws Exception
	 */
	public void setThumbnailAspect(final float aspect) throws Exception {
		CheckUtils.checkPositive(aspect, "aspect");
		thumbnail_aspect = aspect;
	}
	
	/**
	 * Sets the x:y aspect ratio of the thumbnail.
	 * @param aspect
	 * @throws Exception
	 */
	public void setThumbnailScaling(final int scaling) throws Exception {
		CheckUtils.checkPositive(scaling, "scaling");
		thumbnail_scaling = scaling;
	}
	
	
	/**
	 * Sets the current mode.
	 * @param mode
	 */
	public void setMode(final Mode mode) {
		this.mode = mode;
	}
	
	/**
	 * Updates the image.
	 * @param image
	 * @throws Exception
	 */
	public void update(final BufferedImage image) throws Exception {
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
    	Color color;
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
    	
    	if (mode == Mode.CROP) {
	    	Coordinate down = down_click;
	    	Coordinate now = mouse_location;
	    	
	    	if (down != null && now != null) {
	    		color = new Color(0x00, 0xaa, 0xff, 0x88);
	    		g.setColor(color);
	    		g.fillRect(down.getX(), down.getY(), now.getX() - down.getX(), now.getY() - down.getY());
	    		color = new Color(0x00, 0xaa, 0xff, 0x00);
	    		g.setColor(color);
	    		g.drawRect(down.getX(), down.getY(), now.getX() - down.getX(), now.getY() - down.getY());
	    	}
    	}
    	else if (mode == Mode.THUMBNAIL && thumbnail_aspect > 0) {
	    	Coordinate now = mouse_location;
	    	
	    	if (now != null) {
	    		int width = (int) (thumbnail_aspect * thumbnail_scaling);
	    		int height = thumbnail_scaling;
	    		int x = Math.max(0, now.getX() - width / 2);
	    		int y = Math.max(0, now.getY() - height / 2);
	    		
	    		color = new Color(0x00, 0xaa, 0xff, 0x88);
	    		g.setColor(color);
	    		g.fillRect(x, y, width, height);
	    		color = new Color(0x00, 0xaa, 0xff, 0x00);
	    		g.setColor(color);
	    		g.drawRect(x, y, width, height);
	    	}
    	}
    	else {
    		
    	}
    }

    public Dimension getPreferredSize() {
    	return new Dimension(width, height);
    }

    /**
     * Crops the image to the current dragged selection area.
     */
    public void crop() {
    	try {
			Worker worker = new Worker(){
				@Override
				protected void process() throws Exception {
					Coordinate down = down_click;
					Coordinate up = up_click;
					if (down != null && up != null) {
						Map<Coordinate.BoxValues, Integer> values = Coordinate.getXYWH(down, up);
						
						image = GraphicsUtils.getCropped(image, 
												 values.get(Coordinate.BoxValues.X), 
												 values.get(Coordinate.BoxValues.Y), 
												 values.get(Coordinate.BoxValues.WIDTH), 
												 values.get(Coordinate.BoxValues.HEIGHT));
					}
					down_click = null;
					up_click = null;
					repaint();

				}};
			worker.start();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (down_click == null) {
			down_click = new Coordinate(e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (up_click == null) {
			up_click = new Coordinate(e.getX(), e.getY());
		}
		if (mode == Mode.CROP) {
			crop();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse_location = new Coordinate(e.getX(), e.getY());
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				repaint();
			}
		});
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse_location = new Coordinate(e.getX(), e.getY());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (mode == Mode.THUMBNAIL) {
			thumbnail_scaling += -10 * e.getWheelRotation();
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					repaint();
				}
			});
		}
	}
	
	
}
