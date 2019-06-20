package kaflib.gui.composite;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import kaflib.graphics.Canvas;
import kaflib.graphics.ThumbnailFinder;
import kaflib.gui.components.KFrame;
import kaflib.gui.components.KListener;
import kaflib.gui.components.KPanel;
import kaflib.gui.components.ThumbnailButton;
import kaflib.types.Box;
import kaflib.types.Directory;
import kaflib.types.Pair;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;

public class ThumbnailPanel extends KPanel implements KListener {

	private static final long serialVersionUID = 7551052082209772432L;

	private ThumbnailButton images[][];
	private final ThumbnailPanelListener listener;
	
	public ThumbnailPanel(final Collection<Pair<File, Box>> files,
						  final int thumbWidth,
						  final int thumbHeight,
						  final int rows,
						  final int columns,
						  final boolean selectable,
						  final ThumbnailPanelListener listener) throws Exception {
		super(null, rows, columns);
		images = new ThumbnailButton[columns][rows];
		this.listener = listener;
		
		int index = 0;
		for (Pair<File, Box> file : files) {
			if (!FileUtils.isGraphicsFile(file.getFirst())) {
				continue;
			}
			int i = index % columns;
			int j = index / columns;
			if (listener != null) {
				images[i][j] = new ThumbnailButton(file.getFirst(), file.getSecond(), thumbWidth, thumbHeight, this);
				images[i][j].setSelectable(selectable);
			}
			else {
				images[i][j] = new ThumbnailButton(file.getFirst(), file.getSecond(), thumbWidth, thumbHeight, null);
				images[i][j].setSelectable(selectable);
			}
			add(images[i][j]);
			index++;
		}
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
	
	@Override
	public void serialValueChanged(Component component) {
	}

	@Override
	public void asyncValueChanged(Component component) {
		for (int i = 0; i < images.length; i++) {
			for (int j = 0; j < images[i].length; j++) {
				if (component == images[i][j]) {
					listener.selected(images[i][j].getFile());
					return;
				}
			}
		}
		System.err.println("Could not find thumbnail for source of action.");
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
			
			ThumbnailPanel panel = new ThumbnailPanel(files, 90, 90, 3, 4, true, null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
