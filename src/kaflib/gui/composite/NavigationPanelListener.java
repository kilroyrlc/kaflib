package kaflib.gui.composite;

import java.awt.Component;

public interface NavigationPanelListener {
	public void navigationRequested(final int index);
	public void saveRequested(final int index);
	public void deleteRequested(final int index);
	public void refreshRequested(final int index);
	public void customFunctionRequest(final Component component, final int index);

}
