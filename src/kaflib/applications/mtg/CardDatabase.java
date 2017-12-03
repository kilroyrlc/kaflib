package kaflib.applications.mtg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaflib.graphics.GraphicsUtils;
import kaflib.types.Matrix;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.StringUtils;

public class CardDatabase {

	public static final File DEFAULT_ROOT = new File("Z:\\data\\games\\mtg");
	private final File root_directory;
	private final File image_directory;
	private final File deck_directory;
	private final File default_card;
		
	public static final String DB_NAME = "cards.xlsx";
	public static final int COLUMNS = 10;
	public static final int ID = 0;
	public static final int NAME = 1;
	public static final int COST = 2;
	public static final int CMC = 3;
	public static final int TYPE = 4;
	public static final int TEXT = 5;
	public static final int P_T = 6;
	public static final int HAVE = 7;
	public static final int RATING = 8;
	public static final int VOTES = 9;
	
	public static final String INVALID = "invalid";
	public static final String FOREIGN = "foreign";
	
	
	private final File db_file;
	private final Map<Integer, Card> cards;
	private final Map<String, Boolean> have;

	public CardDatabase() throws Exception {
		this(DEFAULT_ROOT);
	}
	
	public CardDatabase(final File rootDirectory) throws Exception {
		root_directory = rootDirectory;
		if (!root_directory.exists()) {
			throw new Exception("No directory.");
		}
		image_directory = new File(root_directory, "images");
		if (!image_directory.exists()) {
			image_directory.mkdir();
		}
		deck_directory = new File(root_directory, "decks");
		if (!deck_directory.exists()) {
			deck_directory.mkdir();
		}
		default_card = new File(image_directory, "none.png");
		
		cards = new HashMap<Integer, Card>();
		have = new HashMap<String, Boolean>();
		
		Matrix<String> matrix;
		
		db_file = new File(root_directory, DB_NAME); 
		if (db_file.exists()) {
			matrix = FileUtils.readXLSXSheet(db_file, false);

			for (int i = 0; i < matrix.getRowCount(); i++) {
				Card card = new Card(matrix.getRow(i));
				cards.put(card.getID(), card);
				
				if (card.isDomestic()) {
					CheckUtils.check(card.getName(), "card: " + card);
					if (matrix.hasValue(i, HAVE) &&
						matrix.get(i, HAVE) != null && 
						matrix.get(i, HAVE).equals("yes")) {
						have.put(card.getName(), true);
					}
					else {
						if (!have.containsKey(card.getName())) {
							have.put(card.getName(), false);
						}		
					}
				}
				
			}
		}

	}
	
	public Set<String> getNames() {
		return have.keySet();
	}
	
	public boolean contains(final int id) {
		return cards.containsKey(id);
	}

	public boolean fullyPopulated(final int id) {
		// Card ids not tracked are not fully populated.
		if (!contains(id)) {
			return false;
		}
		// Foreign/invalid are fully populated.
		if (!cards.get(id).isDomestic()) {
			return true;
		}
		// Cards without rating are not fully populated.
		if (!cards.get(id).hasRating()) {
			return false;
		}
		
		// All criteria above satisfied, fully populated.
		return true;
	}
	
	public void addInvalid(final int id) {
		cards.put(id, Card.createInvalid(id));
	}

	public void addForeign(final int id) {
		cards.put(id, Card.createForeign(id));
	}
	
	public File getDefaultCard() {
		return default_card;
	}
	
	public BufferedImage getDefaultImage() throws Exception {
		if (default_card.exists()) {
			return GraphicsUtils.read(default_card);
		}
		else {
			return null;
		}
	}

	public void setHave(final String name, final boolean value) throws Exception {
		if (!have.containsKey(name)) {
			throw new Exception("No: " + name + ".");
		}
		else {
			have.put(name, value);
		}
	}
	
	public Boolean have(final String name) {
		if (have.containsKey(name)) {
			return have.get(name);
		}
		else {
			return null;
		}
	}
	
	public void add(final Card card) throws Exception {
		cards.put(card.getID(), card);
		if (card.isDomestic()) {
			if (!have.containsKey(card.getName())) {
				have.put(card.getName(), false);
			}
		}
	}
	
	public void removeInvalid(final String id) throws Exception {
		if (!cards.keySet().contains(id)) {
			return;
		}
		Card card = cards.get(id);
		if (card.isDomestic()) {
			return;
		}
		else {
			cards.remove(id);
		}
	}
	
	/**
	 * Returns whether or not the name is found in the db.
	 * @param name
	 * @return
	 */
	public boolean checkName(final String name) {
		return have.containsKey(name);
	}

	public String getName(final String name) {
		for (int id : cards.keySet()) {
			if (cards.get(id).getName().toLowerCase().equals(name.toLowerCase())) {
				return cards.get(id).getName();
			}
		}
		return null;
	}

