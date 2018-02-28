package kaflib.applications.mtg;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Defines a list of card names.
 */
public class CardSet extends CardCollection implements Iterable<Card> {

	private final Set<Card> cards;
	
	/**
	 * Create the list.
	 */
	public CardSet() {
		cards = new HashSet<Card>();
	}
	
	public void add(final Card card) throws Exception {
		if (!card.isDomestic()) {
			throw new Exception("Trying to add invalid card:\n" + card);
		}
		cards.add(card);
	}

	public void add(final Collection<CardInstance> cards) throws Exception {
		for (CardInstance card : cards) {
			if (!card.isDomestic()) {
				throw new Exception("Trying to add invalid card:\n" + card);
			}
			cards.add(card);
		}
	}
	
	/**
	 * Returns a list of all card names.
	 * @return
	 */
	public List<String> getNameList() throws Exception {
		CardList list = new CardList(cards);
		return list.getNameList();
	}

	/**
	 * Creates a card set from a collection of names.
	 * @param db
	 * @param names
	 * @return
	 * @throws Exception
	 */
	public static CardSet getSet(final CardDatabase db, 
								   final Collection<String> names) throws Exception {
		CardSet list = new CardSet();
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
	public static CardSet getSet(final CardDatabase db, 
								   final File excelFile) throws Exception {
		if (!excelFile.exists() || !excelFile.getName().endsWith(".xlsx")) {
			throw new Exception("Invalid excel file: " + excelFile + ".");
		}

		CardSet set = new CardSet();
		for (String name : CardUtils.readNameList(excelFile)) {
			set.add(db.getCard(name));
		}
		return set;
	}

	@Override
	public Iterator<Card> iterator() {
		return cards.iterator();
	}
	
}
