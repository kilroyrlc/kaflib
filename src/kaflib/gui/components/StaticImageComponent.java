package kaflib.gui.components;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;

/**
 * Defines a component displaying an image that is sized to the image, 
 * however big.  See ScrolledImageComponent for a more useful component.
 */
public class StaticImageComponent extends ImageComponent {

	private static final long serialVersionUID = -4582834202035760065L;
	private BufferedImage image;
	
	public StaticImageComponent() {
		this(0, null, null);
		image = null;
	}

	public StaticImageComponent(final Canvas canvas) throws Exception {
		super();
		set(canvas);
	}
	
	public StaticImageComponent(final File file) throws Exception {
		super();
		set(file);
	}

	public StaticImageComponent(final Integer borderWidth, 
				              	final Color borderColor,
				              	final ImageListener listener) {
		super(borderWidth, borderColor, listener);
	}
	
	public void clear() {
		image = null;
		redraw();
	}

	
	public void set(final File file) throws Exception {
		set(GraphicsUtils.read(file));
	}

	public void set(final BufferedImage image) throws Exception {
		this.image = image;
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
			StaticImageComponent image = new StaticImageComponent(5, Color.RED, null);
			image.set(file);
			new KFrame(new KPanel(image));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
