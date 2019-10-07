package kaflib.gui.composite;

import kaflib.gui.components.KFrame;
import kaflib.gui.components.ScaledImageComponent;
import kaflib.gui.components.ThumbnailButton;
import kaflib.gui.components.ThumbnailListener;
import kaflib.types.Directory;
import kaflib.utils.GUIUtils;

public class ImageBrowser extends KFrame implements ThumbnailListener {
	
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
		setContent(panel);
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

	@Override
	public void selection(final ThumbnailButton source) {
		try {
			image.set(source.getFile());
			panel.redraw();
		}
		catch (Exception e) {
			System.err.println("Could not load file: " + source.getFile() + ".");
		}		
	}




	
}
