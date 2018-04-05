package kaflib.applications.mtg;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kaflib.graphics.GraphicsUtils;
import kaflib.gui.ImageComponent;
import kaflib.gui.ProgressLabel;
import kaflib.types.WordTrie;
import kaflib.types.Worker;
import kaflib.utils.FileUtils;
import kaflib.utils.GUIUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a web collector with a coupled UI (I know, right?).
 */
public class CollectorPanel extends JPanel implements KeyListener, ActionListener {
	private static final long serialVersionUID = 1L;
	private final WordTrie tree;
	
	private final JPanel self;
	private final JFormattedTextField count;
	private final JFormattedTextField min;
	private final JFormattedTextField max;
	private final JFormattedTextField value;
	private final JCheckBox tor_only;
	private final JButton file_browse;
	private final JButton start;
	private final ProgressLabel progress;
	private final JPanel center;
	
	private int max_valid;
	
	private static final String CSS_NAME_START = "#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
	private static final String CSS_NAME_END = "nameRow .value";
	private static final String CSS_MANA_START = "#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
	private static final String CSS_MANA_END = "manaRow .value";
	private static final String CSS_CMC_START = "#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
	private static final String CSS_CMC_END = "cmcRow .value";
	private static final String CSS_TYPES_START = "#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
	private static final String CSS_TYPES_END = "typeRow .value";
	private static final String CSS_TEXT_START = "#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
	private static final String CSS_TEXT_END = "textRow .value .cardtextbox";
	private static final String CSS_PT_START = "#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_";
	private static final String CSS_PT_END = "ptRow .value";
	
	private Boolean tor_available;
	private final BufferedImage no_image;
	private final ImageComponent image;
	private final CardDatabase db;
	
	public CollectorPanel(final CardDatabase db, final WordTrie tree) throws Exception {	
		super(new FlowLayout());
		this.db = db;
		tor_available = null;
		self = this;
		this.tree = tree;
		
		max_valid = db.getMaxValid();
		
		NumberFormat format = NumberFormat.getInstance();
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format);
		
		count = new JFormattedTextField(formatter);
		count.setColumns(6);
		count.addKeyListener(this);
		min = new JFormattedTextField(formatter);
		min.setColumns(6);
		min.setValue(1);
		min.addKeyListener(this);
		max = new JFormattedTextField(formatter);
		max.setColumns(6);
		max.setValue(max_valid);
		max.addKeyListener(this);
		value = new JFormattedTextField(formatter);
		value.setColumns(6);
		value.addKeyListener(this);
		tor_only = new JCheckBox("Tor only");
		tor_only.setSelected(true);
		start = new JButton("Start");
		start.addActionListener(this);
		progress = new ProgressLabel(40);
		progress.setText("Scraper ready.");
		
		no_image = db.getDefaultImage();
		
		center = new JPanel(new BorderLayout());
		
		JPanel panel = GUIUtils.getTitledPanel("Random collect");
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Count:"));
		panel.add(count);
		panel.add(new JLabel("from:"));
		panel.add(min);
		panel.add(new JLabel("to:"));
		panel.add(max);
		center.add(panel, BorderLayout.NORTH);

		panel = GUIUtils.getTitledPanel("List");
		panel.setLayout(new BorderLayout());
		file_browse = new JButton("Browse...");
		file_browse.addActionListener(this);
		panel.add(file_browse, BorderLayout.WEST);
		center.add(panel, BorderLayout.CENTER);
		
		panel = GUIUtils.getTitledPanel("One-off");
		panel.setLayout(new BorderLayout());
		panel.setAlignmentX(LEFT_ALIGNMENT);
		JLabel label = new JLabel("Card ID:");
		panel.add(label, BorderLayout.WEST);
		panel.add(value, BorderLayout.CENTER);
		
		center.add(panel, BorderLayout.SOUTH);

		panel = new JPanel(new BorderLayout());
		panel.add(progress, BorderLayout.CENTER);
		panel.add(start, BorderLayout.EAST);
		
		image = new ImageComponent();
		image.update(no_image);
		
		JPanel left_panel = new JPanel();
		left_panel.setLayout(new BorderLayout());
		
