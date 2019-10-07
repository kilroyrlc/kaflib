package kaflib.applications.sandbox;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import kaflib.graphics.Canvas;
import kaflib.graphics.CanvasTransform;
import kaflib.graphics.Filter;
import kaflib.graphics.Opacity;
import kaflib.graphics.RGBPixel;
import kaflib.graphics.transform.LinearEdgeTransform;
import kaflib.graphics.transform.AreaEdgeTransform;
import kaflib.graphics.transform.DabTransform;
import kaflib.graphics.transform.FeatherTransform;
import kaflib.gui.components.DownscaledImageComponent;
import kaflib.gui.components.KButton;
import kaflib.gui.components.KButton.ButtonType;
import kaflib.gui.graphics.CropListener;
import kaflib.gui.graphics.EdgePanel;
import kaflib.gui.graphics.FilterListener;
import kaflib.gui.graphics.ThumbCropComponent;
import kaflib.types.Box;
import kaflib.types.Coordinate;
import kaflib.types.Worker;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;

/**
 * Defines a type that shows a raster image and a client/child-specified 
 * toolbar.
 */
public class ImageEditor extends JFrame {
	 
	private static final long serialVersionUID = 1L;
	private final File source_file;
	private final String name_root;

	// Images.
	private Canvas input;
	private Canvas output;
	
	private final ThumbCropComponent input_image;
	private final DownscaledImageComponent output_image;
	private final DownscaledImageComponent test_image;
	private static final int TEST_SIZE = 160;
	
	// Buttons.
	private final JScrollPane button_scroll;
	private final JPanel button_panel;
	private final EdgePanel edge_filter;
	private final JButton save_button;
	private final JButton reset_button;
	private final JButton feather_button;
	
	/**
	 * Create the editor with the specified image.
	 * @param image
	 * @param inputs
	 * @throws Exception
	 */
	public ImageEditor(final File file) throws Exception {
		super();
		CheckUtils.checkReadable(file, "file: " + file);
		
		this.source_file = file;
		name_root = FileUtils.getFilenameWithoutExtension(file);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel(new BorderLayout());
		JPanel image_panel = new JPanel(new GridLayout(1, 2));
		JPanel temp;
		
		// Images.
		temp = GUIUtils.getTitledPanel("Input");
		input = new Canvas(file);
		input_image = new ThumbCropComponent(file);
		input_image.addListener(new CropListener() {
			@Override
			public void cropChanged() {
				try {
					resetOutput();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		temp.add(input_image);
		image_panel.add(temp);
		

		temp = GUIUtils.getTitledPanel("Output");
		output_image = new DownscaledImageComponent(file);
		resetOutput();
		temp.add(output_image);
		image_panel.add(temp);
		
		panel.add(image_panel, BorderLayout.CENTER);
		
		// Buttons.
		button_panel = new JPanel();
		button_panel.setLayout(new GridLayout(4, 1));
		
		JPanel op_panel = new JPanel(new FlowLayout());
		save_button = GUIUtils.getButton(KButton.ButtonType.SAVE);
		save_button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				save();
			}
		});
		op_panel.add(save_button);
		reset_button = GUIUtils.getButton(KButton.ButtonType.FILE);
		reset_button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				reset();
			}
		});
		op_panel.add(reset_button);
		
		button_panel.add(op_panel);
		temp = GUIUtils.getTitledPanel("Test");
		test_image = new DownscaledImageComponent(new Canvas(TEST_SIZE, TEST_SIZE));
		temp.add(test_image);
		button_panel.add(temp);
		
		edge_filter = new EdgePanel(new FilterListener() {
			@Override
			public void apply(Filter filter, final boolean test) {
				setEnabled(false);
				if (!test) {
					applyEdge(filter);
				}
				else {
					applyEdgeTest(filter);
				}
			}});
		button_panel.add(edge_filter);
		
		feather_button = new JButton("Feather");
		feather_button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				feather();
			}
		});
		button_panel.add(feather_button);
		
		
		button_scroll = new JScrollPane(button_panel);
		panel.add(button_scroll, BorderLayout.WEST);

		setContentPane(panel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pack();
		setVisible(true);

	}
	
	private void applyEdge(final Filter filter) {
		try {
			Worker worker = new Worker() {
				protected void process() throws Exception {
					output_image.set(new Canvas(filter.apply(output)));
				}
			};
			worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setEnabled(true);
		}
	}
	
	private void applyEdgeTest(final Filter filter) {
		try {
			Worker worker = new Worker() {
				protected void process() throws Exception {
					Box thumbnail = input_image.getThumbnail();
					// Thumbnail set, use that for the test.
					if (thumbnail != null) {
						if (input_image.getCrop() != null) {
							Coordinate crop_tl = input_image.getCrop().getTopLeft();
							Coordinate thumb_tl = thumbnail.getTopLeft();
							thumbnail = new Box(thumb_tl.getX() - crop_tl.getX(), 
												thumbnail.getWidth(), 
												thumb_tl.getY() - crop_tl.getY(),
												thumbnail.getHeight());
						}
					}
					// Thumbnail not set, just use the output.
					else {
						thumbnail = output.getBounds();
					}
					if (!thumbnail.isContained(output.getBounds())) {
						thumbnail = output.getRandomBox(TEST_SIZE, TEST_SIZE);
					}
					test_image.set(filter.apply(output.get(thumbnail)));
				}
			};
			worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setEnabled(true);
		}
	}
	
	public void feather() {
		try {
			Worker worker = new Worker() {
				protected void process() throws Exception {
//					output_image.getCanvas().applyTransform(new FeatherTransform());
					output.applyTransform(new DabTransform());
					output_image.set(output);
System.out.println("Done");
				}
			};
			worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setEnabled(true);
		}
	}
	
	public void setEnabled(final boolean enabled) {
		edge_filter.setEnabled(enabled);
	}

	private final void resetOutput() throws Exception {
//		if (input_image.getCrop() != null) {
//			output_image.set(input_image.getSourceCanvas().get(input_image.getCrop()));
//		}
//		else {
//			output_image.set(input_image.getSourceCanvas());
//		}
	}
	
	public void reset() {
		try {
			Worker worker = new Worker() {
				protected void process() throws Exception {
					resetOutput();
				}
			};
			worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setEnabled(true);
		}
	}
	
	public void save() {
		try {
			Worker worker = new Worker() {
				protected void process() throws Exception {
					File new_file = FileUtils.createUniqueFile(source_file.getParentFile(), 
											   				  name_root + "_", 
											   				  ".png");
					output.toPNG(new_file);
					
				}
			};
			worker.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setEnabled(true);
		}
	}

	/**
	 * Unit test function.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			new ImageEditor(new File("data/flag.jpg"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

