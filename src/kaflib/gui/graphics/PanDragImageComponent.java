package kaflib.gui.graphics;

import kaflib.gui.components.DownscaledImageComponent;

public class PanDragImageComponent extends DownscaledImageComponent {

	private static final long serialVersionUID = 1L;
	
	public PanDragImageComponent() throws Exception {
		super();
		addMouseWheelListener(this);
		addMouseMotionListener(this);
	}

}
