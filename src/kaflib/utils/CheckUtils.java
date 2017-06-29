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

import java.io.File;
import java.util.Collection;

/**
 * A set of utility functions for checking variables.  One-shot calls to do
 * frequent things like:
 * if (x == null) {
 *    throw new Exception("X is null.");
 * }
 * 
 * Or:
 * if (file == null) {
 *    throw new Exception("File is null.");
 * }
 * if (!file.exists()) {
 *    throw new Exception("File does not exist.");
 * }
 */
public class CheckUtils {
	
	/**
	 * Checks a variable number of arguments.
	 * @param o
	 * @throws Exception
	 */
	public static void check(final Object... o) throws Exception {
		check(o, "object varargs");
		int i = 0;
		for (Object object : o) {
			if (object == null) {
				throw new Exception("Null value [" + i + "].");
			}
			i++;
		}
	}
	
	/**
	 * Checks that the given object is not null.  Throws if it is.
	 * @param o
	 * @throws Exception
	 */
	public static void check(final Object o) throws Exception {
		check(o, null);
	}
	
	/**
	 * Checks that the given object is not null.  Throws if it is.
	 * @param o
	 * @param description
	 * @throws Exception
	 */
	public static void check(final Object o, final String description) throws Exception {
		if (o == null) {
			if (description != null) {
				throw new Exception("Null object: " + description + ".");
			}
			else {
				throw new Exception("Null object.");				
			}
		}
	}

	/**
	 * Throws if the supplied string is null or empty.
	 * @param string
	 * @param description
	 * @throws Exception
	 */
	public static void checkNonEmpty(final String string) throws Exception {
		checkNonEmpty(string, null);
	}

	
	/**
	 * Throws if the supplied string is null or empty.
	 * @param string
	 * @param description
	 * @throws Exception
	 */
	public static void checkNonEmpty(final String string,
						             final String description) throws Exception {
		check(string, description);
		
		if (string.isEmpty()) {
			if (description == null) {
				throw new Exception("Empty string.");
			}
			else {
				throw new Exception("Empty string: " + description + ".");
			}
		}
	}
	
	/**
	 * Verifies that the supplied collection is non-null and non-empty.
	 * @param collection
	 * @throws Exception
	 */
	public static <T> void checkNonEmpty(final Collection<T> collection) throws Exception {
		checkNonEmpty(collection, null);
	}

	/**
	 * Verifies that the supplied collection is non-null and non-empty.
	 * @param collection
	 * @throws Exception
	 */
	public static <T> void checkNonEmpty(final Collection<T> collection,
										 final String description) throws Exception {
		check(collection, description);
		if (collection.isEmpty()) {
			if (description == null) {
				throw new Exception("Empty collection.");
			}
			else {
				throw new Exception("Empty collection: " + description + ".");
			}
		}
	}

	/**
	 * Throws if the given value is < 1.
	 * @param value
	 * @throws Exception
	 */
	public static void checkPositive(int value) throws Exception {
		checkPositive(value, null);
	}
	
	/**
	 * Throws if the given value is < 1.
	 * @param value
	 * @throws Exception
	 */
	public static void checkPositive(int value, final String description) throws Exception {
		if (value < 1) {
			if (description == null) {
				throw new Exception("Non-positive value: " + value + ".");
			}
			else {
				throw new Exception("Non-positive value: " + description + " = " + value + ".");
			}
		}
	}
	
	/**
	 * Throws if the given value is < 1.
	 * @param value
	 * @throws Exception
	 */
	public static void checkPositive(float value) throws Exception {
		checkPositive(value, null);
	}
	
	/**
	 * Throws if the given value is < 1.
	 * @param value
	 * @throws Exception
	 */
	public static void checkPositive(float value, final String description) throws Exception {
		if (value <= 0) {
			if (description == null) {
				throw new Exception("Non-positive value: " + value + ".");
			}
			else {
				throw new Exception("Non-positive value: " + description + " = " + value + ".");
			}
		}
	}
	
