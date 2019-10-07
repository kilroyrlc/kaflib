package kaflib.gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import kaflib.graphics.Canvas;
import kaflib.graphics.GraphicsUtils;
import kaflib.graphics.ThumbnailFinder;
import kaflib.types.Box;
import kaflib.types.Directory;
import kaflib.types.Pair;
import kaflib.utils.FileUtils;

public class ThumbnailButton extends KButton implements ActionListener {

	private static final long serialVersionUID = 8935605236358068028L;
	private static final Color SELECTED = Color.BLUE;
	private static final Color UNSELECTED = Color.GRAY;
	private boolean selectable;
	private boolean is_selected;
	private final ThumbnailListener listener;
	private File file;
	
	public enum CropMode {
		CENTER,
		RANDOM,
		CROP_ALGORITHM
	}

	public ThumbnailButton(final File file,
						   final CropMode cropMode, 
						   final Integer width,
						   final Integer height,
						   final ThumbnailListener listener) throws Exception {
		this(file, getCropped(file, width, height, cropMode), width, height, listener);
	}
	
	public ThumbnailButton(final File file,
						   final Box box, 
			   			   final ThumbnailListener listener) throws Exception {
		this(file, getImage(file, box, box.getWidth(), box.getHeight()), listener);
	}

	public ThumbnailButton(final File file,
						   final BufferedImage thumbnail, 
						   final ThumbnailListener listener) throws Exception {
		this(file, thumbnail, null, null, listener);
	}
	
	public ThumbnailButton(final File file,
						   final Box box, 
						   final Integer width,
						   final Integer height,
						   final ThumbnailListener listener) throws Exception {
		this(file, getImage(file, box, width, height), width, height, listener);
	}
	
	public ThumbnailButton(final BufferedImage image, 
						   final ThumbnailListener listener) throws Exception {
		this(null, image, null, null, listener);
	}
	
	public ThumbnailButton(final File file,
						   final BufferedImage image, 
						   final Integer width,
						   final Integer height,
						   final ThumbnailListener listener) throws Exception {
		super(listener != null);
		this.listener = listener;
		this.file = file;
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
		super.actionPerformed(e);
		is_selected = !is_selected;
		redraw();
		if (listener != null) {
			listener.selection(this);
		}
	}

	public static List<ThumbnailButton> getButtons(final Directory directory,
												  final int width,
												  final int height,
												  final boolean selectable,
												  final ThumbnailListener listener) throws Exception {
		return getButtons(directory.listImages(), CropMode.CENTER, width, height, selectable, listener);
	}
	
	public static List<ThumbnailButton> getButtons(final Collection<File> files,
			  final CropMode cropMode,
			  final int width,
			  final int height,
			  final boolean selectable,
			  final ThumbnailListener listener) throws Exception {
		List<ThumbnailButton> buttons = new ArrayList<ThumbnailButton>(files.size());
		for (File file : files) {
			if (!FileUtils.isGraphicsFile(file)) {
				continue;
			}
			ThumbnailButton button = new ThumbnailButton(file, 
														cropMode,
														width, 
														height, 
														listener);
			button.setSelectable(selectable);
			buttons.add(button);
		}
		return buttons;
	}

	public static List<ThumbnailButton> getButtonsFromBoxes(final Collection<Pair<File, Box>> files,
												  final CropMode cropMode,
												  final int width,
												  final int height,
												  final boolean selectable,
												  final ThumbnailListener listener) throws Exception {
		List<ThumbnailButton> buttons = new ArrayList<ThumbnailButton>(files.size());
		for (Pair<File, Box> file : files) {
			if (!FileUtils.isGraphicsFile(file.getFirst())) {
				continue;
			}
			ThumbnailButton button;
			if (file.getSecond() != null) {
				button = new ThumbnailButton(file.getFirst(), 
											file.getSecond(), 
											width, 
											height, 
											listener);
			}
			else {
				button = new ThumbnailButton(file.getFirst(), 
											cropMode, 
											width, 
											height, 
											listener);
			}
			button.setSelectable(selectable);
			buttons.add(button);
		}
		return buttons;
	}

	public static List<ThumbnailButton> getButtons(final Collection<Pair<File, BufferedImage>> files,
												  final int width,
												  final int height,
												  final boolean selectable,
												  final ThumbnailListener listener) throws Exception {
		List<ThumbnailButton> buttons = new ArrayList<ThumbnailButton>(files.size());
		for (Pair<File, BufferedImage> file : files) {
			ThumbnailButton button = new ThumbnailButton(file.getFirst(), 
														 file.getSecond(), 
														 width, 
														 height, 
														 listener);
			button.setSelectable(selectable);
			buttons.add(button);
		}
		return buttons;
	}
	
	private static BufferedImage getCropped(final File file,
											final int width,
											final int height,
											final CropMode cropMode) throws Exception {
		CropMode mode = cropMode;
		if (mode == null) {
			mode = CropMode.CENTER;
		}
		Canvas canvas = new Canvas(file);
		switch (mode) {
			case CENTER:
				return canvas.get(new Box(canvas.getCenter(), width, height)).toBufferedImage();
			case RANDOM:
				return canvas.getRandom(width, height).toBufferedImage();
			case CROP_ALGORITHM:
				return canvas.get(ThumbnailFinder.getThumbnail(canvas, 5)).toBufferedImage();
			default: 
				throw new Exception("Unrecognized thumbnail algorithm: " + cropMode + ".");
		}
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
