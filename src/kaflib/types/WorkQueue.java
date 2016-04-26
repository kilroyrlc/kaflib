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

/**
 * Defines a queue of workers that are executed serially.
 */
public class WorkQueue extends Worker {

	private final List<Worker> workers;
	private int completed = 0;
	private int total = 0;
	
	/**
	 * Creates the queue.
	 * @throws Exception
	 */
	public WorkQueue() throws Exception {
		workers = new ArrayList<Worker>();
	}

	/**
	 * Creates the queue with the supplied workers.
	 * @param workers
	 * @throws Exception
	 */
	public WorkQueue(final List<Worker> workers) throws Exception {
		this();
		workers.addAll(workers);
		total = workers.size();
	}

	/**
	 * Adds a worker to the queue.
	 * @param worker
	 * @throws Exception
	 */
	public void add(final Worker worker) throws Exception {
		if (isDone()) {
			throw new Exception("Cannot add worker to completed queue.");
		}
		workers.add(worker);
		total++;
	}
	
	/**
	 * Returns the number of completed workers.
	 * @return
	 */
	public int getCompleted() {
		return completed;
	}
	
	/**
	 * Returns the total number of workers.
	 * @return
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Returns the percent complete, assuming all workers are equal.
	 * @return
	 */
	public int getPercentDone() {
		return (100 * completed) / total;
	}
	
	@Override
	protected void process() throws Exception {
		while (workers.size() > 0) {
			Worker worker = workers.remove(0);
			worker.start();
			worker.blockUntilDone(-1);
			completed++;
		}
	}
	
	/**
	 * Processes all work queues, returns when all have completed.
	 * @param queues
	 * @throws Exception
	 */
	public static void process(final Collection<WorkQueue> queues) throws Exception {
		for (WorkQueue queue : queues) {
			queue.start();
		}
		
		for (WorkQueue queue : queues) {
			if (!queue.isDone()) {
				queue.blockUntilDone(-1);
			}
		}
	}
	
	/**
	 * Returns the percent complete across a collection of work queues.
	 * @param queues
	 * @return
	 * @throws Exception
	 */
	public static int getPercentDone(final Collection<WorkQueue> queues) throws Exception {
		int done = 0;
		int total = 0;
		for (WorkQueue queue : queues) {
			done += queue.getCompleted();
			total += queue.getTotal();
		}
		return (100 * done) / total;
	}
}
