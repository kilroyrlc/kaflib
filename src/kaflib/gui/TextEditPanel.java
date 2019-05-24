package kaflib.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kaflib.types.Worker;
import kaflib.utils.AESUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.GUIUtils.ButtonType;
import kaflib.utils.KeyPair;

public class TextEditPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_LINES = 32;
	
	private final JPanel button_pane;
	
	private final JTextArea text;
	private final JScrollPane scroll;
	private final JButton save;
	
	private final File file;
	private final KeyPair keys;
	
	public TextEditPanel(final File file, final KeyPair keys) throws Exception {
		super(new BorderLayout());
		this.keys = keys;
		this.file = file;
		
		if (keys != null) {
			byte bytes[] = AESUtils.decrypt(file, keys);
			String input = new String(bytes);
			text = new JTextArea(input, DEFAULT_LINES, 80);
		}
		else {
			String input = FileUtils.readString(file);
			text = new JTextArea(input, DEFAULT_LINES, 80);
		}
		text.setEditable(true);
		text.setBorder(GUIUtils.getEmptyBorder(5));
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		
		scroll = new JScrollPane(text);
		add(scroll, BorderLayout.CENTER);

		button_pane = new JPanel();
		button_pane.setLayout(new BoxLayout(button_pane, BoxLayout.Y_AXIS));
		
		save = GUIUtils.getButton(ButtonType.SAVE);
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Worker worker;
				try {
					worker = new Worker() {
						@Override
						protected void process() throws Exception {
							save();
						}
					};
					worker.start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});
		button_pane.add(save);
		
		add(button_pane, BorderLayout.WEST);
	}
	
	private void save() {
		try {
			if (keys == null) {
				FileUtils.write(file, text.getText());
			}
			else {
				AESUtils.encrypt(file, text.getText().getBytes("UTF-8"), keys);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new TextEditPanel(new File("C:\\Users\\0\\Desktop\\controls.txt"), null));
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
