package kaflib.applications.mtg;

import java.util.Collection;
import java.util.Comparator;

import kaflib.utils.CheckUtils;

/**
 * Defines a mtg card.
 */
public class CardInstance implements Comparable<CardInstance> {
	private final int id;
	private final Card card;
	private final boolean invalid;
	private final boolean foreign;
	private Float community_rating;
	private Integer community_votes;

	public static CardInstance merge(final CardInstance a, 
									 final CardInstance b) throws Exception {
		if (!a.getName().equals(b.getName())) {
			throw new Exception("Cards must have identical names.");
		}
		CardInstance instance = a;
		if (a.isInvalid() || a.isForeign()) {
			instance = b;
		}
		if (b.isInvalid() || b.isForeign()) {
			throw new Exception("Neither card is valid.");
		}
		
		if (a.hasRating() && b.hasRating()) {
			instance.setCommunityVotes(a.getVotes() + b.getVotes());
			instance.setCommunityRating(
					((a.getVotes() / instance.getVotes()) * a.getRating()) +
					((b.getVotes() / instance.getVotes()) * b.getRating()));
		}
		else if (a.hasRating()) {
			instance.setCommunityVotes(a.getVotes());
			instance.setCommunityRating(a.getRating());
		}
		else if (b.hasRating()) {
			instance.setCommunityVotes(b.getVotes());
			instance.setCommunityRating(b.getRating());
		}
		else {
			
		}
		return instance;
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
	public CardInstance(final String number,
			final String name,
			final String cost,
			final String cmc,
			final String types,
			final String text,
			final String pt) {
		this(Integer.valueOf(number), name, cost, cmc, types, text, pt);
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
	public CardInstance(final int number,
				final String name,
				final String cost,
				final String cmc,
				final String types,
				final String text,
				final String pt) {
		this.id = number;
		card = new Card(name, cost, cmc, types, text, pt);
		
		if (name.equals(CardDatabase.INVALID)) {
			invalid = true;
		}
		else {
			invalid = false;
		}
		if (name.equals(CardDatabase.FOREIGN)) {
			foreign = true;
		}
		else {
			foreign = false;
		}
		community_rating = null;
		community_votes = null;
	}
	
	public CardInstance(final int number,
						final Card card) throws Exception {
		CheckUtils.check(card, "card");
		this.id = number;
		this.card = card;
		invalid = false;
		foreign = false;
		community_rating = null;
		community_votes = null;
	}

	protected CardInstance(final int number,
						final boolean invalid, 
						final boolean foreign) {
		this.id = number;
		this.card = null;
		this.invalid = invalid;
		this.foreign = foreign;
		community_rating = null;
		community_votes = null;
	}
	
	public Card getCard() {
		return card;
	}

	/**
	 * Returns whether or not the collection of cards are the same card
	 * from different editions.
	 * @param cards
	 * @return
	 */
	public static boolean match(final Collection<CardInstance> cards) {
		if (cards.size() <= 1) {
			return true;
		}
		CardInstance c = cards.iterator().next();
		if (c.isInvalidOrForeign()) {
			return false;
		}
		
		for (CardInstance card : cards) {
			if (card.isInvalidOrForeign()) {
				return false;
			}
			if (!card.getCard().equals(c.getCard())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns whether or not this id points to an invalid/foreign card.
	 * @return
	 */
	public boolean isInvalidOrForeign() {
		return invalid || foreign;
	}
	
	public void setCommunityRating(float rating) {
		community_rating = rating;
	}
	
	public void setCommunityVotes(int votes) {
		community_votes = votes;
	}
	
	/**
	 * Returns whether or not two cards are the same (name) but possibly
	 * from different sets.
	 * @param other
	 * @return
	 */
	public boolean matches(final CardInstance other) {
		if (isInvalidOrForeign() || other.isInvalidOrForeign()) {
			return false;
		}
		return getCard().equals(other.getCard());
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
	public String getName() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card: " + getIDString() + ".");
		}
		return card.getName();
	}
	/**
	 * @return the cost
	 */
	public String getCost() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return card.getCost();
	}
	/**
	 * @return the cmc
	 */
	public String getCMC() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return card.getCMC();
	}
	
	public int getCMCValue() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		String cmc = card.getCMC();
		if (cmc == null || cmc.isEmpty()) {
			return 0;
		}
		return Integer.valueOf(cmc);
	}
	
	/**
	 * @return the types
	 */
	public String getTypes() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return card.getTypes();
	}
	/**
	 * @return the text
	 */
	public String getText() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return card.getText();
	}
	/**
	 * @return the pt
	 */
	public String getPT() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return card.getPT();
	}
	
	public Float getRating() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return community_rating;
	}

	public int getVotes() throws Exception {
		if (isInvalidOrForeign()) {
			throw new Exception("Invalid/foreign card.");
		}
		return community_votes;
	}
	
	public boolean isInvalid() {
		return invalid;
	}
	
	public boolean isForeign() {
		return foreign;
	}
	
	public boolean isDomestic() {
		return !isInvalidOrForeign();
	}
	
	public boolean hasRating() {
		if (community_rating != null && community_votes != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof CardInstance) {
			return equals((CardInstance) o);
		}
		else {
			return false;
		}
	}
	
	public boolean matches(final String name) {
		if (isInvalidOrForeign()) {
			return false;
		}
		if (card.getName().equals(name)) {
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
	public boolean equals(final CardInstance c) {
		return id == c.getID();
	}
	
	public int hashCode() {
		return id;
	}

	public String toString() {
		if (invalid) {
			return getIDString() + "/invalid";
		}
		if (foreign) {
			return getIDString() + "/foreign";
		}
		String rating = "No rating";
		if (hasRating()) {
			rating = community_rating + " (" + community_votes + ")";
		}
		return getIDString() + "/" + card.toString() + "\n" + rating;
	}

	public static CardInstance createInvalid(final String id) {
		return new CardInstance(Integer.valueOf(id), true, false);
	}

	public static CardInstance createInvalid(final int id) {
		return new CardInstance(id, true, false);
	}
	
	public static CardInstance createForeign(final int id) {
		return new CardInstance(id, false, true);
	}
	
	private static Comparator<CardInstance> rating_comparator = null;
	public static Comparator<CardInstance> getRatingComparator() {
		if (rating_comparator == null) {
			rating_comparator = new Comparator<CardInstance>() {
				@Override
				public int compare(CardInstance o1, CardInstance o2) {
					try {
						if (o1.hasRating() && o2.hasRating()) {
							if (o1.getRating() > o2.getRating()) {
								return -1;
							}
							else if (o2.getRating() > o1.getRating()) {
								return 1;
							}
							else {
								if (o1.getVotes() > o2.getVotes()) {
									return -1;
								}
								else if (o2.getVotes() > o1.getVotes()) {
									return 1;
								}
								else {
									return 0;
								}
							}
						}
						else if (o1.hasRating()) {
							return -1;
						}
						else if (o2.hasRating()) {
							return 1;
						}
						else {
							return 0;
						}
					}
					catch (Exception e) {
						return 0;
					}
				}				
			};
		}
		return rating_comparator;
	}
	
	@Override
	public int compareTo(CardInstance o) {
		if (isInvalidOrForeign()) {
			if (o.isInvalidOrForeign()) {
				return 0;
			}
			else {
				return 1;
			}
		}
		else {
			if (o.isInvalidOrForeign()) {
				return -1;
			}
			else {
				return getCard().compareTo(o.getCard());
			}
		}
	}

}
