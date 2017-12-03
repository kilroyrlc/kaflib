package kaflib.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Defines a scrolled list type.
 * @param <T>
 */
public class AddRemoveList<T> extends JPanel implements ActionListener,
														ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private final ScrolledList<T> list;
	private final JButton add;
	private final JButton remove;
	private AddRemoveListListener<T> listener;
	
	public AddRemoveList(final String label,
						 final int visibleRows) {
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
					     final Collection<T> values) {
		super(new BorderLayout());
		list = new ScrolledList<T>(visibleRows, values);
		add(list, BorderLayout.CENTER);

		JPanel top;
		if (label != null) {
			top = new JPanel(new GridLayout(2, 1));
			top.add(new JLabel(label));
		}
		else {
			top = new JPanel(new GridLayout(1, 1));
		}
		
		JPanel panel = new JPanel(new FlowLayout());
		add = new JButton("+");
		add.addActionListener(this);
		panel.add(add);
		remove = new JButton("-");
		remove.addActionListener(this);
		panel.add(remove);
		top.add(panel);
		
		list.addSelectionListener(this);
		
		add(top, BorderLayout.NORTH);
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
	
	public void add(final Collection<T> values) {
		list.add(values);
	}
	
	public void clear() {
		list.clear();
	}
	
	public void add(final T value) {
		list.add(value);
	}
	
	public void remove(final T value) {
		list.remove(value);
	}
	
	public void removeDuplicates() {
		list.removeDuplicates();
	}
	
	public T removeSelected() {
		return list.removeSelected();
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
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(add)) {
			if (listener != null) {
				T value = listener.addPressed();
				if (value != null) {
					list.add(value);
				}
			}
		}
		else if (e.getSource().equals(remove)) {
			removeSelected();
		}
		else {
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (listener != null) {
			listener.itemSelected(list.getSelected());
		}
	}
}
