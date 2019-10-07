package kaflib.gui.composite;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import kaflib.gui.components.KIconButton;
import kaflib.gui.components.KListener;
import kaflib.gui.components.KPanel;
import kaflib.types.Directory;
import kaflib.utils.AESUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.KeyPair;

/**
 * Defines a text editor panel with optional crypto component.
 */
public class TextEditPanel extends KPanel implements KListener {
	
	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_LINES = 32;
	
	private static final long MAX_FILE_SIZE = 2048;
	
	private final KPanel button_pane;
	
	private final JTextArea text;
	private final JScrollPane scroll;
	private final KIconButton save;
	private final KIconButton open;
	
	private File file;
	private final KeyPair keys;
	
	public TextEditPanel(final File file, final KeyPair keys) throws Exception {
		super(new BorderLayout());
		this.keys = keys;
		this.file = file;
		
		text = new JTextArea("", DEFAULT_LINES, 80);
		text.setEditable(true);
		text.setBorder(GUIUtils.getEmptyBorder(5));
		text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		
		scroll = new JScrollPane(text);
		add(scroll, BorderLayout.CENTER);


		save = new KIconButton(KIconButton.IconType.DISK, 
				       		   this,
							   true);
		
		open = new KIconButton(KIconButton.IconType.DIRECTORY, 
				       		   this,
							   true);

		button_pane = new KPanel(true, save, open);
		
		add(button_pane, BorderLayout.WEST);
		
		updateText(file);
	}
	
	private final void updateText(final File file) {
		text.setEditable(false);
		boolean success = false;
		String input = "No accessible file selected.";
		try {
			if (file.length() <= MAX_FILE_SIZE) {
				if (keys != null) {
					byte bytes[] = AESUtils.decrypt(file, keys);
					input = new String(bytes);
					success = true;
				}
				else {
					input = FileUtils.readString(file);
					success = true;
				}
				text.setText(input);
			}
		}
		catch (Exception e) {
			input = e.getMessage();
		}
		
		if (success) {
			text.setEditable(true);
		}
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
	
	private void open() {
		try {
			Directory parent = new Directory();
			if (file != null) {
				parent = new Directory(file.getParent());
			}
			file = GUIUtils.chooseFile(this, parent);
			updateText(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void serialValueChanged(Component component) {
		button_pane.setEnabled(false);
	}

	@Override
	public void asyncValueChanged(Component component) {
		if (component == save) {
			save();
		}
		else if (component == open) {
			open();
		}
		else {
			
		}
		button_pane.setEnabled(true);
	}
	
	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new TextEditPanel(null, null));
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
