package kaflib.gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.border.Border;


/**
 * Defines a JPanel child for the kaflib library.
 */
public class KStyledPanel extends KPanel {

	private static final long serialVersionUID = -855076648750424164L;

	
	public KStyledPanel(String string) {
		super(string);
	}

	@Override
	  protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
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
		
		if (getTitle() != null) {
			g.setColor(Color.WHITE);
			g.setFont(KGUIConstants.PANEL_LABEL);
			g.drawString(getTitle(), 12, 26);
		}
	}
	
	/**
	 * Creates an empty border.
	 * @return
	 */
	protected Border createBorder() {
		return BorderFactory.createEmptyBorder(22, 16, 16, 16);
	}
	
	/**
	 * Test sandbox.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			KStyledPanel panel = new KStyledPanel("Test");
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
