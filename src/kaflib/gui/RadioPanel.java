package kaflib.gui;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import kaflib.utils.GUIUtils;

public class RadioPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final JRadioButton buttons[];
	private final ButtonGroup group;
	
	public RadioPanel(final String title, 
					  final String... options) throws Exception {
		super(new GridLayout(0, 1));
		if (options.length < 1) {
			throw new Exception("Must supply button names.");
		}
		
		if (title != null) {
			setBorder(GUIUtils.getTitledBorder(title));
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
	
	public void setEnabled(final boolean enabled) {
		for (JRadioButton button : buttons) {
			button.setEnabled(enabled);
		}
	}
	
	public String getSelected() {
		return group.getSelection().getActionCommand();
	}
}
