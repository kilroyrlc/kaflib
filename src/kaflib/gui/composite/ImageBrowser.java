package kaflib.gui.composite;

import java.io.File;

import kaflib.gui.components.KFrame;
import kaflib.gui.components.ScaledImageComponent;
import kaflib.types.Directory;
import kaflib.utils.GUIUtils;

public class ImageBrowser extends KFrame implements ThumbnailPanelListener {
	
	private static final long serialVersionUID = 2331238816577899177L;
	private final ThumbnailBrowser panel;
	private final ScaledImageComponent image;
	
	public ImageBrowser(final Directory directory, 
						final int width,
						final int rows,
						final int columns) throws Exception {
		super();
		panel = new ThumbnailBrowser(directory, width, rows, columns, false, this);
		image = new ScaledImageComponent(800, 600);
		panel.getEastPanel().add(image);
		setContent(panel);
	}

	@Override
	public void selected(File file) {
		try {
			image.set(file);
			panel.redraw();
		}
		catch (Exception e) {
			System.err.println("Could not load file: " + file + ".");
		}
	}
	
	public static void main(String args[]) {
		try {
			KFrame frame = new KFrame();
			Directory directory = new Directory(GUIUtils.chooseDirectory(frame));
			frame = new ImageBrowser(directory, 120, 3, 4);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}




	
}
