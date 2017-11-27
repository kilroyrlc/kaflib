package kaflib.gui;

import java.io.File;

public interface FileSelectorListener {
	public void fileSelected(final File file);
	public void noneSelected();
	public void createSelected();
}
