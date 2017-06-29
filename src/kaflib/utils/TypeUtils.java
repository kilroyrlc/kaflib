package kaflib.utils;

import java.io.ObjectOutputStream;
import java.io.Serializable;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains utilities for various Java data types.
 */
public class TypeUtils {

	/**
	 * Returns a random element from the set.
	 * @param values
	 * @return
	 */
	public static <T> T getRandom(final Set<T> values) {
		if (values == null || values.size() == 0) {
			return null;
		}
		int index = RandomUtils.randomInt(values.size());
		Iterator<T> it = values.iterator();
		for (int i = 0; i < index; i++) {
			it.next();
		}
		return it.next();
	}
	
	/**
	 * Creates a list populated with n matching elements.
	 * @param size
	 * @param value
	 * @return
	 */
	public static <T> List<T> getList(final int size, final T value) {
		List<T> list = new ArrayList<T>();
		while (list.size() < size) {
			list.add(value);
		}
		return list;
	}
	
	/**
	 * Returns the varargs as a list.
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> getList(final T... values) {
		List<T> list = new ArrayList<T>();
		for (T t : values) {
			list.add(t);
		}
		return list;
	}

	/**
	 * Returns a list as such: [values][suffix].
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> prepend(final List<T> suffix, final T... values) {
		List<T> list = new ArrayList<T>();
		for (T t : values) {
			list.add(t);
		}
		list.addAll(suffix);
		return list;
	}
	
	/**
	 * Returns a list as such: [values][suffix].
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> prepend(final T suffix[], final T... values) {
		List<T> list = new ArrayList<T>();
		for (T t : values) {
			list.add(t);
		}
		list.addAll(Arrays.asList(suffix));
		return list;
	}
	
	/**
	 * Returns a collection of values based on the supplied collection of keys.
	 * Ignores keys not in the map.
	 * @param map
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	public static <T, U> Set<U> get(final Map<T, U> map, final Collection<T> keys) throws Exception {
		Set<U> set = new HashSet<U>();
		for (T t : keys) {
			if (map.containsKey(t)) {
				set.add(map.get(t));
			}
		}
		return set;
	}
	
	/**
	 * Returns a map containing the specified set of keys.
	 * @param map
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	public static <T, U> Map<T, U> getSubmap(final Map<T, U> map, final Collection<T> keys) throws Exception {
		Map<T, U> sub = new HashMap<T, U>();
		for (T t : keys) {
			if (map.containsKey(t)) {
				sub.put(t, map.get(t));
			}
		}
		return sub;
	}
	
	/**
	 * Creates a list containing prefix and all values.
	 * @param prefix
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> getList(final T prefix, final List<T> values) throws Exception {
		List<T> list = new ArrayList<T>();
		list.add(prefix);
		list.addAll(values);
		return list;
	}
	
	/**
	 * Creates a list containing prefix and all values.
	 * @param prefix
	 * @param values
	 * @return
	 * @throws Exception
	 */
	@SafeVarargs
	public static <T> List<T> getList(final List<T> prefix, T... values) throws Exception {
		List<T> list = new ArrayList<T>();
		list.addAll(prefix);
		for (T t : values) {
			list.add(t);
		}
		return list;
	}

	/**
	 * Returns the text after the final / of a url.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getFilename(final URL url) throws Exception {
		int index = url.toString().lastIndexOf('/');
		
		if (index < 0) {
			return url.toString();
		}
		else {
			return url.toString().substring(index + 1);
		}
	}
	
	/**
	 * Equals test with null checks.  If both are null, they are equal.
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> boolean equals(final T a, final T b) {
		if (a == null && b == null) {
			return true;
		}
		else if (a == null) {
			return false;
		}
		else if (b == null) {
			return false;
		}
		else {
			return a.equals(b);
		}
	}
	
	/**
	 * Returns the equivalence of two lists.
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> boolean equals(final List<T> a, final List<T> b) {
		if (a == null || b == null) {
			if (a == null && b == null) {
				return true;
			}
			return false;
		}
		
		if (a.size() != b.size()) {
			return false;
		}
		
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).equals(b.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns the equivalence of two sets.
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> boolean equals(final Set<T> a, final Set<T> b) {
		if (a == null || b == null) {
			if (a == null && b == null) {
				return true;
			}
			return false;
		}
		
		if (a.size() != b.size()) {
			return false;
		}
		
		for (T i : a) {
			if (!b.contains(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns true if the item is found in the collection.
	 * @param collection
	 * @param item
	 * @return
	 * @throws Exception
	 */
	public static <T> boolean lookup(final Collection<T> collection, final T item) throws Exception {
		for (T t : collection) {
			if (t.equals(item)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the item is found in the array.
	 * @param collection
	 * @param item
	 * @return
	 * @throws Exception
	 */
	public static <T> boolean lookup(final T collection[], final T item) throws Exception {
		for (T t : collection) {
			if (t.equals(item)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Breaks the specified set into n equal(-ish) size subsets.
	 * @param set
	 * @param subsets
	 * @return
	 * @throws Exception
	 */
	public static <T> List<Set<T>> subsets(final Set<T> set, final int subsets) throws Exception {
		CheckUtils.checkPositive(subsets, "subset count");
		if (subsets == 1) {
			return TypeUtils.getList(set);
		}
		List<Set<T>> subs = new ArrayList<Set<T>>();
		for (int i = 0; i < subsets; i++) {
			subs.add(new HashSet<T>());
		}
		int index = 0;
		for (T t : set) {
			subs.get(index).add(t);
			index = (index + 1) % subsets;
		}
		return subs;
	}
	
	/**
	 * Removes the specified number of elements from the collection, returns
	 * them as a list.
	 * @param collection
	 * @param count
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> remove(Collection<T> collection, final int count) throws Exception {
		List<T> list = new ArrayList<T>();
		
		while (list.size() < count && collection.size() > 0) {
			T value = collection.iterator().next();
			list.add(value);
			collection.remove(value);
		}
		return list;
	}
	
	/**
	 * Returns a histogram of items in the collection.
	 * @param collection
	 * @return
	 * @throws Exception
	 */
	public static <T> Map<T, Integer> getHistogram(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}		
		Map<T, Integer> histogram = new HashMap<T, Integer>();
		for (T item : collection) {
			if (histogram.containsKey(item)) {
				histogram.put(item, histogram.get(item) + 1);
			}
			else {
				histogram.put(item, 1);
			}
		}
		return histogram;
	}
	
	/**
	 * Returns a list of unique elements by the most frequent.
	 * @param collection
	 * @return
	 * @throws Exception
	 */
	public static<T> List<T> getByMostFrequent(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return null;
		}
		
		final Map<T, Integer> histogram = getHistogram(collection);
		List<T> list = new ArrayList<T>();
		list.addAll(histogram.keySet());
		Collections.sort(list, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (histogram.get(o1) > histogram.get(o2)) {
					return -1;
				}
				else if (histogram.get(o1) < histogram.get(o2)) {
					return 1;
				}
				return 0;
			}
		});
		return list;
	}
	
	/**
	 * Shorthand for iterator().next() used to access the first element in a
	 * collection that isn't necessarily indexed.
	 * @param collection
	 * @return
	 * @throws Exception
	 */
	public static<T> T getItem(Collection<T> collection) throws Exception {
		CheckUtils.checkNonEmpty(collection, "collection");
		return collection.iterator().next();
	}
	
	
}
