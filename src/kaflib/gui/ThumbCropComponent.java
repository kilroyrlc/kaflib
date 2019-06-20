package kaflib.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;
import kaflib.graphics.ThumbCropBoxes;
import kaflib.gui.components.DownscaledImageComponent;
import kaflib.gui.components.ImageListener;
import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;

/**
 * Defines a swing component that displayes an image with the ability to define
 * a thumbnail and crop.
 */
public class ThumbCropComponent extends DownscaledImageComponent implements ImageListener {

	private static final long serialVersionUID = 87987978L;

	private int thumbnail_height;
	private double thumbnail_aspect;
	
	private int crop_button = MouseEvent.BUTTON1;
	private int thumb_button = MouseEvent.BUTTON3;
	
	private boolean crop_image;
	private Coordinate down_click;
	private Coordinate thumbnail_center;
	private ThumbCropBoxes dimensions;
	
	private Coordinate mouse_location;
	
	private final Set<CropListener> listeners;
	
	private BufferedImage thumbnail_preview = null;

	/**
	 * Creates the component.  Read-only spares the mouse listeners associated
	 * with the editing functions.
	 * @param readOnly
	 * @throws Exception
	 */
	public ThumbCropComponent() throws Exception {
		super();
		crop_image = false;
		down_click = null;


		parseBoxes(null);

		dimensions = new ThumbCropBoxes();
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		listeners = new HashSet<CropListener>();
	}

	public ThumbCropComponent(final File file) throws Exception {
		this(file, null);
		dimensions = new ThumbCropBoxes();
	}
	
	public ThumbCropComponent(final File file, final String serial) throws Exception {
		super(file);
		crop_image = false;
		down_click = null;

		parseBoxes(serial);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		listeners = new HashSet<CropListener>();
	}
	
