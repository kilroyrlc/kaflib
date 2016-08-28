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
import java.util.List;
import java.util.Random;


/**
 * Contains math utilities.
 */
public class MathUtils {
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
	 * Returns the md5 value for the specified bytes.
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static byte[] getMD5(final byte[] bytes) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return digest.digest(bytes);
	}
	
}
