package kaflib.applications.mtg;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import kaflib.gui.ProgressLabel;
import kaflib.types.WordTree;
import kaflib.types.Worker;
import kaflib.utils.FileUtils;
import kaflib.utils.RandomUtils;

/**
 * Defines a web collector with a coupled UI (I know, right?).
 */
public class CollectorPanel extends JPanel implements KeyListener, ActionListener {
	private static final long serialVersionUID = 1L;
	private final WordTree tree;
	
	private final JPanel self;
	private final JFormattedTextField count;
	private final JFormattedTextField min;
	private final JFormattedTextField max;
	private final JFormattedTextField value;
	private final JCheckBox tor_only;
	private final JButton start;
	private final ProgressLabel progress;
	private final JPanel center;
	
	private Boolean tor_available;
	public static final int MAX_ID = 438850;
	private final BufferedImage no_image;
	private final CardDatabase db;
	
	public CollectorPanel(final CardDatabase db, final WordTree tree) throws Exception {	
		super(new BorderLayout());
		this.db = db;
		tor_available = null;
		self = this;
		this.tree = tree;
		
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
		max.setValue(MAX_ID);
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
		
		center = new JPanel(new GridLayout(2, 3));
		JPanel panel = new JPanel(new FlowLayout());

		panel.add(new JLabel("Count:"));
		panel.add(count);
		center.add(panel);
		panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("from:"));
		panel.add(min);
		center.add(panel);
		panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("to:"));
		panel.add(max);
		center.add(panel);

		panel = new JPanel(new FlowLayout());
		panel.add(new JLabel("Card ID:"));
		panel.add(value);
		center.add(panel);
		
		panel = new JPanel(new FlowLayout());
		center.add(panel);
		center.add(start);
		
		add(tor_only, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(progress, BorderLayout.SOUTH);
	}
	
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
			int id = Integer.valueOf(db.getRandomID(true));
			
			int next = id + RandomUtils.randomInt(1, 25);
			if (next >= min && next <= max && !db.contains("" + next)) {
				values.add(next);
			}
			next = id - RandomUtils.randomInt(1, 25);
			if (next >= min && next <= max && !db.contains("" + next)) {
				values.add(next);
			}
		
			next = RandomUtils.randomInt(min, max);
			if (!db.contains("" + next)) {
				values.add(next);
			}
			
			if (values.size() >= count) {
				break;
			}
		}
 		return values;
	}
	
	/**
	 * Perform scrape based on inputs.  Do not call from ui thread.
	 * @param count
	 * @throws Exception
	 */
	private void scrape() throws Exception {
		Collection<Integer> values;
		if (!value.getText().isEmpty()) {
			values = new ArrayList<Integer>();
			values.add(Integer.valueOf(value.getText()));
			value.setText("");
			db.removeInvalid(value.getText());
		}
		else if (!count.getText().isEmpty() &&
				 !min.getText().isEmpty() &&
				 !max.getText().isEmpty()) {
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
			for (Integer value : values) {
				String id = "" + value;
				if (!db.contains(id)) {
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
	
	private Card parseCard(final String html, final String id) throws Exception {
		Document document = Jsoup.parse(html);
		Elements elements;
		
		String name = "";
		String cost = "";
		String cmc = "";
		String types = "";
		String text = "";
		String pt = "";
		

		elements = document.select(".cardDetails #ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_nameRow .value");
		if (elements.size() != 1) {
			return null;
		}
		if (elements.size() == 1) {
			name = elements.get(0).text();
		}
		
		elements = document.select(".cardDetails #ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_manaRow .value");
		for (Element element : elements) {
			cost = cost + replaceColors(element.html()) + "\n";
		}
		
		elements = document.select(".cardDetails #ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_cmcRow .value");
		if (elements.size() > 1) {
			throw new Exception("Multiple cmc: " + html);
		}
		if (elements.size() == 1) {
			cmc = elements.get(0).text();
		}
		
		elements = document.select(".cardDetails #ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_typeRow .value");
		for (Element element : elements) {
			types = types + element.text() + "\n";
		}
		
		elements = document.select(".cardDetails #ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_textRow .value .cardtextbox");
		for (Element element : elements) {
			text = text + replaceColors(element.html()) + "\n";
			text = text.replace("<i>", "");
			text = text.replace("</i>", "");
		}
		
		elements = document.select(".cardDetails #ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ptRow .value");
		if (elements.size() > 1) {
			throw new Exception("Multiple p/t: " + html);
		}
		if (elements.size() == 1) {
			pt = elements.get(0).text();
		}
		
		return new Card(
					id,
					name,
					cost,
					cmc,
					types,
					text,
					pt);
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
	
	private void scrape(final String id) throws Exception {
		Boolean english = isEnglish(get(new URL("http://gatherer.wizards.com/Pages/Card/Languages.aspx?printed=false&multiverseid=" + id)));
		
		if (english == null) {
			db.addInvalid(id);
		}
		else if (english == false) {
			db.addForeign(id);
		}
		else {
			Card card = parseCard(get(new URL("http://gatherer.wizards.com/Pages/Card/Details.aspx?printed=false&multiverseid=" + id)), id);
			
			if (card != null) {
				db.add(card);
				tree.insert(card.getName());
				
				File image_file = new File(db.getImageDirectory(), id + ".png");
				get(image_file, new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + id + "&type=card"));
	
				BufferedImage image = GraphicsUtils.read(image_file);
				if (GraphicsUtils.equal(no_image, image)) {
					image_file.delete();
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
			max.setText("" + MAX_ID);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}


