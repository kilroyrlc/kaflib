package kaflib.applications.mtg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import kaflib.utils.FileUtils;

/**
 * Defines a list of card names.
 */
public class CardList extends CardCollection implements Iterable<Card> {

	private final List<Card> cards;
	private final boolean read_only;


	/**
	 * Create the list.
	 */
	public CardList() {
		cards = new ArrayList<Card>();
		read_only = false;
	}
	
	public CardList(final Collection<Card> cards) throws Exception {
		this.cards = new ArrayList<Card>();
		read_only = false;
		add(cards);
	}
	
	public CardList(final Collection<Card> cards, boolean readOnly) throws Exception {
		this.cards = new ArrayList<Card>();
		add(cards);
		read_only = readOnly;
	}
	
	public void add(final Card card) throws Exception {
		if (read_only) {
			throw new Exception("List is read-only.");
		}

		if (!card.isDomestic()) {
			throw new Exception("Trying to add invalid card:\n" + card);
		}
		cards.add(card);
		Collections.sort(cards);
	}

	public void add(final Collection<Card> cards) throws Exception {
		if (read_only) {
			throw new Exception("List is read-only.");
		}
		
		for (Card card : cards) {
			if (!card.isDomestic()) {
				throw new Exception("Trying to add invalid card:\n" + card);
			}
			this.cards.add(card);
		}
		Collections.sort(this.cards);
	}
	
	public int size() {
		return cards.size();
	}
	
	/**
	 * Returns a list of all card names.
	 * @return
	 */
	public List<String> getNameList() throws Exception {
		List<String> list = new ArrayList<String>();
		for (Card card : cards) {
			list.add(card.getName());
		}
		return list;
	}
	
	/**
	 * Returns the number of lands.
	 * @return
	 */
	public int getLandCount() {
		int count = 0;
		for (Card card : cards) {
			if (card.getTypeValues().contains(Type.LAND)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * @return the read_only
	 */
	public boolean isReadOnly() {
		return read_only;
	}

	/**
	 * Returns whether or not there are duplicates.
	 * @return
	 */
	public boolean hasDuplicates() {
		Set<String> set = new HashSet<String>();
		for (Card card : cards) {
			if (set.contains(card.getName())) {
				return true;
			}
			else {
				set.add(card.getName());
			}
		}
		return false;
	}
	
	
	/**
	 * Creates a card list from a collection of names.
	 * @param db
	 * @param names
	 * @return
	 * @throws Exception
	 */
	public static CardList getList(final CardDatabase db, 
								   final Collection<String> names) throws Exception {
		CardList list = new CardList();
		for (String name : names) {
			list.add(db.getCard(name));
		}
		return list;
	}
	
	/**
	 * Creates a card list from a collection of names.
	 * @param db
	 * @param names
	 * @return
	 * @throws Exception
	 */
	public static CardList readXLSX(final CardDatabase db, 
								   final File file) throws Exception {
		if (!file.exists() || !file.getName().endsWith(".xlsx")) {
			throw new Exception("Invalid excel file: " + file + ".");
		}

		List<Card> list = new ArrayList<Card>();
		for (String name : CardUtils.readNameList(file)) {
			list.add(db.getCard(name));
		}
		return new CardList(list, !file.canWrite());
	}
	
	/**
	 * Creates a card list from a collection of names.
	 * @param db
	 * @param names
	 * @return
	 * @throws Exception
	 */
	public static CardList readTXT(final CardDatabase db, 
								      final File file) throws Exception {
		if (!file.exists() || !file.getName().endsWith(".txt")) {
			throw new Exception("Invalid text file: " + file + ".");
		}

		List<String> lines = FileUtils.readLines(file);
		List<Card> list = new ArrayList<Card>();
		for (String name : lines) {
			list.add(db.getCard(name.trim()));
		}
		return new CardList(list, !file.canWrite());
	}

	@Override
	public Iterator<Card> iterator() {
		return cards.iterator();
	}
	
	
	public static CardList create(final Collection<String> names, final CardDatabase db) throws Exception {
		CardList list = new CardList();
		for (String name : names) {
			list.add(db.getCard(name));
		}
		return list;
	}
}
