package kaflib.applications.mtg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kaflib.types.Matrix;
import kaflib.utils.FileUtils;
import kaflib.utils.StringUtils;
import kaflib.utils.TypeUtils;

/**
 * Defines a deck type.  Contains card lists and format.
 */
public class Deck {
	public enum Format {
		CUBE,
		DRAFT,
		EDH,
		EDH_PRECON,
		SEALED
	}

	
	private final Format format;
	private final String name;
	private final int version;
	private final List<String> cards;
	private final List<String> sideboard;
	private final List<String> want;
	
	/**
	 * Creates a new deck.
	 * @param format
	 * @param name
	 * @throws Exception
	 */
	public Deck(final Format format, final String name) throws Exception {
		this.name = name;
		this.version = 0;
		this.format = format;
		
		cards = new ArrayList<String>();
		sideboard = new ArrayList<String>();
		want = new ArrayList<String>();
	}
	
	/**
	 * Loads a deck from an excel file.
	 * @param excel
	 * @throws Exception
	 */
	public Deck(final File excel) throws Exception {
		Matrix<String> matrix = FileUtils.readXLSXSheet(excel, true);
		Pattern pattern = Pattern.compile("^(.*)_(\\w+)_v(\\d+)\\.xlsx$");
		Matcher matcher = pattern.matcher(excel.getName());
		if (!matcher.matches()) {
			throw new Exception("File doesn't match deck format.");
		}
		name = matcher.group(1);
		format = getFormat(matcher.group(2));
		version = Integer.valueOf(matcher.group(3));	
		cards = new ArrayList<String>();
		sideboard = new ArrayList<String>();
		want = new ArrayList<String>();
		
		for (int i = 0; i < matrix.getRowCount(); i++) {
			if (matrix.hasValue(i, 0)) {
				cards.add(matrix.get(i, 0));
			}
			if (matrix.hasValue(i, 1)) {
				sideboard.add(matrix.get(i, 0));
			}
			if (matrix.hasValue(i, 2)) {
				want.add(matrix.get(i, 0));
			}

		}
	}
	
	/**
	 * Returns the deck name.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the file name corresponding to this deck name.
	 * @return
	 */
	private String getFilename() {
		String v = String.format("%02d", version);
		
		return name + "_" + format.name().toLowerCase() + "_v" + v + ".xlsx";
	}
	
	/**
	 * Returns the deck cards.
	 * @return
	 */
	public List<String> getCards() {
		return cards;
	}
	
	/**
	 * Returns the sideboard.
	 * @return
	 */
	public List<String> getSideboard() {
		return sideboard;
	}
	
	/**
	 * Returns desired cards.
	 * @return
	 */
	public List<String> getWant() {
		return want;
	}
	
	/**
	 * Adds a new card name to the deck.
	 * @param name
	 */
	public void add(final String name) {
		cards.add(name);
	}
	
	/**
	 * Adds a new card name to the sideboard.
	 * @param name
	 */
	public void addSideboard(final String name) {
		sideboard.add(name);
	}
	
	/**
	 * Adds a new card name to the want list.
	 * @param name
	 */
	public void addWant(final String name) {
		want.add(name);
	}

	/**
	 * Writes the deck to a spreadsheet.
	 * @param directory
	 * @throws Exception
	 */
	public void toXLSX(final File directory) throws Exception {
		Matrix<String> matrix = new Matrix<String>();
		matrix.setColumnLabels("Cards", "Sideboard", "Want");
		for (String card : cards) {
			matrix.addRow(TypeUtils.getList(card));
		}
		int i = 0;
		for (String card : sideboard) {
			matrix.set(i, 1, card);
			i++;
		}
		i = 0;
		for (String card : want) {
			matrix.set(i, 2, card);
			i++;
		}
		matrix.toXLSX(new File(directory, getFilename()));
	}
	
	/**
	 * Returns the deck as a list.
	 * @param separator
	 * @return
	 * @throws Exception
	 */
	public String getList(final char separator) throws Exception {
		return StringUtils.concatenate(cards, "; ", false);
	}
	
	/**
	 * Converts a string to format type.
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static Format getFormat(final String format) throws Exception {
		if (format.equals(Format.CUBE.name().toLowerCase())) {
			return Format.CUBE;
		}
		else if (format.equals(Format.DRAFT.name().toLowerCase())) {
			return Format.DRAFT;
		}
		else if (format.equals(Format.EDH.name().toLowerCase())) {
			return Format.EDH;
		}
		else if (format.equals(Format.EDH_PRECON.name().toLowerCase())) {
			return Format.EDH_PRECON;
		}
		else if (format.equals(Format.SEALED.name().toLowerCase())) {
			return Format.SEALED;
		}
		else {
			throw new Exception("Unrecognized format: " + format + ".");
		}
	}
}
