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
 * Provides an alternative to Thread.sleep() that increases sleep time linearly
 * until the desired even occurs and reset() is called.
 * 
 * Specifically, if something is probably going to happen soon, but if it
 * doesn't you don't want to busywait, this is for you.
 */
public class Sleeper {
	
	// The number of milliseconds to increase the wait each cycle.
	private final long stepMS;
	
	// The maximum number of milliseconds to wait each cycle.
	private final Long maxMS;
	
	private long currentMS;
	private long totalMS;
	
	private Long timeoutMS;
	
	/**
	 * Creates the object with default values.
	 * @param step
	 * @param max
	 * @throws Exception
	 */
	public Sleeper() throws Exception {
		this(100);
	}

	/**
	 * Creates the object with specified minimum/step and max values.
	 * @param step
	 * @param max
	 * @throws Exception
	 */
	public Sleeper(final long stepMS) throws Exception {
		if (stepMS < 1) {
			throw new Exception("Invalid paramater: " + stepMS + " step.");
		}
		
		this.stepMS = stepMS;
		this.maxMS = null;
		this.currentMS = this.stepMS;
		this.timeoutMS = null;
	}

	
	/**
	 * Creates the object with specified minimum/step and max values.
	 * @param step
	 * @param max
	 * @throws Exception
	 */
	public Sleeper(final long stepMS, final long maxMS) throws Exception {
		if (stepMS < 1 || maxMS < stepMS) {
			throw new Exception("Invalid paramaters: " + stepMS + " step and " + maxMS + " max.");
		}
		
		this.stepMS = stepMS;
		this.maxMS = maxMS;
		this.currentMS = this.stepMS;
		this.timeoutMS = null;
	}
	
	/**
	 * Sets the a timeout for this object.  Sleep() will throw if the timeout
	 * has been exceeded by sequential calls to sleep without a reset.
	 * Note this depends on reset being used properly.
	 * @param timeoutMS
	 * @throws Exception
	 */
	public void setTimeout(long timeoutMS) throws Exception {
		this.timeoutMS = timeoutMS;
	}
	
	/**
	 * Resets the sleep time to the start/ step value.  Do this when the thing
	 * you've been waiting for happens.
	 * @throws Exception
	 */
	public void reset() {
		this.currentMS = this.stepMS;
		this.totalMS = 0;
	}
	
	/**
	 * Sleeps for the specified time according to linear backoff:
	 *  - The step/minimum, if reset was called.
	 *  - The the last value plus the step.
	 *  Throws an exception if a timeout was specified and has been reached 
	 *  since the last reset call.
	 * @throws Exception
	 */
	public void sleep() throws Exception {
		if (timeoutMS != null) {
			if (totalMS >= timeoutMS) {
				throw new Exception("Wait timed out after " + totalMS + "ms.");
			}
		}
		
		Thread.sleep(currentMS);

		if (timeoutMS != null) {
			totalMS += currentMS;
		}
		
		if (maxMS == null || currentMS < maxMS) {
			currentMS += stepMS;
		}
	}
	
}
