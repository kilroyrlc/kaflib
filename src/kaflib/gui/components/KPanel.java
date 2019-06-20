package kaflib.gui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


/**
 * Defines a JPanel child for the kaflib library.
 */
public class KPanel extends JPanel {

	private static final long serialVersionUID = -8550766858750424164L;

	private String title;
	
	/**
	 * Default constructor.
	 */
	public KPanel() {
		super();
	}

	public KPanel(final Component component) {
		this(null, component);
	}
	
	public KPanel(final String title, 
			      final Component component) {
		super();
		this.title = title;
		if (title != null) {
			setBorder(createBorder());
		}
		add(component);
	}
	
	public KPanel(final String title, 
				  final int rows, 
				  final int columns,
				  final Component... components) {
		super(new GridLayout(rows, columns));
		this.title = title;
		if (title != null) {
			setBorder(createBorder());
		}
		for (Component component : components) {
			add(component);
		}
	}
	
	/**
	 * Create a titled panel.
	 * @param title
	 */
	public KPanel(final String title) {
		super();
		this.title = title;
		if (title != null) {
			setBorder(createBorder());
		}
	}
	
	/**
	 * Creates a panel with a border layout and the specified components.
	 * @param title
	 * @param north
	 * @param center
	 * @param south
	 * @param west
	 * @param east
	 */
	public KPanel(final String title, 
				  final Component north,
				  final Component center,
				  final Component south,
				  final Component west,
				  final Component east) {
		super();
		this.title = title;
		if (title != null) {
			setBorder(createBorder());
		}

		setLayout(new BorderLayout());

		if (north != null) {
			add(north, BorderLayout.NORTH);
		}
		if (center != null) {
			add(center, BorderLayout.CENTER);
		}
		if (south != null) {
			add(south, BorderLayout.SOUTH);
		}
		if (west != null) {
			add(west, BorderLayout.WEST);
		}
		if (east != null) {
			add(east, BorderLayout.EAST);
		}
	}
	
	public KPanel(final boolean vertical,
			      final KButton... buttons) {
		super();
		setLayout(new BoxLayout(this, vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
		for (KButton button : buttons) {
			add(button);
		}
	}
	
	protected Border createBorder() {
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		return BorderFactory.createTitledBorder(border, title, TitledBorder.LEFT, TitledBorder.TOP);
	}

	/**
	 * Sets all contents to be enabled or disabled.
	 */
	public void setEnabled(final boolean enabled) {
		for (Component component : getComponents()) {
			component.setEnabled(enabled);
		}
	}
	
	public void redraw() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				invalidate();
				repaint();
				revalidate();
			}
		});
	}

	
	public String getTitle() {
		return title;
	}
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			new KFrame(JFrame.EXIT_ON_CLOSE, new KPanel("Test"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
