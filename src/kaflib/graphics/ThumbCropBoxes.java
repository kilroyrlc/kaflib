package kaflib.graphics;

import kaflib.types.Box;

/**
 * Defines a convenience class with two boxes representing thumbnail dimensions
 * and crop dimensions.
 */
public class ThumbCropBoxes {
	private Box thumbnail;
	private Box crop;

	public ThumbCropBoxes() {
		this.thumbnail = null;
		this.crop = null;
	}
	
	public ThumbCropBoxes(final Box thumbnail, final Box crop) {
		this.thumbnail = thumbnail;
		this.crop = crop;
	}
	
	public ThumbCropBoxes(final String serial) throws Exception {
		Box t = null;
		Box c = null;
		String lines[] = serial.split("\\n");
		for (String line : lines) {
			if (line.startsWith("c:")) {
				c = new Box(line.substring(2));
			}
			else if (line.startsWith("t:")) {
				t = new Box(line.substring(2));
			}
		}
		thumbnail = t;
		crop = c;
	}
	
	public void setThumbnail(final Box box) {
		thumbnail = box;
	}
	
	public void setCrop(final Box box) {
		if (box.equals(crop)) {
			return;
		}
		
		crop = box;
	}
	
	public Box getThumbnail() {
		return thumbnail;
	}
	
	public Box getCrop() {
		return crop;
	}
	
    public final String toSerial() {
    	String string = "";
    	if (getCrop() != null) {
    		string += "c:" + getCrop().toSerial() + "\n";
    	}
    	if (getThumbnail() != null) {
    		string += "t:" + getThumbnail().toSerial() + "\n";
    	}
    	return string;
    }
    
    public static ThumbCropBoxes getScaledDown(final ThumbCropBoxes original, 
    										   final int factor) throws Exception {
    	Box thumb = original.getThumbnail();
    	if (thumb != null) {
    		thumb = Box.getScaledDown(thumb, factor);
    	}
    	Box crop = original.getCrop();
    	if (crop != null) {
    		crop = Box.getScaledDown(crop, factor);
    	}    	
    	
    	return new ThumbCropBoxes(thumb, crop);
				
    }
    public static ThumbCropBoxes getScaledUp(final ThumbCropBoxes original, 
    										final int factor) throws Exception {
    	Box thumb = original.getThumbnail();
    	if (thumb != null) {
    		thumb = Box.getScaledUp(thumb, factor);
    	}
    	Box crop = original.getCrop();
    	if (crop != null) {
    		crop = Box.getScaledUp(crop, factor);
    	}    	
    	
    	return new ThumbCropBoxes(thumb, crop);
	}
}
