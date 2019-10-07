package kaflib.gui.components;

import java.awt.Color;
import java.awt.Window;

import kaflib.gui.Suggestor;
import kaflib.types.WordTrie;

/**
 * Defines a panel with a label and a text field with auto suggest.
 */
public class SuggestTextFieldPanel extends TextFieldPanel {

	private static final long serialVersionUID = 187L;
	final WordTrie words;
	final Suggestor suggestor;
	
	/**
	 * Create the panel.
	 * @param label
	 * @param width
	 * @param window
	 * @param words
	 */
	protected SuggestTextFieldPanel(final String label, 
									final int width, 
									final Window window,
									final WordTrie words) throws Exception {
		super(label, width);
		this.words = words;
		
        suggestor = new Suggestor(getField(), 
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
