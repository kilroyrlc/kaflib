package kaflib.applications;


import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import kaflib.utils.MTGNameGenerator;


public class MTGNameGeneratorGUI {
	public static void main(String args[]) {
		try {
			final JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			final JPanel panel = new JPanel(new FlowLayout());
			final JTextField field = new JTextField();
			field.setColumns(32);
			field.setEditable(false);
			final JButton button = new JButton("Generate");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						field.setText(MTGNameGenerator.generate(" "));
					}
					catch (Exception ex) {
						field.setText("Exception");
						ex.printStackTrace();
					}
				} 
			});

			field.setText(MTGNameGenerator.generate(" "));
			
			panel.add(field);
			panel.add(button);
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
