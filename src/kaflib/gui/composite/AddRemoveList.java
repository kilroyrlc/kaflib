package kaflib.gui.composite;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import kaflib.gui.components.KIconButton;
import kaflib.gui.components.KListener;
import kaflib.gui.components.KSingleSelectList;

/**
 * Defines a scrolled list type with add and remove buttons.
 * @param <T>
 */
public class AddRemoveList<T> extends JPanel implements ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private final KSingleSelectList<T> list;
	private final KIconButton add;
	private final KIconButton remove;
	private AddRemoveListListener<T> listener;
	
	public AddRemoveList(final String label,
						 final int visibleRows) throws Exception {
		this(label, visibleRows, null);
	}
	
	/**
	 * Create the list with a specified number of visible rows and default 
	 * values.
	 * @param visibleRows
	 * @param values
	 */
	public AddRemoveList(final String label,
						 final int visibleRows, 
					     final Collection<T> values) throws Exception {
		super(new BorderLayout());
		list = new KSingleSelectList<T>(visibleRows, values);
		add(list, BorderLayout.CENTER);

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		
		if (label != null) {
			add(new JLabel(label), BorderLayout.NORTH);
		}
		
		add = new KIconButton(KIconButton.IconType.ADD, true, new KListener(){
			@Override
			public void serialValueChanged(Component component) {
			}
			@Override
			public void asyncValueChanged(Component component) {
				if (listener != null) {
					T value = listener.addPressed();
					if (value != null) {
						list.add(value);
					}
				}
				add.setEnabled(true);
			}});
		left.add(add);
		
		remove = new KIconButton(KIconButton.IconType.REMOVE, true, new KListener(){
			@Override
			public void serialValueChanged(Component component) {
			}
			@Override
			public void asyncValueChanged(Component component) {
				T item = removeSelected();
				if (listener != null) {
					listener.removePressed(item);
				}
				remove.setEnabled(true);
			}});
		
		left.add(remove);
		
		list.addSelectionListener(this);
		
		add(left, BorderLayout.WEST);
	}
	
	public void setListener(final AddRemoveListListener<T> listener) throws Exception {
		if (this.listener != null) {
			throw new Exception("Listener already specified.");
		}
		this.listener = listener;
	}
		
	public void setOperationsEnabled(final boolean enabled) {
		add.setEnabled(enabled);
		remove.setEnabled(enabled);
	}
	
	public List<T> get() {
		return list.get();
	}
	
	public T pop() {
		List<T> values = list.get();
		if (values.size() == 0) {
			return null;
		}
		T item = values.get(0);
		remove(item);
		return item;
	}
	
	protected void redraw() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				invalidate();
				repaint();
				revalidate();
			}
		});
	}
	
	public void add(final Collection<T> values) {
		list.add(values);
		redraw();
	}
	
	public void clear() {
		list.clear();
		redraw();
	}
	
	public void add(final T value) {
		list.add(value);
		redraw();
	}
	
	public void remove(final T value) {
		list.remove(value);
		redraw();
	}
	
	public T removeSelected() {
		return list.removeSelectedItem();
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
			AddRemoveList<String> list = new AddRemoveList<String>("List:", 6, values);
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
	public void valueChanged(ListSelectionEvent e) {
		if (listener != null) {
			listener.itemSelected(list.getSelectedItem());
		}
	}
}
