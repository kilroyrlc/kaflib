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

import java.util.ArrayList;
import java.util.List;

import kaflib.utils.CheckUtils;

/**
 * Defines a key-value pair.
 */
public class Pair <K, V> {

	private K key;
	private V value;
	private boolean readOnly;
	
	/**
	 * Creates a pair with null values.
	 */
	public Pair() {
		this(null, null, false);
	}
	
	/**
	 * Creates a pair with the specified key/first and value/second.
	 * @param key
	 * @param value
	 */
	public Pair(final K key, final V value) {
		this(key, value, false);
	}

	/**
	 * Creates a pair with the specified key/first and value/second.
	 * @param key
	 * @param value
	 * @param finalize
	 */
	public Pair(final K key, final V value, final boolean finalize) {
		this.key = key;
		this.value = value;
		readOnly = finalize;
	}
	
	/**
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * @return the key
	 */
	public K getFirst() {
		return key;
	}
	
	/**
	 * @param key the key to set
	 */
	public void setKey(K key) throws Exception {
		if (readOnly) {
			throw new Exception("Pair was marked read-only.");
		}
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * @return the value
	 */
	public V getSecond() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(V value) throws Exception {
		if (readOnly) {
			throw new Exception("Pair was marked read-only.");
		}
		this.value = value;
	}	
	
	/**
	 * Returns whether or not either value is null.
	 * @return
	 */
	public boolean hasNull() {
		return key == null || value == null;
	}
	
	/**
	 * Returns the pair, space separated.  Null values are represented as
	 * "[null]".
	 */
	public String toString() {
		if (key != null && value != null) {
			return key.toString() + " " + value.toString();
		}
		else if (key != null) {
			return key.toString() + " [null]";
		}
		else {
			return "[null]" + value.toString();
		}
	}
	
	/**
	 * Returns a list of pairs, adding value as the value to each K in the
	 * input list.
	 * 
	 * So [k0, k1, k2], v0 becomes [[k0, v0], [k1, v0], [k2, v0]].
	 * 
	 * @param list
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static <K, V> List<Pair<K, V>> getList(List<K> list, V value) throws Exception {
		CheckUtils.check(list, "list");
		
		List<Pair<K, V>> pair_list = new ArrayList<Pair<K, V>>();
		
		for (K k : list) {
			pair_list.add(new Pair<K, V>(k, value));
		}
		return pair_list;
	}
	
	/**
	 * Determines an aggregate hash code based on the hashes of each value.
	 */
	public int hashCode() {
		return ("" + key.hashCode() + " " + value.hashCode()).hashCode();
	}
	
	/**
	 * Returns true if the pairs are equivalent.
	 */
	public boolean equals(Object o) {
		if (o instanceof Pair<?, ?>) {
			return key.equals(((Pair<?, ?>) o).getKey()) && value.equals(((Pair<?, ?>) o).getValue());
		}
		else {
			return false;
		}
	}
	
}
