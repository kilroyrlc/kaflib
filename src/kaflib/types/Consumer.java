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
 * Defines a Worker subtype that consumes an unbounded amount of information.
 * Basically the the thread runs until the client indicates it is finished
 * supplying data for consumption.
 */
public abstract class Consumer extends Worker {

	private boolean finish;	
	
	/**
	 * Create a new Worker.
	 * @throws Exception
	 */
	public Consumer() throws Exception {
		super();
		finish = false;
	}
	
	/**
	 * Create a new Worker with a Sleeper object initialized to the
	 * specified values.
	 * @param stepMS
	 * @param maxMS
	 * @throws Exception
	 */
	protected Consumer(long stepMS, long maxMS) throws Exception {
		super(stepMS, maxMS);
		finish = false;
	}
	
	/**
	 * Returns whether or not finish has been invoked but the process function
	 * has not yet returned.
	 * @return
	 */
	public boolean isFinishing() {
		return !isDone() && finish;
	}
	
	/**
	 * Invoked by the client to tell this worker to stop looping when it has
	 * completed processing.
	 * @throws Exception
	 */
	public void finish() throws Exception {
		if (finish == true) {
			throw new Exception("Finish already set to true.");
		}
		
		finish = true;
	}
	
	/**
	 * Main body of execution, calls the parent run() which in turn calls the
	 * subtype-defined process() which now has a finishInvoked() function to
	 * see if its client wants it to stop.
	 */
	public void run() {
		super.run();
		
		try {
			if (finish == false) {
				throw new Exception("Process() returned before finish() was called.");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Can be called by a subtype to find out if the client asked this Worker
	 * to finish (i.e. there is no more input).
	 * @return
	 */
	public boolean finishInvoked() {
		return finish;
	}
	
}