	/**
	 * Returns a random card id in the have list.
	 * @return
	 * @throws Exception
	 */
	public Integer getRandomHaveID() throws Exception {
		int start = RandomUtils.randomInt(have.size());
		int i = 0;
		for (String name : have.keySet()) {
			if (i < start) {
				i++;
			}
			else {
				if (have.get(name)) {
					return RandomUtils.getRandom(getCards(name)).getID();
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets a random set of haves.
	 * @param count
	 * @return
	 * @throws Exception
	 */
	public Set<String> getRandomHaveNames(final int count) throws Exception {
		return RandomUtils.getRandom(have.keySet(), count);
	}
	
	public int getRandomID(final boolean domesticOnly) throws Exception {
		while (true) {
			int id = RandomUtils.getRandom(cards.keySet());
			if (!domesticOnly || cards.get(id).isDomestic()) {
				return id;
			}
		}
	}
	
	public File getImage(final String name) {
		for (int id : cards.keySet()) {
			if (cards.get(id).getName().toLowerCase().equals(name.toLowerCase())) {
				File file = new File(image_directory, id + ".png");
				if (file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * Removes cards marked as invalid between the specified range, inclusive.
	 * @param start
	 * @param end
	 * @throws Exception
	 */
	public void removeNonDomestic(final int start, final int end) throws Exception {
		Set<Integer> remove = new HashSet<Integer>();
		for (int id : cards.keySet()) {
			if (!cards.get(id).isDomestic()) {
				int value = Integer.valueOf(id);
				if (value >= start && value <= end) {
					remove.add(id);
				}
			}
		}
		
		for (int id : remove) {
			cards.remove(id);
		}
	}
	
	/**
	 * Removes cards marked as invalid between the specified range, inclusive.
	 * @param start
	 * @param end
	 * @throws Exception
	 */
	public void removeInvalids(final int start, final int end) throws Exception {
		Set<Integer> remove = new HashSet<Integer>();
		for (int id : cards.keySet()) {
			if (cards.get(id).isInvalid()) {
				int value = Integer.valueOf(id);
				if (value >= start && value <= end) {
					remove.add(id);
				}
			}
		}
		
		for (int id : remove) {
			cards.remove(id);
		}
	}

	/**
	 * Returns a file holding a random image matching the card name.  Should
	 * be the same card, just a random set.
	 * @param name
	 * @return
	 */
	public File getRandomImage(final String name) throws Exception {
		Set<File> files = new HashSet<File>();
		for (int id : cards.keySet()) {
			if (cards.get(id).matches(name)) {
				File file = new File(image_directory, cards.get(id).getIDString() + ".png");
				if (file.exists()) {
					files.add(file);
				}
			}
		}
		if (files.size() > 0) {
			return RandomUtils.getRandom(files);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Returns the mtg community rating, out of five.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Float getRating(final String name) throws Exception {
		Set<Card> cards = getCards(name);
		float value = (float) 0.0;
		int count = 0;
		for (Card card : cards) {
			if (card.hasRating()) {
				value += card.getRating();
				count++;
			}
		}
		if (count == 0) {
			return null;
		}
		
		return value / (float) count;
	}
	
	/**
	 * Return the card matching the name.  The ID will be arbitrary.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Card getCard(final String name) throws Exception {
		Set<Card> cards = getCards(name);
		if (cards.size() == 0) {
			return null;
		}
		
		if (!Card.match(cards)) {
			throw new Exception("Cards do not match: " + StringUtils.concatenate(cards, "\n"));
		}
		return cards.iterator().next();
	}

	/**
	 * Returns all card IDs matching the name.
	 * @param name
	 * @return
	 */
	public Set<Integer> getIDs(final String name) {
		Set<Integer> matches = new HashSet<Integer>();
		for (int id : cards.keySet()) {
			if (cards.get(id).matches(name)) {
				matches.add(id);
			}
		}
		return matches;
		
	}
	
	/**
	 * Returns all cards matching the name.
	 * @param name
	 * @return
	 */
	public Set<Card> getCards(final String name) {
		Set<Card> matches = new HashSet<Card>();
		for (int id : getIDs(name)) {
			matches.add(cards.get(id));
		}
		return matches;
	}
	

	public void write() throws Exception {
		write(null);
	}
	
	public void writeBackup() throws Exception {
		write(new File(root_directory, "cards_backup.xlsx"));
	}
	
	public File getImageDirectory() {
		return image_directory;
	}
	
	public File getDeckDirectory() {
		return deck_directory;
	}
	
	/**
	 * Writes the db to an excel file.
	 * @throws Exception
	 */
	public void write(final File file) throws Exception {
		File output_file = file;
		if (output_file == null) {
			output_file = db_file;
		}
		
		Matrix<String> matrix = new Matrix<String>();
		for (int id : cards.keySet()) {
			Card card = cards.get(id);
			
			List<String> values = card.getValues();
			if (card.isDomestic()) {
				if (!have.containsKey(card.getName())) {
					throw new Exception("No have listing for: " + card);
				}
				if (have.get(card.getName())) {
					values.set(HAVE, new String("yes"));
				}
				else {
					values.set(HAVE, new String("no"));
				}
			}
			else {
				values.set(HAVE, new String("n/a"));
			}

			matrix.addRow(values);
		}
		matrix.toXLSX(output_file);
	}
	
	/**
	 * Creates a histogram of converted mana costs.
	 * @param names
	 * @return
	 * @throws Exception
	 */
	public Map<Integer, Integer> getCMCHistogram(final Collection<String> names) throws Exception {
		
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
		Set<String> missing = new HashSet<String>();
		
		for (String name : names) {
			Card card = getCard(name);
			if (card == null) {
				missing.add(name);
			}
			Integer value = StringUtils.toInt(card.getCMC());
			if (value != null) {
				if (!histogram.containsKey(value)) {
					histogram.put(value, 0);
				}
				histogram.put(value, histogram.get(value) + 1);
			}
		}
		
		if (missing.size() > 0) {
			throw new Exception("Missing cards:\n   " + StringUtils.concatenate(missing, "\n   "));
		}
		return histogram;
	}
	
}
