package kaflib.gui.components;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

/**
 * Defines a scrolled list supertype.
 * @param <T>
 */
public abstract class KList<T> extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JScrollPane scroll;
	protected final JList<T> list;
	protected final DefaultListModel<T> model;
	
	public KList(final int visibleRows) {
		this(visibleRows, null);
	}
	
	/**
	 * Create the list with a specified number of visible rows and default 
	 * values.
	 * @param visibleRows
	 * @param values
	 */
	public KList(final int visibleRows, 
					    final Collection<T> values) {
		super(new GridLayout(1, 1));
		scroll = new JScrollPane();
		
		model = new DefaultListModel<T>();
		
		if (values != null) {
			for (T value : values) {
				model.addElement(value);
			}
		}
		
		list = new JList<T>(model);
		scroll.setViewportView(list);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(visibleRows);
		
		add(scroll);
	}

	
	public void addSelectionListener(final ListSelectionListener listener) {
		list.addListSelectionListener(listener);
	}
	
	public List<T> get() {
		List<T> values = new ArrayList<T>();
		for (int i = 0; i < model.getSize(); i++) {
			values.add(model.get(i));
		}
		return values;
	}
	
	public List<T> getSelected() {
		return list.getSelectedValuesList();
	}

	
	public void clear() {
		model.clear();
	}
	
	public void add(final Collection<T> values) {
		for (T element : values) {
			model.addElement(element);
		}
	}
	
	public void add(final T value) {
		model.addElement(value);
	}
	
	public void remove(final T value) {
		model.removeElement(value);
	}
	
	public abstract void removeSelected();
	
}
