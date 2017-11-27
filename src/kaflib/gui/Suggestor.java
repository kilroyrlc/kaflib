package kaflib.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import kaflib.types.CachedWordTree;
import kaflib.types.WordTree;

/**
 * Derived from Dave.
 */
public class Suggestor {
	public static final int MAX_SUGGESTIONS = 16;
	public static final int WORD_TREE_DEFAULT_CACHE_SIZE = 64;
	
	private final JTextField text_field;
	private final Window container;
	private JPanel suggestion_panel;
	private JWindow suggestion_popup;
	private WordTree suggestions;
	private int tW;
	private int tH;
	
	private KeyListener keyListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
			checkForAndShowSuggestions();		
		}
		
	};
	private DocumentListener documentListener = new DocumentListener() {
		@Override
		public void insertUpdate(DocumentEvent de) {
			checkForAndShowSuggestions();
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			checkForAndShowSuggestions();
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
		}
	};
	private final Color suggestionsTextColor;
	private final Color suggestionFocusedColor;

	public Suggestor(final JTextField textField, 
			 		 final Window mainWindow, 
			 		 final WordTree suggestions,
			 		 final boolean justKeyEvents) {
		this(textField, 
			 mainWindow, 
			 suggestions, 
			 Color.WHITE.brighter(), 
			 Color.BLUE, 
	  		 Color.RED, 
	  		 0.75f,
	  		 justKeyEvents);
	}
	
	public Suggestor(final JTextField textField, 
					 final Window mainWindow, 
					 final WordTree suggestions, 
					 final Color popUpBackground, 
					 final Color textColor, 
					 final Color suggestionFocusedColor, 
					 final float opacity,
					 final boolean justKeyEvents) {
		this.text_field = textField;
		this.suggestionsTextColor = textColor;
		this.container = mainWindow;
		this.suggestionFocusedColor = suggestionFocusedColor;
		
		if (justKeyEvents) {
			text_field.addKeyListener(keyListener);
		}
		else {
			text_field.getDocument().addDocumentListener(documentListener);
		}
		
		this.suggestions = suggestions;

		tW = 0;
		tH = 0;

		suggestion_popup = new JWindow(mainWindow);
		suggestion_popup.setOpacity(opacity);

		suggestion_panel = new JPanel();
		suggestion_panel.setLayout(new GridLayout(0, 1));
		suggestion_panel.setBackground(popUpBackground);

		addKeyBindingToRequestFocusInPopUpWindow();
	}

	private void addKeyBindingToRequestFocusInPopUpWindow() {
		text_field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "Down released");
		text_field.getActionMap().put("Down released", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0; i < suggestion_panel.getComponentCount(); i++) {
					if (suggestion_panel.getComponent(i) instanceof SuggestionLabel) {
						((SuggestionLabel) suggestion_panel.getComponent(i)).setFocused(true);
						suggestion_popup.toFront();
						suggestion_popup.requestFocusInWindow();
						suggestion_panel.requestFocusInWindow();
						suggestion_panel.getComponent(i).requestFocusInWindow();
						break;
					}
				}
			}
		});
		suggestion_panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "Down released");
		suggestion_panel.getActionMap().put("Down released", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			int lastFocusableIndex = 0;

			@Override
			public void actionPerformed(ActionEvent ae) {//allows scrolling of labels in pop window (I know very hacky for now :))

				ArrayList<SuggestionLabel> sls = getAddedSuggestionLabels();
				int max = sls.size();

				if (max > 1) {//more than 1 suggestion
					for (int i = 0; i < max; i++) {
						SuggestionLabel sl = sls.get(i);
						if (sl.isFocused()) {
							if (lastFocusableIndex == max - 1) {
								lastFocusableIndex = 0;
								sl.setFocused(false);
								suggestion_popup.setVisible(false);
								setFocusToTextField();
								checkForAndShowSuggestions();//fire method as if document listener change occured and fired it

							} else {
								sl.setFocused(false);
								lastFocusableIndex = i;
							}
						} else if (lastFocusableIndex <= i) {
							if (i < max) {
								sl.setFocused(true);
								suggestion_popup.toFront();
								suggestion_popup.requestFocusInWindow();
								suggestion_panel.requestFocusInWindow();
								suggestion_panel.getComponent(i).requestFocusInWindow();
								lastFocusableIndex = i;
								break;
							}
						}
					}
				} else {//only a single suggestion was given
					suggestion_popup.setVisible(false);
					setFocusToTextField();
					checkForAndShowSuggestions();//fire method as if document listener change occured and fired it
				}
			}
		});
	}

	private void setFocusToTextField() {
		container.toFront();
		container.requestFocusInWindow();
		text_field.requestFocusInWindow();
	}

	public ArrayList<SuggestionLabel> getAddedSuggestionLabels() {
		ArrayList<SuggestionLabel> sls = new ArrayList<>();
		for (int i = 0; i < suggestion_panel.getComponentCount(); i++) {
			if (suggestion_panel.getComponent(i) instanceof SuggestionLabel) {
				SuggestionLabel sl = (SuggestionLabel) suggestion_panel.getComponent(i);
				sls.add(sl);
			}
		}
		return sls;
	}

	private void checkForAndShowSuggestions() {
		suggestion_panel.removeAll();
		tW = 0;
		tH = 0;

		boolean added = wordTyped(text_field.getText());

		if (!added) {
			if (suggestion_popup.isVisible()) {
				suggestion_popup.setVisible(false);
			}
		} else {
			showPopUpWindow();
			setFocusToTextField();
		}
	}

	protected void addWordToSuggestions(final String word) {
		SuggestionLabel suggestionLabel = new SuggestionLabel(word, suggestionFocusedColor, suggestionsTextColor, this);
		calculatePopUpWindowSize(suggestionLabel);
		suggestion_panel.add(suggestionLabel);
	}
	
	protected void addWordsToSuggestions(final Collection<String> words) {
		for (String word : words) {
			addWordToSuggestions(word);
		}
	}

	private void calculatePopUpWindowSize(JLabel label) {
		if (tW < label.getPreferredSize().width) {
			tW = label.getPreferredSize().width;
		}
		tH += label.getPreferredSize().height;
	}

	private void showPopUpWindow() {
		suggestion_popup.getContentPane().add(suggestion_panel);
		suggestion_popup.setMinimumSize(new Dimension(text_field.getWidth(), 30));
		suggestion_popup.setSize(tW, tH);
		suggestion_popup.setVisible(true);

		int windowX = 0;
		int windowY = 0;

		windowX = container.getX() + text_field.getX() + 5;
		if (suggestion_panel.getHeight() > suggestion_popup.getMinimumSize().height) {
			windowY = container.getY() + text_field.getY() + text_field.getHeight() + suggestion_popup.getMinimumSize().height;
		} else {
			windowY = container.getY() + text_field.getY() + text_field.getHeight() + suggestion_popup.getHeight();
		}

		suggestion_popup.setLocation(windowX, windowY);
		suggestion_popup.setMinimumSize(new Dimension(text_field.getWidth(), 30));
		suggestion_popup.revalidate();
		suggestion_popup.repaint();

	}

	/**
	 * Updates the suggestions word tree.
	 * @param suggestions
	 */
	public void setSuggestions(final Collection<String> suggestions) throws Exception {
		this.suggestions = new CachedWordTree(suggestions, WORD_TREE_DEFAULT_CACHE_SIZE);
	}
	
	/**
	 * Updates the suggestions word tree.
	 * @param suggestions
	 */
	public void setSuggestions(final CachedWordTree suggestions) {
		this.suggestions = suggestions;
	}

	
	public JWindow getAutoSuggestionPopUpWindow() {
		return suggestion_popup;
	}

	public Window getContainer() {
		return container;
	}

	public JTextField getTextField() {
		return text_field;
	}

	public void addSuggestion(final String word) throws Exception {
		suggestions.insert(word);
	}

	private boolean wordTyped(final String typedWord) {
		
		if (typedWord.isEmpty()) {
			return false;
		}

		try {
			List<String> matches = suggestions.getOrdered(typedWord, WORD_TREE_DEFAULT_CACHE_SIZE);
			if (matches.size() > MAX_SUGGESTIONS) {
				matches = matches.subList(0, MAX_SUGGESTIONS);
			}
			if (matches.size() > 0) {
				addWordsToSuggestions(matches);
				return true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	try {
	                JFrame frame = new JFrame();
	    	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	    	        JTextField f = new JTextField(16);
	
	    	        CachedWordTree tree = new CachedWordTree(64);
	    	        String string = new String("hammer of the gods will drive my ship to new lands fight the horde sing and cry valhallah i am coming");
	    	        tree.insert(Arrays.asList(string.split("\\s")));
	    	        tree.accessed("horde");
	    	        tree.accessed("the");
	    	        tree.accessed("to");
	    	        tree.accessed("the");
	    	        tree.accessed("and");
	    	        tree.accessed("hammer");
	    	        
	    	        new Suggestor(f, frame, tree, Color.WHITE.brighter(), Color.BLUE, Color.RED, 0.75f, true);
	    	        JPanel p = new JPanel();
	
	    	        p.add(f);
	
	    	        frame.add(p);
	
	    	        frame.pack();
	    	        frame.setVisible(true);
            	}
            	catch (Exception e) {
            		e.printStackTrace();
            	}
            }
        });
	}
}

class SuggestionLabel extends JLabel {

	private static final long serialVersionUID = 1L;
	private boolean focused = false;
	private final JWindow window;
	private final JTextField input_field;
	private Color text_color;
	private Color border_color;

	public SuggestionLabel(String string, final Color borderColor, Color suggestionsTextColor, Suggestor autoSuggestor) {
		super(string);
		this.text_color = suggestionsTextColor;
		this.input_field = autoSuggestor.getTextField();
		this.border_color = borderColor;
		this.window = autoSuggestor.getAutoSuggestionPopUpWindow();

		initComponent();
	}

	private void initComponent() {
		setFocusable(true);
		setForeground(text_color);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				super.mouseClicked(me);
				input_field.setText(getText());
				window.setVisible(false);
			}
		});

		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "Enter released");
		getActionMap().put("Enter released", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ae) {
				input_field.setText(getText());
				window.setVisible(false);
			}
		});
	}

	public void setFocused(boolean focused) {
		if (focused) {
			setBorder(new LineBorder(border_color));
		} else {
			setBorder(null);
		}
		repaint();
		this.focused = focused;
	}

	public boolean isFocused() {
		return focused;
	}

}
