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

/**
 * Attempts to perform a task serially and retries if an exception is thrown.
 * Programming by exception is bad, m'kay, but sometimes it cannot be avoided.
 * 
 * The thread sleeps for a client-specified time (backing off linearly) between
 * retries.
 * 
 * @param <T>
 */
public abstract class Retry<T> {

	private final Sleeper sleeper;
	private final int retries;
	private static final int DEFAULT_RETRIES = 5;

	/**
	 * Create a new RetryWorker with a Sleeper object initialized to the
	 * specified values.
	 * @param stepMS
	 * @param maxMS
	 * @throws Exception
	 */
	public Retry(long stepMS, long maxMS, int retries) throws Exception {
		sleeper = new Sleeper(stepMS, maxMS);
		this.retries = retries;
	}
	
	/**
	 * Create a new RetryWorker with a Sleeper object initialized to the
	 * specified values.
	 * @param stepMS
	 * @param maxMS
	 * @throws Exception
	 */
	public Retry(long stepMS, long maxMS) throws Exception {
		this(stepMS, maxMS, DEFAULT_RETRIES);
	}

	/**
	 * Attempts to execute the contents of run(), retrying if an exception is
	 * thrown or the return value is null.
	 * If retries are exhausted, the last exception is thrown.
	 * @return
	 * @throws Exception
	 */
	public T start() throws Exception {
		Exception last_exception = null; 
		T result = null;
		
		for (int i = 0; i < retries; i++) {
			try {
				result = process();
			}
			catch (Exception e) {
				last_exception = e;
			}
			
			if (result != null) {
				return result;
			}

			sleeper.sleep();
		}
		throw last_exception;
	}	

	public abstract T process() throws Exception;
	
}
