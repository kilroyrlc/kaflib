package kaflib.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import kaflib.graphics.GraphicsUtils;
import kaflib.types.Directory;
import kaflib.utils.AESUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.KeyPair;

/**
 * Defines a panel containing an image viewer, a label, and some buttons.
 */
public class ImagePanel extends KPanel {
	
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	
	private final int width;
	private final int height;
	
	private BufferedImage image;
	private Directory last_saved_directory;
	private final DownscaledImageComponent image_component;
	private final JPanel button_panel;
	private final JTextField file_label;
	private final List<JButton> buttons;
	
	/**
	 * Create a default-size viewer.
	 * @throws Exception
	 */
	public ImagePanel() throws Exception {
		super(new BorderLayout());

		last_saved_directory = null;
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		
		file_label = new JTextField("                                   ");
		file_label.setEditable(false);
		add(file_label, BorderLayout.NORTH);
		
		image_component = new DownscaledImageComponent(width, height);
		add(image_component, BorderLayout.CENTER);
		
		button_panel = new JPanel();
		buttons = new ArrayList<JButton>();
		button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.Y_AXIS));
		addButtons();
		setInputEnabled(false);
		add(button_panel, BorderLayout.WEST);
	}
	
	/**
	 * Add the buttons.
	 * @throws Exception
	 */
	private final void addButtons() throws Exception {
		JButton button = GUIUtils.getButton(KButton.ButtonType.SAVE);
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		button_panel.add(button);
		buttons.add(button);
		
		button = GUIUtils.getButton(KButton.ButtonType.FILE);
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				write();
			}
		});
		button_panel.add(button);
		buttons.add(button);

	}

	public void addButton(final JButton button) {
		button_panel.add(button);
	}
	
	private final void setInputEnabled(final boolean enabled) {
		for (JButton button : buttons) {
			button.setEnabled(enabled);
		}
	}
	
	private void write() {
		File file = GUIUtils.chooseNewFile(this, last_saved_directory);
		if (file == null) {
			return;
		}
		try {
			if (!file.getName().endsWith(".png")) {
				file = new File(file.getParentFile(), file.getName() + ".png");
			}
			GraphicsUtils.writePNG(image, file);
			last_saved_directory = new Directory(file.getParentFile());
		}
		catch (Exception e) {
			GUIUtils.showErrorDialog(this, "Unable to save file:\n" + e.getMessage());
		}
	}
	
	private void save() {
		System.out.println("Save");
	}

	public boolean update(final File file, final BufferedImage image) throws Exception {
		if (last_saved_directory == null) {
			last_saved_directory = new Directory(file.getParentFile());
		}
		
		this.image = image;
		image_component.update(GraphicsUtils.getScaled(this.image, width, height));
		file_label.setText(file.getName());
		setInputEnabled(true);
		return true;
	}
	
	public boolean update(final File file, final KeyPair keys) throws Exception {
		if (last_saved_directory == null) {
			last_saved_directory = new Directory(file.getParentFile());
		}
		
		if (keys != null) {
			String name = AESUtils.decryptName(file, AESUtils.DEFAULT_FILE_EXTENSION, keys);
			if (!FileUtils.isGraphicsFile(new File(file.getParentFile(), name))) {
				image = null;
				file_label.setText("");
				setInputEnabled(false);
				return false;
			}
			image = GraphicsUtils.read(AESUtils.decrypt(file, keys));
		}
		else {
			if (!FileUtils.isGraphicsFile(file)) {
				image = null;
				file_label.setText("");
				setInputEnabled(false);
				return false;
			}
			image = GraphicsUtils.read(file);
		}
		image_component.update(GraphicsUtils.getScaled(image, width, height));
		file_label.setText(file.getName());
		setInputEnabled(true);
		return true;
	}
	

	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			ImagePanel panel = new ImagePanel();
			frame.setContentPane(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
