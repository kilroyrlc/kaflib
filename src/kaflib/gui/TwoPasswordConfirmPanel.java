package kaflib.gui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import kaflib.utils.KeyPair;

public class TwoPasswordConfirmPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final PasswordConfirmPanel inner;
	private final PasswordConfirmPanel outer;
	
	private TwoPasswordConfirmPanel() throws Exception {
		super(new GridLayout(1, 2));
		setBorder(new EmptyBorder(4, 4, 4, 4));
		outer = new PasswordConfirmPanel("Outer");
		inner = new PasswordConfirmPanel("Inner");
		add(outer);
		add(inner);
	}
	
	private PasswordConfirmPanel getOuter() {
		return outer;
	}
	
	private PasswordConfirmPanel getInner() {
		return inner;
	}
	
	public static KeyPair promptForPasswords(final Component parent) throws Exception {
		final TwoPasswordConfirmPanel panel = new TwoPasswordConfirmPanel();
		if (JOptionPane.showConfirmDialog(parent, panel, "Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			return new KeyPair(panel.getOuter().getText(),
							   panel.getInner().getText());
		}
		else {
			return null;
		}
	}
}
