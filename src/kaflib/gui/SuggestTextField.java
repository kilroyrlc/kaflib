package kaflib.gui;

import java.awt.Color;
import java.awt.Window;

import javax.swing.JTextField;

import kaflib.types.WordTrie;

/**
 * Defines a panel with a label and a text field with auto suggest.
 */
public class SuggestTextField extends JTextField {

	private static final long serialVersionUID = 187L;
	final WordTrie words;
	final Suggestor suggestor;
	
	/**
	 * Create the panel.
	 * @param defaultValue
	 * @param width
	 * @param window
	 * @param words
	 */
	public SuggestTextField(final String defaultValue, 
									final int width, 
									final Window window,
									final WordTrie words) throws Exception {
		super(defaultValue, width);
		this.words = words;
		
        suggestor = new Suggestor(this, 
			        			  window, 
			        			  this.words, 
			        			  Color.WHITE.brighter(), 
			        			  Color.BLUE, 
			        			  Color.RED, 
			        			  0.75f,
			        			  true);
		
	}
	
	/**
	 * Return the word tree.
	 * @return
	 */
	public WordTrie getWordTree() {
		return words;
	}

}
