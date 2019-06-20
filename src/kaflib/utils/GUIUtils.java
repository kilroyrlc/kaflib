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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import kaflib.gui.components.KButton;
import kaflib.types.Pair;

/**
 * Defines a set of utilities for doing GUI work.
 */
public class GUIUtils {

	public static JButton getButton(final KButton.ButtonType type) throws Exception {
		switch (type) {
		case DIRECTORY:
			return new JButton(UIManager.getIcon("FileView.directoryIcon"));
		case FILE:
			return new JButton(UIManager.getIcon("FileView.fileIcon"));
		case COMPUTER:
			return new JButton(UIManager.getIcon("FileView.computerIcon"));
		case HARD_DRIVE:
			return new JButton(UIManager.getIcon("FileView.hardDriveIcon"));
		case SAVE:
			return new JButton(UIManager.getIcon("FileView.floppyDriveIcon"));
		case NEW_FOLDER:
			return new JButton(UIManager.getIcon("FileChooser.newFolderIcon"));
		case UP_FOLDER:
			return new JButton(UIManager.getIcon("FileChooser.upFolderIcon"));
		case HOME_FOLDER:
			return new JButton(UIManager.getIcon("FileChooser.homeFolderIcon"));
		case DETAILS_VIEW:
			return new JButton(UIManager.getIcon("FileChooser.detailsViewIcon"));
		case LIST_VIEW:
			return new JButton(UIManager.getIcon("FileChooser.listViewIcon"));
		default:
			throw new Exception("Unrecognized type: " + type + ".");
		}
	}
	
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
	 * Returns a JButton with monospaced text.
	 * @param text
	 * @param size
	 * @return
	 */
	public static JButton getMonospaceButton(final String text, final int size) {
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, size);
		JButton button = new JButton(text);
		button.setFont(font);
		return button;
	}
	
	/** 
	 * Returns a JButton with monospaced bold text.
	 * @param text
	 * @param size
	 * @return
	 */
	public static JButton getMonospaceBoldButton(final String text, final int size) {
		Font font = new Font(Font.MONOSPACED, Font.BOLD, size);
		JButton button = new JButton(text);
		button.setFont(font);
		return button;
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
	 * Returns a numeric field.
	 * @param columns
	 * @return
	 * @throws Exception
	 */
	public static JFormattedTextField getNumberField(int columns) throws Exception {
		return getNumberField(columns, -1, -1);
	}
	
	/**
	 * Returns a numeric field.
	 * @param columns
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 * @throws Exception
	 */
	public static JFormattedTextField getNumberField(int columns, int maxWidth, int maxHeight) throws Exception {
		JFormattedTextField field = new JFormattedTextField(NumberFormat.getIntegerInstance());
		field.setValue(0);
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


	public static File chooseDirectory(final Component parent) {
		return chooseDirectory(parent, null);
	}
	
	/**
	 * Prompts the user to choose a directory, returns null if they did not.
	 * @param parent
	 * @return
	 */
	public static File chooseDirectory(final Component parent,
									   final File startingDirectory) {
		JFileChooser chooser;
		if (startingDirectory != null && 
			startingDirectory.canRead()) {
			if (startingDirectory.isDirectory()) {
				chooser = new JFileChooser(startingDirectory);
			}
			else {
				chooser = new JFileChooser(startingDirectory.getParentFile());
			}
		}
		else {
			chooser = new JFileChooser();
		}
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
	 * Shows a single file chooser that accepts new files as input.
	 * @param parent
	 * @param startingDirectory
	 * @return
	 */
	public static File chooseNewFile(final Component parent,
			final File startingDirectory) {
		JFileChooser chooser;
		if (startingDirectory != null && 
				startingDirectory.canRead()) {
			if (startingDirectory.isDirectory()) {
				chooser = new JFileChooser(startingDirectory);
			}
			else {
				chooser = new JFileChooser(startingDirectory.getParentFile());
			}
		}
		else {
			chooser = new JFileChooser();
		}
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
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
	 * Shows a single file chooser that requires an existing file as input.
	 * @param parent
	 * @param startingDirectory
	 * @return
	 */
	public static File chooseFile(final Component parent,
								  final File startingDirectory) {
		JFileChooser chooser;
		if (startingDirectory != null && 
			startingDirectory.canRead()) {
			if (startingDirectory.isDirectory()) {
				chooser = new JFileChooser(startingDirectory);
			}
			else {
				chooser = new JFileChooser(startingDirectory.getParentFile());
			}
		}
		else {
			chooser = new JFileChooser();
		}
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null && file.exists()) {
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

	public static Set<File> chooseFiles(final Component parent) {
		return chooseFiles(parent, null, null);
	}

	public static Set<File> chooseFiles(final Component parent,
										final File startingDirectory) {
		return chooseFiles(parent, startingDirectory, null);
	}
	
	public static Set<File> chooseFiles(final Component parent,
										final File startingDirectory,
										final FileFilter filter) {
		JFileChooser chooser;
		Set<File> files = new HashSet<File>();
		if (startingDirectory != null && 
				startingDirectory.canRead()) {
			if (startingDirectory.isDirectory()) {
				chooser = new JFileChooser(startingDirectory);
			}
			else {
				chooser = new JFileChooser(startingDirectory.getParentFile());
			}
		}
		else {
			chooser = new JFileChooser();
		}
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (filter != null) {
			chooser.setFileFilter(filter);
		}
		if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			for (File file : chooser.getSelectedFiles()) {
				if (file != null && file.exists()) {
					files.add(file);
				}
			}
		}
		return files;
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
	
	public static void layoutKeyPanels(final JPanel panel, 
			final List<Pair<JComponent, JPanel>> components, 
			final float keyPct) throws Exception {
		panel.setLayout(new GridBagLayout());

		GridBagConstraints constraints;

		float weight = 0;
		float weight_increment = Math.min(((float) 0.7) / components.size(), (float) 0.1);
		int row = 0;
		for (Pair<JComponent, JPanel> line : components) {

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
