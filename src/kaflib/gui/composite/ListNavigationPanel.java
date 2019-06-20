package kaflib.gui.composite;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import kaflib.gui.components.KButton;
import kaflib.gui.components.KFrame;
import kaflib.gui.components.KIconButton;
import kaflib.gui.components.KLabel;
import kaflib.gui.components.KListener;
import kaflib.gui.components.KPanel;
import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a linear, numeric navigation panel.
 */
public class ListNavigationPanel<T> extends KPanel implements KListener {

	private static final long serialVersionUID = 349839633979991537L;
	
	private ListNavigationPanelListener<T> listener;
	
	private KLabel label;
	private KButton back_button;
	private KButton refresh_button;
	private KButton save_button;
	private KButton delete_button;
	private KButton save_next_button;
	private KButton next_button;	
	private int field_length;

	
	private final List<T> values;
	private int index;
	
	/**
	 * Create the panel with the many different optional buttons.
	 * @param min
	 * @param max
	 * @param current
	 * @param first
	 * @param back
	 * @param refresh
	 * @param save
	 * @param delete
	 * @param saveNext
	 * @param next
	 * @param last
	 * @param direct
	 * @param listener
	 * @param custom
	 * @throws Exception
	 */
	public ListNavigationPanel(final List<T> values,
						   final boolean back,
						   final boolean refresh,
						   final boolean save,
						   final boolean delete,
						   final boolean saveNext,
						   final boolean next,
						   final boolean label,
						   final ListNavigationPanelListener<T> listener,
						   final KButton... custom) throws Exception {
		super();
		setLayout(new FlowLayout());
		KPanel temp;
		this.listener = listener;
		this.values = values;
		
		if (back) {
			temp = new KPanel("");
			temp.setLayout(new FlowLayout());
			if (back) {
				back_button = new KIconButton(KIconButton.IconType.LAST, this, true);
				temp.add(back_button);
			}
			add(temp);
		}

		if (refresh || save || delete || custom.length > 0) {
			temp = new KPanel("");
			temp.setLayout(new FlowLayout());
			if (refresh) {
				refresh_button = new KIconButton(KIconButton.IconType.REFRESH, this, true);
				temp.add(refresh_button);
			}
			if (delete) {
				delete_button = new KIconButton(KIconButton.IconType.TRASH, this, true);
				temp.add(delete_button);
			}
			for (KButton button : custom) {
				temp.add(button);
			}
			if (save) {
				save_button = new KIconButton(KIconButton.IconType.DISK, this, true);
				temp.add(save_button);
			}
			add(temp);
		}
		
		if (saveNext || next) {
			temp = new KPanel("");
			temp.setLayout(new FlowLayout());
			if (saveNext) {
				save_next_button = new KIconButton(KIconButton.IconType.SAVE_NEXT, this, true);
				temp.add(save_next_button);
			}
			if (next) {
				next_button = new KIconButton(KIconButton.IconType.NEXT, this, true);
				temp.add(next_button);
			}
			add(temp);
		}
		
		if (label) {
			String longest = new String();
			for (T value : values) {
				if (value.toString().length() > longest.length()) {
					longest = value.toString();
				}
			}
			
			temp = new KPanel("");
			this.label = new KLabel(longest, true);
			field_length = longest.length();
			temp.add(this.label);
			add(temp);
		}
		
		update(0);
	}

	private void update(final int index) throws Exception {
		CheckUtils.checkWithin(index, values);
		this.index = index;
		reload();
	}
	
	/**
	 * Reloads the index field if shown.
	 */
	private void reload() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (label != null) {
					try {
						label.setText(StringUtils.resize(values.get(index).toString(), field_length));
					}
					catch (Exception e) {
						System.err.println("Unabled to resize string: " + values.get(index).toString() + ".");
					}
				}
				if (back_button != null) {
					back_button.setEnabled(index != 0);
				}
				if (next_button != null) {
					next_button.setEnabled(index != values.size() - 1);
				}
				if (save_next_button != null) {
					save_next_button.setEnabled(index != values.size() - 1);
				}
			}
		});
	}

	@Override
	public void serialValueChanged(Component component) {
		setEnabled(false);
	}

	@Override
	public void asyncValueChanged(Component component) {
		if (component == back_button) {
			index = Math.max(0, index - 1);
			reload();
			listener.navigationRequested(values.get(index));
		}
		else if (component == refresh_button) {
			reload();
			listener.navigationRequested(values.get(index));
		}
		else if (component == save_button) {
			reload();
			listener.saveRequested(values.get(index));
		}
		else if (component == delete_button) {
			reload();
			listener.deleteRequested(values.get(index));
		}
		else if (component == save_next_button) {
			listener.saveRequested(values.get(index));
			index = Math.min(values.size() - 1, index + 1);
			reload();
			listener.navigationRequested(values.get(index));
		}
		else if (component == next_button) {
			index = Math.min(values.size() - 1, index + 1);
			reload();
			listener.navigationRequested(values.get(index));
		}
		else {
			reload();
			listener.customFunctionRequest(component, values.get(index));
		}
		setEnabled(true);
	}
	
	
	public static void main(String args[]) {
		try {
			List<String> list = new ArrayList<String>();
			list.add("Psycho");
			list.add("Bullymong");
			list.add("Bandit");
			list.add("Skagg");
			list.add("ROUS");
			list.add("Invisbl Asshole");
			
			ListNavigationPanel<String> panel = new ListNavigationPanel<String>(list,
														true,
														true,
														true,
														true,
														true,
														true,
														true,
														new ListNavigationPanelListener<String>(){
															@Override
															public void navigationRequested(final String value) {
																System.out.println("Nav requested: " + value);
															}

															@Override
															public void saveRequested(final String value) {
																System.out.println("Save requested: " + value);
															}

															@Override
															public void deleteRequested(final String value) {
																System.out.println("Delete requested: " + value);
															}

															@Override
															public void refreshRequested(final String value) {
																System.out.println("Refresh requested: " + value);
															}

															@Override
															public void customFunctionRequest(Component component,
																	final String value) {
																System.out.println("Custom requested: " + value);
															}
														});
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
