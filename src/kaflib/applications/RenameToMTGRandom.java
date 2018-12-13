package kaflib.applications;

import java.io.File;
import java.util.Set;

import javax.swing.JFrame;

import kaflib.types.Directory;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.MTGNameGenerator;

public class RenameToMTGRandom {

	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();

			Set<File> files = GUIUtils.chooseFiles(frame);

			if (files != null && files.size() > 0) {
				Directory working_directory = new Directory(files.iterator().next().getParentFile());
				Directory output_directory = new Directory(working_directory, "output");
				if (!output_directory.exists()) {
					output_directory.mkdir();
				}
				for (File file : files) {
					String name = MTGNameGenerator.generate("_");
					File outfile = new File(output_directory, name + "." + FileUtils.getExtension(file));
					while (name.length() > 24 || outfile.exists()) {
						name = MTGNameGenerator.generate("_");
						outfile = new File(output_directory, name);
					}
					
					FileUtils.copy(outfile, file);
				}
			}
			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
