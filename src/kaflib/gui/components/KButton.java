package kaflib.gui.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import kaflib.types.Worker;

/**
 * Defines a JButton child for the kaflib library.
 */
public class KButton extends JButton implements ActionListener {

	public enum ButtonType {
		DIRECTORY("FileView.directoryIcon"),
		FILE("FileView.fileIcon"),
		COMPUTER("FileView.computerIcon"),
		HARD_DRIVE("FileView.hardDriveIcon"),
		SAVE("FileView.floppyDriveIcon"),
		NEW_FOLDER("FileChooser.newFolderIcon"),
		UP_FOLDER("FileChooser.upFolderIcon"),
		HOME_FOLDER("FileChooser.homeFolderIcon"),
		DETAILS_VIEW("FileChooser.detailsViewIcon"),
		LIST_VIEW("FileChooser.listViewIcon");
		
		private ButtonType(final String label) {
			resource = label;
		}
		
		public String getResource() {
			return resource;
		}
		public Icon getIcon() {
			return UIManager.getIcon(resource);
		}
		
		private final String resource;
		
	};
	
	private static final long serialVersionUID = -855076668750424164L;

	private boolean disable_on_click;
	private Collection<Component> disable;
	private KListener listener;

	protected KButton(final KListener listener) {
		this((String) null, listener, false, false);
	}
	
	public KButton(final String label, final KListener listener) {
		this(label, listener, false, false);
	}

	public KButton(final Icon icon, final KListener listener) {
		this(icon, listener, false, false);
	}

	public KButton(final ButtonType type, final KListener listener) {
		this(type, listener, false, false);
	}

	public KButton(final ButtonType type, 
		       final KListener listener,
			   final boolean monospace,
			   final boolean disableOnClick,
			   final Component... toDisable) {
		this(listener, monospace, disableOnClick, toDisable);
		setIcon(type.getIcon());
	}
	
	public KButton(final Icon icon, 
		       final KListener listener,
			   final boolean monospace,
			   final boolean disableOnClick,
			   final Component... toDisable) {
		this(listener, monospace, disableOnClick, toDisable);
		setIcon(icon);
	}

	public KButton(final String label, 
		       final KListener listener,
			   final boolean monospace,
			   final boolean disableOnClick,
			   final Component... toDisable) {
		this(listener, monospace, disableOnClick, toDisable);
		setText(label);
	}
	
	private KButton(final KListener listener,
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
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			KButton north = new KButton("North", null, false, false);
			KButton center = new KButton("Center", null, true, true, north);
			
			KPanel panel = new KPanel("Test", north, center, null, null, null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
