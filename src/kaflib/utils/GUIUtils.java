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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import kaflib.types.Pair;

/**
 * Defines a set of utilities for doing GUI work.
 */
public class GUIUtils {

	public static Border getTitledBorder(final String title) {
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		return BorderFactory.createTitledBorder(border, title, TitledBorder.LEFT, TitledBorder.TOP);
	}
	
	public static JPanel getTitledPanel(final String title) {
		JPanel panel = new JPanel();
		panel.setBorder(getTitledBorder(title));
		return panel;
	}
	
	/**
	 * Prompts the user for generic text input.
	 * @param parent
	 * @param message
	 */
	public static String showTextInputDialog(final Component parent, final String message) {
		return (String) JOptionPane.showInputDialog(parent, 
								    message, 
								    "Input", 
								    JOptionPane.QUESTION_MESSAGE, 
								    null, 
								    null, 
								    null);
	}
	
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

	/**
	 * Prompts the user to choose a directory, returns null if they did not.
	 * @param parent
	 * @return
	 */
	public static File chooseDirectory(final Component parent) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null && file.exists() && file.isDirectory()) {
				return file;
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	/**
	 * Lays out the key-value components in a grid that doesn't fill.
	 * @param panel
	 * @param components
	 * @param keyPct
	 * @throws Exception
	 */
	public static void layoutKeyValues(final JPanel panel, 
									   final List<Pair<JComponent, JComponent>> components, 
									   final float keyPct) throws Exception {
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints;
		
		float weight = 0;
		float weight_increment = Math.min(((float) 0.7) / components.size(), (float) 0.1);
		int row = 0;
		for (Pair<JComponent, JComponent> line : components) {

			constraints = new GridBagConstraints();
	        constraints.fill = GridBagConstraints.NONE;
	        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
	        constraints.weightx = keyPct;
	        constraints.weighty = weight;
	        constraints.gridx = 0;
	        constraints.gridy = row;
	        constraints.insets = new Insets(3, 3, 3, 3);
	        panel.add(line.getFirst(), constraints);
	        
	        constraints = new GridBagConstraints();
	        constraints.fill = GridBagConstraints.HORIZONTAL;
	        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
	        constraints.weightx = 1 - keyPct;
	        constraints.weighty = weight;
	        constraints.gridx = GridBagConstraints.RELATIVE;
	        constraints.gridy = row;
	        constraints.insets = new Insets(3, 3, 3, 3);
	        panel.add(line.getSecond(), constraints);
			weight += weight_increment;
			row++;
		}
	}
	
}
