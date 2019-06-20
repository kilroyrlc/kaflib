package kaflib.applications;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import kaflib.graphics.Canvas;
import kaflib.gui.components.KRadioPanel;
import kaflib.types.Directory;
import kaflib.types.Worker;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;

public class ImageConverter extends JFrame {

	private static final long serialVersionUID = 1L;
	private final KRadioPanel format;
	private final JButton go;
	private final JButton exit;
	private Directory working_directory;
	
	public ImageConverter() throws Exception {
		super();
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		working_directory = new Directory();
		JPanel panel = new JPanel(new FlowLayout());
		
		format = new KRadioPanel("Format", "png", "jpg");
		go = new JButton("Go...");
		go.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					go.setEnabled(false);
					exit.setEnabled(false);
					format.setEnabled(false);
					Worker worker = new Worker() {
						@Override
						protected void process() throws Exception {
							convert();
						}
					};
					worker.start();
				}
				catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		});
		
		exit = new JButton("Exit");
		exit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		panel.add(format);
		panel.add(go);
		panel.add(exit);
		
		setContentPane(panel);
		pack();
	}
	
	private void exit() {
		setVisible(false);
		dispose();
		System.exit(0);
	}
	
	private void convert() throws Exception {
		Set<File> files = GUIUtils.chooseFiles(this, 
											   working_directory, 
											   new FileNameExtensionFilter("Images", "png", "jpg"));

		if (files != null && files.size() > 0) {
			working_directory = new Directory(files.iterator().next().getParentFile());
			Directory output_directory = new Directory(working_directory, "output");
			if (!output_directory.exists()) {
				output_directory.mkdir();
			}
			for (File file : files) {
				Canvas canvas = new Canvas(file);
				File output = new File(output_directory, file.getName());
				
				if (format.getSelected().equals("png")) {
					output = FileUtils.changeExtension(output, "png");
					canvas.toPNG(output);
				}
				else if (format.getSelected().equals("jpg")) {
					output = FileUtils.changeExtension(output, "jpg");
					canvas.toJPG(output);
				}
				else {
					throw new Exception("Unknown format: " + format.getSelected() + ".");
				}
			}
		}
		
		format.setEnabled(true);
		go.setEnabled(true);
		exit.setEnabled(true);
	}
	
	public static void main(String args[]) {
		try {
			ImageConverter converter = new ImageConverter();
			converter.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
