package kaflib.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import kaflib.gui.components.ImageListener.MouseButton;

/**
 * Defines a swing component that shows an image.
 */
public abstract class ImageComponent extends Component implements MouseListener, 
																  MouseMotionListener, 
																  MouseWheelListener  {

	private static final long serialVersionUID = 654561L;
	private final int border_width;
	private final Color border_color;
	private final ImageListener listener;
	protected Dimension preferred_size;
	
	public ImageComponent() {
		this(null, null, null);
	}
	
	public ImageComponent(final Integer borderWidth, 
			              final Color borderColor,
			              final ImageListener listener) {
		super();
		this.preferred_size = new Dimension(0, 0);
		this.listener = listener;
		
		if (listener != null) {
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
		}
		
		if (borderWidth != null && borderWidth > 0) {
			border_width = borderWidth;
			if (borderColor != null) {
				border_color = borderColor;
			}
			else {
				border_color = Color.BLACK;
			}
		}
		else {
			border_width = 0;
			border_color = null;
		}
	}
	
	protected abstract BufferedImage getImage();

	/**
	 * Performs the redraw action.
	 */
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
	 * Repaints the component.
	 */
    public void paint(Graphics g) {
    	BufferedImage image = getImage();
    	if (image == null) {
    		preferred_size = new Dimension(0, 0);
    		return;
    	}
    	if (border_width <= 0) {
    		preferred_size = new Dimension(image.getWidth(), image.getHeight());
            g.drawImage(image, 0, 0, null);
    	}
    	else {
    		preferred_size = new Dimension(image.getWidth() + (2 * border_width), 
    								       image.getHeight() + (2 * border_width));
    		g.setColor(border_color);
    		g.fillRect(0, 0, 
    				   image.getWidth() + (2 * border_width), 
    				   image.getHeight() + (2 * border_width));
            g.drawImage(image, border_width, border_width, null);
            
    	}
    }

    public Dimension getPreferredSize() {
    	BufferedImage image = getImage();
    	if (image == null) {
    		return super.getPreferredSize();
    	}
    	if (border_width <= 0) {
    		return new Dimension(image.getWidth(), image.getHeight());
    	}
    	else {
    		return new Dimension(image.getWidth() + (2 * border_width), 
    									   image.getHeight() + (2 * border_width));
    	}
    }
    
    
	@Override
	public final void mouseClicked(MouseEvent e) {
	}

	@Override
	public final void mousePressed(MouseEvent e) {
		listener.mouseDown(MouseButton.getButton(e.getButton()), 
						   e.getX() - border_width, 
						   e.getY() - border_width);
	}

	@Override
	public final void mouseReleased(MouseEvent e) {
		listener.mouseUp(MouseButton.getButton(e.getButton()), 
				   e.getX() - border_width, 
				   e.getY() - border_width);
	}

	@Override
	public final void mouseEntered(MouseEvent e) {
	}

	@Override
	public final void mouseExited(MouseEvent e) {
	}

	@Override
	public final void mouseDragged(MouseEvent e) {
		listener.mouseDrag(MouseButton.getButton(e.getButton()),
						   e.getX() - border_width, 
				   		   e.getY() - border_width);		
	}
	
	@Override
	public final void mouseMoved(MouseEvent e) {
		listener.mouseMove(e.getX() - border_width, 
				   		   e.getY() - border_width);
	}

	@Override
	public final void mouseWheelMoved(MouseWheelEvent e) {
		listener.mouseWheelMove(e.getWheelRotation());
	}
}
