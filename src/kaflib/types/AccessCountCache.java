package kaflib.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AccessCountCache<K, V> extends Cache<K, V> {

	private final Map<K, Integer> accesses;
	
	public AccessCountCache(int size) throws Exception {
		super(size);
		accesses = new HashMap<K, Integer>(size + 3);

	}

	@Override
	protected void evict(int count) {
		List<Integer> ordered = new ArrayList<Integer>();
		ordered.addAll(accesses.values());
		Collections.sort(ordered);
		if (ordered.size() < count) {
			return;
		}
		int cutoff = ordered.get(count - 1);

		List<K> remove = new ArrayList<K>();
		for (K key : accesses.keySet()) {
			if (accesses.get(key) <= cutoff) {
				remove.add(key);
			}
		}
		for (K key : remove) {
			remove(key);
			accesses.remove(key);
		}
	}

	@Override
	protected void accessed(K key)  {
		if (key == null) {
			return;
		}
		Integer value = accesses.get(key);
		if (value != null) {
			accesses.put(key, Math.min(value + 1, Integer.MAX_VALUE));
		}
		else {
			accesses.put(key, 1);
		}
	}
	
	public String getContents() {
		StringBuffer buffer = new StringBuffer();
		
		for (K key : map.keySet()) {
			buffer.append(key.toString());
			buffer.append(" : ");
			buffer.append(map.get(key).toString());
			buffer.append(" [");
			buffer.append(accesses.get(key));
			buffer.append("]\n");
		}
		
		return new String(buffer);
	}
	
	/**
	 * Unit test function.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Cache<String, String> cache = new AccessCountCache<String, String>(3);
			
			System.out.println("Looking up aaa.");
			String result = cache.lookup("aaa");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}
			cache.insert("aaa", "aaaaa");
			System.out.println(cache.getContents());

			System.out.println("Looking up aaa.");
			result = cache.lookup("aaa");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}
			System.out.println("Inserting bbb.");
			cache.insert("bbb", "bbbbb");
			System.out.println(cache.getContents());

			System.out.println("Looking up aaa.");
			result = cache.lookup("aaa");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}
			System.out.println("Looking up bbb.");
			result = cache.lookup("bbb");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}

			cache.insert("ccc", "ccccc");
			System.out.println(cache.getContents());

			System.out.println("Looking up bbb.");
			result = cache.lookup("bbb");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}
			cache.insert("ddd", "ddddd");
			System.out.println(cache.getContents());
		
			Thread.sleep(1000);
			System.out.println(cache.getContents());

			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
