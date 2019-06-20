package kaflib.utils;

import java.security.MessageDigest;
import java.util.ArrayList;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.xmlbeans.impl.util.Base64;

import kaflib.types.Pair;


/**
 * Contains math utilities.
 */
public class MathUtils {
	
	private static Map<Pair<Integer, Integer>, Integer> distance_lookup = new HashMap<Pair<Integer, Integer>, Integer>();
	private static final int DISTANCE_MAX_SIZE = 128;
	
	public static final int getDistance(final Pair<Integer, Integer> dxdy) {
		if (distance_lookup.containsKey(dxdy)) {
			return distance_lookup.get(dxdy);
		}
		Pair<Integer, Integer> reverse = new Pair<Integer, Integer>(dxdy.getSecond(), dxdy.getFirst());
		if (distance_lookup.containsKey(reverse)) {
			return distance_lookup.get(reverse);
		}
		
		int value = (int) Math.sqrt((double)(dxdy.getFirst() * dxdy.getFirst()) + (dxdy.getSecond() * dxdy.getSecond()));
		if (distance_lookup.size() < DISTANCE_MAX_SIZE) {
			distance_lookup.put(dxdy, value);
		}
		return value;
	}
	
	/**
	 * Sums the values in the collection.
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public static int sum(final Collection<Integer> values) throws Exception {
		int sum = 0;
		
		for (Integer operand : values) {
			if (Integer.MAX_VALUE - sum < operand) {
				throw new Exception("Overflow.");
			}
			sum += operand;
		}
		return sum;
	}

	public static int average(final Collection<Integer> values) throws Exception {
		CheckUtils.checkNonEmpty(values, "values");
		int sum = sum(values);
		return sum / values.size();
	}
	
	/**
	 * Gets a list of random numbers of the specified size, max value is
	 * INT_MAX.
	 * @param size
	 * @return
	 */
	public static List<Integer> getRandoms(final int size) {
		return getRandoms(size, Integer.MAX_VALUE);
	}
	
	/**
	 * Returns an integeral percentage.
	 * 
	 *  @param numerator the value.
	 *  @param denominator the divisor.
	 *  @return (numerator * 100) / denominator, or 0 if the denominator
	 *          is <= 0.
	 */	
	public static int getPercent(final int numerator, final int denominator) throws Exception {
		if (denominator <= 0) {
			throw new Exception("Denominator must be greater than zero.");
		}
		return (100 * numerator) / denominator;
	}
	
	/**
	 * Gets a list of random numbers with the specified size and max.
	 * @param size
	 * @param max
	 * @return
	 */
	public static List<Integer> getRandoms(final int size, final int max) {
		Random r = new Random();
		List<Integer> l = new ArrayList<Integer>();
	
		while (l.size() < size) {
			l.add(r.nextInt(max));
		}
		return l;
	}
	
	/**
	 * Normalizes the value from 0.0 to 1.0.
	 * @param value
	 * @param max
	 * @return
	 */
	public static double normalize(final int value, final int max) {
		return value / (max - 1);
	}
	
	/**
	 * Normalizes the value from -0.5 to 0.5.
	 * @param value
	 * @param max
	 * @return
	 */
	public static double normalizeAroundZero(final int value, final int max) {
		return ((double) value / (double) max - 1) - 0.5;
	}
	
	/**
	 * Returns the minimum positive value of the two supplied, null if both are
	 * negative.
	 * @param a
	 * @param b
	 * @return
	 */
	public static Integer minPositive(int a, int b) {
		if (a < 0 && b < 0) {
			return null;
		}
		else if (a < 0) {
			return b;
		}
		else if (b < 0) {
			return a;
		}
		else {
			return Math.min(a, b);
		}
	}
	
	/**
	 * Returns the max value in the collection.
	 * @param values
	 * @return
	 */
	public static Integer max(final Collection<Integer> values) {
		Integer max = null;
		for (Integer value : values) {
			if (value == null) {
				continue;
			}
			if (max == null || value > max) {
				max = value;
			}
		}
		return max;
	}

	public static Double min(final Double... values) {
		Double max = null;
		for (Double value : values) {
			if (value == null) {
				continue;
			}
			if (max == null || value < max) {
				max = value;
			}
		}
		return max;
	}
	
	public static Double max(final Double... values) {
		Double max = null;
		for (Double value : values) {
			if (value == null) {
				continue;
			}
			if (max == null || value > max) {
				max = value;
			}
		}
		return max;
	}
	
	/**
	 * Returns the md5 value for the specified bytes.
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static byte[] getMD5(final byte[] bytes) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return digest.digest(bytes);
	}
	
	/**
	 * Encodes the specified bytes into base64.
	 * @param bytes
	 * @param urlSafe
	 * @return
	 * @throws Exception
	 */
	public static byte[] encodeBase64(final byte[] bytes, 
								      final boolean urlSafe) throws Exception {
		byte encoded[] = Base64.encode(bytes);
		if (urlSafe) {
			for (int i = 0; i < encoded.length; i++) {
				if (encoded[i] == '+') {
					encoded[i] = '-';
				}
				if (encoded[i] == '/') {
					encoded[i] = '_';
				}
			}
		}
		return encoded;
		
	}
	
	/**
	 * Decodes the specified bytes from base64.
	 * @param bytes
	 * @param urlSafe
	 * @return
	 * @throws Exception
	 */
	public static byte[] decodeBase64(final byte[] bytes, 
									  final boolean urlSafe) throws Exception {
		if (urlSafe) {
			for (int i = 0; i < bytes.length; i++) {
				if (bytes[i] == '-') {
					bytes[i] = '+';
				}
				if (bytes[i] == '_') {
					bytes[i] = '/';
				}
			}
		}
		
		return Base64.decode(bytes);
	}
	
}
