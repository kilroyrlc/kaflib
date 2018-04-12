package kaflib.gui;

import java.awt.Component;

import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import kaflib.utils.AESUtils;

/**
 * Defines a field for inputting passwords, that is, it uses dots instead of
 * characters.
 */
public class PasswordPanel extends TextFieldPanel {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_PASSWORD_WIDTH = 16;
	private JPasswordField field;

	public PasswordPanel(final String label) {
		this(label, DEFAULT_PASSWORD_WIDTH);
	}
	
	public PasswordPanel(final String label, final int width) {
		super(label, width);
		
		field = new JPasswordField();
		field.setColumns(width);
		getLabel().setLabelFor(field);
		
		this.add(field);
	}

	public SecretKey getKey() throws Exception {
		return getKey(getText().substring(0, AESUtils.SALT_LENGTH).getBytes("UTF-8"));
	}
	
	public SecretKey getKey(final byte salt[]) throws Exception {
		return AESUtils.generateKey(getText(), salt);
	}
	
	public String getText() {
		return new String(field.getPassword());
	}
	
	public JPasswordField getField() {
		return field;
	}
	
	public static SecretKey promptForPassword(final Component parent) throws Exception {
		final PasswordPanel panel = new PasswordPanel("Password");
		if (JOptionPane.showConfirmDialog(parent, panel) == JOptionPane.OK_OPTION) {
			return panel.getKey();
		}
		else {
			return null;
		}
	}
}
