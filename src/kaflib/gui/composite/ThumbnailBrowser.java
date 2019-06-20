package kaflib.gui.composite;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import kaflib.gui.components.KButton;
import kaflib.gui.components.KFrame;
import kaflib.gui.components.KIconButton;
import kaflib.gui.components.KListener;
import kaflib.gui.components.KPanel;
import kaflib.gui.components.StaticImageComponent;
import kaflib.types.Box;
import kaflib.types.Directory;
import kaflib.types.Pair;
import kaflib.utils.GUIUtils;
import kaflib.utils.StringUtils;

public class ThumbnailBrowser extends KPanel implements NavigationPanelListener {

	private static final long serialVersionUID = 2194816206553491579L;

	private final Directory directory;
	private List<File> files;
	private int width;
	private int rows;
	private int columns;
	private final Map<Integer, ThumbnailPanel> cache;
	private int pages;
	private final int thumbs_per_page;
	private final boolean selectable;
	private final ThumbnailPanelListener listener;
	
	private NavigationPanel nav_panel;
	private KPanel thumbnail_container;
	private ThumbnailPanel thumbnail_panel;
	private final KPanel east_panel;
	
	public ThumbnailBrowser(final Directory directory, 
							final int width,
							final int rows,
							final int columns,
							final boolean selectable,
							final ThumbnailPanelListener listener) throws Exception {
		super();
		cache = new HashMap<Integer, ThumbnailPanel>();
		this.directory = directory;
		this.selectable = selectable;
		this.listener = listener;
		this.width = width;
		this.rows = rows;
		this.columns = columns;

		thumbs_per_page = rows * columns;
		setLayout(new BorderLayout());

		east_panel = new KPanel();
		add(east_panel, BorderLayout.EAST);
		
		nav_panel = new NavigationPanel(1, 1, 1, this);
		add(nav_panel, BorderLayout.NORTH);
		updateFiles();
	
		thumbnail_container = new KPanel();
		add(thumbnail_container, BorderLayout.CENTER);
		updatePanel();
	}
	
	private final void updatePanel() throws Exception {
		thumbnail_container.removeAll();
		int page = nav_panel.getCurrent() - 1;
		if (cache.containsKey(page)) {
			thumbnail_container.add(cache.get(page));
		}
		else {
			List<Pair<File, Box>> page_files = new ArrayList<Pair<File, Box>>();
			int end = Math.min(files.size(), (page + 1) * thumbs_per_page);
			for (int i = page * thumbs_per_page; i < end; i++) {
				page_files.add(new Pair<File, Box>(files.get(i), null));
			}
			
			thumbnail_panel = new ThumbnailPanel(page_files, width, width, rows, columns, selectable, listener);
			
			cache.put(page, thumbnail_panel);
		}
		thumbnail_container.add(thumbnail_panel);
		thumbnail_container.redraw();
	}
	
	private final void updateFiles() throws Exception {
		files = new ArrayList<File>();
		files.addAll(directory.listImages());
		Collections.sort(files);
		pages = files.size() / thumbs_per_page;
		if (files.size() % thumbs_per_page > 0) {
			pages++;
		}
		cache.clear();
		nav_panel.update(1, pages, 1);
	}

	public KPanel getEastPanel() {
		return east_panel;
	}
	
	public Set<File> getSelected() {
		Set<File> files = new HashSet<File>();
		for (ThumbnailPanel panel : cache.values()) {
			files.addAll(panel.getSelected());
		}
		return files;
	}
	
	@Override
	public void navigationRequested(int index) {
		try {
			updatePanel();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveRequested(int index) {
	}

	@Override
	public void deleteRequested(int index) {
	}

	@Override
	public void refreshRequested(int index) {
	}

	@Override
	public void customFunctionRequest(Component component, int index) {
	}
	
	public static void main(String args[]) {
		try {
			KFrame frame = new KFrame();
			Directory directory = new Directory(GUIUtils.chooseDirectory(frame));
			
			final ThumbnailBrowser panel = new ThumbnailBrowser(directory, 
														  120, 
														  3, 
														  4, 
														  false,
														  null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	
}
