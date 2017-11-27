package kaflib.applications.mtg;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import kaflib.gui.Suggestor;
import kaflib.types.WordTree;

public class Query {
	
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame("MTG DB");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JPanel outer = new JPanel(new BorderLayout());
			JPanel top = new JPanel(new FlowLayout());
			JPanel bottom = new JPanel(new BorderLayout());
			
			final JLabel label = new JLabel("");
			final CardDatabase db = new CardDatabase();
			final JTextField field = new JTextField(32);
			field.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					Boolean value = db.have(field.getText());
					if (value == null) {
						label.setText("No '" + field.getText() + "'.");
					}
					else if (value == true) {
						label.setText("Have '" + field.getText() + "'.");
					}
					else {
						label.setText("Don't have '" + field.getText() + "'.");
					}					
				}
				@Override
				public void focusLost(FocusEvent e) {
				
				}
			});
			
			WordTree tree = new WordTree(db.getNames());
			new Suggestor(field, 
							  frame, 
							  tree,
							  true);
			label.setText("Read " + db.getNames().size() + " cards.");

			top.add(field);
			
			JButton button = new JButton("Add");
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						db.setHave(field.getText(), true);
						Boolean value = db.have(field.getText());
						if (value == null) {
							label.setText("No '" + field.getText() + "'.");
						}
						else if (value == true) {
							label.setText("Have '" + field.getText() + "'.");
							field.setText("");
						}
						else {
							label.setText("Don't have '" + field.getText() + "'.");
							field.setText("");
						}	
					}
					catch (Exception ex) {
						System.out.println("Unable to store value:\n" + ex.getMessage());
					}
				}
			});
			top.add(button);
			
			button = new JButton("Save");
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						label.setText("Saving on stupid ui thread.");
						db.write();
						label.setText("Saved.");
					}
					catch (Exception ex) {
						System.out.println("Unable to write.");
						ex.printStackTrace();
					}
				}
			});
			top.add(button);
			
			bottom.add(label, BorderLayout.CENTER);
			
			outer.add(top, BorderLayout.CENTER);
			outer.add(bottom, BorderLayout.SOUTH);
			
			frame.setContentPane(outer);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
