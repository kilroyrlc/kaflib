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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Type representing a price, that is dollars and cents.  It is currently
 * designed for USD (but to reject other monetary types) and can be expanded
 * to do other currencies as necessary.
 */
public class Price {

	private int dollars;
	private int cents;
	
	/**
	 * Regular expression for a price, places the dollars and cents in groups 
	 * 1 and 2.  Price will still have commas and the cents values may be 0-n 
	 * digits.
	 */
	public static final Pattern PRICE_REGEX_DOLLARS_CENTS = Pattern.compile("^[\\s\\t]*[\\$]?(?<dollars>[\\d,]*)\\.(?<cents>\\d+)(?:[\\D][\\W\\t]*)?$");
	public static final Pattern PRICE_REGEX_DOLLARS_ONLY = Pattern.compile("^[\\s\\t]*[\\$]?(?<dollars>[\\d,]*)(?:[\\D][\\W\\t]*)?$");
	public static final Pattern PRICE_REGEX_CENTS_ONLY = Pattern.compile("^[\\s\\t]*[\\$]?[0]*(?:\\.(?<cents>\\d+))(?:[\\D][\\W\\t]*)?$");
	
	/**
	 * Creates a $0.00 price.
	 */
	public Price() {
		dollars = 0;
		cents = 0;
	}
	
	/**
	 * Creates at price with the dollars/cents value.
	 * @param price
	 * @throws Exception
	 */
	public Price(final double price) throws Exception {
		this((int) price, (int) ((price * 100) % 100));
	}
	
