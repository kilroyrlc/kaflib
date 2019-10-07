package kaflib.gui.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Defines a custom-drawn graphics frame.  The main thing here is that there
 * is frame-wide mouse focus.  Everything is painted by hand though.
 */
public abstract class KGraphicsFrame extends JFrame implements MouseListener, 
															   MouseMotionListener,
															   KeyListener {

	private static final long serialVersionUID = -555865476626050289L;
	private final Content content;
	
	public KGraphicsFrame() throws Exception {
		super();
		content = new Content();
		setContentPane(content);
		setGlassPane(new GlassPane());
		getGlassPane().addMouseListener(this);
		getGlassPane().addMouseMotionListener(this);
		getGlassPane().addKeyListener(this);
	}
	
	protected abstract void paintFrame(Graphics g);
	public abstract Dimension size();
	
	public Dimension getPreferredSize() {
		return size();
	}
		
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	
	class Content extends JPanel {
		private static final long serialVersionUID = 1L;

		public Content() {
		}
		
		public Dimension getPreferredSize() {
			return getSize();
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			paintFrame(g);
		}
	}
	
	class GlassPane extends Container {

		private static final long serialVersionUID = 3757609564457263713L;

		public GlassPane() {
			
		}
		
	}	
	
	public static void main(String args[]) {
		try {
			KGraphicsFrame frame = new KGraphicsFrame(){

				private static final long serialVersionUID = 719184928201975553L;

				@Override
				protected void paintFrame(Graphics g) {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, 800, 600);
					g.setColor(Color.ORANGE);
					g.fillOval(30, 30, 10, 20);
				}

				@Override
				public Dimension size() {
					return new Dimension(800, 600);
				}};
				
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
			frame.getContentPane().invalidate();
			frame.getContentPane().revalidate();			
			frame.getContentPane().repaint();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

