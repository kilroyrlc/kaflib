package kaflib.applications.mtg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Defines a list of card names.
 */
public class CardList implements Iterable<Card> {

	private final List<Card> cards;
	
	/**
	 * Create the list.
	 */
	public CardList() {
		cards = new ArrayList<Card>();
	}
	
	public void add(final Card card) throws Exception {
		if (!card.isDomestic()) {
			throw new Exception("Trying to add invalid card:\n" + card);
		}
		cards.add(card);
		Collections.sort(cards);
	}

	public void add(final Collection<Card> cards) throws Exception {
		for (Card card : cards) {
			if (!card.isDomestic()) {
				throw new Exception("Trying to add invalid card:\n" + card);
			}
			cards.add(card);
		}
		Collections.sort(this.cards);
	}
	
	/**
	 * Returns a list of all card names.
	 * @return
	 */
	public List<String> getNameList() {
		List<String> list = new ArrayList<String>();
		for (Card card : cards) {
			list.add(card.getName());
		}
		return list;
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
	public static CardList getList(final CardDatabase db, 
								   final File excelFile) throws Exception {
		if (!excelFile.exists() || !excelFile.getName().endsWith(".xlsx")) {
			throw new Exception("Invalid excel file: " + excelFile + ".");
		}

		CardList list = new CardList();
		for (String name : CardUtils.readNameList(excelFile)) {
			list.add(db.getCard(name));
		}
		return list;
	}

	@Override
	public Iterator<Card> iterator() {
		return cards.iterator();
	}
	
}
