package kaflib.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaflib.utils.TypeUtils;

/**
 * Defines a subtype of WordTree where a cache of recent values is maintained.
 */
public class CachedWordTree extends WordTree implements Serializable {

	private static final long serialVersionUID = 3L;
	private Map<String, Integer> cache;
	private int cache_size;
	private int relative_time;
	
	/**
	 * Create the tree with the specified cache size.
	 * @param cacheSize
	 */
	public CachedWordTree(final int cacheSize) {
		super();
		cache = new HashMap<String, Integer>(cacheSize);
		cache_size = cacheSize;
		relative_time = 0;
	}
	
	/**
	 * Create the tree with the specified cache size.
	 * @param cacheSize
	 */
	public CachedWordTree(final Collection<String> initialValues,
						  final int cacheSize) throws Exception {
		super(initialValues);
		cache = new HashMap<String, Integer>(cacheSize);
		cache_size = cacheSize;
		relative_time = 0;
	}
	
	/**
	 * Add a value to the cache.  Unfortunately the word tree has no idea what
	 * value was selected.
	 * @param word
	 * @throws Exception
	 */
	public void accessed(final String word) throws Exception {
		cache.put(word, relative_time);
		relative_time = (relative_time % (Integer.MAX_VALUE - 5)) + 1;
		
		// Need to evict, do so asynchronously.
		if (cache.size() > cache_size) {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					while (cache.size() > cache_size * 0.7) {
						cache.remove(TypeUtils.getRandom(cache.keySet()));
					}	
				}
			};
			worker.start();
		}
	}
	
	/**
	 * Retuns the most accessed value.
	 * @return
	 */
	public String getMostFrequent() {
		String string = null;
		int value = 0;
		for (String key : cache.keySet()) {
			if (cache.get(key) > value) {
				string = key;
				value = cache.get(value);
			}
		}
		return string;
	}
	
	/**
	 * Return a list of matches, with cache hits listed first by most recent
	 * access.
	 * @param prefix
	 * @param maxSize
	 * @return
	 * @throws Exception
	 */
	public List<String> getOrdered(final String prefix, final int maxSize) throws Exception {
		Set<String> words = super.get(prefix, maxSize);
		List<String> nonhits = new ArrayList<String>();
		List<String> hits = new ArrayList<String>();
		
		for (String word : words) {
			if (cache.containsKey(word)) {
				hits.add(word);
			}
			else {
				nonhits.add(word);
			}
		}
		Collections.sort(hits, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				Integer time_1 = cache.get(o1);
				Integer time_2 = cache.get(o2);
				if (time_1 == null || time_2 == null) {
					return 0;
				}
				if (time_1 > time_2) {
					return -1;
				}
				else if (time_1 < time_2) {
					return 1;
				}
				else {
					return 0;
				}
			}});
		
		hits.addAll(nonhits);
		
		return hits;
	}
	
}
