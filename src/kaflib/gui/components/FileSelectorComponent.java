package kaflib.gui.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JComboBox;

import kaflib.types.FilenameExtensionFilter;
import kaflib.types.FilenamePrefixFilter;

/**
 * Defines a dropdown file selector
 */
public class FileSelectorComponent extends JComboBox<String> implements ItemListener {

	private static final long serialVersionUID = 1L;
	private static final String NONE_TEXT = "";
	private static final String CREATE_TEXT = "<Create>";
	private String last;
	private boolean none;
	private boolean create;
	private File directory;
	private FilenameFilter filter;
	private FileSelectorListener listener;
	
	
	/**
	 * Creates the component initialized to the specified directory and 
	 * extensions.
	 * @param directory
	 * @param extensions
	 * @throws Exception
	 */
	public FileSelectorComponent(final boolean empty,
								 final boolean create,
								 final File directory, 
								 final String... extensions) throws Exception {
		super();
		this.directory = directory;
		this.none = empty;
		this.create = create;
		this.listener = null;
		this.last = null;
		if (!directory.exists() || !directory.isDirectory()) {
			throw new Exception("Must specify a directory.");
		}
		if (extensions.length > 0) {
			filter = new FilenameExtensionFilter(extensions);
		}
		else {
			filter = null;
		}
		this.addItemListener(this);
		refresh();
	}

	public FileSelectorComponent(final boolean empty,
			final boolean create,
			final File directory, 
			final String prefix) throws Exception {
		super();
		this.directory = directory;
		this.none = empty;
		this.create = create;
		this.listener = null;
		this.last = null;
		if (!directory.exists() || !directory.isDirectory()) {
			throw new Exception("Must specify a directory.");
		}
		if (prefix != null) {
			filter = new FilenamePrefixFilter(prefix);
		}
		else {
			filter = null;
		}
		this.addItemListener(this);
		refresh();
	}
	
	/**
	 * Returns the selected file or null if create.
	 * @return
	 */
	public File getSelected() {
		String selected = (String) getSelectedItem();
		if (selected.equals(CREATE_TEXT) || selected.equals(NONE_TEXT)) {
			return null;
		}
		
		return new File(directory, selected);
	}
	
	public void setListener(final FileSelectorListener listener) throws Exception {
		if (this.listener != null) {
			throw new Exception("Listener already specified.");
		}
		this.listener = listener;
	}
	
	/**
	 * Refreshes the file list.
	 */
	public final void refresh() {
		File files[];
		if (filter != null) {
			files = directory.listFiles(filter);
		}
		else {
			files = directory.listFiles();
		}
		removeAllItems();
		if (none) {
			addItem(NONE_TEXT);
		}
		for (File file : files) {
			addItem(file.getName());
		}
		if (create) {
			addItem(CREATE_TEXT);
		}
	}
	
	public void add(final File file) {
		add(file.getName());
	}
	
	public void add(final String name) {
		addItem(name);
	}
	
	public void setSelected(final String name) {
		setSelectedItem(name);
	}
	
	public void setSelected(final File file) {
		setSelected(file.getName());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (listener == null || 
			e.getStateChange() == ItemEvent.DESELECTED) { 
			return;
		}
		String selected = (String) getSelectedItem();
		if (selected.equals(last)) {
			return;
		}
		else if (selected.equals(CREATE_TEXT)) {
			listener.createSelected();
		}
		else if (selected.equals(NONE_TEXT)) {
			listener.noneSelected();
		}
		else {
			listener.fileSelected(getSelected());
		}
	}
	
}
