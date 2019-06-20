package kaflib.gui.components;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;

import kaflib.graphics.GraphicsUtils;

public class KGUIConstants {

	private static BufferedImage background;
	private static final File background_file = new File("data/cf_pattern.png");
	public static final Font BUTTON_MONOSPACE = new Font(Font.MONOSPACED, Font.BOLD, 12);
	public static final Font BUTTON_SANS_SERIF = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	public static final Font PANEL_LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 16);
	
	public static BufferedImage getBackgroundImage() {
		if (background == null) {
			if (!background_file.exists()) {
				System.err.println("Couldn't find background file: " + background_file + ".");
				return null;
			}
			try {
				background = GraphicsUtils.read(background_file);
			}
			catch (Exception e) {
				System.err.println("Couldn't read background file: " + background_file + ".");
			}
		}
		return background;
	}
	
}
