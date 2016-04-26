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

import java.util.Collection;

/**
 * Thread subtype that supports execution state and terminate requests.
 */
public abstract class Worker extends Thread {

	private boolean terminate;
	private boolean done;				
	private Exception exception;
	private Sleeper sleeper;	
	
	/**
	 * Create a new Worker.
	 * @throws Exception
	 */
	public Worker() throws Exception {
		done = false;
		terminate = false;
		sleeper = new Sleeper();
		exception = null;
	}
	
	/**
	 * Create a new Worker with a LinearBackoff object initialized to the
	 * specified values.
	 * @param stepMS
	 * @param maxMS
	 * @throws Exception
	 */
	protected Worker(final long stepMS, final long maxMS) throws Exception {
		done = false;
		sleeper = new Sleeper(stepMS, maxMS);
		exception = null;
	}
	
	/**
	 * Returns whether or not this worker has finished all processing.
	 * @return
	 */
	public boolean isDone() {
		return done;
	}
	
	/**
	 * Returns the sleeper object.
	 * @return
	 */
	public Sleeper getSleeper() {
		return sleeper;
	}
	
	/**
	 * Invokes the sleeper object sleep function.
	 * @throws Exception
	 */
	public void sleep() throws Exception {
		sleeper.sleep();
	}

	/**
	 * Indicates that the thread should cease processing and return as quickly
	 * as possible.
	 */
	public void terminate() {
		terminate = true;
	}
	
	/**
	 * Blocks until done is set.
	 * @param timeout
	 * @throws Exception
	 */
	public void blockUntilDone(long timeout) throws Exception {
		long start = System.currentTimeMillis();
		while (!isDone()) {
			sleeper.sleep();
			if (timeout > 0 && System.currentTimeMillis() - start >= timeout) {
				throw new Exception("Timed out waiting for worker " + 
									toString() + " to finish.");
			}
		}
	}
	
	/**
	 * Returns the last thrown exception, if it exists.
	 * @return
	 */
	public Exception thrownException() {
		return exception;
	}
	
	/**
	 * Overrides Thread's run(), call by invoking start().
	 */
	public void run() {
		try {
			process();
			done = true;
		}
		catch (Exception e) {
			if (e != null) {
				e.printStackTrace();
				exception = e;
			}
			done = true;
		}
	}
	
	/**
	 * Called by a subtype to find out if the client asked this Worker to
	 * terminate (e.g. user requested halt).
	 * @return
	 */
	public boolean terminateInvoked() {
		return terminate;
	}
	
	/**
	 * Subtype-defined processing function.  Invoked asynchronously via
	 * start().  Must check for terminateInvoked().
	 * A LinearBackoff is supplied by this type to assist sleeps between loops.
	 */
	protected abstract void process() throws Exception;
	
	/**
	 * Returns false if any of the supplied workers is still processing, true
	 * if they are all done.
	 * @param workers
	 * @return
	 */
	public static boolean areDone(final Collection<? extends Worker> workers) {
		if (workers == null || workers.size() == 0) {
			return true;
		}
		for (Worker worker : workers) {
			if (!worker.isDone()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Blocks until all workers are done, throws if the timeout is reached.
	 * @param workers
	 * @param timeout
	 * @throws Exception
	 */
	public static void waitUntilDone(final Collection<? extends Worker> workers, final long timeoutMS) throws Exception {
		Sleeper sleeper = null;
		if (timeoutMS > 0) {
			sleeper = new Sleeper(250, timeoutMS / 2);
			sleeper.setTimeout(timeoutMS);
		}
		else {
			sleeper = new Sleeper(250, 5000);
		}
		while (!areDone(workers)) {
			sleeper.sleep();
		}
	}
}
