package kaflib.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 * Defines a JPanel child for the kaflib library.
 */
public class KStyledButton extends KButton {

	private static final long serialVersionUID = -855076668750424164L;

	private Font font;

	public KStyledButton(final String label, final KListener listener) {
		super(label, listener);
	}

	public KStyledButton(final String label, 
			       final KListener listener,
				   final boolean monospace,
				   final boolean disableOnClick,
				   final Component... toDisable) {
		super(label, listener, monospace, disableOnClick, toDisable);
	}
	
	@Override
	  protected void paintComponent(Graphics g) {
	    BufferedImage image = KGUIConstants.getBackgroundImage();
		if (image != null) {
			for (int i = 0; i < getWidth() / image.getWidth() + 1; i++) {
				for (int j = 0; j < getHeight() / image.getHeight() + 1; j++) {
					g.drawImage(KGUIConstants.getBackgroundImage(), 
								i * image.getWidth(), 
								j * image.getHeight(), null);
				}
			}
		}
		g.setColor(Color.GRAY);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
		g.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
		
		if (getText() != null) {
			int j = getHeight() / 2 + KGUIConstants.BUTTON_MONOSPACE.getSize() / 2;
			
			g.setColor(Color.WHITE);
			g.setFont(font);
			g.drawString(getText(), 6, j);
		}
	}
	

	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			KStyledButton north = new KStyledButton("North", null, false, false);
			KStyledButton center = new KStyledButton("Center", null, true, true, north);
			
			KPanel panel = new KPanel("Test", north, center, null, null, null);
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
