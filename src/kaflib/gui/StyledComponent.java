package kaflib.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kaflib.types.Coordinate;

public class StyledComponent extends Component {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_BORDER_SIZE = 11;
	
	protected final int width;
	protected final int height;
	private final int border_size;
	private final int content_width;
	private final int content_height;
	private final List<Color> colors;
	
	public StyledComponent(final Coordinate size,
						   final 
						   Color... colors) throws Exception {
		super();
		this.colors = new ArrayList<Color>();
		for (Color color : colors) {
			this.colors.add(color);
		}
		if (this.colors.size() == 0) {
			this.colors.add(Color.WHITE);
		}
		
		width = size.getX();
		height = size.getY();
		border_size = DEFAULT_BORDER_SIZE;
		content_width = width - 2 * border_size;
		content_height = height - 2 * border_size;

	}
	
	protected final int getContentWidth() {
		return content_width;
	}
	
	protected final int getContentHeight() {
		return content_height;
	}
	
	protected final int getBorderSize() {
		return border_size;
	}
	
    public Dimension getPreferredSize() {
    	return new Dimension(width, height);
    }
	
    public void paint(Graphics g) {
    	
    	Color color = new Color(0, 0, 0, 0x33);
		g.setColor(color);
    	g.drawRect(0, 0, width - 1, height - 1);
    	color = new Color(0, 0, 0, 0x99);
		g.setColor(color);
    	g.drawRect(1, 1, width - 3, height - 3);
    	color = new Color(0, 0, 0, 0xcc);
		g.setColor(color);
    	g.drawRect(2, 2, width - 5, height - 5);
    	
    	color = colors.get(0).brighter();
    	for (int i = 3; i < border_size; i++) {
    		g.setColor(color);
        	g.drawRect(i, i, width - 2 * i - 1, height - 2 * i - 1);
        	if (i % 2 == 0) { 
        		color = color.darker();
        	}
    	}
    }
    
    public static JPanel getBordered(final Coordinate size,
    								 final Color fg, 
    								 final Color bg) throws Exception {

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		panel.add(new StyledComponent(size, fg, bg));
		return panel;
    }
	
	public static void main(String args[]) {
		try {
			int cards = 5;
			JFrame frame = new JFrame();
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1, cards));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			for (int i = 0; i < cards; i++) {
				panel.add(StyledComponent.getBordered(new Coordinate(200, 300), Color.BLUE, Color.CYAN));
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
