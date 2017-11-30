package kaflib.applications.mtg;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import kaflib.gui.AddRemoveList;
import kaflib.gui.AddRemoveListListener;
import kaflib.gui.FileSelectorComponent;
import kaflib.gui.FileSelectorListener;
import kaflib.gui.ImageComponent;
import kaflib.gui.Suggestor;
import kaflib.types.Matrix;
import kaflib.types.WordTree;
import kaflib.types.Worker;
import kaflib.utils.GUIUtils;

public class CardCenter extends JFrame implements FocusListener, 
										   ActionListener,
										   AddRemoveListListener<String>,
										   FileSelectorListener {

	private static final long serialVersionUID = 1L;
	private final File root;
	
	private final CollectorPanel scrape;
	private final JFrame self;
	private final JFrame scrape_frame;
	private final JPanel top_panel;
	private final JPanel search_panel;
	private final JPanel deck_panel;
	private final JPanel message_panel;
	private final JLabel label;
	private final JTextField field;

	// Search panel.
	private final WordTree tree;
	private final Suggestor suggestor;
	private final JCheckBox have;
	private final JButton save;
	
	// Deck panel.
	private final FileSelectorComponent card_list_file;
	private final AddRemoveList<String> card_list;
	
	// Image panel.
	private final ImageComponent image;

	private final CardDatabase db;
	private final BufferedImage none;
	private String current_name;
	
	public CardCenter(final File root) throws Exception {
		super("Query");
		this.root = root;
		self = this;
		db = new CardDatabase(this.root);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		top_panel = new JPanel(new BorderLayout());
		search_panel = new JPanel(new FlowLayout());
		deck_panel = new JPanel(new BorderLayout());
		message_panel = new JPanel(new BorderLayout());		
		label = new JLabel("Ready");
	
		JButton search = new JButton("Search:");
		search_panel.add(search);
		
		field = new JTextField(32);
		field.addFocusListener(this);
		tree = new WordTree(db.getNames());
		suggestor = new Suggestor(field, 
								  this, 
								  tree,
								  true);
		label.setText("Read " + db.getNames().size() + " cards.");
		message_panel.add(label);
		search_panel.add(field);

		// Search panel.
		have = new JCheckBox("Have");
		have.addActionListener(this);
		search_panel.add(have);
		save = new JButton("Save");
		save.addActionListener(this);
		search_panel.add(save);
		
		// Deck panel.
		card_list_file = new FileSelectorComponent(true, true, db.getDeckDirectory(), "xlsx");
		card_list_file.setListener(this);
		deck_panel.add(card_list_file, BorderLayout.NORTH);

		card_list = new AddRemoveList<String>(null, 12);
		card_list.setListener(this);
		deck_panel.add(card_list, BorderLayout.CENTER);
		
		// Image panel.
		image = new ImageComponent();
		current_name = null;
		none = db.getDefaultImage();
		
		top_panel.add(search_panel, BorderLayout.NORTH);
		top_panel.add(deck_panel, BorderLayout.CENTER);
		top_panel.add(image, BorderLayout.EAST);
		top_panel.add(message_panel, BorderLayout.SOUTH);
		setImage();
		
		setContentPane(top_panel);
		pack();
		setVisible(true);
		
		scrape = new CollectorPanel(db, tree);
		scrape_frame = new JFrame("Scraper");
		scrape_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		scrape_frame.setContentPane(scrape);
		scrape_frame.pack();
		scrape_frame.setVisible(true);

		
		write(true);
		startImageUpdater();

	}

	/**
	 * Set the image based on the current card name.  This should not be
	 * done on a UI thread.
	 * @throws Exception
	 */
	private void setImage() throws Exception {
		if (current_name != null) {
			File file = db.getRandomImage(current_name);
			if (file != null) {
				image.update(file);
				return;
			}
		}
		if (none != null) {
			image.update(none);				
		}
	}
	
	protected void startImageUpdater() {
		try {		
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					try {
						while (self != null) {
							setImage();
							Thread.sleep(5000);
						}
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(self, "Unable to update image.");
						e.printStackTrace();
					}
				}
			};
			worker.run();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(self, "Unable to update image.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Write the db, possibly to backup file name.
	 * @param backup
	 */
	public void write(final boolean backup) {
		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					try {
						if (backup) {
							db.writeBackup();
						}
						else {
							db.write();
						}
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(self, "Unable to write db file.");
						e.printStackTrace();
					}
				}
			};
			worker.run();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(self, "Unable to kick of async task.");
			e.printStackTrace();
		}
	}
		
	/**
	 * @return the tree
	 */
	public WordTree getTree() {
		return tree;
	}

	/**
	 * @return the suggestor
	 */
	public Suggestor getSuggestor() {
		return suggestor;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource().equals(have)) {
				db.setHave(field.getText(), have.isSelected());
			}
			else if (e.getSource().equals(save)) {
				label.setText("Saving...");
				Worker worker = new Worker() {
					@Override
					protected void process() throws Exception {
						try {
							// Write db.
							db.write();
							
							// Write deck.
							File deck_file = card_list_file.getSelected();
							if (deck_file != null) {
								Matrix.createMatrix(card_list.get()).toXLSX(deck_file);
							}
							
							label.setText("Saved.");
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(self, "Unable to save:\n" + e.getMessage());
							e.printStackTrace();
						}
					}
				};
				worker.start();
				
			}
			else {
				System.out.println("Unknown source:\n" + e);
			}
		}
		catch (Exception ex) {
			System.out.println("Unable to store value:\n" + ex.getMessage());
		}		
	}
	
	/**
	 * Updates gui based on a card being found.  Should not be run from the UI thread.
	 */
	private void cardFound() {
		try {
			have.setSelected(db.have(current_name));
			setImage();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Unable to update card to: " + current_name + ".");
			e.printStackTrace();
		}
	}

	public void textFocusEvent() {
		if (field.getText().isEmpty()) {
			label.setText("");
			have.setEnabled(false);
			return;
		}
		if (db.checkName(field.getText())) {
			current_name = field.getText();
			label.setText(current_name);
			have.setEnabled(true);
			cardFound();
		}
		else {
			label.setText("'" + field.getText() + "' not found.");
			have.setEnabled(false);
			current_name = null;
		}
		
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource().equals(field)) {
			textFocusEvent();
		}
		else {
			System.out.println("Unknown source:\n" + e);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource().equals(field)) {
			textFocusEvent();
		}
		else {
			System.out.println("Unknown source:\n" + e);
		}
	}
	
	public static void main(String args[]) {
		try {
			File root = null;
			if (CardDatabase.DEFAULT_ROOT.exists()) {
				root = CardDatabase.DEFAULT_ROOT;
			}
			else {
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					root = chooser.getSelectedFile();
				}
			}

			if (root == null) {
				return;
			}
			
			new CardCenter(root);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String addPressed() {
		return current_name;
	}

	/**
	 * Updates the card list to the specified file.  This should probably only
	 * be called from fileSelected() and on a non-ui thread.
	 * @param file
	 */
	private void updateCardList(final File file) {
		try {
			card_list.clear();
			if (!file.exists()) {
				return;
			}
			CardList list = CardList.getList(db, file);
			for (Card card : list) {
				card_list.add(card.getName());
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(self, "Unable to write db file.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void fileSelected(final File file) {
		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					updateCardList(file);
				}
			};
			worker.run();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(self, "Unable to kick of async task.");
			e.printStackTrace();
		}		
	}

	@Override
	public void createSelected() {
		card_list.clear();		
		String name = GUIUtils.showTextInputDialog(this, "Enter new deck name:");
		if (name == null || name.isEmpty()) {
			return;
		}
		if (!name.endsWith(".xlsx")) {
			name = name + ".xlsx";
		}
		card_list_file.add(name);
		card_list_file.setSelected(name);
	}

	@Override
	public void noneSelected() {
		card_list.clear();
	}

	@Override
	public void itemSelected(final String item) {
		field.setText(item);
		textFocusEvent();
	}

}
