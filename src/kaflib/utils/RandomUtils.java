package kaflib.utils;

import java.util.ArrayList;
import java.util.Arrays;

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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import kaflib.types.Direction;
import kaflib.types.Percent;

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
	
	public static boolean randomBoolean(final Percent percentTrue) throws Exception {
		if (percentTrue.get() < 0 || percentTrue.get() > 100) {
			throw new Exception("Not a percent: " + percentTrue + ".");
		}
		if (randomInt(100) <= percentTrue.get()) {
			return true;
		}
		else {
			return false;
		}		
	}
	
	public static boolean randomBoolean(final double percentTrue) throws Exception {
		return randomBoolean((int) (percentTrue * 100));
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

	public static Integer randomD6() {
		try {
			return randomInt(1, 6);
		}
		catch (Exception e) {
			System.err.println("Invalid hard coded value for d6.");
			return null;
		}
	}

	/**
	 * Returns a random integer at least min, <= max.
	 * @param bound
	 * @return
	 */
	public static int randomInt(final int min, final int max) throws Exception {
		if (max < min) {
			throw new Exception("Invalid max " + max + " < min " + min + ".");
		}
		if (min == max) {
			return min;
		}
		
		return min + randomInt(max - min + 1);
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
	 * Returns a random long at least min, <= max.
	 * @param bound
	 * @return
	 */
	public static double randomDouble(final double min, final double max) throws Exception {
		if (max <= min) {
			throw new Exception("Invalid max <= min.");
		}
		
		return ThreadLocalRandom.current().nextDouble(min, max);
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
	 * Returns a date up to the specified days into the past.
	 * @param msInFuture
	 * @return
	 * @throws Exception
	 */
	public static Date randomPastDate(final int minDaysInPast, final int maxDaysInPast) throws Exception {
		Calendar calendar = Calendar.getInstance();
		int days = randomInt(minDaysInPast, maxDaysInPast);
		calendar.add(Calendar.DATE, -1 * days);
		return calendar.getTime();
	}
	
	/**
	 * Returns a random value from the array.
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static <T> T getRandom(final T values[]) throws Exception {
		CheckUtils.checkNonEmpty(values, "values");
		return values[randomInt(values.length)];
	}	
	
	/**
	 * Returns a random value from the set.
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static <T> T getRandom(final Collection<T> values) {
		if (values.size() == 0) {
			return null;
		}
		
		int random = randomInt(values.size());
		
		if (values instanceof List) {
			return ((List<T>) values).get(random);
		}
		else {
			Iterator<T> iterator = values.iterator();
			for (int i = 0; i < random; i++) {
				iterator.next();
			}
			return iterator.next();
		}
	}	

	public static <T> T getRandom(final Collection<T> values, T not) {
		Collection<T> filtered = new ArrayList<T>();
		for (T value : values) {
			if (!value.equals(not)) {
				filtered.add(value);
			}
		}
		return getRandom(filtered);
	}
	
	/**
	 * Returns a specified number of values from the set, at random.
	 * @param values
	 * @param count
	 * @return
	 * @throws Exception
	 */
	public static <T> Set<T> getRandom(final Set<T> values, 
									   final int count) throws Exception {
		Set<T> randoms = new HashSet<T>();

		// Use iterators to get random values.
		if (count < values.size() / 4) {
			while (randoms.size() < count) {
				randoms.add(RandomUtils.getRandom(values));
			}
		}
		// Create a list, select random indices from it, convert it back to a set.
		else {
			List<T> list = new ArrayList<T>();
			list.addAll(values);
			randoms.addAll(RandomUtils.getRandom(list, count));
		}
		return randoms;
	}
	
	/**
	 * Returns a specified number of values from the list, at random.  Indices
	 * are not repeated, but if multiple indices have identical values there 
	 * could be duplicates.
	 * @param values
	 * @param count
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> getRandom(final List<T> values,
										final int count) throws Exception {
		if (count >= values.size()) {
			return values;
		}
		
		Set<Integer> indices = RandomUtils.randomSet(Math.min(count, values.size()),
													 0, 
													 values.size() - 1);
		List<T> list = new ArrayList<T>();
		for (Integer i : indices) {
			list.add(values.get(i));
		}
		return list;
	}
	
	/**
	 * Randomly arranges the supplied values into a list.
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> randomize(final Collection<T> values) throws Exception {
		List<T> destination = new ArrayList<T>(values.size());
		while (!values.isEmpty()) {
			T value = TypeUtils.getRandom(values);
			destination.add(value);
			values.remove(value);
		}
		return destination;
	}
	
	/**
	 * Returns a specified number of distinct integers between min and max,
	 * inclusive, ordered randomly.
	 * @param count
	 * @param min
	 * @param max
	 * @return
	 * @throws Exception
	 */
	public static List<Integer> randomizedSet(final int count, 
										  final int min, 
										  final int max) throws Exception {
		Set<Integer> values = randomSet(count, min, max);
		
		return randomize(values);
	}
	
	/**
	 * Returns a specified number of distinct integers between min and max,
	 * inclusive.
	 * @param count
	 * @param min
	 * @param max
	 * @return
	 * @throws Exception
	 */
	public static Set<Integer> randomSet(final int count, 
										final int min, 
										final int max) throws Exception {
		Set<Integer> values = new HashSet<Integer>();
		if (count < 1 || count > max - min) {
			throw new Exception("Invalid parameters, count: " + count + ", min: " + 
								min + ", max: " + max + ".");
		}

		while (values.size() < count) {
			values.add(RandomUtils.randomInt(min, max));
		}

		return values;
	}
	
	/**
	 * Returns a direction that is max 45 degrees the last direction, biased
	 * toward continuing direction or going toward bias.  If a set of valid
	 * directions is supplied, returns null if there are no valid choices.
	 * @param bias
	 * @param current
	 * @return
	 */
	public static Direction getBiasedDirection(final Direction current,
											   final Direction bias,
											   Set<Direction> valid) throws Exception {
		if (valid == null) {
			valid = new HashSet<Direction>();
			valid.addAll(Arrays.asList(Direction.values()));
		}

		Direction steer = Direction.steer(current, bias);
		
		// 50% of the time, continue the current direction.
		if (RandomUtils.randomBoolean() && valid.contains(current)) {
			return current;
		}
		// 25% of the time, steer toward bias.
		else if (RandomUtils.randomBoolean() && valid.contains(steer)) {
			return steer;
		}
		else {
			for (int i = 0; i < 3; i++) {
				steer = Direction.getRandomFortyFive(current);
				if (valid.contains(steer)) {
					return steer;
				}
			}			
			
			if (valid.contains(current)) {
				return current;
			}
		}
		return null;
	}
	
	public static void main(final String args[]) {
		try {
			System.out.println(randomPastDate(1, 50));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
