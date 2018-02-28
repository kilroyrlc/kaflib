package kaflib.applications.mtg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import kaflib.types.Directory;
import kaflib.types.Matrix;
import kaflib.utils.FileUtils;
import kaflib.utils.StringUtils;

/**
 * Utilities for the mtg stuff.
 */
public class CardUtils {

	public static Set<Integer> getIDs(final File file) throws Exception {
		String text = FileUtils.readString(file, null);
		Set<Integer> ids = new HashSet<Integer>();
		Pattern pattern = Pattern.compile("^(\\d+)\\D.*", Pattern.MULTILINE);
		String chunks[] = text.split("multiverseid\\=");
		for (int i = 1; i < chunks.length; i++) {
			String chunk = StringUtils.truncateIf(chunks[i], 16);
			Matcher matcher = pattern.matcher(chunk);
			if (matcher.matches()) {
				 ids.add(Integer.valueOf(matcher.group(1)));
			}			
		}
		return ids;
	}
	
	public static void importScrapeBlobs() throws Exception {
		Set<Integer> ids = new HashSet<Integer>();
		File directory = null;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION ||
			chooser.getSelectedFiles() == null) {
			return;
		}
		for (File file : chooser.getSelectedFiles()) {
			ids.addAll(getIDs(file));
			if (directory == null) {
				directory = file.getParentFile();
			}
		}
		FileUtils.write(new File(directory, "ids.txt"), 
				        StringUtils.concatenate(ids, "\n"));
	}
	
	/**
	 * Import an excel file with cards that are owned, including gui file
	 * prompt.
	 * @throws Exception
	 */
	public static void importHaveFile() throws Exception {
		CardDatabase db = new CardDatabase();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JFileChooser chooser = new JFileChooser(db.getDeckDirectory());
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("List files", "xlsx"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION ||
			chooser.getSelectedFile() == null) {
			return;
		}
		List<String> names = readNameList(chooser.getSelectedFile());
		frame.setVisible(false);
		importHaveList(db, names, true);
	}
	
	/**
	 * Read a list of card names.
	 * @param excelFile
	 * @return
	 * @throws Exception
	 */
	public static List<String> readNameList(final File excelFile) throws Exception {
		Matrix<String> matrix = FileUtils.readXLSXSheet(excelFile, false);
		List<String> names = new ArrayList<String>();

		for (int i = 0; i < matrix.getRowCount(); i++) {
			if (matrix.hasValue(i, 0)) {
				names.add(matrix.get(i, 0).trim());
			}
		}
		return names;
	}
	
	/**
	 * Import a list of cards that are owned.
	 * @param db
	 * @param names
	 * @param print
	 * @throws Exception
	 */
	public static void importHaveList(final CardDatabase db, 
								      final List<String> names,
								      final boolean print) throws Exception {
		Set<String> missing = new HashSet<String>();
		for (String name : names) {
			if (!db.checkName(name)) {
				missing.add(name);
			}
		}
		if (missing.size() > 0) {
			System.out.println("" + missing.size() + " cards missing, database unchanged:\n" + 
							   StringUtils.concatenate(missing, "\n"));
			return;
		}
		if (print) {
			System.out.println("Marking " + names.size() + " cards as have.");
		}
		for (String name : names) {
			boolean had = db.have(name);
			db.setHave(name, true);
			
			if (print) {
				String line = StringUtils.resize(name + ":", 32);
				if (had) {
					line += "yes";
				}
				else {
					line += "no ";
				}
				
				line += " -> ";
				if (db.have(name)) {
					line += "yes";
				}
				else {
					line += "no ";
				}
				System.out.println(line);
			}
		}
		db.write();
	}
	
	/**
	 * Write an html file with all the cards in the list.
	 * @param db
	 * @param directory
	 * @param name
	 * @param names
	 * @throws Exception
	 */
	public static void generateHTML(final CardDatabase db,
							  final File directory, 
							  final String name, 
							  final List<String> names) throws Exception {
		CardList list = CardList.getList(db, names);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>\n<body>\n<table>\n   <tr>\n");
		int column = 0;
		for (Card card : list) {
			File image = db.getImage(card.getName());
			FileUtils.copy(new File(directory, image.getName()), image);
			buffer.append("      <td><img src=\"" + 
						  image.getName() + 
						  "\" alt=\"" + 
						  card.getName() + 
						  "\" border=\"0\" /></td>\n");
			column++;
			if (column == 4) {
				column = 0;
				buffer.append("   </tr>\n   <tr>\n");
			}
		}
		buffer.append("   </tr>\n</table>\n</body>\n</html>\n");
		FileUtils.write(new File(directory, name + ".html"), new String(buffer));
	}
	
	public static void generateText(final CardDatabase db,
									final File directory, 
									final String name, 
									final List<String> names) throws Exception {
		CardList list = CardList.getList(db, names);
		StringBuffer buffer = new StringBuffer();
		for (Card card : list) {
			buffer.append(card.getName() + "; ");
		}
		FileUtils.write(new File(directory, name + ".txt"), new String(buffer));
	}

	
	public static Map<String, CardInstance> getMergedCards(final CardDatabase db) throws Exception {
		Map<String, CardInstance> cards = new HashMap<String, CardInstance>();
		for (CardInstance instance : db) {
			if (instance.isInvalidOrForeign()) {
				continue;
			}
			
			if (!cards.containsKey(instance.getName())) {
				cards.put(instance.getName(), instance);
			}
			else {
				cards.put(instance.getName(), 
						  CardInstance.merge(cards.get(instance.getName()), instance));
			}
		}
		return cards;
	}

	/**
	 * Write an html file with all the cards in the list.
	 * @param db
	 * @param directory
	 * @param name
	 * @param names
	 * @throws Exception
	 */
	public static void generateRated(final CardDatabase db,
							  final Directory directory, 
							  final String name,
							  final int count,
							  final boolean reverse) throws Exception {
		// Create a map with all card instances merged.
		Map<String, CardInstance> cards = getMergedCards(db);
		
		List<CardInstance> artifacts = new ArrayList<CardInstance>();
		List<CardInstance> spells = new ArrayList<CardInstance>();
		List<CardInstance> land = new ArrayList<CardInstance>();
		List<CardInstance> enchantment = new ArrayList<CardInstance>();
		List<CardInstance> planeswalker = new ArrayList<CardInstance>();
		List<CardInstance> creature = new ArrayList<CardInstance>();
		
		for (CardInstance instance : cards.values()) {
			if (!instance.hasRating()) {
				continue;
			}
			
			if (Type.getTypes(instance.getTypes()).contains(Type.ARTIFACT)) {
				artifacts.add(instance);
				Collections.sort(artifacts, CardInstance.getRatingComparator());
				if (reverse) {
					Collections.reverse(artifacts);
				}
				artifacts = artifacts.subList(0, Math.min(count, artifacts.size()));
			}
			if (Type.getTypes(instance.getTypes()).contains(Type.INSTANT) ||
				Type.getTypes(instance.getTypes()).contains(Type.SORCERY)) {
				spells.add(instance);
				Collections.sort(spells, CardInstance.getRatingComparator());
				if (reverse) {
					Collections.reverse(spells);
				}
				spells = spells.subList(0, Math.min(count, spells.size()));
			}
			if (Type.getTypes(instance.getTypes()).contains(Type.LAND) &&
				!Type.getTypes(instance.getTypes()).contains(Type.BASIC_LAND)) {
				land.add(instance);
				Collections.sort(land, CardInstance.getRatingComparator());
				if (reverse) {
					Collections.reverse(land);
				}
				land = land.subList(0, Math.min(count, land.size()));
			}
			if (Type.getTypes(instance.getTypes()).contains(Type.ENCHANTMENT)) {
				enchantment.add(instance);
				Collections.sort(enchantment, CardInstance.getRatingComparator());
				if (reverse) {
					Collections.reverse(enchantment);
				}
				enchantment = enchantment.subList(0, Math.min(count, enchantment.size()));
			}
			if (Type.getTypes(instance.getTypes()).contains(Type.PLANESWALKER)) {
				planeswalker.add(instance);
				Collections.sort(planeswalker, CardInstance.getRatingComparator());
				if (reverse) {
					Collections.reverse(planeswalker);
				}
				planeswalker = planeswalker.subList(0, Math.min(count, planeswalker.size()));
			}
			if (Type.getTypes(instance.getTypes()).contains(Type.CREATURE)) {
				creature.add(instance);
				Collections.sort(creature, CardInstance.getRatingComparator());
				if (reverse) {
					Collections.reverse(creature);
				}
				creature = creature.subList(0, Math.min(count, creature.size()));
			}
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>\n<body>\n");
		buffer.append(getTable(db, "Lands", directory, land));
		buffer.append(getTable(db, "Planeswalkers", directory, planeswalker));
		buffer.append(getTable(db, "Creatures", directory, creature));
		buffer.append(getTable(db, "Artifacts", directory, artifacts));
		buffer.append(getTable(db, "Enchantments", directory, enchantment));
		buffer.append(getTable(db, "Spells", directory, spells));
		buffer.append("</body>\n</html>\n");
		FileUtils.write(new File(directory, name + ".html"), new String(buffer));
	}
	
	public static String getTable(final CardDatabase db,
								  final String title,
								  final Directory directory,
						 		  final List<CardInstance> list) throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table>\n");
		buffer.append("   <tr><th colspan=\"4\">" + title + "</th></tr>\n   <tr>\n");
		int column = 0;
		for (CardInstance card : list) {
			if (card.isInvalidOrForeign() || !card.hasRating()) {
				System.err.println("Snuck through:\n" + card.toString());
				continue;
			}
			
			File image = db.getRandomImage(card.getName());
			FileUtils.copy(new File(directory, image.getName()), image);
			buffer.append("      <td><img src=\"" + 
						  image.getName() + 
						  "\" alt=\"" + 
						  card.getName() + 
						  "\" border=\"0\" /><br>\n" +
						  card.getRating() + " (" + card.getVotes() + ")</td>");
			column++;
			if (column == 4) {
				column = 0;
				buffer.append("   </tr>\n   <tr>\n");
			}
		}
		buffer.append("   </tr>\n</table>\n\n");
		return buffer.toString();
	}
	
	/**
	 * Generate an html file with a table of all have cards.
	 * @param db
	 * @param directory
	 * @throws Exception
	 */
	public static void generateHaveHTML(final CardDatabase db,
										final File directory) throws Exception {
		List<String> names = new ArrayList<String>();
		names.addAll(db.getHaves());
		Collections.sort(names);
		StringBuffer buffer = new StringBuffer();
		buffer.append("<html>\n<body>\n<table>\n");
		String bgcolor = "bgcolor = \"#eeeeee\"";
		
		for (String name : names) {
			if (bgcolor.equals("bgcolor = \"#eeeeee\"")) {
				bgcolor = "bgcolor = \"#dddddd\"";
			}
			else {
				bgcolor = "bgcolor = \"#eeeeee\"";
			}
			
			buffer.append("   <tr " + bgcolor + "><td>");

			Integer id = db.getIDs(name).iterator().next();
			if (id != null) {
				buffer.append("<a href=\"http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=" + id + "\">");
			}
			buffer.append(name);
			if (id != null) {
				buffer.append("</a>");
			}
			buffer.append("</td></tr>");
		}
		buffer.append("   </tr>\n</table>\n</body>\n</html>\n");
		FileUtils.write(new File(directory, "havelist.html"), new String(buffer));
	}
	
	
	public static void main(String args[]) {
		try {
			//CardDatabase db = new CardDatabase();
			//generateHaveHTML(db, db.getDeckDirectory());
			//generateRated(db, db.getTempDirectory(), "top", 32, false);
			//generateRated(db, db.getTempDirectory(), "bottom", 32, true);
			//importHaveFile();
			importScrapeBlobs();
			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