	public ThumbCropComponent(final int maxWidth, final int maxHeight) throws Exception {
		super(maxWidth, maxHeight);
		crop_image = false;
		down_click = null;

		parseBoxes(null);

		dimensions = new ThumbCropBoxes();
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);	
		listeners = new HashSet<CropListener>();
	}

	public ThumbCropComponent(final BufferedImage image) throws Exception {
		super(image);
		crop_image = false;
		down_click = null;
		dimensions = new ThumbCropBoxes();
		thumbnail_aspect = 1;
		thumbnail_height = 61;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		listeners = new HashSet<CropListener>();
	}

	public void addListener(final CropListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(final CropListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Updates the image.
	 * @param image
	 * @throws Exception
	 */
	public void update(final Canvas image) throws Exception {
		update(image, null);
	}
	
	/**
	 * Updates the image.
	 * @param image
	 * @throws Exception
	 */
	public void update(final BufferedImage image) throws Exception {
		update(new Canvas(image));
	}
	
	/**
	 * Updates the image.
	 * @param file
	 * @throws Exception
	 */
	public void update(final File file) throws Exception {
		update(new Canvas(file), null);
	}

	public void update(final Canvas image, final String serial) throws Exception {
		super.set(image);
		parseBoxes(serial);
		updateThumbnail();
	}
	
	public void update(final BufferedImage image, final String serial) throws Exception {
		update(new Canvas(image), serial);
	}

	/**
	 * Parses a serial string to set the crop and thumbnail info.
	 * @param serial
	 * @throws Exception
	 */
	private final void parseBoxes(final String serial) throws Exception {
		// Parse the serial dimension information if it is supplied.
		if (serial != null && serial.length() > 0) {
			dimensions = new ThumbCropBoxes(serial);
			if (getScalingFactor() != 1) {
				dimensions = ThumbCropBoxes.getScaledDown(dimensions, getScalingFactor());
			}
			thumbnail_aspect = dimensions.getThumbnail().getAspectRatio();
			thumbnail_height = dimensions.getThumbnail().getHeight();
			thumbnail_center = dimensions.getThumbnail().getCenter();
		}
		// No dimension information, clear.
		else {
			dimensions = new ThumbCropBoxes();
			thumbnail_center = null;
			thumbnail_preview = null;
			thumbnail_aspect = 1.0;
			thumbnail_height = 60;
		}
		// If there is dimension information, update the thumbnail.
		if (dimensions.getThumbnail() != null && 
			dimensions.getThumbnail().isContained(GraphicsUtils.getBounds(getShownImage()))) {
			thumbnail_preview = GraphicsUtils.getCropped(getShownImage(), (dimensions.getThumbnail()));
		}
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
		thumbnail_height = scaling;
	}
	
	public Box getCrop() {
		return dimensions.getCrop();
	}
	
	public Box getThumbnail() {
		return dimensions.getThumbnail();
	}
	
	/**
	 * Repaints the component.
	 */
    public void paint(Graphics g) {
    	super.paint(g);
    	Color color;
    	
    	// Mouse is being dragged.
    	if (down_click != null) {
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
    	// Draw a crop box if we're doing an outline rather than an actual crop.
    	if (getCrop() != null && !crop_image) {
    		g.setColor(Color.ORANGE);
    		g.drawRect(getCrop().getXMin(), getCrop().getYMin(), getCrop().getWidth(), getCrop().getHeight());
    	}

    	// Draw the centerpoint of the thumbnail.
    	if (thumbnail_center != null) {
    		g.setColor(Color.ORANGE);
    		g.fillOval(thumbnail_center.getX(), thumbnail_center.getY(), 5, 5);
    	}
    	
    	// Draw the thumbnail preview.
    	if (thumbnail_preview != null){
    		color = Color.GRAY;
    		g.fillRect(12, 12, thumbnail_preview.getWidth() + 8, thumbnail_preview.getHeight() + 8);
            g.drawImage(thumbnail_preview, 16, 16, null);
    	}
    }

    public final String toSerial() {
    	if (getScalingFactor() == 1) {
       		return dimensions.toSerial();
    	}
    	else {
    		try {
	    		return ThumbCropBoxes.getScaledUp(dimensions, getScalingFactor()).toSerial();
    		}
    		catch (Exception e) {
    			return "Error serializing scaled: " + dimensions.toSerial() + ".";
    		}
    	}
    }
    
    /**
     * Crops the image to the current dragged selection area.
     */
    public void cropImage(final Box box) {
    	try {
			Worker worker = new Worker(){
				@Override
				protected void process() throws Exception {
//					update(GraphicsUtils.getCropped(getSourceBufferedImage(), box));
					redraw();
				}};
			worker.start();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void updateThumbnail() {
    	updateThumbnail(null);
    }
    
    private void updateThumbnail(final Coordinate center) {
    	try {
    		// No current center, no new center specified.  Return.
    		if (center == null && thumbnail_center == null) {
    			return;
    		}
    		// New center specified, update.
    		if (center != null) {
				thumbnail_center = center;
    		}
    		
    		int width = (int) (thumbnail_aspect * thumbnail_height);
    		int height = thumbnail_height;
    		Box thumbnail = new Box(thumbnail_center, width, height);
    		thumbnail = Box.slideInbounds(thumbnail, GraphicsUtils.getBounds(getShownImage()));
    		dimensions.setThumbnail(thumbnail);
    		thumbnail_center = thumbnail.getCenter();
    		thumbnail_preview = GraphicsUtils.getCropped(getShownImage(), thumbnail);
			redraw();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
	private void notifyListeners() {
		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					for (CropListener listener : listeners) {
						listener.cropChanged();
					}
				}
			};
			worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
    
	@Override
	public void mouseDown(MouseButton button, int x, int y) {
		if (down_click == null) {
			down_click = new Coordinate(x, y);
		}
	}

	@Override
	public void mouseUp(MouseButton button, int x, int y) {
		try {
			if (down_click == null) {
				return;
			}
			final Coordinate up_click = new Coordinate(x, y);
			
			if (button == MouseButton.LEFT) {
				if (Box.isBox(down_click, up_click)) {
					Box crop = new Box(down_click, up_click);
					
					crop = crop.crop(new Box(getShownImage().getWidth(),
							getShownImage().getHeight()));
					
					if (crop_image) {			
						cropImage(crop);
					}
					dimensions.setCrop(crop);
					down_click = null;
					redraw();
					notifyListeners();
				}
			}
			else if (button == MouseButton.RIGHT) {
				Worker worker = new Worker(){
					@Override
					protected void process() throws Exception {
						updateThumbnail(up_click);
						notifyListeners();
					}};
				worker.start();
			}
			else {
				
			}
			down_click = null;
			redraw();
		}
		catch (Exception ex) {
			GUIUtils.showErrorDialog(this, "Error handling mouse release:\n" + ex.getMessage());
			ex.printStackTrace();
		}		
	}

	@Override
	public void mouseDrag(MouseButton button, int x, int y) {
		mouse_location = new Coordinate(x, y);
		redraw();
	}

	@Override
	public void mouseMove(int x, int y) {
		mouse_location = new Coordinate(x, y);
	}

	@Override
	public void mouseWheelMove(int rotation) {
		if (thumbnail_center != null) {
			thumbnail_height += -10 * rotation;
	    	try {
				Worker worker = new Worker(){
					@Override
					protected void process() throws Exception {
						updateThumbnail();
					}};
				worker.start();
	    	}
	    	catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
		}
	}

	
	public static void main(String args[]) {
		try {
			ImagePanel image_panel = new ImagePanel();
			JFrame image_frame = new JFrame();
			final File data_file = new File("data/tc.txt");
			image_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			String string = null;
			if (data_file.exists()) {
				string = FileUtils.readString(data_file);
			}
			final ThumbCropComponent component = new ThumbCropComponent(new File("data/flag.jpg"), string);
			image_panel.add(component);
	
			JButton button = new JButton("Save TC");
			button.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String serial = component.toSerial();
						if (serial != null) {
							FileUtils.write(data_file, component.toSerial());
						}
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
			});
			image_panel.addButton(button);
			
			image_frame.setContentPane(image_panel);
			image_frame.pack();
			image_frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


}
