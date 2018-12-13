package kaflib.applications;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


import kaflib.graphics.Canvas;
import kaflib.types.Directory;
import kaflib.types.Pair;

public class Sandbox {
	public static void main(String args[]) {
		try {

			Directory directory = new Directory("Z:\\data\\graphics\\sample_images");
			Set<File> files = directory.list("jpg", "png");
			Set<Pair<File, File>> traversed = new HashSet<Pair<File, File>>();
			
			for (File file : files) {
				for (File other : files) {
					if (file.equals(other) || 
						traversed.contains(new Pair<File, File>(file, other))) {
						continue;
					}
					traversed.add(new Pair<File, File>(file, other));
					traversed.add(new Pair<File, File>(other, file));
					
					Canvas canvas = new Canvas(file);
					Canvas other_canvas = new Canvas(other);
					if (!canvas.aspectRatioMatches(other_canvas, 2)) {
						continue;
					}
					
					System.out.println(file.getName() + " / " + other.getName() + ": " + 
									   canvas.isSimilar(other_canvas));
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
