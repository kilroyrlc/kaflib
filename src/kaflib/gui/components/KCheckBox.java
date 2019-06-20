package kaflib.gui.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import kaflib.types.Worker;

/**
 * Defines a JButton child for the kaflib library.
 */
public class KCheckBox extends JCheckBox implements ActionListener {

	private static final long serialVersionUID = -85507621424164L;

	private boolean disable_on_click;
	private Collection<Component> disable;
	private KListener listener;

	public KCheckBox(final String label, final KListener listener) {
		this(label, listener, false, false);
	}

	public KCheckBox(final Icon icon, final KListener listener) {
		this(icon, listener, false, false);
	}

	public KCheckBox(final Icon icon, 
		       final KListener listener,
			   final boolean monospace,
			   final boolean disableOnClick,
			   final Component... toDisable) {
		this(listener, monospace, disableOnClick, toDisable);
		setIcon(icon);
	}

	public KCheckBox(final String label, 
		       final KListener listener,
			   final boolean monospace,
			   final boolean disableOnClick,
			   final Component... toDisable) {
		this(listener, monospace, disableOnClick, toDisable);
		setText(label);
	}
	
	private KCheckBox(final KListener listener,
				   final boolean monospace,
				   final boolean disableOnClick,
				   final Component... toDisable) {
		super();
		this.listener = listener;

		if (monospace) {
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
		}
		
		disable = new ArrayList<Component>();
		disable_on_click = disableOnClick;
		if (disableOnClick || listener != null) {
			addActionListener(this);
		}
		
		if (disableOnClick) {
			for (Component component : toDisable) {
				disable.add(component);
			}
		}
	}
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			KCheckBox north = new KCheckBox("North", null, false, false);
			KCheckBox center = new KCheckBox("Center", null, true, true, north);
			
			KPanel panel = new KPanel("Test", north, center, null, null, null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private final Component getComponent() {
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (disable_on_click) {
			setEnabled(false);
		}
		if (disable != null) {
			for (Component component : disable) {
				component.setEnabled(false);
			}
		}
		if (listener != null) {
			listener.serialValueChanged(this);
			try {
				Worker worker = new Worker(){
	
					@Override
					protected void process() throws Exception {
						listener.asyncValueChanged(getComponent());
					}
				};
				worker.start();
			} 
			catch (Exception ex) {
				System.err.print("Unable to kick off asynchronous response.");
			}
		}
	}
	
}
