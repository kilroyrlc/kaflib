package kaflib.utils;

import java.io.InputStream;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kaflib.types.Bitstream;
import kaflib.types.Pair;

/**
 * A set of utilities for manipulating strings.
 */
public class StringUtils {
	
	/**
	 * Insert commas in the specified number such that 1000000 becomes 
	 * 1,000,000.
	 * @param number
	 * @return
	 */
	public static String commatize(final int number) {
		String string = String.valueOf(number);
		for (int i = string.length() - 3; i > 0; i-= 3) {
			string = string.substring(0, i) + "," + string.substring(i);
		}
		return string;
	}
	
	/**
	 * Creates a string of specified length consisting of the character 
	 * repeated.
	 * @param character
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String toString(final char character, final int length) throws Exception {
		CheckUtils.checkNonNegative(length, "length");
		
		if (length == 0) {
			return "";
		}
		
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buffer.append(character);
		}
		return new String(buffer);
	}
	
	/**
	 * Appends value to buffer if buffer is empty.
	 * @param buffer
	 * @param value
	 * @return
	 */
	public static StringBuffer appendIfEmpty(final StringBuffer buffer, final String value) {
		if (buffer.length() == 0) {
			buffer.append(value);
			return buffer;
		}
		else {
			return buffer;
		}
	}
	
