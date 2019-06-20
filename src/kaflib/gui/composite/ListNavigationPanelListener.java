package kaflib.gui.composite;

import java.awt.Component;

public interface ListNavigationPanelListener<T> {
	public void navigationRequested(final T value);
	public void saveRequested(final T value);
	public void deleteRequested(final T value);
	public void refreshRequested(final T value);
	public void customFunctionRequest(final Component component, final T value);

}
