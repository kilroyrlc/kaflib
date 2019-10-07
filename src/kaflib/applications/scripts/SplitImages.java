package kaflib.applications.scripts;

import java.io.File;

import kaflib.gui.components.KFrame;
import kaflib.types.Directory;
import kaflib.types.Percent;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.RandomUtils;

public class SplitImages {
	public static final Percent A_PERCENT = new Percent(.90);
	
	public static void main(String args[]) {
		try {
			KFrame frame = new KFrame();
			Directory d = GUIUtils.chooseDirectory(frame);
			if (d == null) {
				return;
			}
			File files[] = d.listFiles();
			
			Directory a = new Directory(d, "a");
			Directory b = new Directory(d, "b");
			if (a.exists() || b.exists()) {
				System.err.println("Need to be able to create a an b subdirs.");
			}
			a.mkdir();
			b.mkdir();
			
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}
				if (RandomUtils.randomBoolean(A_PERCENT)) {
					FileUtils.copyTo(a, file);
					file.delete();
				}
				else {
					FileUtils.copyTo(b, file);
					file.delete();
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}


