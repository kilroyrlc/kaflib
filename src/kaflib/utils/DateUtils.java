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

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides functions for dealing with date types.
 */
public class DateUtils {

	/**
	 * Checks if the two days are adjacent.  A simple +/- calendar comparison
	 * will fail if there are hms.
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static boolean areAdjacentDays(final Date a, final Date b) throws Exception {
		CheckUtils.check(a, "first date");
		CheckUtils.check(b, "second date");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date aa = format.parse(format.format(a));
		Date bb = format.parse(format.format(b));

		if (DateUtils.getDate(aa, -1).equals(bb) || DateUtils.getDate(aa, 1).equals(bb)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Checks if the two days are identical.  A simple +/- calendar comparison
	 * will fail if there are hms.
	 * @param a
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public static boolean areTheSameDay(final Date a, final Date b) throws Exception {
		CheckUtils.check(a, "first date");
		CheckUtils.check(b, "second date");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date aa = format.parse(format.format(a));
		Date bb = format.parse(format.format(b));
		if (aa.equals(bb)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns a Date for the given yyyymmdd string.
	 * @param yyyymmdd
	 * @return
	 * @throws Exception
	 */
	public static Date getDate(final String yyyymmdd) throws Exception {
		return new SimpleDateFormat("yyyyMMdd").parse(yyyymmdd);
	}
	
	/**
	 * Returns the date as month/day/year with the supplied separator.
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String getYYYYMM(final Date date, 
									final String separator) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy" + separator + "MM");
		return format.format(date);
	}
	
	/**
	 * Returns the date as month/day/year with the supplied separator.
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String getMMDDYYYY(final Date date, 
									 final String separator) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("MM" + separator + "dd" + separator + "yyyy");
		return format.format(date);
	}
	
	/**
	 * Returns the date as unseparated year-month-day.
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String getYYYYMMDD(final Date date) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(date);
	}
	
	/**
	 * Returns today in yyyymmdd format.
	 * @return
	 * @throws Exception
	 */
	public static String getYYYYMMDD() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(Calendar.getInstance().getTime());
	}

	/**
	 * Returns the date in yyyymmdd format with the given year/month/day separator.
	 * @param separator
	 * @return
	 * @throws Exception
	 */
	public static String getYYYYMMDD(final String separator) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String unformatted = format.format(Calendar.getInstance().getTime());
		
		return unformatted.substring(0, 4) + separator +
			   unformatted.substring(4, 6) + separator +
			   unformatted.substring(6);
	}
	
	/**
	 * Returns the date as unseparated year-month-day, underscore, hour-
	 * minute-second.
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String getYYYYMMDDHHMMSS(final Date date) throws Exception {
		CheckUtils.check(date, "date");
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss");
		return format.format(date);
	}

	/**
	 * Returns the date as yyyy/MM/dd hh:mm:ss.
	 * @return
	 * @throws Exception
	 */
	public static String getString() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		return format.format(Calendar.getInstance().getTime());
	}

	/**
	 * Returns the current hh:mm:ss time.
	 * @return
	 * @throws Exception
	 */
	public static String getHHMMSS() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		return format.format(Calendar.getInstance().getTime());
	}
	
	/**
	 * Gets the date for the specified days in the past/future.
	 * @param delta
	 * @return
	 * @throws Exception
	 */
	public static Date getDate(final int deltaDays) throws Exception {
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.DATE, deltaDays);
    	return calendar.getTime();
	}

	/**
	 * Returns whether or not the two dates are equal to the second.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equalsToSecond(final Date a, final Date b) throws Exception {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return getYYYYMMDDHHMMSS(a).equals(getYYYYMMDDHHMMSS(b));
	}
	
	/**
	 * Returns the date.
	 * @return
	 * @throws Exception
	 */
	public static Date getDate() throws Exception {
    	return Calendar.getInstance().getTime();
	}
	
	/**
	 * Gets the date for the specified days in the past/future.
	 * @param from
	 * @param deltaDays
	 * @return
	 * @throws Exception
	 */
	public static Date getDate(final Date from, final int deltaDays) throws Exception {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(from);
    	calendar.add(Calendar.DATE, deltaDays);
    	return calendar.getTime();
	}
	
	public static int getDay(final Date date) throws Exception {
		return date.toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth();
	}
	
	public static int getMonth(final Date date) throws Exception {
		return date.toInstant().atZone(ZoneId.systemDefault()).getMonthValue();
	}
	
	public static int getYear(final Date date) throws Exception {
		return date.toInstant().atZone(ZoneId.systemDefault()).getYear();
	}
	
}
