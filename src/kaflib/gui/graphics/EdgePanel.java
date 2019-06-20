package kaflib.gui.graphics;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import kaflib.graphics.RGBPixel;
import kaflib.graphics.transform.EdgeFilter;
import kaflib.gui.SliderPanel;
import kaflib.types.Worker;
import kaflib.utils.GUIUtils;

/**
 * Panel to allow GUI adjustment of an edge filter.
 */
public class EdgePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final SliderPanel edge_size_panel;
	private final SliderPanel edge_delta_panel;
	private final JCheckBox edge_mask;
	private final JButton edge_test;
	private final JButton edge_apply;
	private final FilterListener listener;
	public static final String DEFAULT_TITLE = "Edges";
	
	public EdgePanel(final FilterListener listener) throws Exception {
		this(listener, DEFAULT_TITLE);
	}
	
	public EdgePanel(final FilterListener listener, final String title) throws Exception {
		super(new GridLayout(4, 1));
		this.listener = listener;
		setBorder(GUIUtils.getTitledBorder(title));
		GUIUtils.getTitledPanel("Edge");
		
		edge_size_panel = new SliderPanel("Edge size", 1, 16, true, false, null);
		edge_size_panel.setValue(4);
		add(edge_size_panel);

		edge_delta_panel = new SliderPanel("Edge delta", 1, 255, true, false, null);
		edge_delta_panel.setValue(110);
		add(edge_delta_panel);
		
		edge_mask = new JCheckBox("Remove background");
		add(edge_mask);
		
		JPanel button_panel = new JPanel(new FlowLayout());
		edge_test = new JButton("Test");
		edge_test.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				applyEdge(true);
			}});
		button_panel.add(edge_test);
		
		edge_apply = new JButton("Apply");
		edge_apply.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				applyEdge(false);
			}});
		button_panel.add(edge_apply);
		add(button_panel);
	}
	
	public void setEnabled(final boolean enabled) {
		edge_size_panel.setEnabled(enabled);
		edge_delta_panel.setEnabled(enabled);
		edge_mask.setEnabled(enabled);
		edge_apply.setEnabled(enabled);
		edge_test.setEnabled(enabled);
	}
	
	private void applyEdge(final boolean test) {
		try {
			Worker worker = new Worker(){
				@Override
				protected void process() throws Exception {
					EdgeFilter filter = new EdgeFilter(
							   RGBPixel.OPAQUE_BLACK, 
							   edge_size_panel.getValue(), 
							   edge_delta_panel.getValue(),
							   edge_mask.isSelected());
					listener.apply(filter, test);
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
}
