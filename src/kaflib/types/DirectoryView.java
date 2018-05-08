package kaflib.types;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import kaflib.utils.CheckUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a dynamic, randomized listing of all files in a directory 
 * (recursive).  Meant for on-the-fly random pulling from a big structure.
 * That is, it doesn't keep a list of all files.  It does build a directory
 * list as it goes, this is to help ensure the random selection isn't top-
 * heavy.
 */
public class DirectoryView {

	private final Set<Directory> directories;
	private int cache_size;
	private final String extensions[];
	private final RandomStack<File> cache;
	private Worker cacher;
	private static final double RECACHE_THRESHOLD = 0.5;
	
	public DirectoryView(final Directory root, 
						 final int cacheSize,
						 final String... extensions) throws Exception {
		CheckUtils.checkNonNegative(cacheSize, "cache size");
		
		cache_size = cacheSize;
		this.extensions = extensions;
		directories = new HashSet<Directory>();
		directories.add(root);
		directories.addAll(root.directories());
		
		cache = new RandomStack<File>();
		runCacher();
	}

	private boolean belowRecacheThreshold() {
		if (cache_size == 0) {
			return false;
		}
		
		return cache.size() <= (int)((double) cache_size * RECACHE_THRESHOLD);
	}
	
	private synchronized final void runCacher() throws Exception {
		// Only run one cacher at a time.
		if (cacher != null && !cacher.isDone()) {
			return;
		}
		// Don't start a recache unless we've hit the threshold.
		if (belowRecacheThreshold()) {
			return;
		}
		
		cacher = new Worker(){
			@Override
			protected void process() throws Exception {
				while (cache.size() < cache_size) {
					cache.push(getRandomFile());
				}
			}
		};
		cacher.start();
	}
	
	private File getRandomFile() throws Exception {
		File file = null;

		for (int i = 0; i < 2 * directories.size(); i++) {
			Directory directory = TypeUtils.getRandom(directories);
			directories.addAll(directory.directories());
			file = directory.getRandom(extensions);
		}
		return file;
	}
	
	public File next() throws Exception {
		File file = null;
		
		while (file == null || !file.exists()) {
			// Not caching or cache is empty, return serially.
			if (cache_size == 0 || cache.size() == 0) {
				file = getRandomFile();
			}
			// Caching, pop a file, then kick off the cacher.
			else {
				file = cache.pop();
				if (belowRecacheThreshold()) {
					runCacher();
				}
			}
		}
		return file;
	}
}
