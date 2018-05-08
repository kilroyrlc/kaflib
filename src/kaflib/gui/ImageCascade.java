package kaflib.gui;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import kaflib.utils.CheckUtils;

public class ImageCascade extends JPanel {

	private static final long serialVersionUID = 1L;
	private final List<ImageComponent> cascade;

	public ImageCascade(final BufferedImage... images) throws Exception {
		super(new GridLayout(1, images.length));
		
		cascade = new ArrayList<ImageComponent>();
		
		for (BufferedImage image : images) {
			ImageComponent component = new ImageComponent(image);
			cascade.add(component);
			this.add(component);
		}
	}
	
	public void update(final BufferedImage image, final int index) throws Exception {
		CheckUtils.checkRange(index, 0, cascade.size() - 1, "panel index");
		cascade.get(index).update(image);
	}

}
