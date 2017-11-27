package kaflib.applications.mtg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Defines a mtg card.
 */
public class Card implements Comparable<Card> {
	private final int id;
	private final String name;
	private final String cost;
	private final String cmc;
	private final String types;
	private final String text;
	private final String pt;

	/**
	 * Create a card.
	 * @param number
	 * @param name
	 * @param cost
	 * @param cmc
	 * @param types
	 * @param text
	 * @param pt
	 */
	public Card(final String number,
			final String name,
			final String cost,
			final String cmc,
			final String types,
			final String text,
			final String pt) {
		this.id = Integer.valueOf(number);
		this.name = name;
		this.cost = cost;
		this.cmc = cmc;
		this.types = types;
		this.text = text;
		this.pt = pt;
	}
	
	/**
	 * Create a card.
	 * @param number
	 * @param name
	 * @param cost
	 * @param cmc
	 * @param types
	 * @param text
	 * @param pt
	 */
	public Card(final int number,
				final String name,
				final String cost,
				final String cmc,
				final String types,
				final String text,
				final String pt) {
		this.id = number;
		this.name = name;
		this.cost = cost;
		this.cmc = cmc;
		this.types = types;
		this.text = text;
		this.pt = pt;
	}
	
	/**
	 * Create a card from a list of column values.
	 * @param values
	 * @throws Exception
	 */
	public Card(final List<String> values) throws Exception {
		if (values.size() >= 7) {
			this.id = Integer.valueOf(values.get(CardDatabase.ID));
			this.name = values.get(CardDatabase.NAME);
			this.cost = values.get(CardDatabase.COST);
			this.cmc = values.get(CardDatabase.CMC);
			this.types = values.get(CardDatabase.TYPE);
			this.pt = values.get(CardDatabase.P_T);
			
			String temp = values.get(CardDatabase.TEXT);
			temp = temp.replace("<i>", "");
			temp = temp.replace("</i>", "");
			this.text = temp;
			
		}
		else if (values.size() == 2) {
			this.id = Integer.valueOf(values.get(CardDatabase.ID));
			this.name = values.get(CardDatabase.NAME);
			this.cost = "";
			this.cmc = "";
			this.types = "";
			this.text = "";
			this.pt = "";
		}
		else {
			throw new Exception("Incorrect number of values.");
		}	
	}

	/**
	 * Returns whether or not the collection of cards are the same card
	 * from different editions.
	 * @param cards
	 * @return
	 */
	public static boolean match(final Collection<Card> cards) {
		if (cards.size() <= 1) {
			return true;
		}
		Card c = cards.iterator().next();
		
		for (Card card : cards) {
			if (!card.matches(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns whether or not two cards are the same (name) but possibly
	 * from different sets.
	 * @param other
	 * @return
	 */
	public boolean matches(final Card other) {
		return name.equals(other.getName());
	}
	
	/**
	 * @return the number
	 */
	public int getID() {
		return id;
	}
	
	public String getIDString() {
		return String.format("%d", getID());
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the cost
	 */
	public String getCost() {
		return cost;
	}
	/**
	 * @return the cmc
	 */
	public String getCMC() {
		return cmc;
	}
	
	public int getCMCValue() {
		if (cmc == null || cmc.isEmpty()) {
			return 0;
		}
		return Integer.valueOf(cmc);
	}
	
	/**
	 * @return the types
	 */
	public String getTypes() {
		return types;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return the pt
	 */
	public String getPT() {
		return pt;
	}
	
	public boolean isInvalid() {
		return getName().equals(CardDatabase.INVALID);
	}
	
	public boolean isDomestic() {
		return !getName().equals(CardDatabase.INVALID) &&
			   !getName().equals(CardDatabase.FOREIGN);
	}
	
	/**
	 * Returns the card as db values.
	 * @return
	 */
	public List<String> getValues() {
		List<String> list = new ArrayList<String>(CardDatabase.COLUMNS);
		for (int i = 0; i < CardDatabase.COLUMNS; i++) {
			list.add("");
		}
		list.set(CardDatabase.ID, getIDString());
		list.set(CardDatabase.NAME, getName());
		list.set(CardDatabase.COST, getCost());
		list.set(CardDatabase.CMC, getCMC());
		list.set(CardDatabase.TYPE, getTypes());
		list.set(CardDatabase.TEXT, getText());
		list.set(CardDatabase.P_T, getPT());
		
		return list;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Card) {
			return equals((Card) o);
		}
		else {
			return false;
		}
	}
	
	public boolean matches(final String name) {
		if (getName().equals(name)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns an id check on these cards.  They could still be the same
	 * card but a different set.
	 * @param c
	 * @return
	 */
	public boolean equals(final Card c) {
		return id == c.getID();
	}
	
	public int hashCode() {
		return id;
	}

	public String toString() {
		return getIDString() + "/" + name + 
			   "\n   " + cost +
			   "\n   " + cmc +
			   "\n   " + types +
			   "\n   " + text +
			   "\n   " + pt;
	}
	

	public static Card createForeign(final String id) {
		return createForeign(Integer.valueOf(id));
	}

	public static Card createInvalid(final String id) {
		return createInvalid(Integer.valueOf(id));
	}
	
	public static Card createForeign(final int id) {
		return new Card(id,
						CardDatabase.FOREIGN,
						"",
						"",
						"",
						"",
						"");
	}
	
	public static Card createInvalid(final int id) {
		return new Card(id,
						CardDatabase.INVALID,
						"",
						"",
						"",
						"",
						"");
	}

	public enum Classification {
		LENGENDARY,
		PLANESWALKER,
		CREATURE,
		ARTIFACT,
		SPECIAL_LAND,
		ENCHANTMENT,
		INSTANT,
		SORCERY,
		BASIC_LAND,
		UNKNOWN
	}
	public static Classification getClassification(final Card card) {
		String types = card.getTypes().toLowerCase().trim();
		if (types.startsWith("legendary")) {
			return Classification.LENGENDARY;
		}
		else if (types.startsWith("planeswalker")) {
			return Classification.PLANESWALKER;
		}
		else if (types.startsWith("creature") || 
				 types.startsWith("artifact creature") || 
				 types.startsWith("enchantment creature")) {
			return Classification.CREATURE;
		}
		else if (types.startsWith("artifact")) {
			return Classification.ARTIFACT;
		}
		else if (types.startsWith("land")) {
			return Classification.SPECIAL_LAND;
		}
		else if (types.startsWith("enchantment") ||
				 types.startsWith("world enchantment") ||
				 types.startsWith("tribal enchantment")) {
			return Classification.ENCHANTMENT;
		}
		else if (types.startsWith("instant") ||
				 types.startsWith("interrupt") ||
				 types.startsWith("tribal instant")) {
			return Classification.INSTANT;
		}
		else if (types.startsWith("sorcery") ||
				 types.startsWith("tribal sorcery")) {
			return Classification.SORCERY;
		}
		else if (types.startsWith("basic")) {
			return Classification.BASIC_LAND;
		}
		else {
			return Classification.UNKNOWN;
		}
	}
	
	@Override
	public int compareTo(Card o) {
		if (getName().equals(o.getName())) {
			return 0;
		}
		if (getClassification(this).ordinal() < getClassification(o).ordinal()) {
			return -1;
		}
		else if (getClassification(this).ordinal() > getClassification(o).ordinal()) {
			return 1;
		}
		else {
			
			if (getCMCValue() > o.getCMCValue()) {
				return -1;
			}
			else if (getCMCValue() < o.getCMCValue()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

}
