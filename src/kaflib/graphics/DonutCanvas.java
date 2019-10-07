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
import kaflib.utils.CoordinateUtils;
import kaflib.utils.GUIUtils;

/**
 * Defines a set of pixels forming a donut shape.
 */
public class DonutCanvas {
	private final Map<Coordinate, RGBPixel> outer;
	private final Map<Coordinate, RGBPixel> inner;
	private final int outer_radius;
	private final int inner_radius;
	
	public DonutCanvas(final int outerRadius, final int innerRadius) throws Exception {
		outer_radius = outerRadius;
		inner_radius = innerRadius;
		Set<Coordinate> outerc = CoordinateUtils.getCircle(outer_radius, new Coordinate(outer_radius, outer_radius));
		Set<Coordinate> innerc = CoordinateUtils.getCircle(inner_radius, new Coordinate(outer_radius, outer_radius));
		outerc.removeAll(innerc);
		outer = new HashMap<Coordinate, RGBPixel>(outerc.size());
		for (Coordinate coordinate : outerc) {
			outer.put(coordinate, null);
		}
		inner = new HashMap<Coordinate, RGBPixel>(innerc.size());
		for (Coordinate coordinate : innerc) {
			inner.put(coordinate, null);
		}
	}
	
	public int getDiameter() {
		return outer_radius * 2 + 1;
	}
	
	public Canvas getCanvas() throws Exception {
		Canvas canvas = new Canvas(getDiameter(), getDiameter(), RGBPixel.TRANSPARENT_BLACK);
		outer.keySet().parallelStream().forEach(c -> canvas.uncheckedSet(c, get(c)));
		inner.keySet().parallelStream().forEach(c -> canvas.uncheckedSet(c, get(c)));
		return canvas;
	}

	public Canvas getOuterCanvas() throws Exception {
		Canvas canvas = new Canvas(getDiameter(), getDiameter(), RGBPixel.TRANSPARENT_BLACK);
		outer.keySet().parallelStream().forEach(c -> canvas.uncheckedSet(c, get(c)));
		return canvas;
	}

	public Canvas getInnerCanvas() throws Exception {
		Canvas canvas = new Canvas(getDiameter(), getDiameter(), RGBPixel.TRANSPARENT_BLACK);
		inner.keySet().parallelStream().forEach(c -> canvas.uncheckedSet(c, get(c)));
		return canvas;
	}
	
	public int getOuterRadius() {
		return outer_radius;
	}

	public int getInnerRadius() {
		return inner_radius;
	}

	
	public Coordinate getCenter() {
		return new Coordinate(outer_radius, outer_radius);
	}
	
	public int outerSize() {
		return outer.size();
	}
	
	public int innerSize() {
		return inner.size();
	}
	
	public Set<Coordinate> getOuterCoordinates() {
		return outer.keySet();
	}
	
	public Set<Coordinate> getInnerCoordinates() {
		return inner.keySet();
	}
	
	public RGBPixel get(final Coordinate coordinate) {
		if (outer.containsKey(coordinate)) {
			return outer.get(coordinate);
		}
		if (inner.containsKey(coordinate)) {
			return inner.get(coordinate);
		}
		return null;
	}

	public void set(final int x, int y, final RGBPixel value) throws Exception {
		set(new Coordinate(x, y), value);
	}
	
	public void uncheckedSet(final Coordinate coordinate, final RGBPixel value) {
		if (outer.containsKey(coordinate)) {
			outer.put(coordinate, value);
		}
		if (inner.containsKey(coordinate)) {
			inner.put(coordinate, value);
		}

	}
	
	public void set(final Coordinate coordinate, final RGBPixel value) throws Exception {
		if (outer.containsKey(coordinate)) {
			outer.put(coordinate, value);
		}
		if (inner.containsKey(coordinate)) {
			inner.put(coordinate, value);
		}
		throw new Exception("Invalid coordinate: " + coordinate + ".");
	}

	public List<RGBPixel> toInnerSerial() {
		List<Coordinate> ordered = new ArrayList<Coordinate>();
		List<RGBPixel> values = new ArrayList<RGBPixel>();
		ordered.addAll(inner.keySet());
		Collections.sort(ordered);
		for (Coordinate coordinate : ordered) {
			values.add(inner.get(coordinate));
		}
		return values;
	}

	public List<RGBPixel> toOuterSerial() {
		List<Coordinate> ordered = new ArrayList<Coordinate>();
		List<RGBPixel> values = new ArrayList<RGBPixel>();
		ordered.addAll(outer.keySet());
		Collections.sort(ordered);
		for (Coordinate coordinate : ordered) {
			values.add(outer.get(coordinate));
		}
		return values;
	}
	
	public static void main(String args[]) {
		try {
			StaticImageComponent a = new StaticImageComponent();
			StaticImageComponent b = new StaticImageComponent();
			StaticImageComponent c = new StaticImageComponent();
			
			KFrame frame = new KFrame(new KPanel(null, 1, 3, a, b, c));
			File file = GUIUtils.chooseFile(frame, null);
			if (file == null) {
				return;
			}
			Canvas canvas = new Canvas(file);
			a.set(canvas);
			int size = Math.min(canvas.getHeight(), canvas.getWidth());
			DonutCanvas circle = CanvasUtils.getRandomDonut(canvas, size / 3, size / 5);
			b.set(circle.getOuterCanvas());
			c.set(circle.getInnerCanvas());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