		left_panel.add(tor_only, BorderLayout.NORTH);
		left_panel.add(center, BorderLayout.CENTER);
		left_panel.add(panel, BorderLayout.SOUTH);
		add(left_panel);
		add(image, BorderLayout.EAST);

	}
	
	private static int randomNeighbor(final int seed, 
									  final int min, 
									  final int max) throws Exception {
		int neighbor = seed;
		if (RandomUtils.randomBoolean()) {
			neighbor += RandomUtils.randomInt(1, 25);
		}
		else {
			neighbor -= RandomUtils.randomInt(1, 25);
		}
		neighbor = Math.max(min, neighbor);
		neighbor = Math.min(max, neighbor);
		return neighbor;
	}
	
	/*
	 private Set<Integer> generateHaveTargets(final int count) throws Exception {
		Set<Integer> values = new HashSet<Integer>();
		for (String name : db.getRandomHaveNames(2 * count)) {
			for (Integer id : db.getIDs(name)) {
				if (!db.fullyPopulated(id)) {
					values.add(id);
				}
				if (values.size() >= count) {
					return values;
				}
			}
		}
		
		return values;
	}
	*/
	/**
	 * Generates collect targets.
	 * @param count
	 * @param min
	 * @param max
	 * @return
	 * @throws Exception
	 */
	private Set<Integer> generateTargets(final int count, 
										 final int min, 
										 final int max) throws Exception {
		Set<Integer> values = new HashSet<Integer>();
 		for (int i = 0; i < 2 * count; i++) {
 			Set<Integer> temp = new HashSet<Integer>();
 			// Add three neighbors to a random valid id.
			Integer id = db.getRandomID(true);
			if (id != null) {
				temp.add(randomNeighbor(id, min, max));
				temp.add(randomNeighbor(id, min, max));
				temp.add(randomNeighbor(id, min, max));
			}
 			// Add three neighbors to a random card I have.
			id = db.getRandomHaveID();
			if (id != null) {
				temp.add(randomNeighbor(id, min, max));
				temp.add(randomNeighbor(id, min, max));
				temp.add(randomNeighbor(id, min, max));
			}
		
			temp.add(RandomUtils.randomInt(min, max));
			temp.add(RandomUtils.randomInt(min, max));
			temp.add(RandomUtils.randomInt(min, max));

			// Add all temp ids not already in the db.
			for (Integer value : temp) {
				if (!db.fullyPopulated(value)) {
					values.add(value);
				}
				if (values.size() >= count) {
			 		return values;
				}
			}
		}
 		return values;
	}
	

	private void scrape() throws Exception {
		scrape(null);
	}
	
	/**
	 * Perform scrape based on inputs.  Do not call from ui thread.
	 * @param count
	 * @throws Exception
	 */
	private void scrape(final File file) throws Exception {
		Collection<Integer> values;
		if (file != null) {
			values = new HashSet<Integer>();
			for (String line : FileUtils.readLines(file)) {
				if (StringUtils.isInt(line.trim())) { 
					values.add(Integer.valueOf(line.trim()));
				}
			}
		}
		else if (!value.getText().isEmpty()) {
			values = new ArrayList<Integer>();
			values.add(Integer.valueOf(value.getText()));
			value.setText("");
			db.removeInvalid(value.getText());
		}
		else if (!count.getText().isEmpty() &&
				 !min.getText().isEmpty() &&
				 !max.getText().isEmpty()) {
//			values = generateHaveTargets(Integer.valueOf(count.getText()));

			values = generateTargets(Integer.valueOf(count.getText()), 
								  Integer.valueOf(min.getText()), 
								  Integer.valueOf(max.getText()));
		}
		else {
			JOptionPane.showMessageDialog(this, "Must specify value or count/min/max.");
			return;
		}

		try {
			progress.register(this, values.size());
			for (Integer id : values) {
				if (torAvailable()) {
					progress.setText("Scraping: " + id + " using tor.");
				}
				else {
					progress.setText("Scraping: " + id + " using your ip.");
				}
				
				scrape(id);
				if (values.size() > 3) {
					progress.setText(progress.getText() + " (sleeping)");
					Thread.sleep(RandomUtils.randomInt(500, 10000));
				}
				progress.increment(this);
			}
	
			progress.setText("Scrape finished, save recommended.");
			progress.release(this);
		}
		catch (Exception e) {
			progress.release(this);
			throw e;
		}
	}
	
	
	private String replaceColors(final String html) {
		String temp = html.replaceAll("\\<img[^\\>]+alt=\"", "[");
		return temp.replaceAll("\"\\s+align.*\\>", "]");
	}
	
	
	
	private Set<CardInstance> parseCards(final String html, final int id) throws Exception {
		Document document = Jsoup.parse(html);
		Set<CardInstance> set = new HashSet<CardInstance>();
		
		Elements elements = document.select(".cardDetails");
		if (elements.size() == 1) {
			set.add(parse(elements.first(), id, ""));			
		}
		else if (elements.size() == 2) { 
			set.add(parse(elements.get(0), id, "ctl02_"));	
			set.add(parse(elements.get(1), id, "ctl03_"));	
		}
		else {
		}
		
		return set;
	}
		


	/**
	 * Search for id matches in the document.
	 * @param document
	 * @return
	 * @throws Exception
	 */
	private Integer parseID(final Element document) throws Exception {
		Pattern pattern = Pattern.compile("^(\\d+)\\D.*", Pattern.MULTILINE);
		String chunks[] = document.outerHtml().split("multiverseid\\=");
		for (int i = 1; i < chunks.length; i++) {
			String chunk = StringUtils.truncateIf(chunks[i], 16);
			Matcher matcher = pattern.matcher(chunk);
			if (matcher.matches()) {
				return Integer.valueOf(matcher.group(1));
			}			
		}
		return null;
	}
	
	
	private CardInstance parse(final Element document, final int id, final String substr) throws Exception {
		Elements elements;
		String name = "";
		String cost = "";
		String cmc = "";
		String types = "";
		String text = "";
		String pt = "";
		try {
			Integer found_id = parseID(document);
			if (found_id == null) {
				found_id = id;
			}
			
			elements = document.select(CSS_NAME_START + substr + CSS_NAME_END);
			if (elements == null || elements.size() != 1) {
				System.err.println(CSS_NAME_START + substr + CSS_NAME_END);
				if (elements != null) {
					System.err.println("Matches " + elements.size() + " elements.");
				}
				throw new Exception("Could not parse name.");
			}
			if (elements.size() == 1) {
				name = elements.get(0).text();
			}
			

			elements = document.select(CSS_MANA_START + substr + CSS_MANA_END);
			if (elements == null) {
				throw new Exception("Could not parse cost.");
			}
			for (Element element : elements) {
				cost = cost + replaceColors(element.html()) + "\n";
			}

			elements = document.select(CSS_CMC_START + substr + CSS_CMC_END);
			if (elements.size() > 1) {
				throw new Exception("Multiple cmc: " + found_id + ".");
			}
			if (elements.size() == 1) {
				cmc = elements.get(0).text();
			}
			

			elements = document.select(CSS_TYPES_START + substr + CSS_TYPES_END);
			if (elements == null) {
				throw new Exception("Could not parse types.");
			}
			for (Element element : elements) {
				types = types + element.text() + "\n";
			}
			

			elements = document.select(CSS_TEXT_START + substr + CSS_TEXT_END);
			if (elements == null) {
				throw new Exception("Could not parse text.");
			}
			for (Element element : elements) {
				text = text + replaceColors(element.html()) + "\n";
				text = text.replace("<i>", "");
				text = text.replace("</i>", "");
			}
			

			elements = document.select(CSS_PT_START + substr + CSS_PT_END);
			if (elements.size() > 1) {
				throw new Exception("Multiple p/t: " + found_id + ".");
			}
			if (elements.size() == 1) {
				pt = elements.get(0).text();
			}
			
			CardInstance card = new CardInstance(
								found_id,
								name,
								cost,
								cmc,
								types,
								text,
								pt);
	
			
			float rating = 0;
			int votes = 0;
			
			elements = document.select(".textRatingValue");
			if (elements != null) {
				if (elements.size() > 1) {
					throw new Exception("Multiple rating tags: " + found_id + ".");
				}
				if (elements.size() == 1) {
					rating = Float.valueOf(elements.get(0).text());
				}
			}
	
			elements = document.select(".totalVotesValue");
			if (elements != null) {
				if (elements.size() > 1) {
					throw new Exception("Multiple votes tags: " + found_id + ".");
				}
				if (elements.size() == 1) {
					votes = Integer.valueOf(elements.get(0).text());
				}
			}
	
			if (votes >= 15) {
				card.setCommunityRating(rating);
				card.setCommunityVotes(votes);
			}
			return card;
		}
		catch (Exception e) {
			System.err.println("Error parsing " + id + ".");
			System.err.println("Html: " + document.outerHtml());
			throw e;
		}
		
	}
	
	private boolean torAvailable() {
		if (tor_available != null) {
			return tor_available;
		}
		
		try {
			FileUtils.torGet(new URL("http://gatherer.wizards.com/Pages/Card/Languages.aspx?printed=false&multiverseid=" + RandomUtils.randomInt(1, 500)));
			tor_available = true;
		}
		catch (Exception e) {
			tor_available = false;
		}
		return tor_available;
	}
	
	private Boolean isEnglish(final String html) throws Exception {	
		Document document = Jsoup.parse(html);
		Elements elements;
		elements = document.select(".cardList td");
		if (elements.size() < 1) {
			return null;
		}
		
		for (Element element : elements) {
			if (element.text().equals("English")) {
				return false;
			}
		}
		return true;
	}
	
	private String get(final URL url) throws Exception {
		if (torAvailable()) {
			return FileUtils.torGet(url);
		}
		else if (!tor_only.isSelected()) {
			return FileUtils.read(url);
		}
		else {
			throw new Exception("Tor required but unavailable.  Check that it is installed/running.");
		}
	}

	private void get(final File file, final URL url) throws Exception {
		if (torAvailable()) {
			FileUtils.torDownload(file, url);
		}
		else if (!tor_only.isSelected()) {
			FileUtils.download(file, url);
		}
		else {
			throw new Exception("Tor required but unavailable.  Check that it is installed/running.");
		}
	}
	
	private void scrape(final int id) throws Exception {
		Boolean english = isEnglish(get(new URL("http://gatherer.wizards.com/Pages/Card/Languages.aspx?printed=false&multiverseid=" + id)));
		
		if (english == null) {
			db.addInvalid(id);
		}
		else if (english == false) {
			db.addForeign(id);
		}
		else {
			Set<CardInstance> cards = parseCards(get(new URL("http://gatherer.wizards.com/Pages/Card/Details.aspx?printed=false&multiverseid=" + id)), id);
			
			if (cards != null) {
				for (CardInstance card : cards) {
					if (card != null) {
						db.add(card);
						tree.insert(card.getName());
						File image_file = new File(db.getImageDirectory(), card.getIDString() + ".png");
						get(image_file, new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + card.getIDString() + "&type=card"));
						BufferedImage image = GraphicsUtils.read(image_file);
						if (GraphicsUtils.equal(no_image, image)) {
							image_file.delete();
						}
						else {
							this.image.update(image);
						}
					}
				}
			}
			else {
				db.addInvalid(id);
			}
		}
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource().equals(start)) {
				start.setEnabled(false);
				Worker worker = new Worker() {
					@Override
					protected void process() throws Exception {
						try {
							scrape();
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(self, "Scrape error:\n" + e.getMessage());
						}
						finally {
							start.setEnabled(true);
						}
					}
					
				};
				worker.start();
			}
			else if (e.getSource().equals(file_browse)) {
				final File file = GUIUtils.chooseFile(this, db.getImageDirectory().getParentFile());
				if (file == null) {
					return;
				}
				start.setEnabled(false);
				Worker worker = new Worker() {
					@Override
					protected void process() throws Exception {
						try {
							scrape(file);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(self, "Scrape error:\n" + e.getMessage());
						}
						finally {
							start.setEnabled(true);
						}
					}
					
				};
				worker.start();
			}
			else {
				
			}
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(self, "Could not handle action.");
			ex.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		Object source = e.getSource();
		if (source.equals(count) || source.equals(min) || source.equals(max)) {
			value.setText("");
		}
		else {
			count.setText("");
			min.setText("1");
			max.setText("" + max_valid);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}


