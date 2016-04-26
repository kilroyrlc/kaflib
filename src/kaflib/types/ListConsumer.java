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

/**
 * Asynchronously processess a queue of items.  How to process them is defined
 * by the subtype.
 * @param <T>
 */
public abstract class ListConsumer<T> extends Consumer {

	private final List<T> queue;
	
	/**
	 * Creates the object with an empty queue.
	 * @throws Exception
	 */
	public ListConsumer() throws Exception {
		super();
		queue = new ArrayList<T>();
	}
	
	/**
	 * Creates the object with a sleeper initialized to the specified values.
	 * @param stepMS
	 * @param maxMS
	 * @throws Exception
	 */
	protected ListConsumer(long stepMS, long maxMS) throws Exception {
		super(stepMS, maxMS);
		queue = new ArrayList<T>();
	}
	
	/**
	 * Add a part to the queue.  Only valid before finish() has been invoked.
	 * @param part
	 * @throws Exception
	 */
	public void add(final T item) throws Exception {
		if (finishInvoked() || queue == null) {
			throw new Exception("Cannot add an item after finish has been called.");
		}
		queue.add(item);
	}
	
	/**
	 * Returns the queue size.
	 * @return
	 */
	public int size() {
		if (queue == null) {
			return 0;
		}
		else {
			return queue.size();
		}
	}
	
	/**
	 * Does the processing.  Loops until finish and empty queue.
	 */
	protected void process() throws Exception {
		// Loop while there's items in the queue or finish has not been 
		// invoked.
		while (queue.size() > 0 || !finishInvoked()) {
			if (terminateInvoked()) {
				break;
			}
			
			// Queue is empty, sleep based on linear backoff.
			if (queue.size() == 0) {
				sleep();
				continue;
			}
			else {
				getSleeper().reset();
			}
			process(queue.remove(0));
		}
	}
	
	protected abstract void process(final T item) throws Exception;

}
