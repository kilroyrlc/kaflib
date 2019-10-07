package kaflib.gui.composite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import kaflib.graphics.Canvas;
import kaflib.graphics.ThumbnailFinder;
import kaflib.gui.components.KFrame;
import kaflib.gui.components.KPanel;
import kaflib.gui.components.ThumbnailButton;
import kaflib.types.Box;
import kaflib.types.Directory;
import kaflib.types.Pair;
import kaflib.utils.GUIUtils;

/**
 * Defines a single panel of thumbnails.
 */
public class ThumbnailPanel extends KPanel {

	private static final long serialVersionUID = 7551052082209772432L;

	private ThumbnailButton images[][];
	
	public ThumbnailPanel(final Collection<ThumbnailButton> buttons,
						  final int rows,
						  final int columns) throws Exception {
		super(null, rows, columns);
		images = new ThumbnailButton[columns][rows];
		int index = 0;
		for (ThumbnailButton button : buttons) {
			int i = index % columns;
			int j = index / columns;
			images[i][j] = button;
			add(button);
			index++;
		}
	}

	public void disable() {
		for (int i = 0; i < images.length; i++) {
			for (int j = 0; j < images[i].length; j++) {
				if (images[i][j] != null) {
					images[i][j].setEnabled(false);
				}
			}
		}
	}
	
	public List<File> get() {
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < images.length; i++) {
			for (int j = 0; j < images[i].length; j++) {
				if (images[i][j] != null) {
					files.add(images[i][j].getFile());
				}
			}
		}
		return files;
	}
	
	
	public List<File> getSelected() {
		List<File> selected = new ArrayList<File>();
		for (int i = 0; i < images.length; i++) {
			for (int j = 0; j < images[i].length; j++) {
				if (images[i][j] != null && images[i][j].isSelected()) {
					selected.add(images[i][j].getFile());
				}
			}
		}
		return selected;
	}

	/**
	 * Returns all files not selected.
	 * @return
	 */
	public List<File> getUnselected() {
		List<File> unselected = new ArrayList<File>();
		for (int i = 0; i < images.length; i++) {
			for (int j = 0; j < images[i].length; j++) {
				if (images[i][j] != null && !images[i][j].isSelected()) {
					unselected.add(images[i][j].getFile());
				}
			}
		}
		return unselected;
	}
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			KFrame frame = new KFrame();
			Directory directory = new Directory(GUIUtils.chooseDirectory(frame));
			List<Pair<File, Box>> files = new ArrayList<Pair<File, Box>>();
			System.out.println("Building thumbnails.");
			for (File file : directory.listImages()) {
				Box box = ThumbnailFinder.getThumbnail(new Canvas(file), 5);
				files.add(new Pair<File, Box>(file, box));
				if (files.size() >= 11) {
					break;
				}
			}
			System.out.println("Built thumbnails.");
			
			ThumbnailPanel panel = new ThumbnailPanel(ThumbnailButton.getButtons(directory, 120, 120, true, null), 
													  3, 4);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
