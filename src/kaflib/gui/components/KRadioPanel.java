package kaflib.gui.components;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

public class KRadioPanel extends KPanel {
	
	private static final long serialVersionUID = 1L;
	private final JRadioButton buttons[];
	private final ButtonGroup group;
	
	public KRadioPanel(final String title, 
					  final String... options) throws Exception {
		super(title);
		if (options.length < 1) {
			throw new Exception("Must supply button names.");
		}
		
		group = new ButtonGroup();
		buttons = new JRadioButton[options.length];
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JRadioButton(options[i]);
			buttons[i].setActionCommand(options[i]);
			group.add(buttons[i]);
			add(buttons[i]);
		}
		buttons[0].setSelected(true);
	}

	public void clearSelected() {
		group.clearSelection();
	}
	
	public void setEnabled(final boolean enabled) {
		for (JRadioButton button : buttons) {
			button.setEnabled(enabled);
		}
	}
	
	public String getSelected() {
		if (group.getSelection() == null) {
			return null;
		}
		return group.getSelection().getActionCommand();
	}
}
