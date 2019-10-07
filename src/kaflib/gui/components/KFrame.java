package kaflib.gui.components;

import javax.swing.JFrame;

public class KFrame extends JFrame {

	private static final long serialVersionUID = -4823526645340754345L;
	private KPanel content;

	public KFrame() throws Exception {
		super();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		content = null;
	}
	
	public KFrame(final int closeOperation) throws Exception {
		super();
		setDefaultCloseOperation(closeOperation);
		content = null;
	}

	public KFrame(final KPanel panel) throws Exception {
		this(JFrame.EXIT_ON_CLOSE, panel);
	}
	
	public KFrame(final int closeOperation, final KPanel panel) throws Exception {
		super();
		content = panel;
		setDefaultCloseOperation(closeOperation);
		setContentPane(panel);
	}
	
	public void setContent(final KPanel panel) throws Exception {
		if (content != null) {
			throw new Exception("Cannot reset content.");
		}
		setContentPane(panel);
	}
	
	public void packAndShow() {
		pack();
		setVisible(true);
	}
}