	/**
	 * Checks if the given string is in the array.
	 * @param search
	 * @param list
	 * @return
	 */
	public static boolean contains(final String search, final String... list) {
		for (String check : list) {
			if (check.equals(search)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Resizes the string to either pad out to the specified length or truncate
	 * with "..." indicating truncation.
	 * @param string
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String resize(final String string, final int length) throws Exception {
		return resize(string, length, ' ');
	}
	
	/**
	 * Returns the specified number of ___-separated lines lines.
	 * @param string
	 * @param lineCount
	 * @return
	 * @throws Exception
	 */
	public static String getLines(final String string, final int lineCount, final String separator) throws Exception {
		int index = 0;
		for (int i = 0; i < lineCount; i++) {
			index = string.indexOf(separator, index) + separator.length();
			if (index < 0) {
				return string;
			}
		}
		return string.substring(0, index);
	}
	
	/**
	 * Resizes the string to either pad out to the specified length or truncate
	 * with "..." indicating truncation.
	 * @param string
	 * @param length
	 * @param pad
	 * @return
	 * @throws Exception
	 */
	public static String resize(final String string, final int length, final char pad) throws Exception {
		CheckUtils.check(string, "string");
		if (string.length() > 4 && length < 4) {
			throw new Exception("Cannot resize down to less than 4.");
		}
		if (string.length() > length) {
			return string.substring(0, length - 3) + "...";
		}
		else if (string.length() < length) {
			StringBuffer buffer = new StringBuffer(length);
			buffer.append(string);
			while (buffer.length() < length) {
				buffer.append(pad);
			}
			return buffer.toString();
		}
		else {
			return string;
		}
	}
	
	/**
	 * Truncates the string if it exceeds the specified length.
	 * @param string
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String truncateIf(final String string, 
									final int length, 
									final boolean useElipses,
									final boolean fromStart) throws Exception {
		CheckUtils.check(string, "string");
		if (useElipses && string.length() > 4 && length < 4) {
			throw new Exception("Cannot resize down to less than 4.");
		}
		if (string.length() > length) {
			if (fromStart) {
				if (useElipses) {
					return string.substring(0, length - 3) + "...";
				}
				else {
					return string.substring(0, length);
				}
			}
			else {
				if (useElipses) {
					return "..." + string.substring(string.length() - length - 3);
				}
				else {
					return string.substring(string.length() - length);
				}
			}
		}
		else {
			return string;
		}
	}

	/**
	 * Truncates the string with '...' if it exceeds the specified length.
	 * @param string
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String truncateIf(final String string, final int length) throws Exception {
		return truncateIf(string, length, true, true);
	}
	
	/**
	 * Returns the toString() of each item in the collection, separated by
	 * specified separator string.
	 * @param collection
	 * @param separator
	 * @return
	 * @throws Exception
	 */
	public static <T> String concatenate(final Collection<T> collection, 
			  						     final String separator) throws Exception {
		return concatenate(collection, separator, null, false);
	}
	
	/**
	 * Returns the toString() of each item in the collection, separated by
	 * specified separator string.
	 * @param collection
	 * @param separator
	 * @return
	 * @throws Exception
	 */
	public static <T> String concatenate(final Collection<T> collection, 
			  						     final String separator,
									     final boolean separatorInElementOk) throws Exception {
		return concatenate(collection, separator, null, separatorInElementOk);
	}
	
	/**
	 * Returns the toString() of each item in the collection, separated by
	 * specified separator string.
	 * @param collection
	 * @param separator
	 * @param itemWidth
	 * @return
	 * @throws Exception
	 */
	public static <T> String concatenate(final Collection<T> collection, 
									     final String separator,
									     final Integer itemWidth,
									     final boolean separatorInElementOk) throws Exception {
		if (collection.size() == 0) {
			return "";
		}
		
		StringBuffer buffer = new StringBuffer();
		
		for (T value : collection) {
			if (value != null) {
				if (!separatorInElementOk && value.toString().contains(separator)) {
					throw new Exception("Value: " + value + " contains separator: " + separator + ".");
				}
				if (itemWidth != null && itemWidth > 0) {
					buffer.append(StringUtils.resize(value.toString(), itemWidth));
				}
				else {
					buffer.append(value.toString());
				}
			}
			buffer.append(separator);
		}
		return buffer.subSequence(0, buffer.length() - separator.length()).toString();
	}
	
	/**
	 * Concatenates the tokens with the separator between.
	 * @param separator
	 * @param tokens
	 * @return
	 * @throws Exception
	 */
	public static String concatenate(final String separator, final String... tokens) throws Exception {
		StringBuffer buffer = new StringBuffer();
		
		for (String token : tokens) {
			buffer.append(token);
			buffer.append(separator);
		}
		
		return buffer.substring(0, buffer.lastIndexOf(separator));
	}
	
	
	/**
	 * Parses the given string to a date object.
	 * @param date
	 * @return
	 */
	public static Date getDate(final String date) throws Exception {
		CheckUtils.checkNonEmpty(date, "date");
		
		DateFormat format = DateFormat.getDateInstance();
		try {
			return format.parse(date);
		}
		catch (Exception e) {
			throw new Exception("Not a date: " + date + ".");
		}
	}
	

	/**
	 * Returns a date object based on the specified format (see SimpleDateFormat).
	 * @param date
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static String toString(final Date date, final String pattern) throws Exception {
		CheckUtils.check(date, "date");
		CheckUtils.checkNonEmpty(pattern, "pattern");
		
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}
	
	/**
	 * Returns whether or not the string can be parsed as a date.
	 * @param date
	 * @return
	 */
	public static boolean isDate(final String date) {
		DateFormat format = DateFormat.getDateInstance();
		try {
			format.parse(date);
			return true;
		}
		catch (Exception e) {
			return false;
		}		
	}
	
	/**
	 * Returns the date based on the format.  Null if it did not match.
	 * @param string
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static Date getDate(final String string, final String format) throws Exception {
		SimpleDateFormat f = new SimpleDateFormat(format);
		try {
			Date date = f.parse(string);
			return date;
		}
		catch (Exception e) {
			return null;
		}
		
	}

	public static Pair<String, String> splitAtFirst(final String string, final String split) throws Exception {
		Pair<String, String> pair = new Pair<String, String>(null, null);
		int index = string.indexOf(split);
		if (index < 0) {
			pair.setKey(string);
		}
		else {
			pair.setKey(string.substring(0, index));
			if (index + 1 < string.length()) {
				pair.setValue(string.substring(index + 1));
			}
		}
		return pair;
	}
	
	/**
	 * Convert each item in the list of strings to lower case.
	 * @param strings
	 * @return
	 * @throws Exception
	 */
	public static List<String> toLower(final List<String> strings) throws Exception {
		List<String> lower = new ArrayList<String>();
		for (String string : strings) {
			lower.add(new String(string));
		}
		return lower;
	}
	
	/**
	 * Returns whether or not the given string contains all of the tokens, in
	 * order, with anything between them.
	 * @param string
	 * @param tokens
	 * @return
	 * @throws Exception
	 */
	public static boolean containsInOrder(final String string, 
										  final List<String> tokens) throws Exception {
		CheckUtils.check(string, "string");
		CheckUtils.checkNonEmpty(tokens, "tokens");
		
		int stringIndex = 0;
		int tokensIndex = 0;

		while (stringIndex < string.length()) {
			int index = string.indexOf(tokens.get(tokensIndex), stringIndex);
			
			// Token not found.
			if (index < 0) {
				return false;
			}
			
			stringIndex = index + tokens.get(tokensIndex).length();
			tokensIndex++;
			if (tokensIndex >= tokens.size()) {
				return true;
			}
		}

		return tokensIndex >= tokens.size();
	}
	
	/**
	 * Returns true if the input is null or its toString is empty.
	 * @param input
	 * @return
	 */
	public static boolean isNullOrEmpty(final Object input) {
		return input == null || input.toString().isEmpty();
	}

	/**
	 * Returns whether or not the value is an integer based on a regex.
	 * Negatives and commas are permitted.
	 * @param string
	 * @return
	 */
	public static boolean isInt(final String string) {
		return toInt(string) != null;
	}

	/**
	 * Converts the specified string to an int.  Permissive of commas and
	 * decimal values (that are truncated).  Returns null if it is not a
	 * valid integer.
	 * @param string
	 * @return
	 */
	public static Integer toInt(final String string) {
		Pattern pattern = Pattern.compile("^[-]?([\\d\\,]+)(?:\\.[0]*)?$");
		Matcher matcher = pattern.matcher(string);
		if (matcher.matches()) {
			Integer i = Integer.valueOf(matcher.group(1).replace(",", ""));
			if (string.startsWith("-")) {
				i = i * -1;
			}
			return i;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns whether or not the value is an number based on a regex.
	 * Negatives, commas, decimals are permitted.
	 * @param string
	 * @return
	 */
	public static boolean isNumber(final String string) {
		if (string.matches("^\\d+$") || 
			string.matches("^[-]?[\\d\\,]*[\\.]?[\\d]*$")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * Returns true if a string in the collection starts with the specified value.
	 * @param collection
	 * @param item
	 * @return
	 * @throws Exception
	 */
	public static boolean lookupStart(final Collection<String> collection, final String item) throws Exception {
		for (String s : collection) {
			if (item.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any string in the array starts with the specified value.
	 * @param collection
	 * @param item
	 * @return
	 * @throws Exception
	 */
	public static boolean lookupStart(final String collection[], final String item) throws Exception {
		for (String s : collection) {
			if (item.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Trims to word characters (A-z_0-9), also permits a trailing period.
	 * @param input
	 * @return
	 */
	public static String trimToWord(final String input) throws Exception {
		if (input == null) {
			return null;
		}
		if (input.isEmpty()) {
			return "";
		}
		
		Pattern pattern = Pattern.compile("[\\W]*(\\w+(?:.*[\\w\\.])?)[\\W]*");
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		else {
			throw new Exception("Pattern could not handle input:\n" + input);
		}
	}
	
	/**
	 * Splits the string at the first instance of split.  E.g.
	 * (blahblah, hb) returns [bla, lah].
	 * @param string
	 * @param split
	 * @return
	 */
	public static Pair<String, String> splitFirst(final String string, final String split) {
		int index = string.indexOf(split);
		if (index < 0) {
			return new Pair<String, String>(string, "");
		}
		return new Pair<String, String>(string.substring(0, index), 
									    string.substring(index + split.length()));
	}
	
	public static List<String> split(final String string, final String regex) throws Exception {
		String tokens[] = string.split(regex);
		return Arrays.asList(tokens);
	}
	
	/**
	 * Splits the string based on the given separator.
	 * @param string
	 * @param separator
	 * @return
	 * @throws Exception
	 */
	public static List<String> parse(final String string, final String separator, boolean trim) throws Exception {
		String left = string;
		List<String> tokens = new ArrayList<String>();
		while (left.indexOf(separator) >= 0) {
			int index = left.indexOf(separator);
			if (trim) {
				tokens.add(left.substring(0, index).trim());
			}
			else {
				tokens.add(left.substring(0, index));
			}
			left = left.substring(index + separator.length());
		}
		tokens.add(left.substring(0));
		return tokens;
	}
	
	/**
	 * Splits the csv string.
	 * @param string
	 * @return
	 * @throws Exception
	 */	
	public static List<String> parse(final String csvString, boolean trim) throws Exception {
		return parse(csvString, ",", trim);
	}

	/**
	 * Splits the csv string.
	 * @param string
	 * @return
	 * @throws Exception
	 */	
	public static List<String> parse(final String csvString) throws Exception {
		return parse(csvString, ",", true);
	}
	
	/**
	 * Removes all instances of the subsequence.
	 * @param input
	 * @param remove
	 * @return
	 * @throws Exception
	 */
	public static String remove(final String input, final String remove, boolean ignoreCase) throws Exception {
		
		String temp = new String(input);
		
		int index = temp.toLowerCase().indexOf(remove.toLowerCase());
		while (index > 0) {
			String end = "";
			
			if (index + remove.length() <= temp.length()) {
				end = temp.substring(index + remove.length());
			}
			
			temp = temp.substring(0, index) + end;

			index = temp.toLowerCase().indexOf(remove.toLowerCase());
		}
		
		return temp;
	}
	
	/**
	 * Removes the specified subsequence and one space before or after.
	 * @param input
	 * @param remove
	 * @return
	 * @throws Exception
	 */
	public static String removeAndOneWhitespace(final String input, 
												final String remove, 
												boolean ignoreCase) throws Exception {
		return removeAndOneWhitespace(input, remove, ignoreCase, false);
	}
	
	/**
	 * Removes the specified subsequence and one space before or after.
	 * @param input
	 * @param remove
	 * @return
	 * @throws Exception
	 */
	public static String removeAndOneWhitespace(final String input, 
												final String remove, 
												boolean ignoreCase, 
												boolean andNonWhitespace) throws Exception {
		String value = remove(input, remove + " ", ignoreCase); 
		value = remove(value, " " + remove, ignoreCase);
		
		if (andNonWhitespace) {
			value = remove(value, remove, ignoreCase);
		}
		
		return value;
	}

	/**
	 * Chops everything after (and including) substring. 
	 * @param input
	 * @param substring
	 * @return
	 * @throws Exception
	 */
	public static String truncateAt(final String input, final String... substrings) throws Exception {
		int index = input.length();
		for (String substring : substrings) {
			int value = input.indexOf(substring);
			if (value >= 0 && value < index) {
				index = value;
			}
		}
		
		if (index == input.length()) {
			return input;
		}
		else {
			return input.substring(0, index);
		}
		
	}

	/**
	 * Appends string to buffer if string is not null.
	 * @param buffer
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static StringBuffer appendIf(StringBuffer buffer, final String string) throws Exception {
		return appendIf(buffer, string, "");
	}
	
	/**
	 * Appends string to buffer if string is not null, nullValue if it is null.
	 * @param buffer
	 * @param string
	 * @param nullValue
	 * @return
	 * @throws Exception
	 */
	public static StringBuffer appendIf(StringBuffer buffer, final String string, final String nullValue) throws Exception {
		CheckUtils.check(buffer, "string buffer");
		if (string == null) {
			return buffer.append(nullValue);
		}
		else {
			return buffer.append(string);
		}
	}
	
	/**
	 * Returns whether or not the given string is in scientific notation format.
	 * E.g. 1.022E23
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static boolean isScientificNotation(final String string) throws Exception {
		if (string.matches("[-]?\\d\\.\\d*[Ee][-]?\\d+")) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	/**
	 * Replaces characters not matching the okay class with the specified character.
	 * @param string
	 * @param okayClass
	 * @param replace
	 * @return
	 * @throws Exception
	 */
	public static String replace(final String string, final String okayClass, char replace) throws Exception {
		CheckUtils.check(string, "string");
		CheckUtils.check(okayClass, "okay class");
		
		if (!okayClass.startsWith("[") || !okayClass.endsWith("]")) {
			throw new Exception("Must specify a regex character class [...].");
		}
		
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			if (!string.substring(i, i+1).matches(okayClass)) {
				buffer.append(replace);
			}
			else {
				buffer.append(string.charAt(i));
			}
		}
		return new String(buffer);
	}
	
	/**
	 * Replaces all nonword characters (^\W or not [a-zA-Z_0-9]) with the specified character.
	 * @param string
	 * @param replace
	 * @return
	 * @throws Exception
	 */
	public static String replaceNonWordsWith(final String string, char replace) throws Exception {
		CheckUtils.check(string, "string");
		
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			if (string.substring(i, i+1).matches("[\\W]")) {
				buffer.append(replace);
			}
			else {
				buffer.append(string.charAt(i));
			}
		}
		return new String(buffer);
	}
	
	/**
	 * Removes all nonword characters (^\W or not [a-zA-Z_0-9]).
	 * @param string
	 * @param replace
	 * @return
	 * @throws Exception
	 */
	public static String removeNonWords(final String string) throws Exception {
		return keepOnly(string, "[\\w]");
	}
	
	/**
	 * Removes all characters not matching the character class specified as a
	 * regular expression.
	 * 
	 * E.g. "blahblah" with "[ba]" would return "baba".
	 * 
	 * @param string
	 * @param characterClass
	 * @return
	 * @throws Exception
	 */
	public static String keepOnly(final String string, final String characterClass) throws Exception {
		CheckUtils.check(string, "string");
		if (!characterClass.startsWith("[") ||
			!characterClass.endsWith("]")) {
			throw new Exception("Must specify a character class.");
		}
		
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			if (string.substring(i, i+1).matches(characterClass)) {
				buffer.append(string.charAt(i));
			}
		}
		return new String(buffer);
	}
	
	/**
	 * Returns 100 * matching characters between strings /
	 * total characters in both.
	 * 
	 * Position independent.  Case insensitive.
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static int getPercentCharacterMatch(final String a, final String b) throws Exception {
		CheckUtils.checkNonEmpty(a, "a string");
		CheckUtils.checkNonEmpty(b, "b string");

		StringBuffer aa = new StringBuffer(a.toLowerCase());
		StringBuffer bb = new StringBuffer(b.toLowerCase());
		int matches = 0;
		
		while (aa.length() > 0) {
			int index = bb.indexOf("" + aa.charAt(0));
			if (index >= 0) {
				bb.deleteCharAt(index);
				matches++;
			}
			aa.deleteCharAt(0);
		}
		
		return (100 * matches) / Math.max(a.length(), b.length());
	}
	
	/**
	 * Performs a really naive diff of two strings.
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static String diff(final String a, final String b) {
		if (a == null || b == null) {
			return a + "\n" + b;
		}
		
		if (a.length() == b.length()) {
			StringBuffer a_buffer = new StringBuffer();
			StringBuffer b_buffer = new StringBuffer();
			
			for (int i = 0; i < a.length(); i++) {
				if (a.charAt(i) == b.charAt(i)) {
					a_buffer.append(a.charAt(i));
					b_buffer.append(b.charAt(i));
				}
				else {
					a_buffer.append("[" + a.charAt(i) + "]");
					b_buffer.append("[" + b.charAt(i) + "]");
				}
			}
			a_buffer.append("\n");
			a_buffer.append(b_buffer);
			
			return new String(a_buffer);
		}
		else {
			return "Diffing different length strings has not yet been implemented.";
		}
	}

	/**
	 * Formats the time in ms to days, hours...
	 * @param timeMS
	 * @return
	 */
	public static String formatTime(final long timeMS) {
		long remaining = timeMS / 1000;
		int seconds = (int) remaining % 60;
		remaining = remaining / 60;
		int minutes = (int) remaining % 60;
		remaining = remaining / 60;
		int hours = (int) remaining % 24;
		int days = (int) remaining / 24;
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(getPluralized(days, "day"));
		buffer.append(getPluralized(hours, "hour"));
		buffer.append(getPluralized(minutes, "minute"));
		buffer.append(getPluralized(seconds, "second"));
		return new String(buffer);
	}
	
	/**
	 * Returns the unit pluralized if the value is > 0.
	 * @param value
	 * @param unit
	 * @return
	 */
	private static String getPluralized(final int value, final String unit) {
		if (value > 1) {
			return value + " " + unit + "s ";
		}
		else if (value == 1) {
			return value + " " + unit + " ";
		}
		else {
			return "";
		}
	}
	
	/**
	 * Returns the integer percent of capitalized characters in the string.
	 * @param string
	 * @return
	 */
	public static int percentCaps(final String string) {
		if (string == null || string.isEmpty()) {
			return 0;
		}
		int caps = 0;
		for (int i = 0; i < string.length(); i++) {
			String c = string.substring(i, i + 1);
			if (!c.equals(c.toLowerCase())) {
				caps++;
			}
		}
		return 100 * caps / string.length();
	}
	
	
	public static final char WORD_VALUES[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
			 'u', 'v', 'w', 'x', 'y', 'z'};
	
	/**
	 * Converts the specified bytes to 0-9a-z values.  Takes 5-bit chunks and
	 * maps them into a list of characters.
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static String mapToWords(final byte bytes[]) throws Exception {
		Bitstream stream = new Bitstream(bytes);
		StringBuffer buffer = new StringBuffer();
		
		while (!stream.isEmpty()) {
			buffer.append(WORD_VALUES[stream.remove(5)]);
		}
		
		return new String(buffer);
	}

	public static<K, V> String toString(final Map<K, V> map) throws Exception {
		StringBuffer buffer = new StringBuffer();
		for (K k : map.keySet()) {
			if (map.get(k) == null) {
				buffer.append(k.toString() + ": null\n");
			}
			else {
				buffer.append(k.toString() + ": " + map.get(k).toString() + "\n");
			}
		}
		return new String(buffer);
	}
	
	/**
	 * Reads the stream to a utf8 string.
	 * @param stream
	 * @return
	 * @throws Exception
	 */
	public static String read(final InputStream stream) throws Exception {
		return new String(TypeUtils.read(stream), "UTF-8");
	}

	
	private final static char[] hexArray = "0123456789abcdef".toCharArray();
	/**
	 * Writes the bytes to a hex string.
	 * @param bytes
	 * @return
	 */
	public static String toHex(final byte bytes[]) {
	    char[] chars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        chars[j * 2] = hexArray[v >>> 4];
	        chars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(chars);
	}
	
}

