package kaflib.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;
import kaflib.utils.CheckUtils;

/**
 * Defines a swing component that shows an image.
 */
public class DownscaledImageComponent extends ImageComponent {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	
	private BufferedImage displayed_image;
	private int scaling_factor; // Always down: 1/2 = 2, 1/4 = 4...
	private final int max_width;
	private final int max_height;

	public DownscaledImageComponent() throws Exception {
		this(0, null, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
	}
	
	public DownscaledImageComponent(final File file) throws Exception {
		this(0, null, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
		set(file);
	}

	public DownscaledImageComponent(final Canvas canvas) throws Exception {
		this(0, null, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
		set(canvas);
	}

	public DownscaledImageComponent(final BufferedImage image) throws Exception {
		this(0, null, DEFAULT_WIDTH, DEFAULT_HEIGHT, null);
		set(image);
	}

	public DownscaledImageComponent(final int maxWidth, final int maxHeight) throws Exception {
		this(0, null, maxWidth, maxHeight, null);
	}
	
	public DownscaledImageComponent(final File file,
						            final int maxWidth, 
									final int maxHeight) throws Exception {
		this(0, null, maxWidth, maxHeight, null);
		set(file);
	}

	public DownscaledImageComponent(final Canvas canvas,
            final int maxWidth, 
			final int maxHeight) throws Exception {
		this(0, null, maxWidth, maxHeight, null);
		set(canvas);
	}
	
	/**
	 * Creates an empty component of the specified dimensions.
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public DownscaledImageComponent(final Integer borderWidth, 
					                final Color borderColor,
					                final int maxWidth, 
									final int maxHeight,
									final ImageListener listener) throws Exception {
		super(borderWidth, borderColor, listener);
		CheckUtils.checkPositive(maxWidth, maxHeight);
		max_width = maxWidth;
		max_height = maxHeight;
	}

	public void update(final File file) throws Exception {
		set(new Canvas(file));
	}

	public void update(final Canvas canvas) throws Exception {
		updateImage(canvas.toBufferedImage());
	}

	public void update(final BufferedImage image) throws Exception {
		updateImage(image);
	}
	
	public void set(final File file) throws Exception {
		set(new Canvas(file));
	}

	public void set(final Canvas canvas) throws Exception {
		updateImage(canvas.toBufferedImage());
	}

	public void set(final BufferedImage image) throws Exception {
		updateImage(image);
	}
	
	public BufferedImage getShownImage() {
		return displayed_image;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(max_width, max_width);
	}
	
	private void updateImage(final BufferedImage image) throws Exception {
		if (image == null) {
			redraw();
			return;
		}
		
		displayed_image = image;
		scaling_factor = 1;
		while ((displayed_image.getWidth() > max_width) ||
		       (displayed_image.getHeight() > max_height)) {
			displayed_image = GraphicsUtils.getScaledDown(displayed_image, 2);
			scaling_factor *= 2;
		}
		redraw();
	}

	/**
	 * Returns scaling factor as a 1/x integer.  So scaled by 1/4 = 4.
	 * @return
	 * @throws Exception
	 */
	protected int getScalingFactor() {
		return scaling_factor;
	}
	
	public void clear() throws Exception {
		set((BufferedImage) null);
	}

	@Override
	protected BufferedImage getImage() {
		return displayed_image;
	}
	
	public static void main(String args[]) {
		try {
			File file = new File("data/flag.jpg");
			KPanel panel = new KPanel("Test", 
									  1, 
									  3,
									  new DownscaledImageComponent(file, 800, 600),
									  new DownscaledImageComponent(file, 400, 300),
									  new DownscaledImageComponent(file, 200, 200));
			new KFrame(panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
