package kaflib.gui;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Defines a text field with attached label.
 */
public class TextFieldPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JTextField field;

	/**
	 * Create the field with the given values.
	 * @param label
	 * @param defaultValue
	 * @param width
	 */
	public TextFieldPanel(final String label, final String defaultValue, final int width) {
		super(new FlowLayout(FlowLayout.LEFT));

		this.label = new JLabel(label);
		field = new JTextField(defaultValue);
		field.setColumns(width);
		this.label.setLabelFor(field);
		
		this.add(this.label);
		this.add(field);
	}

	protected TextFieldPanel(final String label, final int width) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.label = new JLabel(label);
		this.add(this.label);
	}
	
	/**
	 * Returns the field component.
	 * @return
	 */
	public JComponent getField() {
		return field;
	}

	/**
	 * Returns the pixel width of the label.
	 * @return
	 */
	public int getLabelWidth() {
		return label.getWidth();
	}
	
	/**
	 * Sets the pixel width of the label.
	 * @param width
	 */
	public void setLabelWidth(final int width) {
		label.setSize(label.getHeight(), width);
	}
	
	/**
	 * Returns the label.
	 * @return
	 */
	protected JLabel getLabel() {
		return label;
	}
	
	/**
	 * Returns the text in the field.
	 * @return
	 */
	public String getText() {
		return field.getText();
	}
	
	/**
	 * Sets the text in the field.
	 * @param text
	 */
	public void setText(String text) {
		field.setText(text);
	}
	
}
