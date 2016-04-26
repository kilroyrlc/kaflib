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

import java.util.Random;

import kaflib.utils.CheckUtils;

/**
 * Defines a mutex type.
 */
public class Mutex {

	private final Lock mutex;	
	private Integer client;	

	private static final int MAX_CHECK_FREQUENCY_MS = 500;
	
	/**
	 * Creates the mutex.
	 */
	public Mutex() {
		mutex = new Lock();
		client = null;
	}

	/**
	 * Nonblocking lock.  Returns an unlock combo if successful, null if
	 * unsuccessful.
	 * @return
	 */
	public Integer lock() {
		if (mutex.tset() == true) {
			int combo = (new Random()).nextInt();
			client = combo;
			return combo;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Blocking lock.  Returns null if timeout occurs.
	 * @param timeout_ms
	 * @return
	 * @throws Exception
	 */
	public Integer lock(final long timeoutMS) throws Exception {
		CheckUtils.checkPositive(timeoutMS, "timeout");
		
		long start = System.currentTimeMillis();
		int wait = 100;
		
		while (!mutex.tset()) {
			if (System.currentTimeMillis() - start > timeoutMS) {
				return null;
			}
			
			Thread.sleep(wait);
			
			if (wait < MAX_CHECK_FREQUENCY_MS) {
				wait += 100;
			}
		}
		
		int combo = new Random().nextInt();
		client = combo;
		return combo;
	}
	
	/**
	 * Unlocks the mutex.  The combo returned from lock() must be supplied.
	 * This ensures the client that locked the resource also unlocks it.
	 * @param combo
	 * @throws Exception
	 */
	public void unlock(final int combo) throws Exception {
		if (combo == client) {
			mutex.unlock();
		}
		else {
			throw new Exception("Lock has been obtained by another client.");
		}
	}

	/**
	 * Utility to check if a combo is locked or not.  Throws on locked.
	 * @param combo
	 * @throws Exception
	 */
	public static void checkCombo(int combo) throws Exception {
		checkCombo(combo, null);
	}
	
	/**
	 * Utility to check if a combo is locked or not.  Throws on locked.
	 * @param combo
	 * @throws Exception
	 */
	public static void checkCombo(final Integer combo, final String message) throws Exception {
		if (combo == null) {
			if (message != null) {
				throw new Exception("Mutex locked: " + message + ".");
			}
			else {
				throw new Exception("Mutex locked.");
			}
		}
	}
	
}


/**
 * Defines the internal mutex type.  That is, the tset functionality.
 */
class Lock {
	private boolean locked = false;
	
	/**
	 * Atomic test-and-set.
	 * @return
	 */
	public synchronized boolean tset() {
		if (locked) {
			return false;
		}
		else {
			locked = true;
			return true;
		}
	}
	
	/**
	 * Releases the lock.
	 * @throws Exception
	 */
	public synchronized void unlock() throws Exception {
		if (!locked) {
			throw new Exception("Mutex already unlocked.");
		}
		locked = false;
	}
	
}
