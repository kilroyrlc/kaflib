package kaflib.gui.components;

import java.awt.Component;

public interface KListener {
	public void serialValueChanged(final Component component);
	public void asyncValueChanged(final Component component);
}