	/**
	 * Attempts to parse the supplied string as a dollars/cents value.
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static Pair<Integer, Integer> match(final String string) throws Exception {
		Matcher matcher = PRICE_REGEX_DOLLARS_CENTS.matcher(string);
		if (matcher.matches() && matcher.group(1) != null && !matcher.group(1).isEmpty()) {

			int dollars;
			Integer cents;
			if (matcher.group("dollars").isEmpty()) {
				if (matcher.group("cents").isEmpty()) {
					return null;
				}
				dollars = 0;
			}
			else {
				dollars = Integer.valueOf(matcher.group(1).replace(",", ""));
			}
			
			cents = matchCents(matcher.group("cents"));
			if (cents == null) {
				return null;
			}
			return new Pair<Integer, Integer>(dollars, cents);
			
		}
		matcher = PRICE_REGEX_DOLLARS_ONLY.matcher(string);
		if (matcher.matches() && matcher.group(1) != null && !matcher.group(1).isEmpty()) {
			int dollars = Integer.valueOf(matcher.group("dollars").replace(",", ""));
			return new Pair<Integer, Integer>(dollars, 0);
			
		}
		matcher = PRICE_REGEX_CENTS_ONLY.matcher(string);
		if (matcher.matches()) {
			Integer cents = matchCents(matcher.group("cents"));
			if (cents == null) {
				return null;
			}
			
			return new Pair<Integer, Integer>(0, cents);
		}
		return null;
	}
	
	/**
	 * Parses cents from a string.  \d\d is cool.  0 is cool.  \d\d\d... is not
	 * nor is \d. 
	 * @param cents
	 * @return
	 * @throws Exception
	 */
	private static Integer matchCents(final String cents) throws Exception {
		if (cents.length() > 2) {
			return Integer.valueOf(cents.substring(0, 2));
		}
		else if (cents.length() == 1) {
			if (cents.equals("0")) {
				return 0;
			}
			else {
				throw new Exception("Ambiguous cents: " + cents);
			}
		}
		else if (cents.length() == 2) {
			return Integer.valueOf(cents);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Create a price based on the input string.
	 * @param string
	 * @throws Exception
	 */
	public Price(final String string) throws Exception {
		Pair<Integer, Integer> values = match(string);
		CheckUtils.check(values, "parsed price: " + string);
		dollars = values.getFirst();
		cents = values.getSecond();
	}
	
	/**
	 * Create a price with the specified dollars and cents.
	 * @param dollars
	 * @param cents
	 * @throws Exception
	 */
	public Price(int dollars, int cents) throws Exception {
		this.dollars = dollars;
		this.cents = cents;
		validate();
	}
	
	/**
	 * Validate that the price is legitimate.  Currently, negative prices are
	 * permitted but maybe they shouldn't be.  Cents values must be 0-99.
	 * @throws Exception
	 */
	private void validate() throws Exception {
		if (cents > 99 || cents < 0) {
			throw new Exception("Invalid cents: " + cents + ".");
		}
	}
	
	/**
	 * Returns dollars.
	 * @return
	 */
	public int getDollars() {
		return dollars;
	}

	/**
	 * Returns cents.
	 * @return
	 */
	public int getCents() {
		return cents;
	}
	
	/**
	 * Returns the price as a string.  Either:
	 *  - Undecorated: 4999.99
	 *  - Decorated: $4,999.99
	 * @param undecorated
	 * @return
	 */
	public String toString(boolean undecorated) {
		if (undecorated) {
			return dollars + "." + String.format("%02d", cents);
		}
		else {
			return "$" + StringUtils.commatize(dollars) + "." + String.format("%02d", cents);
		}
	}
	
	/**
	 * Returns the higher value.
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static Price max(final Price a, final Price b) throws Exception {
		if (a.toDouble() >= b.toDouble()) {
			return a;
		}
		else {
			return b;
		}
	}
	
	/**
	 * Returns a decimal representation of the price.
	 * @return
	 */
	public double toDouble() {
		double value = cents;
		value = value / 100;
		return dollars + value;
	}

	/**
	 * Returns whether or not the two prices are equal.
	 */
	public boolean equals(Object o) {
		if (o instanceof Price) {
			return equals((Price) o);
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns whether or not the two prices are equal.
	 * @param other
	 * @return
	 */
	public boolean equals(final Price other) {
		if (dollars == other.getDollars() &&
			cents == other.getCents()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns the price as a string, include a dollar sign and commatized
	 * dollar values.
	 */
	public String toString() {
		return toString(false);
	}
	
	/**
	 * Returns whether or not the supplied text matches a price.  
	 * Leading/trailing whitespace is ignored.  Okay formats include:
	 *  - $9.99
	 *  - 9.99
	 *  - $9
	 *  - 9
	 *  - $.99
	 *  - .99
	 * @param text
	 * @return
	 */
	public static boolean isPrice(final String text) throws Exception {
		if (match(text) == null) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Returns whether or not the prices is valid, the Excel flag will permit
	 * single digit decimal values such as $22.1.
	 * @param price
	 * @return
	 */
	public static boolean isPrice(final double price, final boolean excel) throws Exception {
		// There's probably a more elegant way to do this, but for now, 
		// convert it to a string and let the other one do its thing.
		if (excel) {
			return isPrice(padCentsIf(String.valueOf(price)));
		}
		else {
			return isPrice(String.valueOf(price));
		}
	}
	
	/**
	 * Parses the value to a price, returning null if it is invalid.
	 * @param value
	 * @return
	 */
	public static Price parse(final String value) throws Exception {
		if (!isPrice(value)) {
			return null;
		}
		try {
			return new Price(value);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Checks the input string for formatting like '$35.4' which is can be how
	 * Excel represents '$35.40'.  Adds a zero to the end of any single-digit
	 * cent input.
	 * @param price
	 * @return
	 * @throws Exception
	 */
	public static String padCentsIf(final String price) {
		if (price == null) {
			return null;
		}
		
		int decimal = price.lastIndexOf('.');
		if (decimal == price.length() - 2) {
			return price + "0";
		}
		else {
			return price;
		}
	}

	
	/**
	 * Averages the prices together.
	 * @param prices
	 * @return
	 * @throws Exception
	 */
	public static Price max(final Collection<Price> prices) throws Exception {
		if (prices == null || prices.size() == 0) {
			return null;
		}
		
		Price max = prices.iterator().next();
		for (Price price : prices) {
			if (price.toDouble() > max.toDouble()) {
				max = price;
			}
		}
		return max;
	}
	
	/**
	 * Averages the prices together.
	 * @param prices
	 * @return
	 * @throws Exception
	 */
	public static Price average(final Collection<Price> prices) throws Exception {
		double total = 0;
		for (Price price : prices) {
			total += price.toDouble();
		}
		return new Price(total / prices.size());
	}
	
	/**
	 * Returns the median of the supplied prices.
	 * @param prices
	 * @return
	 * @throws Exception
	 */
	public static Price median(List<Price> prices) throws Exception {
		CheckUtils.checkNonEmpty(prices, "prices");
		
		Collections.sort(prices, new Comparator<Price>(){

			@Override
			public int compare(Price o1, Price o2) {
				if (o1.toDouble() < o2.toDouble()) {
					return -1;
				}
				if (o1.toDouble() > o2.toDouble()) {
					return 1;
				}
				return 0;
			}});
		return prices.get(prices.size() / 2);
	}
	
}
