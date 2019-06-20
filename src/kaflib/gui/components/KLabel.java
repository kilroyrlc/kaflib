package kaflib.gui.components;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Defines a JButton child for the kaflib library.
 */
public class KLabel extends JLabel {

	private static final long serialVersionUID = -8550657689794164L;

	public KLabel(final String text) {
		this(text, false);
	}
	
	public KLabel(final String text,
				   final boolean monospace) {
		super(text);
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
			KLabel field = new KLabel("Test text", true);
			KPanel panel = new KPanel("Test", null, field, null, null, null);
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
