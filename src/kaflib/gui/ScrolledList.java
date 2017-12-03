package kaflib.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * Defines a scrolled list type.
 * @param <T>
 */
public class ScrolledList<T> extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JScrollPane scroll;
	private final JList<T> list;
	private final DefaultListModel<T> model;
	
	public ScrolledList(final int visibleRows) {
		this(visibleRows, null);
	}
	
	/**
	 * Create the list with a specified number of visible rows and default 
	 * values.
	 * @param visibleRows
	 * @param values
	 */
	public ScrolledList(final int visibleRows, 
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
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
	
	public T getSelected() {
		return list.getSelectedValue();
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
	
	public void removeDuplicates() {
		for (int i = model.size() - 1; i > 0; i--) {
			for (int j = i - 1; j >= 0; j--) {
				if (model.getElementAt(i).equals(model.getElementAt(j))) {
					model.remove(i);
				}
				break;
			}
		}
	}
	
	public T removeSelected() {
		T selected = getSelected();
		model.removeElement(getSelected());
		return selected;
	}
	
	/**
	 * Simple driver.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			List<String> values = new ArrayList<String>();
			values.add("Starting a");
			values.add("Starting b");
			values.add("Starting c");
			values.add("Starting d");
			ScrolledList<String> list = new ScrolledList<String>(6, values);
			frame.setContentPane(list);
			frame.pack();
			frame.setVisible(true);
			
			for (int i = 0; i < 5; i++) {
				System.out.println("Added: " + i + ".");
				list.add(new String("Element " + i));
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
