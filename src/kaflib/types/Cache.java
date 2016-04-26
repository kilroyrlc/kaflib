package kaflib.types;

/*
 * Copyright (c) 2015 Christopher Ritchie
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

import java.util.HashMap;
import java.util.Map;

import kaflib.utils.CheckUtils;

/**
 * Defines a key-value cache that evicts either based on recent use or total
 * use.
 */
public class Cache <K, V> {

	public enum EvictionMethod {LRU, ACCESSES};

	// Map of keys to value and integer value holding usage data.
	private final Map<K, Pair<V, Integer>> map;
	private final EvictionMethod method;
	private final int size;

	
	// Defines a counter to represent time.  It increments every access rather
	// than asking System for time in millis.
	private int counter;

	// Mutex and default timeout to handle concurrency.
	private Mutex mutex;
	private long lockTimeoutMS = 5000;

	/**
	 * Creates the cache with the given retention/eviction method and size.
	 * @param method
	 * @param size
	 * @throws Exception
	 */
	public Cache(final EvictionMethod method, int size) throws Exception {
		CheckUtils.check(method, "eviction method");
		CheckUtils.checkPositive(size);
		
		this.method = method;
		this.size = size;
		counter = 0;
		mutex = new Mutex();
		
		map = new HashMap<K, Pair<V, Integer>>(size + 1);
	}

	/**
	 * Checks the cache for the given key.  Returns null if it is not found,
	 * returns the item if it is.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public V lookup(final K key) throws Exception {
		CheckUtils.check(key, "key");
		
		int combo = mutex.lock(lockTimeoutMS);
		
		if (!map.containsKey(key)) {
			mutex.unlock(combo);
			return null;
		}
		
		accessed(key);
		V value = map.get(key).getKey();
		
		mutex.unlock(combo);
		
		return value;
	}

	/**
	 * Adds the specified key and value to the cache or resets the item if it
	 * already exists.
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public void insert(final K key, final V value) throws Exception {
		CheckUtils.check(key, "key");
		
		evict();
		
		int combo = mutex.lock(lockTimeoutMS);
		
		Pair<V, Integer> pair;
		
		if (map.containsKey(key)) {
			pair = map.get(key);
			pair.setKey(value);
		}
		else {
			pair = new Pair<V, Integer>(value, 0);
		}

		map.put(key, pair);
		accessed(key);
		
		mutex.unlock(combo);
	}

	/**
	 * Removes all items from the cache.
	 */
	public void flush() throws Exception {
		int combo = mutex.lock(lockTimeoutMS);
		map.clear();
		mutex.unlock(combo);
	}
	
	/**
	 * Evicts lines from the cache until there is room for one more.
	 * @throws Exception
	 */
	private void evict() throws Exception {
		int combo = mutex.lock(lockTimeoutMS);

		while (map.size() > size - 1) {
			map.remove(findEvictee());
		}
		
		mutex.unlock(combo);
	}
	
	/**
	 * Finds the eviction candidate based on the eviction method.  Note this
	 * function does not lock the map so it is not thread safe.
	 */
	private K findEvictee() throws Exception {
		int evicteeAccess = Integer.MAX_VALUE;
		K evictee = null;
		
		for (K key : map.keySet()) {
			int access = map.get(key).getValue();
			
			if (method == EvictionMethod.LRU || method == EvictionMethod.ACCESSES) {
				// If the accesses/access time value is less than the least,
				// this is the new candidate.
				if (access < evicteeAccess) {
					evictee = key;
					evicteeAccess = access;
				}
				// If they are equal, don't always evict the first one.  This
				// could be random, but meh.
				else if (access == evicteeAccess && System.currentTimeMillis() % 4 == 0) {
					evictee = key;
					evicteeAccess = access;
				}
				else {
					// Don't evict this.
				}
			}
			else {
				throw new Exception("Added eviction method without defining protocol.");
			}			
		}

		return evictee;
	}
	
	/**
	 * Handles updating a cache line for an access.
	 * @param key
	 * @throws Exception
	 */
	private void accessed(final K key) throws Exception {
		CheckUtils.check(key, "key");
		
		if (method == EvictionMethod.LRU) {
			// Cycle time back to 0.
			if (counter == Integer.MAX_VALUE - 1) {
				counter = 0;
			}

			map.get(key).setValue(counter++);
		}
		else if (method == EvictionMethod.ACCESSES) {
			map.get(key).setValue(Math.min(map.get(key).getValue() + 1, Integer.MAX_VALUE - 1));
		}
		else {
			throw new Exception("Added eviction method without defining protocol.");
		}
	}

	/**
	 * Writes the contents of the buffer.
	 * @return
	 */
	public String getContents() {
		StringBuffer buffer = new StringBuffer();
		
		for (K key : map.keySet()) {
			buffer.append(key.toString());
			buffer.append(" : ");
			buffer.append(map.get(key).getKey().toString());
			buffer.append(" [");
			buffer.append(map.get(key).getValue().toString());
			buffer.append("]\n");
		}
		
		return new String(buffer);
	}

	/**
	 * Returns a string with basic information.
	 */
	public String toString() {
		return new String(method + " cache, size: " + map.size());
	}
	
	/**
	 * Unit test function.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Cache<String, String> cache = new Cache<String, String>(Cache.EvictionMethod.LRU, 3);
			
			System.out.println("Looking up aaa.");
			String result = cache.lookup("aaa");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}
			cache.insert("aaa", "aaaaa");
			System.out.println(cache.toString());

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
			System.out.println(cache.toString());

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
			System.out.println(cache.toString());

			System.out.println("Looking up bbb.");
			result = cache.lookup("bbb");
			if (result != null) {
				System.out.println("Hit.");
			}
			else {
				System.out.println("Miss.");
			}
			cache.insert("ddd", "ddddd");
			System.out.println(cache.toString());
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