	/**
	 * Throws if the given value is < 1.
	 * @param value
	 * @throws Exception
	 */
	public static void checkPositive(long value, final String description) throws Exception {
		if (value < 1) {
			if (description == null) {
				throw new Exception("Non-positive value: " + value + ".");
			}
			else {
				throw new Exception("Non-positive value: " + description + " = " + value + ".");
			}
		}
	}
	
	
	/**
	 * Throws if the given value is < 0.
	 * @param value
	 * @throws Exception
	 */
	public static void checkNonNegative(int value) throws Exception {
		checkNonNegative(value, null);
	}

	/**
	 * Throws if the given value is < 0.
	 * @param value
	 * @throws Exception
	 */
	public static void checkNonNegative(int value, String description) throws Exception {
		if (value < 0) {
			if (description == null) {
				throw new Exception("Negative value: " + value + ".");
			}
			else {
				throw new Exception("Negative value: " + description + " = " + value + ".");
			}
		}
	}
	
	/**
	 * Verifies that the given index is within the collection size.
	 * @param index
	 * @param collection
	 * @throws Exception
	 */
	public static <T> void checkWithin(int index, final Collection<T> collection) throws Exception {
		if (index < 0 || index >= collection.size()) {
			throw new Exception("Index " + index + " out of collection range: " + 
							    collection.size() + ".");
		}
	}

	/**
	 * Verifies that the file non-null and readable.
	 * @param file
	 * @throws Exception
	 */
	public static void checkReadable(final File file) throws Exception {
		checkReadable(file, null);
	}
	
	/**
	 * Verifies that the file non-null and readable.
	 * @param file
	 * @param description
	 * @throws Exception
	 */
	public static void checkReadable(final File file, final String description) throws Exception {
		check(file, description);
		
		if (!file.exists()) {
			throw new Exception("File not found: " + file.getAbsolutePath() + ".");
		}
		if (!file.canRead()) {
			throw new Exception("Cannot read file: " + file.getAbsolutePath() + ".");
		}
	}

	/**
	 * Verifies that the file is non-null and writable.
	 * @param file
	 * @throws Exception
	 */
	public static void checkWritable(final File file) throws Exception {
		checkWritable(file, null);
	}
	
	/**
	 * Verifies that the file is non-null and writable.
	 * @param file
	 * @param description
	 * @throws Exception
	 */
	public static void checkWritable(final File file, final String description) throws Exception {
		check(file, description);
		
		if (!file.exists() || !file.canWrite()) {
			throw new Exception("Cannot write file: " + file.toString() + ".");
		}
	}

	/**
	 * Checks that the given collection contains the given object.
	 * @param collection
	 * @param object
	 * @throws Exception
	 */
	public static <T> void checkContains(final Collection<T> collection, final T object) throws Exception {
		checkContains(collection, object, null);
	}
	
	/**
	 * Checks that the given collection contains the given object.
	 * @param collection
	 * @param object
	 * @param description
	 * @throws Exception
	 */
	public static <T> void checkContains(final Collection<T> collection, final T object, final String description) throws Exception {
		if (collection.contains(object)) {
			return;
		}
		if (description != null) {
			throw new Exception("Collection does not contain object: " + object.toString() + ": " + description + ".");
		}
		else {
			throw new Exception("Collection does not contain object: " + object.toString() + ".");
		}
		
	}

	/**
	 * Checks that the two objects are equal.
	 * @param a
	 * @param b
	 * @throws Exception
	 */
	public static <T> void checkEquals(final T a, final T b) throws Exception {
		checkEquals(a, b, null);
	}
	
	/**
	 * Checks that the two objects are equal.
	 * @param a
	 * @param b
	 * @param description
	 * @throws Exception
	 */
	public static <T> void checkEquals(final T a, final T b, final String description) throws Exception {
		if (a == null && b == null) {
			return;
		}
		check(a, description);
		check(b, description);
		
		if (!a.equals(b)) {
			if (description == null) {
				throw new Exception(a + " != " + b + ".");
			}
			else {
				throw new Exception(description + ": " + a + " != " + b + ".");
			}
		}
	}
	
	/**
	 * Checks that the specified file is a valid, existing directory.
	 * @param file
	 * @throws Exception
	 */
	public static void checkDirectory(final File file) throws Exception {
		CheckUtils.check(file, "directory");
		if (!file.exists() || !file.isDirectory()) {
			throw new Exception("File: " + file + " does not exist or is not a directory.");
		}
	}
	
}
