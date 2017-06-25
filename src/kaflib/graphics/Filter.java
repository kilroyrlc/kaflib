package kaflib.graphics;

import java.awt.image.BufferedImage;

public abstract class Filter {

	private final BufferedImage input;
	
	public Filter(final BufferedImage image) throws Exception {
		input = image;
	}

	protected BufferedImage getInput() throws Exception {
		return input;
	}
	
	public abstract BufferedImage apply() throws Exception;
}
