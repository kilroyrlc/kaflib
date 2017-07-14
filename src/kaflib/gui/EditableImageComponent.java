package kaflib.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.swing.SwingUtilities;

import kaflib.graphics.GraphicsUtils;
import kaflib.types.Coordinate;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;

/**
 * Defines a swing component with a bufferedimage and editing capabilities.
 */
public class EditableImageComponent extends ImageComponent implements MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * Defines image modes - read only, crop, select thumbnail.
	 */
	public enum Mode {
		VIEW,
		CROP,
		THUMBNAIL
	}
	
	private static final long serialVersionUID = 87987978L;

	private int thumbnail_scaling;
	private float thumbnail_aspect;
	
	private Mode mode;
	private Coordinate mouse_location;
	private Coordinate down_click;
	private Coordinate up_click;
	
	private BufferedImage thumbnail = null;

	/**
	 * Creates the component.  Read-only spares the mouse listeners associated
	 * with the editing functions.
	 * @param readOnly
	 * @throws Exception
	 */
	public EditableImageComponent() throws Exception {
		super();
		initializeSubclassLocals();
	}

	public EditableImageComponent(final File file) throws Exception {
		super(file);
		initializeSubclassLocals();
	}
	
	public EditableImageComponent(final int width, final int height) throws Exception {
		super(width, height);
		initializeSubclassLocals();
	}

	public EditableImageComponent(final BufferedImage image) throws Exception {
		super(image, 1);
		initializeSubclassLocals();
	}

	/**
	 * Common initializer so each constructor doesn't have to copy/paste the 
	 * same code.
	 * @throws Exception
	 */
	private final void initializeSubclassLocals() throws Exception {
		mode = Mode.VIEW;
		down_click = null;
		up_click = null;
		thumbnail_aspect = 1;
		thumbnail_scaling = 60;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	public void update(final BufferedImage image) throws Exception {
		thumbnail = null;
		super.update(image);
	}
	
	/**
	 * Returns the down click location, or null if the mouse was upclicked 
	 * last.
	 * @return
	 */
	protected Coordinate getDownClick() {
		return down_click;
	}
	
	/**
	 * Returns the up click location, or null if the mouse was downclicked 
	 * last.
	 * @return
	 */
	protected Coordinate getUpClick() {
		return up_click;
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
	public void requestMode(final Mode mode) {
		setMode(mode);
	}
	
	/**
	 * Sets the current mode.
	 * @param mode
	 */
	protected void setMode(final Mode mode) {
		this.mode = mode;
	}
	
	/**
	 * Repaints the component.
	 */
    public void paint(Graphics g) {
    	super.paint(g);
    	Color color;
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
	    		int x = Math.min(Math.max(0, now.getX() - width / 2), getImage().getWidth());
	    		int y = Math.min(Math.max(0, now.getY() - height / 2), getImage().getHeight());
	    		
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
    	if (thumbnail != null){
    		color = Color.GRAY;
    		g.fillRect(12, 12, thumbnail.getWidth() + 8, thumbnail.getHeight() + 8);
            g.drawImage(thumbnail, 16, 16, null);
    	}
    }

    public BufferedImage getThumbnail() {
    	return thumbnail;
    }
    
    /**
     * Crops the image to the current dragged selection area.
     */
    public void crop() {
		final Coordinate down = down_click;
		final Coordinate up = up_click;
    	try {
			Worker worker = new Worker(){
				@Override
				protected void process() throws Exception {

					if (down != null && up != null) {
						Coordinate a = down.bound(getImage().getWidth(), getImage().getHeight());
						Coordinate b = up.bound(getImage().getWidth(), getImage().getHeight());
						Map<Coordinate.BoxValues, Integer> values = Coordinate.getXYWH(a, b);
						
						update(GraphicsUtils.getCropped(getImage(), 
												 values.get(Coordinate.BoxValues.X), 
												 values.get(Coordinate.BoxValues.Y), 
												 values.get(Coordinate.BoxValues.WIDTH), 
												 values.get(Coordinate.BoxValues.HEIGHT)));
					}
				}};
			worker.start();
			down_click = null;
			up_click = null;
			repaint();

    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void thumb() {
		final Coordinate up = up_click;    	
    	try {
			Worker worker = new Worker(){
				@Override
				protected void process() throws Exception {
					if (up != null) {
			    		int width = (int) (thumbnail_aspect * thumbnail_scaling);
			    		int height = thumbnail_scaling;
			    		int x = Math.min(Math.max(0, up.getX() - width / 2), getImage().getWidth());
			    		int y = Math.min(Math.max(0, up.getY() - height / 2), getImage().getHeight());
			    		
						thumbnail = GraphicsUtils.getCropped(getImage(), x, y, width, height); 
					}
				}};
			worker.start();
			down_click = null;
			up_click = null;
			repaint();

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
		else if (mode == Mode.THUMBNAIL) {
			thumb();
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
