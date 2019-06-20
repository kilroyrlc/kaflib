package kaflib.applications.mtg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import kaflib.graphics.GraphicsUtils;
import kaflib.gui.AddRemoveList;
import kaflib.gui.AddRemoveListListener;
import kaflib.gui.FileSelectorComponent;
import kaflib.gui.FileSelectorListener;
import kaflib.gui.RatingPanel;
import kaflib.gui.Suggestor;
import kaflib.gui.components.DownscaledImageComponent;
import kaflib.types.DemandWorker;
import kaflib.types.Matrix;
import kaflib.types.WordTrie;
import kaflib.types.Worker;
import kaflib.utils.GUIUtils;
import kaflib.utils.StringUtils;

public class CardCenter extends JFrame implements FocusListener, 
										   ActionListener,
										   AddRemoveListListener<String>,
										   FileSelectorListener {

	private static final long serialVersionUID = 1L;
	private final File root;
	private final CardDatabase db;
	
	private final CollectorPanel scrape;
	private final JFrame self;
	private final JFrame scrape_frame;
	private final JPanel outer_panel;
	private final JPanel card_panel;
	private final JPanel card_list_panel;
	private final JPanel message_panel;

	private final DemandWorker updater;
	
	// Status panel.
	private final JLabel status;
	private final JButton save;
	
	// Deck panel.
	private final FileSelectorComponent card_list_file;
	private final AddRemoveList<String> card_list;
	private final JButton generate_html;
	private final JButton generate_text;
	private final JButton remove_duplicates;
	private final JLabel deck_info;
	
	// Card panel.
	private final JTextField search;
	private final WordTrie tree;
	private final Suggestor suggestor;
	private final JCheckBox have;
	private final RatingPanel community_rating;

	private final DownscaledImageComponent image;
	private final BufferedImage none;
	private String current_name;
	
	public CardCenter(final File root) throws Exception {
		super("Query");
		this.root = root;
		self = this;
		db = new CardDatabase(this.root);
		current_name = null;
		none = db.getDefaultImage();

		JPanel temp;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		outer_panel = new JPanel(new BorderLayout());
		message_panel = new JPanel(new BorderLayout());		

		//
		// Right panel - card list.
		//
		card_list_panel = GUIUtils.getTitledPanel("Card list");
		card_list_panel.setLayout(new BorderLayout());

		card_list_file = new FileSelectorComponent(true, true, db.getDeckDirectory(), "xlsx");
		card_list_file.setListener(this);
		card_list = new AddRemoveList<String>(null, 12);
		card_list.setListener(this);
		
		JPanel list_bottom = new JPanel(new GridLayout(2, 1));
		
		
		JPanel list_buttons = new JPanel(new FlowLayout());
		list_buttons.setBorder(new EmptyBorder(4, 4, 4, 4));
		remove_duplicates = new JButton("Remove duplicates");
		remove_duplicates.addActionListener(this);
		remove_duplicates.setHorizontalAlignment(SwingConstants.RIGHT);
		list_buttons.add(remove_duplicates);
		generate_html = new JButton("HTML...");
		generate_html.addActionListener(this);
		generate_html.setHorizontalAlignment(SwingConstants.RIGHT);
		list_buttons.add(generate_html);
		generate_text = new JButton("Text...");
		generate_text.addActionListener(this);
		generate_text.setHorizontalAlignment(SwingConstants.RIGHT);
		list_buttons.add(generate_text);
		
		deck_info = new JLabel();
		list_bottom.add(deck_info);
		list_bottom.add(list_buttons);
		
		card_list_panel.add(card_list, BorderLayout.CENTER);
		card_list_panel.add(card_list_file, BorderLayout.NORTH);
		card_list_panel.add(list_bottom, BorderLayout.SOUTH);
		
		//
		// Center panel - card and info.
		//
		search = new JTextField(32);
		search.addFocusListener(this);
		tree = new WordTrie(db.getNames());
		suggestor = new Suggestor(search, 
								  this, 
								  tree,
								  true);

		card_panel = GUIUtils.getTitledPanel("Card");
		card_panel.setLayout(new BorderLayout());
		card_panel.add(search, BorderLayout.NORTH);
		
		temp = new JPanel(new BorderLayout());
		have = new JCheckBox("Have");
		have.addActionListener(this);
		temp.add(have, BorderLayout.NORTH);
		//have.setVerticalAlignment(SwingConstants.TOP);
		
		community_rating = new RatingPanel("Community rating:", 
					    				   10,
					    				   true);
		temp.add(community_rating);
		
		image = new DownscaledImageComponent();
		
		card_panel.add(image, BorderLayout.WEST);
		card_panel.add(temp, BorderLayout.CENTER);
		
		
		//
		// Bottom panel - status message and save all.
		//
		status = new JLabel("Ready");
		status.setForeground(Color.BLUE.darker());
		status.setText("Read " + db.getNames().size() + " cards, " + db.getHaveCount() + " distinct owned.");
		save = new JButton("Save all");
		save.addActionListener(this);
		message_panel.setBorder(new EmptyBorder(6, 6, 6, 6));
		message_panel.add(status, BorderLayout.CENTER);
		message_panel.add(save, BorderLayout.EAST);

		
		outer_panel.add(card_list_panel, BorderLayout.EAST);
		outer_panel.add(card_panel, BorderLayout.CENTER);
		outer_panel.add(message_panel, BorderLayout.SOUTH);
		setImage();
		
		setContentPane(outer_panel);
		pack();
		setVisible(true);
		
		scrape = new CollectorPanel(db, tree);
		scrape_frame = new JFrame("Scraper");
		scrape_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		scrape_frame.setContentPane(scrape);
		scrape_frame.pack();
		scrape_frame.setVisible(true);

		write(true);
		
		// Kick off a thread to update the GUI as selections are made.
		updater = new DemandWorker(100, 5000) {
			@Override
			public void processLoop() {
				updateGUI();
			}
		};
	}
	
	/**
	 * Set the image based on the current card name.  This should not be
	 * done on a UI thread.
	 * @throws Exception
	 */
	private void setImage() throws Exception {
		if (current_name != null) {
			File file = db.getRandomImage(current_name);
			BufferedImage i = GraphicsUtils.read(file);
			if (i.getWidth() == none.getWidth() &&
				i.getHeight() == none.getWidth()) {
				i = GraphicsUtils.rotate(i, GraphicsUtils.Rotation.CLOCKWISE);
			}
			if (file != null) {
				image.update(i);
				return;
			}
		}
		if (none != null) {
			image.update(none);				
		}
	}
	
	/**
	 * This function is called by the updater frequently after a kick() call.
	 * After a short time with no kicks, the thread finishes.  So:
	 * User input -> kick (update regularly)
	 * No user input -> wait a bit longer and then stop
	 */
	protected void updateGUI() {
		try {
			updateDeckInfo();
			
			// Text field is empty.
			if (search.getText().isEmpty()) {
				current_name = null;
				status.setText("");
				have.setEnabled(false);
				return;
			}
			// Text field contents found in db.  Update card image,
			// have field, etc.
			if (db.checkName(search.getText())) {
				// If the card hasn't changed, do nothing.
				if (current_name != null && 
					search.getText() != null &&
					current_name.equals(search.getText())) {
					return;
				}
				
				current_name = search.getText();
				status.setText("Card: " + current_name + ", have: " + db.have(current_name) + ".");
				have.setEnabled(true);
				have.setSelected(db.have(current_name));
				
				Float rating = db.getRating(current_name);
				if (rating == null) {
					community_rating.setValue(0);
					community_rating.setEnabled(false);
				}
				else {		
					community_rating.setValue((int)(rating * 2) - 1);
					community_rating.setEnabled(true);
				}
				setImage();
				search.setSelectionStart(0);
				search.setSelectionEnd(search.getText().length());
			}
			// Text field contents not in db.
			else {
				status.setText("'" + search.getText() + "' not found.");
				have.setSelected(false);
				have.setEnabled(false);
				current_name = null;
			}		

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
	public WordTrie getTree() {
		return tree;
	}

	/**
	 * @return the suggestor
	 */
	public Suggestor getSuggestor() {
		return suggestor;
	}

	/**
	 * Writes an html file for the current list.
	 * @param names
	 * @param listName
	 */
	private void asyncGenerateText(final List<String> names, final String listName) {
		final File directory = GUIUtils.chooseDirectory(this, db.getDeckDirectory());
		if (directory == null) {
			return;
		}

		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					try {
						CardUtils.generateText(db, directory, listName, names);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(self, "Unable to generate html:\n" + e.getMessage());
						e.printStackTrace();
					}
				}
			};
			worker.start();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(self, "Unable to spawn thread:\n" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes an html file for the current list.
	 * @param names
	 * @param listName
	 */
	private void asyncGenerateHTML(final List<String> names, final String listName) {
		final File directory = GUIUtils.chooseDirectory(this, db.getDeckDirectory());
		if (directory == null) {
			return;
		}

		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					try {
						CardUtils.generateHTML(db, directory, listName, names);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(self, "Unable to generate html:\n" + e.getMessage());
						e.printStackTrace();
					}
				}
			};
			worker.start();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(self, "Unable to spawn thread:\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// Have toggled.
			if (e.getSource().equals(have)) {
				db.setHave(search.getText(), have.isSelected());
			}
			// Save pressed.
			else if (e.getSource().equals(save)) {
				status.setText("Saving...");
				Worker worker = new Worker() {
					@Override
					protected void process() throws Exception {
						try {
							// Write db.
							db.write();
							
							// Write deck.
							File deck_file = card_list_file.getSelected();
							if (deck_file != null && (!deck_file.exists() || deck_file.canWrite())) {
								Matrix.createMatrix(card_list.get()).toXLSX(deck_file);
							}
							
							card_list_file.refresh();
							status.setText("Saved.");
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(self, "Unable to save:\n" + e.getMessage());
							e.printStackTrace();
						}
					}
				};
				worker.start();
			}
			// Remove duplicates pressed.
			else if (e.getSource().equals(remove_duplicates)) {
				card_list.removeDuplicates();
			}
			// Generate text file pressed.
			else if (e.getSource().equals(generate_text)) {
				asyncGenerateText(card_list.get(), 
						StringUtils.truncateAt(card_list_file.getSelected().getName(), ".xlsx"));
			}
			// Generate html file pressed.
			else if (e.getSource().equals(generate_html)) {
				asyncGenerateHTML(card_list.get(), 
						StringUtils.truncateAt(card_list_file.getSelected().getName(), ".xlsx"));
			}
			else {
				System.out.println("Unknown source:\n" + e);
			}
		}
		catch (Exception ex) {
			System.out.println("Unable to store value:\n" + ex.getMessage());
		}		
	}
	
	public void textFocusEvent() {
		kickUpdater();
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		if (e.getSource().equals(search)) {
			textFocusEvent();
		}
		else {
			System.out.println("Unknown source:\n" + e);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (e.getSource().equals(search)) {
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
		kickUpdater();
		search.setText("");
		return current_name;
	}

	@Override
	public String removePressed(String item) {
		kickUpdater();
		return null;
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
			CardList list = CardList.readXLSX(db, file);
			for (Card card : list) {
				card_list.add(card.getName());
			}
			card_list.setOperationsEnabled(!list.isReadOnly());
			remove_duplicates.setEnabled(!list.isReadOnly());
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
		card_list_file.setEnabled(false);
		
		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					card_list.clear();		
					String name = GUIUtils.showTextInputDialog(self, "Enter new deck name:");
					if (name != null && !name.isEmpty()) {
						if (!name.endsWith(".xlsx")) {
							name = name + ".xlsx";
						}
						File file = new File(db.getDeckDirectory(), name);
						if (!file.exists()) {
							Matrix.toEmptyXLSX(file);
							card_list_file.refresh();
							card_list_file.setSelected(name);	
						}
						else {
							JOptionPane.showMessageDialog(self, "Deck already exists: " + file + ".");							
						}
					}
					else {
						JOptionPane.showMessageDialog(self, "Invalid deck name.");
					}
					card_list_file.setEnabled(true);
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
	public void noneSelected() {
		card_list.clear();
	}

	private void kickUpdater() {
		try {
			if (updater != null) {
				updater.kick();
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "GUI update failed.");
			e.printStackTrace();
		}
	}
	
	private void updateDeckInfo() throws Exception {
		List<String> list = card_list.get();
		if (list == null || list.size() == 0) {
			deck_info.setText("");
			return;
		}
		CardList cards = CardList.create(list, db);
		deck_info.setText(cards.size() + " cards, " + cards.getLandCount() + " lands.");
	}
	
	@Override
	public void itemSelected(final String item) {
		search.setText(item);
		kickUpdater();
	}


}
