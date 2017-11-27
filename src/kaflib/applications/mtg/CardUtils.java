package kaflib.applications.mtg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import kaflib.types.Matrix;
import kaflib.utils.FileUtils;
import kaflib.utils.StringUtils;

public class CardUtils {

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
	
	public static void main(String args[]) {
		try {
			importHaveFile();
			System.exit(0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
