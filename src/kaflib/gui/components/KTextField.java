package kaflib.gui.components;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Defines a JButton child for the kaflib library.
 */
public class KTextField extends JTextField {

	private static final long serialVersionUID = -855065768750424164L;
	private KListener listener;

	public KTextField(final int columns,
					   final boolean monospace,
					   final KListener listener) {
		super(columns);
		this.listener = listener;
		if (monospace) {
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
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
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			KTextField field = new KTextField(16, true, null);
			KPanel panel = new KPanel("Test", null, field, null, null, null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
