package kaflib.gui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.ListSelectionModel;

/**
 * Defines a scrolled list that allows multiple selections.
 * @param <T>
 */
public class KMultiSelectList<T> extends KList<T> {

	private static final long serialVersionUID = 5286003970530943614L;

	public KMultiSelectList(int visibleRows) {
		super(visibleRows);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	public KMultiSelectList(final int visibleRows, final Collection<T> values) {
		super(visibleRows, values);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	public List<T> removeSelectedItems() {
		List<T> selected = getSelected();
		for (T t : selected) {
			model.removeElement(t);
		}
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
			KList<String> list = new KMultiSelectList<String>(6, values);
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

	@Override
	public void removeSelected() {
		removeSelectedItems();
	}

}
