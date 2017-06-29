package kaflib.types;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

/**
 * Defines a set of words stored in tree form.  That is, the words 
 * {bat, car, cat, cart, cert} would look like this:
 * 
 * [b]      [c]
 *  |       / \
 * [a]    [a] [e]
 *  |     / \   \
 * [t]  [r] [t] [r]
 *       |       |
 *      [t]     [t]
 *      
 * (In reality the implementation uses letters as edges rather than nodes,
 * but this is of no concern to using it).
 *      
 * The obvious application is autocomplete.
 */
public class WordTree implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Node root;
	
	/**
	 * Creates an empty word tree.
	 */
	public WordTree() {
		root = new Node();
	}

	/**
	 * Creates a word tree with initial values.
	 */
	public WordTree(final Collection<String> initialValues) throws Exception {
		root = new Node();
		for (String word : initialValues) {
			root.insert(word);
		}
	}
	
	/**
	 * Inserts the specified word to the tree.
	 * @param word
	 * @throws Exception
	 */
	public void insert(final String word) throws Exception {
		CheckUtils.checkNonEmpty(word, "word");
		root.insert(word);
	}
	
	/**
	 * Inserts the specified word to the tree.
	 * @param word
	 * @throws Exception
	 */
	public void insert(final Collection<String> words) throws Exception {
		CheckUtils.checkNonEmpty(words, "word");
		for (String word : words) {
			insert(word);
		}
	}

	/**
	 * Returns up to maxSize values from the tree.
	 * @param maxSize
	 * @return
	 * @throws Exception
	 */
	public Set<String> get(final int maxSize) throws Exception {
		return get(null, maxSize);
	}
	
	/**
	 * Returns up to maxSize values matching the specified prefix.
	 * @param prefix
	 * @param maxSize
	 * @return
	 * @throws Exception
	 */
	public Set<String> get(final String prefix, final int maxSize) throws Exception {
		Set<String> words;
		if (prefix == null) {
			words = root.getAll(new String(), maxSize);
		}
		else {	
			words = root.getAll(prefix, maxSize);
		}
		return words;
	}
	
	/**
	 * Simple function tester.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			WordTree tree = new WordTree();
			System.out.println("Adding {bat, cat, cart, cert}.");
			tree.insert("bat");
			tree.insert("cat");
			tree.insert("cat");
			tree.insert("cart");
			tree.insert("cert");
			System.out.println("All: " + StringUtils.concatenate(tree.get(16), ", "));
			System.out.println("b: " + StringUtils.concatenate(tree.get("b", 16), ", "));
			System.out.println("ba: " + StringUtils.concatenate(tree.get("ba", 16), ", "));
			System.out.println("bat: " + StringUtils.concatenate(tree.get("bat", 16), ", "));
			System.out.println("c: " + StringUtils.concatenate(tree.get("c", 16), ", "));
			System.out.println("ca: " + StringUtils.concatenate(tree.get("ca", 16), ", "));
			System.out.println("ce: " + StringUtils.concatenate(tree.get("ce", 16), ", "));
			System.out.println("car: " + StringUtils.concatenate(tree.get("car", 16), ", "));
			System.out.println("cert: " + StringUtils.concatenate(tree.get("cert", 16), ", "));
			System.out.println("carb: " + StringUtils.concatenate(tree.get("carb", 16), ", "));
			System.out.println("cr: " + StringUtils.concatenate(tree.get("cr", 16), ", "));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}


class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Map<Character, Node> children;
	private boolean is_word;
	
	public Node() {
		children = new HashMap<Character, Node>();
		is_word = false;
	}
	
	/**
	 * Inserts the remainder of the word to this subtree.
	 * @param word
	 * @throws Exception
	 */
	public void insert(final String word) throws Exception {
		CheckUtils.check(word, "word");
		
		// No more letters, mark this as a complete word.
		if (word.isEmpty()) {
			is_word = true;
			return;
		}

		Character letter = word.charAt(0);
		String subword = word.substring(1);
		if (!children.containsKey(letter)) {
			children.put(letter, new Node());
		}
		children.get(letter).insert(subword);
		
	}
	
	/**
	 * Returns whether or not this node terminated a word.
	 * @return
	 */
	public boolean isWord() {
		return is_word;
	}
	
	/**
	 * Returns the entire subtree as a list of strings.
	 * @param maxSize
	 * @return
	 * @throws Exception
	 */
	public Set<String> getAll(final String word, 
							   final int maxSize) throws Exception {
		Set<String> list = new HashSet<String>();
		
		// No more specifier, return the whole subtree.
		if (word.isEmpty()) {
			for (Character letter : children.keySet()) {
				Set<String> sublist = children.get(letter).getAll(word, maxSize);
				if (children.get(letter).isWord()) {
					sublist.add(new String(""));
				}
				for (String string : sublist) {
					list.add("" + letter + string);
					if (list.size() > maxSize) {
						return list;
					}
				}
			}	
		}
		// More definition, continue down the tree.
		else {
			Character letter = word.charAt(0);
			String subword = word.substring(1);
			
			// The next letter is not in the tree, no match.
			if (!children.containsKey(letter)) {
				return list;
			}
			
			Set<String> sublist = children.get(letter).getAll(subword, maxSize);			
			if (subword.isEmpty() && children.get(letter).isWord()) {
				sublist.add(new String(""));
			}
			for (String string : sublist) {
				list.add("" + letter + string);
				if (list.size() > maxSize) {
					return list;
				}
			}
		}
		return list;
	}
	
}

