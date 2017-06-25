package kaflib.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import kaflib.types.Mutex;

/**
 * Takes a deck of values and creates a shoe of n decks of these values, 
 * shuffled.
 */
public class RandomShoe<T> {

	private final List<T> deck;
	private final List<Integer> shoe;
	private Random random;
	private int burners;
	private int shoe_size;
	private boolean endless;
	private Mutex mutex;
	
	/**
	 * Creates the shoe of values.
	 * @param values
	 * @param decks
	 * @throws Exception
	 */
	public RandomShoe(final List<T> values, 
					  final int decks, 
					  final int burnCards,
					  final boolean endless) throws Exception {
		CheckUtils.checkNonEmpty(values, "deck");
		CheckUtils.checkPositive(decks, "deck count");
		CheckUtils.checkPositive(burnCards, "burners");
		
		// Set the initial shoe size, endless values.
		shoe_size = values.size() * decks;
		this.endless = endless;
		
		if (burners > shoe_size / 2) {
			throw new Exception("Burner count way too big.");
		}
		
		// Copy the deck.
		deck = new ArrayList<T>();
		deck.addAll(values);
		
		// Allocate the first shoe.
		shoe = new ArrayList<Integer>(shoe_size);
		burners = burnCards;
		random = new Random();
		mutex = new Mutex();
		
		// Shuffle first deck.
		shuffle();
	}
	
	/**
	 * Returns whether or not the source deck contains the specified value.  It
	 * may not be in the current shoe.
	 * @param value
	 * @return
	 */
	public boolean deckContains(final T value) {
		Iterator<T> i = deck.iterator();
		while (i.hasNext()) {
			if (i.next().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the shoe size.
	 * @return
	 */
	public int shoeSize() {
		return shoe.size();
	}
	
	/**
	 * Returns the deck of possible values.
	 * @return
	 */
	public int deckSize() {
		return deck.size();
	}
	
	/**
	 * Shuffles the shoe, which is populated with an equal number of references
	 * to each deck index.  Burns the specified number of items.
	 * @throws Exception
	 */
	private void shuffle() throws Exception {
		try {
			shoe.clear();
			
			// Populate the shoe with deck indexes, evenly distributed.		
			for (int i = 0; i < shoe_size; i++) {
				shoe.add(i % deck.size());
			}
			
			// Shuffle each position.
			for (int i = 0; i < shoe.size(); i++) {
				int temp = shoe.get(i);
				int swapi = random.nextInt(shoe.size());
				shoe.set(i, shoe.get(swapi));
				shoe.set(swapi, temp);
			}
			
			for (int i = 0; i < burners; i++) {
				shoe.remove(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the next item in the shoe, thread safe.  Returns null if the shoe
	 * is empty and endless is off.
	 * @return
	 * @throws Exception
	 */
	public T getNext() throws Exception {
		int combo = mutex.lock(3000);
		
		if (shoe.size() == 0) {
			if (endless == false) {
				return null;
			}
			else {
				shuffle();
			}
		}
		
		T value = deck.remove(0);
		
		mutex.unlock(combo);
		return value;
	}
	
}
