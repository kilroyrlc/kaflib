package kaflib.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaflib.gui.components.KFrame;
import kaflib.gui.components.KPanel;
import kaflib.gui.components.StaticImageComponent;
import kaflib.types.Coordinate;
import kaflib.types.Pair;
import kaflib.utils.CoordinateUtils;
import kaflib.utils.GUIUtils;

/**
 * Defines a set of pixels forming a circle.
 */
public class CircularCanvas {
	private final Map<Coordinate, RGBPixel> pixels;
	private final int radius;
	
	public CircularCanvas(final int radius) throws Exception {
		this.radius = radius;
		Set<Coordinate> coordinates = CoordinateUtils.getCircle(radius, new Coordinate(radius, radius));
		pixels = new HashMap<Coordinate, RGBPixel>(coordinates.size());
		for (Coordinate coordinate : coordinates) {
			pixels.put(coordinate, null);
		}
	}
	
	public CircularCanvas(final List<RGBPixel> values, final int radius) throws Exception {
		this.radius = radius;
		Set<Coordinate> coordinates = CoordinateUtils.getCircle(radius, new Coordinate(radius, radius));
		List<Coordinate> ordered = new ArrayList<Coordinate>();
		ordered.addAll(coordinates);
		Collections.sort(ordered);
		
		if (values.size() != ordered.size()) {
			throw new Exception("Got " + values.size() + " pixels, expected " + ordered.size() + ".");
		}
		
		pixels = new HashMap<Coordinate, RGBPixel>(values.size());
		for (int i = 0; i < values.size(); i++) {
			pixels.put(ordered.get(i), values.get(i));
		}
	}
	
	public int getDiameter() {
		return radius * 2 + 1;
	}
	
	public Canvas getCanvas() throws Exception {
		Canvas canvas = new Canvas(getDiameter(), getDiameter(), RGBPixel.TRANSPARENT_BLACK);
		getCoordinates().parallelStream().forEach(c -> canvas.uncheckedSet(c, get(c)));
		return canvas;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public Coordinate getCenter() {
		return new Coordinate(radius, radius);
	}
	
	public int size() {
		return pixels.size();
	}
	
	public Set<Coordinate> getCoordinates() {
		return pixels.keySet();
	}
	
	public RGBPixel get(final Coordinate coordinate) {
		if (!pixels.containsKey(coordinate)) {
			return null;
		}
		return pixels.get(coordinate);
	}

	public void set(final int x, int y, final RGBPixel value) throws Exception {
		set(new Coordinate(x, y), value);
	}
	
	public void uncheckedSet(final Coordinate coordinate, final RGBPixel value) {
		if (!pixels.containsKey(coordinate)) {
			return;
		}
		pixels.put(coordinate, value);
	}
	
	public void set(final Coordinate coordinate, final RGBPixel value) throws Exception {
		if (!pixels.containsKey(coordinate)) {
			throw new Exception("Invalid coordinate: " + coordinate + ".");
		}
		pixels.put(coordinate, value);
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		List<Coordinate> ordered = new ArrayList<Coordinate>();
		ordered.addAll(pixels.keySet());
		Collections.sort(ordered);
		for (Coordinate coordinate : ordered) {
			buffer.append(coordinate + ": " + pixels.get(coordinate) + "\n");
		}
		return new String(buffer);
	}

	public List<Pair<Coordinate, RGBPixel>> toSerialPair() {
		List<Coordinate> ordered = new ArrayList<Coordinate>();
		List<Pair<Coordinate, RGBPixel>> values = new ArrayList<Pair<Coordinate, RGBPixel>>();
		ordered.addAll(pixels.keySet());
		Collections.sort(ordered);
		for (Coordinate coordinate : ordered) {
			values.add(new Pair<Coordinate, RGBPixel>(coordinate, pixels.get(coordinate)));
		}
		return values;
	}
	
	public List<RGBPixel> toSerial() {
		List<Coordinate> ordered = new ArrayList<Coordinate>();
		List<RGBPixel> values = new ArrayList<RGBPixel>();
		ordered.addAll(pixels.keySet());
		Collections.sort(ordered);
		for (Coordinate coordinate : ordered) {
			values.add(pixels.get(coordinate));
		}
		return values;
	}
	
	public static void main(String args[]) {
		try {
			StaticImageComponent a = new StaticImageComponent();
			StaticImageComponent b = new StaticImageComponent();
			
			KFrame frame = new KFrame(new KPanel(null, 1, 2, a, b));
			File file = GUIUtils.chooseFile(frame, null);
			if (file == null) {
				return;
			}
			Canvas canvas = new Canvas(file);
			a.set(canvas);
			CircularCanvas circle = CanvasUtils.getRandomCircle(canvas, Math.min(canvas.getHeight(), canvas.getWidth()) / 3);
			b.set(circle.getCanvas());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
