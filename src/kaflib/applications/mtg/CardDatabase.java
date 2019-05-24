package kaflib.applications.mtg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaflib.graphics.GraphicsUtils;
import kaflib.types.Directory;
import kaflib.types.Matrix;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.RandomUtils;
import kaflib.utils.StringUtils;

public class CardDatabase implements Iterable<CardInstance> {

	public static final File DEFAULT_ROOT = new File("Z:\\data\\games\\mtg");
	private final File root_directory;
	private final File image_directory;
	private final File deck_directory;
	private final File default_card;
		
	public static final String DB_NAME = "cards.xlsx";	
	public static final String HAVE_NAME = "haves.xlsx";
	private static final int COLUMNS = 10;
	private static final int ID = 0;
	private static final int NAME = 1;
	private static final int COST = 2;
	private static final int CMC = 3;
	private static final int TYPE = 4;
	private static final int TEXT = 5;
	private static final int P_T = 6;
	private static final int RESERVED = 7;
	private static final int RATING = 8;
	private static final int VOTES = 9;
	
	public static final String INVALID = "invalid";
	public static final String FOREIGN = "foreign";
	
	
	private final File db_file;
	private final File have_file;
	private final Map<Integer, CardInstance> cards;
	private final Set<String> have;
	private final Map<String, Card> name_index;

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
		
		cards = new HashMap<Integer, CardInstance>();
		have = new HashSet<String>();
		name_index = new HashMap<String, Card>();
		
