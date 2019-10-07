package kaflib.gui.components;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;


/**
 * Defines a component that scales the input image to match the component size.
 */
public class ScaledImageComponent extends ImageComponent {

	private static final long serialVersionUID = -4582834202078980065L;
	private BufferedImage image;
	private final int width;
	private final int height;
	
	public ScaledImageComponent(final int width, final int height) {
		this(width, height, 0, null, null);
		image = null;
	}
	
	public ScaledImageComponent(final int width, 
								final int height,
								final Integer borderWidth, 
				                final Color borderColor,
				                final ImageListener listener) {
		super(borderWidth, borderColor, listener);
		this.width = width;
		this.height = height;
	}
	
	public void clear() {
		image = null;
		redraw();
	}
	
	public void set(final File file) throws Exception {
		set(GraphicsUtils.read(file));
	}

	public void set(final BufferedImage image) throws Exception {
		this.image = null;
		this.image = GraphicsUtils.getScaled(image, width, height);
		redraw();
	}

	public void set(final Canvas canvas) throws Exception {
		set(canvas.toBufferedImage());
	}
	
	@Override
	protected BufferedImage getImage() {
		return image;
	}

	public static void main(String args[]) {
		try {
			File file = new File("data/flag.jpg");
			ScaledImageComponent image = new ScaledImageComponent(2000, 1200, 3, Color.RED, null);
			image.set(file);
			new KFrame(new KPanel(image));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
