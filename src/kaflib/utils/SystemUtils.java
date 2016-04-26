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

import java.util.Arrays;
import java.util.List;

/**
 * Contains utilities for system interaction.
 */
public class SystemUtils {

	/**
	 * Executes the specified command serially.
	 * @param command
	 * @throws Exception
	 */
	public static void excecuteCommandSerially(final String... command) throws Exception {
		CheckUtils.check(command, "command");
		final Process p = excecuteCommandAsynchronously(command);
		p.waitFor();
	}

	/**
	 * Executes the specified command serially.
	 * @param command
	 * @throws Exception
	 */
	public static void excecuteCommandSerially(final List<String> command) throws Exception {
		CheckUtils.check(command, "command");
		final Process p = excecuteCommandAsynchronously(command);
		p.waitFor();
	}
	
	/**
	 * Executes the specified command asynchronously, returns the process.
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static Process excecuteCommandAsynchronously(final String command[]) throws Exception {
		CheckUtils.check(command, "command");
		try {
			return Runtime.getRuntime().exec(command);
		}
		catch (Exception e) {
			System.out.println("Command: " + StringUtils.concatenate(Arrays.asList(command), " ", true));
			throw e;
		}
	}
	
	/**
	 * Executes the specified command asynchronously, returns the process.
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static Process excecuteCommandAsynchronously(final List<String> command) throws Exception {
		CheckUtils.checkNonEmpty(command, "command");
		return excecuteCommandAsynchronously(command.toArray(new String[command.size()]));
	}
}
