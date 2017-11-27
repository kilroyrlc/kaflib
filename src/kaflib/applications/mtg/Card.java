package kaflib.applications.mtg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Defines a card where every field is a string.
 */
public class Card {
	private final String id;
	private final String name;
	private final String cost;
	private final String cmc;
	private final String types;
	private final String text;
	private final String pt;
	
	public Card(final String number,
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
	
	public Card(final List<String> values) throws Exception {
		if (values.size() >= 7) {
			this.id = values.get(CardDatabase.ID);
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
			this.id = values.get(CardDatabase.ID);
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
	
	public boolean matches(final Card other) {
		return name.equals(other.getName()) &&
			   cost.equals(other.getCost()) &&
			   cmc.equals(other.getCMC()) &&
			   types.equals(other.getTypes()) &&
			   text.equals(other.getText()) &&
			   pt.equals(other.getPT());
	}
	
	/**
	 * @return the number
	 */
	public String getNumber() {
		return id;
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
	
	public List<String> getValues() {
		List<String> list = new ArrayList<String>();
		list.add(getNumber());
		list.add(getName());
		list.add(getCost());
		list.add(getCMC());
		list.add(getTypes());
		list.add(getText());
		list.add(getPT());
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
	
	public boolean equals(final Card c) {
		return id.equals(c.getNumber());
	}
	
	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return id + "/" + name + 
			   "\n   " + cost +
			   "\n   " + cmc +
			   "\n   " + types +
			   "\n   " + text +
			   "\n   " + pt;
	}
	
	public static Card createForeign(final String number) {
		return new Card(number,
						CardDatabase.FOREIGN,
						"",
						"",
						"",
						"",
						"");
	}
	
	public static Card createInvalid(final String number) {
		return new Card(number,
						CardDatabase.INVALID,
						"",
						"",
						"",
						"",
						"");
	}
	
}
