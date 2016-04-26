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

import java.awt.Component;
import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Defines a set of utilities for doing GUI work.
 */
public class GUIUtils {

	/**
	 * Shows an error dialog.
	 * @param parent
	 * @param message
	 */
	public static void showErrorDialog(final Component parent, final String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows an error dialog.
	 * @param parent
	 * @param title
	 * @param message
	 */
	public static void showErrorDialog(final Component parent, final String title, final String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows a confirm dialog.
	 * @param parent
	 * @param title
	 * @param message
	 * @return
	 */
	public static int showConfirmDialog(final Component parent, final String title, final String message) {
		return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION);
	}
	
	/**
	 * Returns a price input field.
	 * @param columns
	 * @return
	 * @throws Exception
	 */
	public static JFormattedTextField getPriceField(int columns) throws Exception {
		return getPriceField(columns, -1, -1);
	}
	
	/**
	 * Returns a price input field.
	 * @param columns
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 * @throws Exception
	 */
	public static JFormattedTextField getPriceField(int columns, int maxWidth, int maxHeight) throws Exception {
		JFormattedTextField field = new JFormattedTextField(NumberFormat.getCurrencyInstance());
		field.setValue(new Double(0));
		field.setColumns(columns);
		
		if (maxWidth > 0 && maxHeight > 0) {
			field.setMaximumSize(new Dimension(maxWidth, maxHeight));
		}
		
		return field;
	}
	
	/**
	 * Returns an empty border.
	 * @param width
	 * @return
	 */
	public static Border getEmptyBorder(int width) {
		return new EmptyBorder(width, width, width, width);
	}
	
}
