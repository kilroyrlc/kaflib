package kaflib.gui.components;

import java.awt.Component;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import kaflib.graphics.GraphicsUtils;

public class KIconButton extends KButton {

	private static final long serialVersionUID = 5599468658793339700L;

	public enum IconType {
		AWARD("award"),
		COMMENT("comment"),
		DIRECTORY("directory"),
		DISK("disk"),
		END("end"),
		HEART("heart"),
		INFO("info"),
		LAST("last"),
		LOCKED("locked"),
		MAIL("mail"),
		MOTORCYCLE("motorcycle"),
		MUSIC("music"),
		NEXT("next"),
		NO("no"),
		OK("ok"),
		QUESTION("question"),
		REFRESH("refresh"),
		SAVE_NEXT("save_next"),
		START("start"),
		TRASH("trash"),
		UNLOCKED("unlocked");
		
		private IconType(final String label) {
			resource = label;
		}
		
		public String getResource() {
			return resource;
		}
		public File getFile() {
			return new File("data/icons/" + getResource() + ".png");
		}
		private final String resource;
		
	};

	
	public KIconButton(final IconType icon, 
				       final KListener listener) throws Exception {
		this(icon, listener, false);
	}

	public KIconButton(final IconType icon, 
			   final boolean disableOnClick,
		       final KListener listener) throws Exception {
		super(new ImageIcon(GraphicsUtils.read(icon.getFile())),
				listener,
				false,
				disableOnClick);
	}
	
	public KIconButton(final IconType icon, 
				       final KListener listener,
					   final boolean disableOnClick,
					   final Component... toDisable) throws Exception {
		super(new ImageIcon(GraphicsUtils.read(icon.getFile())),
				listener,
				false,
				disableOnClick,
				toDisable);
	}
	
	public static void main(String args[]) {
		try {
			KPanel panel = new KPanel("Test", 3, 2,
									  new KIconButton(IconType.DIRECTORY, null),
									  new KIconButton(IconType.LOCKED, null),
									  new KIconButton(IconType.NO, null),
									  new KIconButton(IconType.QUESTION, null),
									  new KIconButton(IconType.START, null),
									  new KIconButton(IconType.REFRESH, null));
			new KFrame(JFrame.EXIT_ON_CLOSE, panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
