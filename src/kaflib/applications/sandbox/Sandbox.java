package kaflib.applications.sandbox;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import kaflib.types.Directory;
import kaflib.utils.FileUtils;

public class Sandbox {
	public static void main(String args[]) {
		try {
			Set<Directory> directories = new HashSet<Directory>();
			directories.add(new Directory("C:\\data\\code\\kilroy\\source\\archive\\"));
			while (directories.size() > 0) {
				Directory directory = directories.iterator().next();
				directories.remove(directory);
				for (File file : directory.listFiles()) {
					if (file.isDirectory()) {
						directories.add(new Directory(file));
					}
					else {
						if (file.getName().endsWith(".JPG")) {
							File output = FileUtils.changeExtension(file, "jpg");
							file.renameTo(output);
							System.out.println(file + "-> " + output);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}


