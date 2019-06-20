package kaflib.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import kaflib.utils.CheckUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.StringUtils;

/**
 * Panel with a slider on it.
 */
public class SliderPanel extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;
	private final SliderListener listener;
	private final JSlider slider;
	private final JLabel value;
	private final JButton apply;

	public SliderPanel(final String title,
			   SliderListener listener) throws Exception {
		this(title, 0, 99, false, false, listener);
	}
	
	public SliderPanel(final String title, 
					   final int min, 
					   final int max,
					   final boolean showValue,
					   final boolean applyButton,
					   SliderListener listener) throws Exception {
		super(new BorderLayout());
		this.listener = listener;
		if (title != null) {
			setBorder(GUIUtils.getTitledBorder(title));
		}
		
		slider = new JSlider(min, max);
		if (!applyButton) {
			slider.addChangeListener(this);
		}
		add(slider, BorderLayout.CENTER);
		
		if (showValue) {
			value = new JLabel();
			Font font = new Font(Font.MONOSPACED, Font.PLAIN, 14);
			value.setFont(font);
			String m = "" + max;
			value.setText(StringUtils.toString(' ', m.length()));
			add(value, BorderLayout.EAST);
		}
		else {
			value = null;
		}
		if (applyButton) {
			JPanel p = new JPanel(new FlowLayout());
			p.setAlignmentX(LEFT_ALIGNMENT);
			apply = new JButton("Apply");
			apply.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					apply();
				}
			});
			p.add(apply);
			add(p, BorderLayout.SOUTH);
		}
		else {
			apply = null;
		}
	}
	
	public void setValue(final int value) throws Exception {
		CheckUtils.checkRange(value, slider.getMinimum(), slider.getMaximum(), "slider value");
		slider.setValue(value);
	}
	
	private void changed() {
		if (listener != null) {
			listener.valueChanged(slider.getValue());
		}
	}
	
	public int getValue() {
		return slider.getValue();
	}
	
	private void apply() {
		changed();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		changed();
	}
	
	public static void main(String args[]) {
		try {

			SliderPanel panel = new SliderPanel("test", 0, 69, true, false, new SliderListener(){
				@Override
				public void valueChanged(int value) {
					System.out.println("Value changed.");
				}}
			);

			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
