package kaflib.gui;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import kaflib.graphics.Canvas;
import kaflib.utils.CheckUtils;
import kaflib.utils.GUIUtils;

public class ImageCascade extends JPanel {

	private static final long serialVersionUID = 1L;
	private final List<ImageComponent> cascade;

	public ImageCascade(final BufferedImage... images) throws Exception {
		super(new FlowLayout());
		
		cascade = new ArrayList<ImageComponent>();
		
		for (BufferedImage image : images) {
			ImageComponent component = new ImageComponent(image);
			JPanel panel = GUIUtils.getTitledPanel("");
			panel.add(component);
			cascade.add(component);
			this.add(panel);
		}
	}
	
	public ImageCascade(final Canvas... images) throws Exception {
		super(new FlowLayout());
		
		cascade = new ArrayList<ImageComponent>();
		
		for (Canvas image : images) {
			ImageComponent component = new ImageComponent(image);
			JPanel panel = GUIUtils.getTitledPanel("");
			panel.add(component);
			cascade.add(component);
			this.add(panel);
		}
	}
	
	public void update(final Canvas image, final int index) throws Exception {
		CheckUtils.checkRange(index, 0, cascade.size() - 1, "panel index");
		cascade.get(index).update(image.toBufferedImage());
	}
	
	public void update(final BufferedImage image, final int index) throws Exception {
		CheckUtils.checkRange(index, 0, cascade.size() - 1, "panel index");
		cascade.get(index).update(image);
	}

}