		db_file = new File(root_directory, DB_NAME); 
		have_file = new File(root_directory, HAVE_NAME); 
		readDBs();
	}

	private final void readDBs() throws Exception {
		Matrix<String> matrix;

		// Read card db.
		if (!db_file.exists()) {
			return;
		}
		matrix = FileUtils.readXLSXSheet(db_file, false);

		for (int i = 0; i < matrix.getRowCount(); i++) {
			CardInstance instance = parseCard(matrix.getRow(i));
			cards.put(instance.getID(), instance);
			
			if (instance.isDomestic()) {
				Card card = instance.getCard();
				
				CheckUtils.check(card.getName(), "card: " + instance);
				name_index.put(card.getName(), card);
			}
		}
		
		// Read list of owned cards.
		if (!have_file.exists()) {
			return;
		}
		matrix = FileUtils.readXLSXSheet(have_file, false);
		
		for (int i = 0; i < matrix.getRowCount(); i++) {
			String name = matrix.get(i, 0);
			if (name == null || name.isEmpty()) {
				continue;
			}
			if (!name_index.containsKey(name)) {
				throw new Exception("Have: " + name + " not found in db.");
			}
			have.add(name);
		}
	}
	
	public Set<String> getNames() {
		return name_index.keySet();
	}
	
	public boolean contains(final int id) {
		return cards.containsKey(id);
	}

	public int getMaxValid() {
		int max = 1;
		for (Integer id : cards.keySet()) {
			if (id > max) {
				if (cards.get(id).isDomestic()) {
					max = id;
				}
			}
		}
		return max;
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
		cards.put(id, CardInstance.createInvalid(id));
	}

	public void addForeign(final int id) {
		cards.put(id, CardInstance.createForeign(id));
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
	
	/**
	 * Sets whether or not the card is owned.
	 * @param card
	 * @param value
	 * @throws Exception
	 */
	public void setHave(final Card card, final boolean value) throws Exception {
		String name = card.getName();
		if (value == true) {
			if (!have.contains(name)) {
				have.add(name);
			}
		}
		else {
			if (have.contains(name)) {
				have.remove(name);
			}
		}
	}

	/**
	 * Sets whether or not the card is owned.
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public void setHave(final String name, final boolean value) throws Exception {
		if (!name_index.containsKey(name)) {
			throw new Exception("Can't find: " + name + ".");
		}
		setHave(name_index.get(name), value);
	}
	
	public int getHaveCount() {
		return have.size();
	}
	
	/**
	 * Returns whether or not the card is owned.
	 * @param name
	 * @return
	 */
	public boolean have(final String name) {
		if (have.contains(name)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Adds the specified card index to the instance set, the have set, 
	 * and the index set.
	 * @param instance
	 * @throws Exception
	 */
	public void add(final Collection<CardInstance> instances) throws Exception {
		for (CardInstance instance : instances) {
			add(instance);
		}
	}
	
	/**
	 * Adds the specified card index to the instance set, the have set, 
	 * and the index set.
	 * @param instance
	 * @throws Exception
	 */
	public void add(final CardInstance instance) throws Exception {
		cards.put(instance.getID(), instance);
		if (instance.isDomestic()) {
			name_index.put(instance.getName(), instance.getCard());
		}
	}
	
	/**
	 * Removes the specified id if it is invalid.
	 * @param id
	 * @throws Exception
	 */
	public void removeInvalid(final String id) throws Exception {
		if (!cards.keySet().contains(id)) {
			return;
		}
		CardInstance card = cards.get(id);
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
		if (!name_index.containsKey(name)) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Does a lowercase match of the supplied name to a tracked card name.
	 * @param name
	 * @return
	 */
	public String matchName(final String name) {
		for (String indexed : name_index.keySet()) {
			if (indexed.toLowerCase().equals(name.toLowerCase())) {
				return indexed;
			}
		}
		return null;
	}

	/**
	 * Returns the card matching the given name, or null if it is not found.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Card getCard(final String name) throws Exception {
		if (!name_index.containsKey(name)) {
			return null;
		}
		else {
			return name_index.get(name);
		}
	}
	
	/**
	 * Returns a random card id in the have list.
	 * @return
	 * @throws Exception
	 */
	public Integer getRandomHaveID() throws Exception {
		if (have.size() == 0) {
			return null;
		}
		String name = RandomUtils.getRandom(have);
		return RandomUtils.getRandom(getCardInstances(name)).getID();
	}
	
	/**
	 * Gets a random set of haved cards.
	 * @param count
	 * @return
	 * @throws Exception
	 */
	public Set<String> getRandomHaveNames(final int count) throws Exception {
		return RandomUtils.getRandom(getHaves(), count);
	}
	
	/**
	 * Returns all names where have is true.
	 * @return
	 * @throws Exception
	 */
	public Set<String> getHaves() throws Exception {
		return have;
	}
		
	public int getRandomID(final boolean domesticOnly) throws Exception {
		while (true) {
			int id = RandomUtils.getRandom(cards.keySet());
			if (!domesticOnly || cards.get(id).isDomestic()) {
				return id;
			}
		}
	}
	
	public File getImage(final String name) throws Exception {
		for (int id : cards.keySet()) {
			CardInstance card = cards.get(id);
			if (card == null || card.isInvalidOrForeign()) {
				continue;
			}
			
			if (card.getName().toLowerCase().equals(name.toLowerCase())) {
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
		for (CardInstance instance : getCardInstances(name)) {
			File file = new File(image_directory, instance.getIDString() + ".png");
			if (file.exists()) {
				files.add(file);
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
		Set<CardInstance> cards = getCardInstances(name);
		float value = (float) 0.0;
		int count = 0;
		for (CardInstance card : cards) {
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
	public Set<CardInstance> getCardInstances(final String name) {
		Set<CardInstance> matches = new HashSet<CardInstance>();
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
	
	public Directory getTempDirectory() throws Exception {
		return new Directory(root_directory, "temp");
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
			CardInstance instance = cards.get(id);
			
			List<String> values = getValues(instance);
			values.set(RESERVED, "");
			matrix.addRow(values);
		}
		matrix.toXLSX(output_file);
		
		FileUtils.toXLSX(have, have_file);
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
				continue;
			}
			Integer value = getCMC(card);
			if (value != null) {
				if (!histogram.containsKey(value)) {
					histogram.put(value, 0);
				}
				histogram.put(value, histogram.get(value) + 1);
			}
		}
		
		if (missing.size() > 0) {
			throw new Exception("Missing cards:\n   '" + StringUtils.concatenate(missing, "'\n   '"));
		}
		return histogram;
	}
	
	public Integer getCMC(final Card card) throws Exception {
		CheckUtils.check(card, "card");
		if (card.getCMC() == null) {
			return null;
		}
		return StringUtils.toInt(card.getCMC());
	}
	
	public static CardInstance parseCard(final List<String> values) throws Exception {
		CardInstance card;
		
		if (values.size() >= 7) {
			String text = values.get(TEXT);
			text = text.replace("<i>", "");
			text = text.replace("</i>", "");
			
			card = new CardInstance(Integer.valueOf(values.get(ID)),
					values.get(NAME),
					values.get(COST),
					values.get(CMC),
					values.get(TYPE),
					text,
					values.get(P_T));
			
			
			if (values.size() >= 10 && 
				!values.get(CardDatabase.RATING).isEmpty() && 
				!values.get(CardDatabase.VOTES).isEmpty()) {
				card.setCommunityRating(Float.valueOf(values.get(RATING)));
				card.setCommunityVotes(Integer.valueOf(values.get(VOTES)));
			}
		}
		else if (values.size() == 2) {
			if (values.get(NAME).equals(INVALID)) {
				card = CardInstance.createInvalid(Integer.valueOf(values.get(ID)));
			}
			else if (values.get(NAME).equals(FOREIGN)) {
				card = CardInstance.createForeign(Integer.valueOf(values.get(ID)));
			}
			else {
				throw new Exception("Invalid row:\n" + StringUtils.concatenate(values, "\n"));
			}
		}
		else {
			throw new Exception("Incorrect number of values.");
		}	
		return card;
	}
	
	public static List<String> getValues(final CardInstance instance) throws Exception {
		List<String> list = new ArrayList<String>(CardDatabase.COLUMNS);
		for (int i = 0; i < CardDatabase.COLUMNS; i++) {
			list.add("");
		}
		list.set(ID, instance.getIDString());
		if (instance.isInvalid()) {
			list.set(NAME, INVALID);
			return list;
		}
		if (instance.isForeign()) {
			list.set(NAME, FOREIGN);
			return list;
		}
	
		list.set(NAME, instance.getName());
		list.set(COST, instance.getCost());
		list.set(CMC, instance.getCMC());
		list.set(TYPE, instance.getTypes());
		list.set(TEXT, instance.getText());
		list.set(P_T, instance.getPT());
		
		if (instance.hasRating()) {
			list.set(RATING, String.format("%.2f", instance.getRating()));	
			list.set(VOTES, String.format("%d", instance.getVotes()));	
		}
		
		return list;
	}

	@Override
	public Iterator<CardInstance> iterator() {
		return cards.values().iterator();
	}
}
