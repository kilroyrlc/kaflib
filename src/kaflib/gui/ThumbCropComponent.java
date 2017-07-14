package kaflib.gui;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Defines a swing component wherein the user can select a thumbnail with the
 * left mouse button and crop with the right.
 */
public class ThumbCropComponent extends EditableImageComponent {

	private static final long serialVersionUID = 54654L;

	public ThumbCropComponent() throws Exception {
		super();
	}
	
	public ThumbCropComponent(final File file) throws Exception {
		super(file);
	}

	public ThumbCropComponent(final BufferedImage image) throws Exception {
		super(image);
	}
	
	public void requestMode(final Mode mode) {
		// Do nothing.
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
		if (e.getButton() == MouseEvent.BUTTON1) {
			setMode(Mode.THUMBNAIL);
		}
		else {
			setMode(Mode.CROP);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
	}
}
