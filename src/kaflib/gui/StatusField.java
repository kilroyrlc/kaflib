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

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kaflib.types.Worker;
import kaflib.utils.CheckUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a basic GUI window that provides a status one-liner to the user.
 * Also contains a ProgressLabel that permits the display of progress.
 */
public class StatusField extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// Main gui element.
	private final ProgressLabel text;
	
	// Number of text columns.
	private final int width;
	
	// Minimum duration a message should be displayed.
	private int displayTimeMS;
	
	// Last message change.
	private long lastDisplayMS;
	
	// The message to be displayed at any given time.
	private String message;
	
	// Parent frame.
	private final JFrame frame;
	
	// Thread for updating the UI asynchronously.
	private RecallThread recall;
	
	public static final int DEFAULT_WIDTH = 80;
	public static final int DEFAULT_DISPLAY_TIME_MS = 1000;
	
	/**
	 * Creates a status window with its own frame parent.
	 * @throws Exception
	 */
	public StatusField(final String title) throws Exception {
		super(new FlowLayout());
		
		frame = new JFrame();
		frame.setTitle(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		width = DEFAULT_WIDTH;
		displayTimeMS = DEFAULT_DISPLAY_TIME_MS;
		recall = null;
		
		text = new ProgressLabel(width);
		
		text.setText("");
		lastDisplayMS = System.currentTimeMillis();

		add(text);
		
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Creates a status field with the default text width.
	 * @throws Exception
	 */
	public StatusField() throws Exception {
		this(DEFAULT_WIDTH);
	}
	
	/**
	 * Creates the status window.
	 * @param parent
	 * @throws Exception
	 */
	public StatusField(int width) throws Exception {
		super(new FlowLayout());

		frame = null;
		this.width = width;
		displayTimeMS = DEFAULT_DISPLAY_TIME_MS;
		recall = null;
		
		text = new ProgressLabel(width);
		
		text.setText("");
		lastDisplayMS = System.currentTimeMillis();
		
		add(text);
	}
	
	/**
	 * Provides access to the progress bar.
	 * @return
	 * @throws Exception
	 */
	public ProgressLabel getProgressBar() {
		return text;
	}
	
	/**
	 * Sets the minimum amount of time a message will be displayed.
	 * @param timeMS
	 * @throws Exception
	 */
	public void setDisplayTime(final int timeMS) throws Exception {
		CheckUtils.checkPositive(timeMS, "time");
		displayTimeMS = timeMS;
	}
	
	/**
	 * Sets the text to display.  Its actual display is dependent on the
	 * update interval.  So call this as frequently as you like.
	 * @param text
	 */
	public void setText(final String text) {
		if (text == null) {
			message = "";
		}
		else {
			message = text;
		}
		
		final long timeMS = System.currentTimeMillis() - lastDisplayMS;
		
		// If > display_time has elapsed since the last update, just update
		// the text box straightaway.
		if (lastDisplayMS < 1 || timeMS > displayTimeMS) {
			updateText();
		}
		// Otherwise, check the recall thread, if it has not been set, set it
		// to update the text at the appropriate time.
		else {
			if (recall == null || recall.isDone()) {
				recall = new RecallThread(timeMS);
				recall.start();
			}
		}
	}

	/**
	 * Internal method to actually update the text.  This should be invoked
	 * from the recall thread when the minimum display interval has elapsed.
	 */
	private void updateText() {
		if (message == null) {
			return;
		}
		lastDisplayMS = System.currentTimeMillis();
		
		if (message.length() < width) {
			text.setText(message);
		}
		else {
			try {
				text.setText(StringUtils.resize(message, width));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Disposes of the GUI component.
	 */
	public void close() {
		this.setVisible(false);
		if (frame != null) {
			frame.setVisible(false);
			frame.dispose();
		}
	}
	
	/**
	 * Thread that handles calling update after the given interval.
	 */
	class RecallThread extends Thread {
		private boolean done;
		private long timeMS;
		
		/**
		 * Creates the thread to call updateText() after the supplied wait.
		 * @param timeMS
		 */
		public RecallThread(long timeMS) {
			done = false;
			this.timeMS = timeMS;
		}
		
		/**
		 * Main thread execution.  Waits for the given number of milliseconds,
		 * calls updateText(), completes.
		 */
		public void run() {
			try {
				Thread.sleep(timeMS);
				updateText();
				done = true;
			}
			catch (Exception e) {
				System.out.println("Status field recall thread failed to sleep.");
				e.printStackTrace();
			}
		}
		
		/**
		 * Returns whether or not the thread completed.
		 * @return
		 */
		public boolean isDone() {
			return done;
		}
	}
	
	/**
	 * Unit test.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			final StatusField field = new StatusField("Unit test");
			final Object handle0 = new Object();
			final Object handle1 = new Object();
			
			field.setText("One client, ten seconds.");
			field.getProgressBar().register(handle0, 10);
			for (int i = 0; i < 10; i++) {
				Thread.sleep(1000);
				field.getProgressBar().increment(handle0);
			}
			field.getProgressBar().release(handle0);
			field.setText("Onto the next test...");
			Thread.sleep(3000);

			field.setText("Running.");
			
			Worker worker = new Worker() {
				protected void process() throws Exception {
					int value = RandomUtils.randomInt(400, 800);
					field.getProgressBar().register(handle1, value);
					while (field.getProgressBar().getValue(handle1) < field.getProgressBar().getMax(handle1)) {
						Thread.sleep(50);
						field.getProgressBar().increment(handle1);
					}
					field.getProgressBar().release(handle1);
				}
			};
			worker.start();

			int value = RandomUtils.randomInt(30, 45);
			field.getProgressBar().register(handle0, value);
			for (int i = value; i > 0; i--) {
				Thread.sleep(RandomUtils.randomLong(50, 1000));
				field.getProgressBar().setValue(handle0, value - i);
				
				if (!worker.isDone() && RandomUtils.randomBoolean(20)) {
					field.getProgressBar().setValue(handle1, field.getProgressBar().getValue(handle1) + 50);
				}
			}
			field.getProgressBar().release(handle0);

			field.setText("Waiting on second thread.");
			worker.blockUntilDone(null);
			
			field.setText("Done.");
			
			field.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
