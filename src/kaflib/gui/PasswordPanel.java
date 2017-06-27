package kaflib.gui;

import javax.swing.JComponent;
import javax.swing.JPasswordField;

/**
 * Defines a field for inputting passwords, that is, it uses dots instead of
 * characters.
 */
public class PasswordPanel extends TextFieldPanel {

	private static final long serialVersionUID = 1L;
	private JPasswordField field;
	
	public PasswordPanel(final String label, final int width) {
		super(label, width);
		
		field = new JPasswordField();
		field.setColumns(width);
		getLabel().setLabelFor(field);
		
		this.add(field);
	}

	public String getText() {
		return new String(field.getPassword());
	}
	
	public JComponent getField() {
		return field;
	}
}
