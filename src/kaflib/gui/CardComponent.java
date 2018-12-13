package kaflib.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kaflib.types.Coordinate;

public class CardComponent extends MottledStyledComponent {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_BORDER_SIZE = 11;
	public static final int DEFAULT_WIDTH = 200;
	public static final int DEFAULT_HEIGHT = 300;
	
	public CardComponent(final Color... colors) throws Exception {
		super(new Coordinate(DEFAULT_WIDTH, DEFAULT_HEIGHT), colors);
	}

    public Dimension getPreferredSize() {
    	return new Dimension(width, height);
    }
	
    public void paint(Graphics g) {
    	super.paint(g);
    }
    
	public static void main(String args[]) {
		try {
			int cards = 5;
			JFrame frame = new JFrame();
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1, cards));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			for (int i = 0; i < cards; i++) {
				panel.add(CardComponent.getBordered(new Coordinate(200, 300), Color.BLUE, Color.CYAN));
			}
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
