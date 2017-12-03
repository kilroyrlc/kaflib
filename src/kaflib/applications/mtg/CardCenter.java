package kaflib.applications.mtg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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

import kaflib.gui.AddRemoveList;
import kaflib.gui.AddRemoveListListener;
import kaflib.gui.FileSelectorComponent;
import kaflib.gui.FileSelectorListener;
import kaflib.gui.ImageComponent;
import kaflib.gui.RatingPanel;
import kaflib.gui.Suggestor;
import kaflib.types.Matrix;
import kaflib.types.WordTree;
import kaflib.types.Worker;
import kaflib.utils.FileUtils;
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

	// Status panel.
	private final JLabel status;
	private final JButton save;
	
	// Deck panel.
	private final FileSelectorComponent card_list_file;
	private final AddRemoveList<String> card_list;
	private final JButton generate_html;
	private final JButton remove_duplicates;
	
	// Card panel.
	private final JTextField search;
	private final WordTree tree;
	private final Suggestor suggestor;
	private final JCheckBox have;
	private final RatingPanel community_rating;

	private final ImageComponent image;
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
		
		temp = new JPanel(new FlowLayout());
		temp.setBorder(new EmptyBorder(4, 4, 4, 4));
		remove_duplicates = new JButton("Remove duplicates");
		remove_duplicates.addActionListener(this);
		remove_duplicates.setHorizontalAlignment(SwingConstants.RIGHT);
		temp.add(remove_duplicates);
		generate_html = new JButton("HTML...");
		generate_html.addActionListener(this);
		generate_html.setHorizontalAlignment(SwingConstants.RIGHT);
		temp.add(generate_html);
		
		card_list_panel.add(card_list, BorderLayout.CENTER);
		card_list_panel.add(card_list_file, BorderLayout.NORTH);
		card_list_panel.add(temp, BorderLayout.SOUTH);
		
		//
		// Center panel - card and info.
		//
		search = new JTextField(32);
		search.addFocusListener(this);
		tree = new WordTree(db.getNames());
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
		
		image = new ImageComponent();
		
		card_panel.add(image, BorderLayout.WEST);
		card_panel.add(temp, BorderLayout.CENTER);
		
		
		//
		// Bottom panel - status message and save all.
		//
		status = new JLabel("Ready");
		status.setForeground(Color.BLUE.darker());
		status.setText("Read " + db.getNames().size() + " cards.");
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
	
	private void generateHTML(final File directory, final String name, final List<String> names) throws Exception {
		CardList list = CardList.getList(db, names);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>\n<body>\n<table>\n   <tr>\n");
		int column = 0;
		for (Card card : list) {
			File image = db.getImage(card.getName());
			FileUtils.copy(new File(directory, image.getName()), image);
			buffer.append("      <td><img src=\"" + 
						  image.getName() + 
						  "\" alt=\"" + 
						  card.getName() + 
						  "\" border=\"0\" /></td>\n");
			column++;
			if (column == 4) {
				column = 0;
				buffer.append("   </tr>\n   <tr>\n");
			}
		}
		buffer.append("   </tr>\n</table>\n</body>\n</html>\n");
		FileUtils.write(new File(directory, name + ".html"), new String(buffer));
	}
	
	private void asyncGenerateHTML(final List<String> names, final String listName) {
		final File directory = GUIUtils.chooseDirectory(this);
		if (directory == null) {
			return;
		}

		try {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					try {
						generateHTML(directory, listName, names);
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
			if (e.getSource().equals(have)) {
				db.setHave(search.getText(), have.isSelected());
			}
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
							if (deck_file != null && deck_file.canWrite()) {
								Matrix.createMatrix(card_list.get()).toXLSX(deck_file);
							}
							
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
			else if (e.getSource().equals(remove_duplicates)) {
				card_list.removeDuplicates();
			}
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
	
	/**
	 * Updates gui based on a card being found.  Should not be run from the UI thread.
	 */
	private void cardFound() {
		try {
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
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Unable to update card to: " + current_name + ".");
			e.printStackTrace();
		}
	}

	public void textFocusEvent() {
		if (search.getText().isEmpty()) {
			status.setText("");
			have.setEnabled(false);
			return;
		}
		if (db.checkName(search.getText())) {
			current_name = search.getText();
			status.setText(current_name);
			have.setEnabled(true);
			cardFound();
		}
		else {
			status.setText("'" + search.getText() + "' not found.");
			have.setEnabled(false);
			current_name = null;
		}
		
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
			card_list.setOperationsEnabled(!list.isReadOnly());
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
		search.setText(item);
		textFocusEvent();
	}

}
