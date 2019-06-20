package kaflib.gui.composite;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import kaflib.gui.components.KButton;
import kaflib.gui.components.KFrame;
import kaflib.gui.components.KIconButton;
import kaflib.gui.components.KLabel;
import kaflib.gui.components.KListener;
import kaflib.gui.components.KPanel;
import kaflib.gui.components.KTextField;
import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a linear, numeric navigation panel.
 */
public class NavigationPanel extends KPanel implements KListener {

	private static final long serialVersionUID = 349839633979991537L;
	
	private NavigationPanelListener listener;
	
	private KButton first_button;
	private KButton back_button;
	private KButton refresh_button;
	private KButton save_button;
	private KButton delete_button;
	private KButton save_next_button;
	private KButton next_button;
	private KButton last_button;
	private KTextField direct;
	private KLabel label;
	
	private int current;
	private int min;
	private int max;

	public NavigationPanel(final int min,
						   final int max,
						   final int current,
						   final NavigationPanelListener listener,
						   final KButton... custom) throws Exception {
		this(min, max, current, true, true, true, false, false, false, true, true, true, listener, custom);
	}
	
	public NavigationPanel(final int min,
			   final int max,
			   final int current,
			   final boolean first,
			   final boolean back,
			   final boolean refresh,
			   final boolean next,
			   final boolean last,
			   final boolean direct,
			   final NavigationPanelListener listener,
			   final KButton... custom) throws Exception {
		this(min, max, current, first, back, refresh, false, false, false, next, last, direct, listener, custom);
	}
	
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
	public NavigationPanel(final int min,
						   final int max,
						   final int current,
						   final boolean first,
						   final boolean back,
						   final boolean refresh,
						   final boolean save,
						   final boolean delete,
						   final boolean saveNext,
						   final boolean next,
						   final boolean last,
						   final boolean direct,
						   final NavigationPanelListener listener,
						   final KButton... custom) throws Exception {
		super();
		setLayout(new FlowLayout());
		KPanel temp;
		this.listener = listener;
		
		if (first || back) {
			temp = new KPanel("");
			temp.setLayout(new FlowLayout());
			if (first) {
				first_button = new KIconButton(KIconButton.IconType.START, this, true);
				temp.add(first_button);
			}
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
		
		if (saveNext || next || last) {
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
			if (last) {
				last_button = new KIconButton(KIconButton.IconType.END, this, true);
				temp.add(last_button);
			}
			add(temp);
		}
		
		if (direct) {
			temp = new KPanel("");
			temp.setLayout(new FlowLayout());

			this.direct = new KTextField(6, false, this);
			temp.add(this.direct);

			this.label = new KLabel("of xxx,xxx");
			temp.add(this.label);
			
			add(temp);
		}
		
		update(min, max, current);
	}

	public int getCurrent() {
		return current;
	}
	
	/**
	 * Update the min/max values.
	 * @param min
	 * @param max
	 * @param current
	 * @throws Exception
	 */
	public void update(final int min, final int max, final int current) throws Exception {
		CheckUtils.checkRange(current, min, max);
		this.min = min;
		this.max = max;
		if (current > max) {
			this.current = max;
		}
		else if (current < min) {
			this.current = min;
		}
		else {
			this.current = current;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (direct != null) {
					label.setText("of " + StringUtils.commatize(max));
				}
			}
		});
		reload();
	}
	
	/**
	 * Reloads the index field if shown.
	 */
	private void reload() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (first_button != null) {
					first_button.setEnabled(current != min);
				}
				if (back_button != null) {
					back_button.setEnabled(current != min);
				}
				if (save_next_button != null) {
					save_next_button.setEnabled(current != max);
				}
				if (next_button != null) {
					next_button.setEnabled(current != max);
				}
				if (last_button != null) {
					last_button.setEnabled(current != max);
				}
				if (direct != null) {
					direct.setText("" + current);
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
		if (component == first_button) {
			current = min;
			reload();
			listener.navigationRequested(current);
		}
		else if (component == back_button) {
			current = Math.max(min, current - 1);
			reload();
			listener.navigationRequested(current);
		}
		else if (component == refresh_button) {
			reload();
			listener.navigationRequested(current);
		}
		else if (component == save_button) {
			reload();
			listener.saveRequested(current);
		}
		else if (component == delete_button) {
			reload();
			listener.deleteRequested(current);
		}
		else if (component == save_next_button) {			
			listener.saveRequested(current);
			current = Math.min(max, current + 1);
			reload();
			listener.navigationRequested(current);
		}
		else if (component == next_button) {
			current = Math.min(max, current + 1);
			reload();
			listener.navigationRequested(current);
		}
		else if (component == last_button) {
			current = max;
			reload();
			listener.navigationRequested(current);
		}
		else {
			reload();
			listener.customFunctionRequest(component, current);
		}
		setEnabled(true);
	}
	
	
	public static void main(String args[]) {
		try {
			NavigationPanel panel = new NavigationPanel(0, 69, 42,
														new NavigationPanelListener(){
															@Override
															public void navigationRequested(int index) {
																System.out.println("Nav requested: " + index);
															}

															@Override
															public void saveRequested(int index) {
																System.out.println("Save requested: " + index);
															}

															@Override
															public void deleteRequested(int index) {
																System.out.println("Delete requested: " + index);
															}

															@Override
															public void refreshRequested(int index) {
																System.out.println("Refresh requested: " + index);
															}

															@Override
															public void customFunctionRequest(Component component,
																	int index) {
																System.out.println("Custom requested: " + index);
															}
														});
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
