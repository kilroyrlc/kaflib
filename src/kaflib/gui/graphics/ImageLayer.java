package kaflib.gui.graphics;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;

import kaflib.graphics.Canvas;
import kaflib.gui.components.KButton;
import kaflib.gui.components.KPanel;
import kaflib.utils.GUIUtils;
import kaflib.utils.StringUtils;


public class ImageLayer extends KPanel {
	private static final long serialVersionUID = 1L;
	private final JButton layer_button;
	private final JButton dupe_button;
	private final JButton delete_button;
	
	private final File file;
	private final String name;
	private Canvas canvas;
	private final ImageLayerListener listener;
	private final boolean immutable;
	private final Color default_bg;
	
	enum Operation {SELECT, DUPLICATE, DELETE};
	
	public ImageLayer(final File file, final ImageLayerListener listener) throws Exception {
		this(file, listener, false);
	}
	
	public ImageLayer(final File file, final ImageLayerListener listener, final boolean immutable) throws Exception {
		super();
		setLayout(new FlowLayout());
		layer_button = GUIUtils.getMonospaceButton(StringUtils.resize(file.getName(), 32, ' '), 16);
		layer_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clicked(Operation.SELECT);
			}
		});	
		add(layer_button);
		
		dupe_button = GUIUtils.getButton(KButton.ButtonType.FILE);
		dupe_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clicked(Operation.DUPLICATE);
			}
		});		
		add(dupe_button);
		
		delete_button = GUIUtils.getMonospaceButton("x", 16);
		if (immutable) {
			delete_button.setEnabled(false);
		}
		else {
			delete_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					clicked(Operation.DELETE);
				}
			});		
		}
		add(delete_button);
		
		this.file = file;
		name = file.getName();
		this.listener = listener;
		this.immutable = immutable;
		canvas = new Canvas(file);
		default_bg = getBackground();
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public void setCanvas(final Canvas canvas) {
		this.canvas = canvas;
	}
	
	public void save() throws Exception {
		canvas.toFile(file);
	}
	
	public boolean isImmutable() {
		return immutable;
	}
	
	public File getFile() {
		return file;
	}
	
	public void delete() {
		file.delete();
	}
	
	public String getName() {
		return name;
	}

	public void setSelected(final boolean pressed) {
		if (pressed) {
			setBackground(default_bg.darker());
		}
		else {
			setBackground(default_bg);
		}
	}
	
	private void clicked(final Operation operation) {
		if (operation == Operation.SELECT) {
			listener.requestVisibility(this);
		}
		else if (operation == Operation.DUPLICATE) {
			listener.duplicate(this);
		}
		else if (operation == Operation.DELETE) {
			listener.delete(this);
		}
	}
	
}
