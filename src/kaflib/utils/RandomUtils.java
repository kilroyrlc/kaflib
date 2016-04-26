package kaflib.utils;

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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * Utilities for generating random values of different types.
 */
public class RandomUtils {

	private static final Random random = new Random();
	
	/**
	 * Returns evenly-distributed true or false.
	 * @return
	 */
	public static boolean randomBoolean() {
		return random.nextBoolean();
	}
	
	/**
	 * Returns true n percent of the time.
	 * @param percentTrue
	 * @return
	 * @throws Exception
	 */
	public static boolean randomBoolean(final int percentTrue) throws Exception {
		if (percentTrue < 0 || percentTrue > 100) {
			throw new Exception("Not a percent: " + percentTrue + ".");
		}
		if (randomInt(100) <= percentTrue) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns a random integer in the full signed range.
	 * @return
	 */
	public static int randomInt() {
		return random.nextInt();
	}
	
	/**
	 * Returns a random long in the full signed range.
	 * @return
	 */
	public static long randomLong() {
		return random.nextLong();
	}
	
	/**
	 * Returns a random integer, up to bound (exclusive).
	 * @param bound
	 * @return
	 */
	public static int randomInt(final int bound) {
		return random.nextInt(bound);
	}
	
	/**
	 * Returns a random long, up to bound (exclusive).
	 * @param bound
	 * @return
	 */
	public static long randomLong(final long bound) {
		return Math.abs(random.nextLong()) % bound;
	}
	

	/**
	 * Returns a random integer at least min, <= max.
	 * @param bound
	 * @return
	 */
	public static int randomInt(final int min, final int max) throws Exception {
		if (max <= min) {
			throw new Exception("Invalid max <= min.");
		}
		
		return min + randomInt(max - min);
	}
	
	/**
	 * Returns a random long at least min, <= max.
	 * @param bound
	 * @return
	 */
	public static long randomLong(final long min, final long max) throws Exception {
		if (max <= min) {
			throw new Exception("Invalid max <= min.");
		}
		
		return min + randomLong(max - min);
	}
	
	/**
	 * Generates an n-length string of random digits.  This could be
	 * implemented using format strings but would probably not handle
	 * lengths > int size.
	 * @param length
	 * @return
	 */
	public static String randomIntString(final int length) {
		int value = randomInt(Integer.MAX_VALUE);
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < length; i++) {
			if (value <= 0) {
				value = randomInt(Integer.MAX_VALUE);	
			}
			buffer.append(value % 10);
			value = value / 10;
		}
		return new String(buffer);
	}

	/**
	 * Returns a date up to msInFuture milliseconds into the future.
	 * @param msInFuture
	 * @return
	 * @throws Exception
	 */
	public static Date randomFutureDate(final long msInFuture) throws Exception {
		long time = Calendar.getInstance().getTimeInMillis();
		time += randomLong(msInFuture);
		return new Date(time);
	}
	
	/**
	 * Returns a date up to msInFuture milliseconds into the future.
	 * @param msInFuture
	 * @return
	 * @throws Exception
	 */
	public static Date randomFutureTime(final long minMsInFuture, final long maxMsInFuture) throws Exception {
		long time = Calendar.getInstance().getTimeInMillis();
		time += randomLong(minMsInFuture, maxMsInFuture);
		return new Date(time);
	}

	/**
	 * Returns a date up to msInFuture milliseconds into the future.
	 * @param msInFuture
	 * @return
	 * @throws Exception
	 */
	public static Date randomFutureDate(final int minDaysInFuture, final int maxDaysInFuture) throws Exception {
		Calendar calendar = Calendar.getInstance();
		int days = randomInt(minDaysInFuture, maxDaysInFuture);
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}
	
	
	/**
	 * Returns a random value from the set.
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static <T> T getRandom(final Set<T> values) throws Exception {
		CheckUtils.checkNonEmpty(values, "values");
		int random = randomInt(values.size());
		Iterator<T> iterator = values.iterator();
		for (int i = 0; i < random; i++) {
			iterator.next();
		}
		return iterator.next();
	}
	
	
	
}
