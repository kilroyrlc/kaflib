package kaflib.gui.composite;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import kaflib.gui.components.KFrame;
import kaflib.gui.components.KPanel;
import kaflib.gui.components.ThumbnailButton;
import kaflib.gui.components.ThumbnailButton.CropMode;
import kaflib.gui.components.ThumbnailListener;
import kaflib.types.Directory;
import kaflib.types.MRUCache;
import kaflib.utils.GUIUtils;

/**
 * Defines a GUI to browse thumbnails.
 */
public class ThumbnailBrowser extends KPanel implements NavigationPanelListener {

	private static final long serialVersionUID = 2194816206553491579L;
	public static final int DEFAULT_CACHE_SIZE = 8;
	
	private final Directory directory;
	private final List<File> files;
	private Set<File> selected;
	
	private int width;
	private int rows;
	private int columns;
	private final MRUCache<Integer, ThumbnailPanel> cache;
	private int pages;
	private final int thumbs_per_page;
	private final boolean selectable;
	private int cache_size;
	
	private final ThumbnailListener listener;
	
	private NavigationPanel nav_panel;
	private KPanel thumbnail_container;
	private ThumbnailPanel thumbnail_panel;
	private final KPanel east_panel;
	
	public ThumbnailBrowser(final Directory directory, 
							final int width,
							final int rows,
							final int columns,
							final boolean selectable,
							final ThumbnailListener listener) throws Exception {
		super();
		cache_size = DEFAULT_CACHE_SIZE;
		cache = new MRUCache<Integer, ThumbnailPanel>(cache_size);
		this.directory = directory;
		this.selectable = selectable;
		this.listener = listener;
		this.width = width;
		this.rows = rows;
		this.columns = columns;
		this.cache_size = DEFAULT_CACHE_SIZE;
		files = new ArrayList<File>();
		
		selected = new HashSet<File>();

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
	
	/**
	 * Updates the shown panel based on the current page indicated in the 
	 * nav panel.
	 * @throws Exception
	 */
	protected final void updatePanel() throws Exception {
		if (thumbnail_panel != null) {
			selected.removeAll(thumbnail_panel.getUnselected());
			selected.addAll(thumbnail_panel.getSelected());
		}
		
		thumbnail_container.removeAll();
		int page = nav_panel.getCurrent() - 1;
		ThumbnailPanel panel = cache.lookup(page);
		
		if (panel != null) {
			thumbnail_container.add(panel);
			thumbnail_panel = panel;
		}
		else {
			
			thumbnail_panel = getPanel(page);
			
			cache.insert(page, thumbnail_panel);
		}
		thumbnail_container.add(thumbnail_panel);
		thumbnail_container.redraw();
	}
	
	protected List<File> getFiles(final int page) throws Exception {
		int end = Math.min(files.size(), (page + 1) * thumbs_per_page);
		return files.subList(page * thumbs_per_page, end);
	}
	
	protected ThumbnailPanel getCurrent() {
		return thumbnail_panel;
	}
	
	protected ThumbnailPanel getPanel(int page) throws Exception {
		return new ThumbnailPanel(ThumbnailButton.getButtons(getFiles(page), CropMode.CENTER, width, width, selectable, listener),
								  rows,
								  columns);
	}
	
	protected NavigationPanel getNavigationPanel() {
		return nav_panel;
	}
	
	protected Directory getDirectory() {
		return directory;
	}
	
	protected Collection<File> readDirectory() throws Exception {
		return directory.listImages();		
	}
	
	/**
	 * Refreshes the file list.
	 * @throws Exception
	 */
	protected void updateFiles() throws Exception {
		files.clear();
		files.addAll(readDirectory());
		Collections.sort(files);
		pages = files.size() / thumbs_per_page;
		if (files.size() % thumbs_per_page > 0) {
			pages++;
		}
		if (pages == 0) {
			pages = 1;
		}
		cache.clear();
		nav_panel.update(1, pages, 1);
	}

	/**
	 * Returns all thumbnails with a selected state across all cached panels.
	 * @return
	 */
	public Set<File> getSelected() {
		Set<File> files = new HashSet<File>();
		files.addAll(selected);
		files.addAll(thumbnail_panel.getSelected());
		return files;
	}
	
	public List<File> get() {
		return thumbnail_panel.get();
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
														  true,
														  null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	
}
