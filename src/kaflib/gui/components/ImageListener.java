package kaflib.gui.components;

import java.awt.event.MouseEvent;

public interface ImageListener {
	public enum MouseButton {
		LEFT(MouseEvent.BUTTON1),
		RIGHT(MouseEvent.BUTTON3);
		
		private MouseButton(final int eventButton) {
			event = eventButton;
		}
		public int getEvent() {
			return event;
		}
		public static MouseButton getButton(final int eventButton) {
			for (MouseButton button : MouseButton.values()) {
				if (eventButton == button.getEvent()) {
					return button;
				}
			}
			return null;
		}
		
		private final int event;
	}
	
	public void mouseDown(final MouseButton button, final int x, final int y);
	public void mouseUp(final MouseButton button, final int x, final int y);
	public void mouseDrag(final MouseButton button, final int x, final int y);
	public void mouseMove(final int x, final int y);
	public void mouseWheelMove(final int rotation);
}
