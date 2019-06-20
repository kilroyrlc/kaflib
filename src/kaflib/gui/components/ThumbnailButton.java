package kaflib.gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;
import kaflib.types.Box;

public class ThumbnailButton extends KButton implements ActionListener {

	private static final long serialVersionUID = 8935605236358068028L;
	private static final Color SELECTED = Color.BLUE;
	private static final Color UNSELECTED = Color.GRAY;
	private boolean selectable;
	private boolean is_selected;
	private File file;

	public ThumbnailButton(final File file,
						   final Box box, 
			   			   final KListener listener) throws Exception {
		this(getImage(file, box, box.getWidth(), box.getHeight()), listener);
		this.file = file;
	}

	public ThumbnailButton(final File file,
			   final Box box, 
			   final Integer width,
			   final Integer height,
			   final KListener listener) throws Exception {
		this(getImage(file, box, width, height), width, height, listener);
		this.file = file;
	}
	
	public ThumbnailButton(final BufferedImage image, 
						   final KListener listener) throws Exception {
		this(image, null, null, listener);
	}
	
	public ThumbnailButton(final BufferedImage image, 
						   final Integer width,
						   final Integer height,
						   final KListener listener) throws Exception {
		super(listener);
		if (width == null && height == null) {
			this.setIcon(new ImageIcon(image));
		}
		else {
			this.setIcon(new ImageIcon(GraphicsUtils.getScaled(image, width, height)));
		}
		selectable = false;

		this.is_selected = false;
		
		if (listener == null) {
			addActionListener(this);
		}
	}
	
	public void setSelected(final boolean selected) {
		if (!selectable) {
			return;
		}
		if (selected != is_selected) {
			is_selected = selected;
			redraw();
		}
	}
	
	public void setSelectable(final boolean selectable) {
		this.selectable = selectable;
		if (!selectable) {
			is_selected = false;
		}
	}
	

	protected static BufferedImage getImage(final File file, 
										  final Box box) throws Exception {
		return getImage(file, box, null, null);
	}
	
	protected static BufferedImage getImage(final File file, 
										  final Box box, 
										  final Integer width, 
										  final Integer height) throws Exception {
		Canvas canvas = new Canvas(file);
		if (box != null && box.isContained(canvas.getBounds())) {
			canvas = canvas.get(box);
			if (width != null && height != null) {
				return canvas.getCropped(width, height).toBufferedImage();
			}
			else {
				return canvas.toBufferedImage();
			}
		}
		else {
			if (width == null || height == null) {
				throw new Exception("No crop box or dimensions specified for: " + file + ".");
			}
			return canvas.getRandom(width, height).toBufferedImage();
		}
	}

	public File getFile() {
		return file;
	}
	
	public boolean isSelected() {
		return is_selected;
	}
	
    public void paint(Graphics g) {
    	super.paint(g);
    	if (selectable) {
			g.fillOval(6, 6, 13, 13);
			if (is_selected) {
				g.setColor(SELECTED);
			}
			else {
				g.setColor(UNSELECTED);
			}
			g.fillOval(7, 7, 9, 9);
    	}
	}
    
	@Override
	public void actionPerformed(ActionEvent e) {
		is_selected = !is_selected;
		super.actionPerformed(e);
		redraw();
	}
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			File file = new File("data/flag.jpg");
			Canvas flag = new Canvas(file);
			KButton north = new ThumbnailButton(flag.getRandom(50, 50).toBufferedImage(), null);
			KButton center= new ThumbnailButton(flag.getRandom(50, 50).toBufferedImage(), null);
			
			KPanel panel = new KPanel("Test", north, center, null, null, null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
