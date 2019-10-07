package kaflib.gui.components;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import kaflib.utils.CheckUtils;

public class RatingPanel extends KPanel implements MouseListener {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_SCALE = 5;
	
	private final Color highlight_color;
	private final Color set_color;
	private final Color unset_color;
	private final JLabel stars[];
	private Integer selection;
	
	public RatingPanel() throws Exception {
		this(null, DEFAULT_SCALE, false);
	}
	
	public RatingPanel(final String label, 
					   final int scale,
					   final boolean readOnly) throws Exception {
		super(new FlowLayout());
		stars = new JLabel[scale];
		highlight_color = Color.GREEN.brighter();
		set_color = Color.GREEN;
		unset_color = Color.DARK_GRAY;
		selection = null;
		
		if (label != null) {
			add(new JLabel(label));
		}
		for (int i = 0; i < scale; i++) {
			stars[i] = createLabel(readOnly);
			add(stars[i]);
		}
	}
	
	private final JLabel createLabel(final boolean readOnly) throws Exception {
		JLabel label = new JLabel("$");
		if (!readOnly) {
			label.addMouseListener(this);
		}
		label.setForeground(unset_color);
		return label;
	}

	public void setEnabled(final boolean enabled) {
		for (JLabel label : stars) {
			label.setEnabled(enabled);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		for (int i = 0; i < stars.length; i++) {	
			if (e.getSource().equals(stars[i])) {
				selection = i;
				return;
			}
		}
	}

	public void setValue(final int value) throws Exception {
		CheckUtils.checkRange(value, 0, stars.length - 1, "Value out of range");
		selection = value;
		updateStars();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Color color = highlight_color;
		for (int i = 0; i < stars.length; i++) {	
			stars[i].setForeground(color);
			if (e.getSource().equals(stars[i])) {
				color = unset_color;
			}
		}
	}

	private void updateStars() {
		Color color = set_color;
		for (int i = 0; i < stars.length; i++) {	
			stars[i].setForeground(color);
			if (i == selection) {
				color = unset_color;
			}
		}		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		if (selection == null) {
			for (int i = 0; i < stars.length; i++) {	
				stars[i].setForeground(unset_color);
			}
		}
		else {
			updateStars();
		}
	}
	
	/**
	 * Simple driver.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			frame.setContentPane(new RatingPanel("Rating:", 7, false));
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
