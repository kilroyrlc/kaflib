package kaflib.gui;

import java.awt.Color;
import java.awt.Window;

import kaflib.types.WordTree;

/**
 * Defines a panel with a label and a text field with auto suggest.
 */
public class SuggestTextFieldPanel extends TextFieldPanel {

	private static final long serialVersionUID = 187L;
	final WordTree words;
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
									final WordTree words) {
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
	public WordTree getWordTree() {
		return words;
	}

}
