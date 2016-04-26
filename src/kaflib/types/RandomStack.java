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
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Holds a collection of objects inserted in random order.
 */
public class RandomStack<T> {
	
	private final List<T> list;
	private final Random random;
	
	/**
	 * Create an empty stack.
	 */
	public RandomStack() {
		list = new ArrayList<T>();
		random = new Random();
	}
	
	/**
	 * Create a stack starting with the specified values.  Their order will be
	 * randomized.
	 * @param values
	 */
	public RandomStack(final Collection<T> values) {
		this();
		
		if (values != null && values.size() > 0) {
			for (T t : values) {
				push(t);
			}
		}
	}
	
	/**
	 * Add the specified value to a random place in the stack.
	 * @param value
	 */
	public void push(final T value) {
		list.add(random.nextInt(list.size() + 1), value);
	}
	
	/**
	 * Adds the specified values to the stack.
	 * @param values
	 */
	public void push(final Collection<T> values) {
		for (T value : values) {
			push(value);
		}
	}
	
	/**
	 * Remove the top value of the stack, a random element previously inserted.
	 * @return
	 */
	public T pop() {
		if (list.size() == 0) {
			return null;
		}
		else {
			return list.remove(0);
		}
	}
	
	/**
	 * Returns the specified number of values, or fewer if the stack contains
	 * fewer.
	 * @param count
	 * @return
	 */
	public List<T> pop(final int count) {
		List<T> values = new ArrayList<T>();
		while (values.size() < count && list.size() > 0) {
			values.add(pop());
		}
		return values;
	}
	
	/**
	 * View the top value of the stack, a random element previously inserted.
	 * @return
	 */
	public T top() {
		if (list.size() == 0) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
	/**
	 * Returns the number of elements in the stack.
	 * @return
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * Returns all list elements.
	 * @return
	 */
	public List<T> getAll() {
		return list;
	}
}
