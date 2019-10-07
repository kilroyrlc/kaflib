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
public abstract class Cache <K, V> {

	// Map of keys to value and integer value holding usage data.
	protected final Map<K, V> map;
	private int size;
	
	private Worker evict_worker;
	
	/**
	 * Creates the cache with the given retention/eviction method and size.
	 * @param method
	 * @param size
	 * @throws Exception
	 */
	public Cache(final int size) throws Exception {
		CheckUtils.checkPositive(size);
		setSize(size);
		map = new HashMap<K, V>(size + 3);
	}
	
	public final void setSize(final int size) throws Exception {
		this.size = size;
		if (map != null && map.size() > size) {
			startEvictions();
		}
	}
	
	protected synchronized void startEvictions() throws Exception {
		if (map.size() <= size || evict_worker != null && !evict_worker.isDone()) {
			return;
		}
		evict_worker = new Worker() {

			@Override
			protected void process() throws Exception {
				int count = map.size() - size;
				while (count > 0) {
					evict(count);
					count = map.size() - size;
				}
			}
			
		};
		evict_worker.start();
	}
	
	protected abstract void evict(final int count);
	protected abstract void accessed(final K key);
	
	
	
	/**
	 * Checks the cache for the given key.  Returns null if it is not found,
	 * returns the item if it is.
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public V lookup(final K key) {
		if (key == null) {
			return null;
		}

		V value = map.get(key);
		if (value != null) {
			accessed(key);
		}
		return value;
	}

	/**
	 * Returns whether or not the key is contained in the cache.
	 * @param key
	 * @return
	 */
	public boolean contains(final K key) {
		if (key == null || !map.containsKey(key)) {
			return false;
		}
		else {
			return true;
		}
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

		if (map.containsKey(key)) {
			return;
		}
		else {
			map.put(key, value);
			accessed(key);
			if (map != null && map.size() > size) {
				startEvictions();
			}
		}
	}
	
	protected void remove(final K key) {
		map.remove(key);
	}

	/**
	 * Removes all items from the cache.
	 */
	public synchronized void clear() throws Exception {
		map.clear();
	}

	public String toString() {
		return "Cache of size: " + map.size();
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
			buffer.append(map.get(key).toString());
			buffer.append(" [");
			buffer.append(map.get(key).toString());
			buffer.append("]\n");
		}
		
		return new String(buffer);
	}


	
}
