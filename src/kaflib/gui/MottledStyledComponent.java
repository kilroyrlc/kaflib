package kaflib.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import kaflib.graphics.Canvas;
import kaflib.types.Coordinate;

public class MottledStyledComponent extends StyledComponent {

	private static final long serialVersionUID = 1L;
	private final BufferedImage image;
	
	public MottledStyledComponent(Coordinate size, Color... colors) throws Exception {
		super(size, colors);
		
		Canvas canvas = Canvas.createMottled(new Coordinate(getContentWidth(), getContentHeight()), 
											 3,
											 colors);
		image = canvas.toBufferedImage();

	}

    public void paint(Graphics g) {
    	super.paint(g);
    	g.drawImage(image, 
    				getBorderSize(), 
    				getBorderSize(), 
    				getContentWidth(), 
    				getContentHeight(), 
    				null);
    }
	
}
