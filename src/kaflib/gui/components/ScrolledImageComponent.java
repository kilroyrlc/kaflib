package kaflib.gui.components;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JScrollPane;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;

public class ScrolledImageComponent extends KPanel {

	private static final long serialVersionUID = -163026813414707128L;
	private final JScrollPane scroll;
	private StaticImageComponent image;
	private Dimension preferred_size;
	
	public ScrolledImageComponent() {
		this(null, null);
	}
	
	public ScrolledImageComponent(final Integer width, final Integer height) {
		scroll = new JScrollPane();
		image = new StaticImageComponent(3, Color.BLACK, null);
		scroll.setViewportView(image);
		add(scroll);
		if (width != null && height != null) {
			preferred_size = new Dimension(width, height);
		}
	}
	
	public void set(final File file) throws Exception {
		set(GraphicsUtils.read(file));
	}

	public void set(final BufferedImage image) throws Exception {
		this.image.set(image);
	}

	public void set(final Canvas canvas) throws Exception {
		set(canvas.toBufferedImage());
	}
	
	public Dimension getPreferredSize() {
		if (preferred_size == null) {
			return super.getPreferredSize();
		}
		else {
			return preferred_size;
		}
	}
	
	public static void main(String args[]) {
		try {
			File file = new File("data/flag.jpg");
			ScrolledImageComponent image = new ScrolledImageComponent(400, 200);
			image.set(file);
			new KFrame(image);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
