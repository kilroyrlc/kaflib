package kaflib.gui.components;

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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import kaflib.types.Worker;
import kaflib.utils.CheckUtils;

/**
 * Text field subclass that notifies its client when:
 *    - The string value has changed and
 *       - The field has lost focus or
 *       - After a specified delay from the last keystroke
 */
public class ResponsiveTextField extends JTextField implements FocusListener, KeyListener {

	private static final long serialVersionUID = 1L;

	private final ResponsiveTextFieldListener listener;
	private final long latencyMS;
	private Worker waitThread;
	private String lastValue;
	private long lastTyped;
	
	/**
	 * Creates the field with the specified listener and delay.
	 * @param client
	 * @param delayMS
	 * @throws Exception
	 */
	public ResponsiveTextField(final ResponsiveTextFieldListener client, 
							   final long delayMS) throws Exception {
		super();
		
		CheckUtils.check(client, "listener");
		CheckUtils.checkPositive(delayMS, "delay");
		
		latencyMS = delayMS;
		listener = client;
		
		addFocusListener(this);
		addKeyListener(this);
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		notifyListener();
	}

	/**
	 * Notifies the listener if the text has changed since the last notification.
	 */
	private void notifyListener() {
		if (!getText().equals(lastValue)) {
			lastValue = getText();
			listener.textChanged(lastValue);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		lastTyped = System.currentTimeMillis();
		
		try {
			if (waitThread == null || waitThread.isDone()) {
				waitThread = new Worker() {
					@Override
					protected void process() throws Exception {
						while (System.currentTimeMillis() - lastTyped < latencyMS) {
							Thread.sleep(250);
						}
						notifyListener();
					}
				};
				waitThread.start();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
}
