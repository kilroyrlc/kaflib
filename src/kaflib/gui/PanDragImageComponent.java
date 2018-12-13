package kaflib.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class PanDragImageComponent extends ImageComponent implements MouseMotionListener,
																	 MouseWheelListener {

	private static final long serialVersionUID = 1L;
	
	public PanDragImageComponent() throws Exception {
		super();
		addMouseWheelListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

}
