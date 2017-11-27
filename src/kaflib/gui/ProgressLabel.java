package kaflib.gui;

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import kaflib.types.Pair;
import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a progress bar with text overlay.  The progress bar actually
 * supports two progress displays, the more complete value is always shown
 * in the background.  The text overlay can be updated by any client and is
 * truncated to match the width parameter.
 */
public class ProgressLabel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

	// Default colors.
	private static final Color GREY = new Color(0xf8f8f8);
	private static final Color FRONT_PROGRESS = new Color(0xccffcc);
	private static final Color BACK_PROGRESS = new Color(0x00cc00);
	private static final Color FRONT_BORDER = new Color(0x66ff66);
	private static final Color BACK_BORDER = new Color(0x00aa00);

	private static final int DEFAULT_HEIGHT = 28;
	
	// Stores the two clients whose progress is tracked by each bar.
	private final Pair<Client, Client> clients;
	
	// Number of horizontal columns.
	private int columns;
	
	// Current text to be displayed on next GUI update.
	private String text;
	
	// GUI dimensions.
	private final Dimension size;
	
	/**
	 * Create the gui with the specified width.
	 * @param columns
	 * @throws Exception
	 */
	public ProgressLabel(final int columns) throws Exception {
		super();
		CheckUtils.checkPositive(columns, "columns");
		
		this.columns = columns;
		size = new Dimension(8 * columns, DEFAULT_HEIGHT);
		clients = new Pair<Client, Client>();
	}

	/**
	 * Returns the minimum dimensions.
	 */
	public Dimension getMinimumSize() {
		return size;
	}

	/**
	 * Returns the preferred dimensions.
	 */
	public Dimension getPreferredSize() {
		return size;
	}

	/**
	 * Obtains exclusive access to one of the status bars.  
	 * @param handle
	 * @param max
	 * @return true if the registration was successful, false if both progress
	 * spots are taken.
	 * @throws Exception if the supplied handle is not unique.
	 */
	public synchronized boolean register(final Object handle, 
										 final int max) throws Exception {
		CheckUtils.checkNonNegative(max, "value");
		
		Client c = new Client(handle, max);

		if (contains(handle) != null) {
			throw new Exception("Need unique client handle.");
		}
		
		if (clients.getKey() == null) {
			clients.setKey(c);
			return true;
		}
		else if (clients.getValue() == null) {
			clients.setValue(c);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Releases your lock on the progress bar.  No further progress updates
	 * will be successful.
	 * @param handle
	 * @throws Exception
	 */
	public void release(final Object handle) throws Exception {
		if (handle == null) {
			return;
		}
		if (clients.getKey() != null && clients.getKey().getHandle() == handle) {
			clients.setKey(null);
		}
		else if (clients.getValue() != null && clients.getValue().getHandle() == handle) {
			clients.setValue(null);
		}
		else {
			throw new Exception("Releasing unregistered handle: " + handle.toString() + ".");
		}
	}
	
	/**
	 * Returns the client for the given handle, null if it matches neither.
	 * @param handle
	 * @return
	 * @throws Exception
	 */
	private Client contains(final Object handle) throws Exception {
		CheckUtils.check(handle, "client pointer");
		if (clients.getFirst() != null && clients.getFirst().getHandle() == handle) {
			return clients.getFirst();
		}
		else if (clients.getSecond() != null && clients.getSecond().getHandle() == handle) {
			return clients.getSecond();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns the client for the given handle.  Throws if there is none.
	 * @param client
	 * @return
	 * @throws Exception
	 */
	private Client getClient(final Object handle) throws Exception {
		Client c = contains(handle);
		if (c == null) {
			throw new Exception("Unknown handle: " + handle.toString() + 
								", did you register it?");
		}
		return c;
	}
	
	/**
	 * Resets progress to zero and the max value to the specified value.
	 * @param client
	 * @param max
	 * @throws Exception
	 */
	public void reset(final Object client, final int max) throws Exception {
		getClient(client).reset(max);
		repaint();
	}

	/**
	 * Sets the max value to the specified value, must be larger than the
	 * current value.
	 * @param client
	 * @param max
	 * @throws Exception
	 */
	public void setMax(final Object client, final int max) throws Exception {
		getClient(client).setMax(max);
		repaint();
	}

	/**
	 * Increments the current value.
	 * @param client
	 * @throws Exception
	 */
	public void increment(final Object client) throws Exception {
		getClient(client).increment();
		repaint();
	}
	
	/**
	 * Sets the current value.
	 * @param client
	 * @param value
	 * @throws Exception
	 */
	public void setValue(final Object client, final int value) throws Exception {
		getClient(client).setValue(value);
		repaint();
	}

	/**
	 * Sets the displayed text value.  This is immediate and triggers a 
	 * repaint.  Consider using StatusField for its throttling feature.
	 * @param text
	 */
	public void setText(final String text) {
		this.text = text;
		repaint();
	}
	
	/**
	 * Returns the currently-displayed text.
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Returns the current value for the specified client.
	 * @param client
	 * @return
	 * @throws Exception
	 */
	public int getValue(final Object client) throws Exception {
		return getClient(client).getValue();
	}
	
	/**
	 * Returns the max value for the specified client.
	 * @param client
	 * @return
	 * @throws Exception
	 */
	public int getMax(final Object client) throws Exception {
		return getClient(client).getMax();
	}
	
	/**
	 * Invoked by parent to draw the contents of the component.
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Integer widthFront = null;
		Integer widthBack = null;

		if (clients.getFirst() != null && clients.getSecond() != null) {
			widthFront = Math.min(clients.getFirst().getWidth(getWidth()), 
								   clients.getSecond().getWidth(getWidth()));
			widthBack = Math.max(clients.getFirst().getWidth(getWidth()), 
					   			   clients.getSecond().getWidth(getWidth()));
		}
		else if (clients.getFirst() != null) {
			widthBack = clients.getFirst().getWidth(getWidth());
		}
		else if (clients.getSecond() != null) {
			widthBack = clients.getSecond().getWidth(getWidth());
		}
		else {
			// Leave both null, no progress.
		}
		
		// Paint the background.
		g.setColor(GREY);
		g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);

		if (widthBack != null) {
			// Paint the back progress.
			g.setColor(BACK_PROGRESS);
			g.fillRect(2, 2, widthBack - 4, getHeight() - 4);
	
			g.setColor(BACK_BORDER);
			g.drawRect(2, 2, widthBack - 4, getHeight() - 4);
		}
		if (widthFront != null) {
			// Paint the front progress.
			g.setColor(FRONT_PROGRESS);
			g.fillRect(2, 2, widthFront - 4, getHeight() - 4);

			g.setColor(FRONT_BORDER);
			g.drawRect(2, 2, widthFront - 4, getHeight() - 4);
		}
		
		try {
			if (text != null) {
				g.setColor(Color.BLACK);
				g.setFont(FONT);
				g.drawString(StringUtils.resize(text, columns), 8, 18);
			}
		}
		catch (Exception e) {
			System.out.println("ProgressLabel unable to render string:\n" + text);
		}

		g.setColor(Color.BLACK);
		g.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
	}
	
}

/**
 * Defines a type holding information about each ProgressLabel client.
 */
class Client {
	private final Object handle;
	private int value;
	private int max;
	private long startTimeMS;
	
	/**
	 * Creates the client.
	 * @param handle
	 * @param max
	 * @throws Exception
	 */
	public Client(final Object handle, final int max) throws Exception {
		CheckUtils.check(handle, "client handle");
		CheckUtils.checkNonNegative(max, "value");
		this.handle = handle;
		reset(max);
	}
	
	/**
	 * @return the progress value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Increments the progress value.
	 * @throws Exception
	 */
	public synchronized final void increment() throws Exception {
		if (value >= max) {
			throw new Exception("Invalid progress value: " + value + ", max = " + max + ".");
		}
		this.value++;
	}

	/**
	 * Sets the progress value.
	 * @param value
	 * @throws Exception
	 */
	public synchronized final void setValue(final int value) throws Exception {
		if (value < 0 || value > max) {
			throw new Exception("Invalid progress value: " + value + ", max = " + max + ".");
		}
		this.value = value;
	}

	/**
	 * @return the max progress.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Sets progress to zero, max to the specified value.
	 * @param max
	 * @throws Exception
	 */
	public synchronized void reset(final int max) throws Exception {
		CheckUtils.checkNonNegative(max);
		this.startTimeMS = System.currentTimeMillis();
		this.value = 0;
		this.max = max;
	}
	
	/**
	 * Sets the max value.
	 * @param max
	 * @throws Exception
	 */
	public void setMax(final int max) throws Exception {
		if (value > max) {
			throw new Exception("Invalid progress value: " + value + ", max = " + max + ".");
		}
		this.max = max;
	}

	/**
	 * Returns the start time.
	 * @return
	 */
	public long getStartTime() {
		return startTimeMS;
	}

	/**
	 * Returns the client handle.
	 * @return
	 */
	public Object getHandle() {
		return handle;
	}	

	/**
	 * Returns the relative progress.  Specifically, provide this function the
	 * status bar width in pixels, this tells you how much to fill.
	 * @param totalWidth
	 * @return
	 */
	public int getWidth(final int totalWidth) {
		return (totalWidth * value) / max;
	}
}