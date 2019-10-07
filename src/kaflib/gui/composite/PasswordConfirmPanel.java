package kaflib.gui.composite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.crypto.SecretKey;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import kaflib.gui.components.KPanel;
import kaflib.types.Worker;
import kaflib.utils.AESUtils;

/**
 * Defines a simple panel that prompts the user to enter their password twice.
 * Shows green if the input matches (not related to validity) and red if input
 * seems complete without a match.
 */
public class PasswordConfirmPanel extends KPanel implements KeyListener, FocusListener {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_PASSWORD_WIDTH = 16;
	
	private final JLabel label;
	private final JPanel password_panel;
	private final JPasswordField field_a;
	private final JPasswordField field_b;
	private boolean validating = false;
	
	/**
	 * Create the panel with the label text.
	 * @param labelText
	 * @throws Exception
	 */
	public PasswordConfirmPanel(final String labelText) throws Exception {
		super(new BorderLayout());
		label = new JLabel(labelText);
		label.setBorder(new EmptyBorder(0, 0, 0, 6));
		
		field_a = new JPasswordField(DEFAULT_PASSWORD_WIDTH);
		field_b = new JPasswordField(DEFAULT_PASSWORD_WIDTH);
		field_a.addKeyListener(this);
		field_b.addKeyListener(this);
		field_a.addFocusListener(this);
		field_b.addFocusListener(this);
		
		password_panel = new JPanel(new GridLayout(2, 1));
		password_panel.add(field_a);
		password_panel.add(field_b);
		
		add(label, BorderLayout.WEST);
		add(password_panel, BorderLayout.CENTER);
	}

	/**
	 * Returns the first password.
	 * @return
	 */
	private String getA() {
		return new String(field_a.getPassword());
	}
	
	/**
	 * Returns the second password.
	 * @return
	 */
	private String getB() {
		return new String(field_b.getPassword());
	}
	
	/**
	 * Returns if the first field is empty.
	 * @return
	 */
	private boolean aEmpty() {
		return field_a.getPassword().length == 0;
	}
	
	/**
	 * Returns if the second field is empty.
	 * @return
	 */
	private boolean bEmpty() {
		return field_b.getPassword().length == 0;
	}
	
	/**
	 * Returns whether or not the password is at least as long as the 
	 * parameter. Returns null if the passwords do not match.
	 * @param length
	 * @return
	 */
	public Boolean isLongerOrEqual(final int length) {
		if (getLength() != null || getLength() >= length) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns the length of the password, or null if they do not match.
	 * @return
	 */
	private Integer getLength() {
		if (!match()) {
			return null;
		}
		else {
			return getA().length();
		}
	}
	
	
	public void setEnabled(final boolean enabled) {
		field_a.setEnabled(enabled);
		field_b.setEnabled(enabled);
	}
	
	public boolean match() {
		return getA().equals(getB());
	}
	
	public void clear() {
		field_a.setText("");
		field_b.setText("");
	}
	

	public SecretKey getKey() throws Exception {
		if (!match()) {
			return null;
		}
		
		return AESUtils.generateKey(getA(), 
				getA().substring(0, AESUtils.SALT_LENGTH).getBytes("UTF-8"));
	}

	public SecretKey getKey(final byte salt[]) throws Exception {
		if (!match()) {
			return null;
		}
		return AESUtils.generateKey(getA(), salt);
	}
	
	public String getText() {
		if (match()) {
			return getA();
		}
		else {
			return null;
		}
	}

	/**
	 * Checks that the fields match, on a separate non-even thread.
	 */
	private void checkMatch() {
		if (!validating) {
			return;
		}
		try {
			Worker worker = new Worker() {
	
				@Override
				protected void process() throws Exception {
					if (match() && getA().length() >= 8 && getB().length() >= 8) {
						field_a.setForeground(Color.GREEN);
						field_b.setForeground(Color.GREEN);
					}
					else {
						field_a.setForeground(Color.RED);
						field_b.setForeground(Color.RED);
					}
				}
				
			};
		worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (!validating && !aEmpty() && !bEmpty()) {
			validating = true;
		}
		checkMatch();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (validating) {
			checkMatch();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	public static SecretKey promptForPassword(final Component parent) throws Exception {
		final PasswordConfirmPanel panel = new PasswordConfirmPanel("Password");
		if (JOptionPane.showConfirmDialog(parent, panel) == JOptionPane.OK_OPTION) {
			return panel.getKey();
		}
		else {
			return null;
		}
	}
}
