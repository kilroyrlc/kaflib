package kaflib.types;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaflib.utils.DateUtils;
import kaflib.utils.FileUtils;

/**
 * Defines a convenience class that holds a string/date map, with file IO.
 */
public class DateStore {

	private final Map<String, Date> map;
	
	public DateStore() {
		map = new HashMap<String, Date>();
	}
	
	public DateStore(final File xlsx) throws Exception {
		if (xlsx != null && xlsx.exists()) {
			Matrix<String> matrix = FileUtils.readXLSXSheet(xlsx, false);
		
			if (matrix.getColumnCount() != 2) {
				throw new Exception("Invalid data file: " + xlsx + ", too many columns.");
			}
			
			map = new HashMap<String, Date>(matrix.getRowCount());
	
			for (int i = 0; i < matrix.getRowCount(); i++) {
				map.put(matrix.get(i, 0), DateUtils.getDate(matrix.get(i, 1)));
			}
		}
		else {
			map = new HashMap<String, Date>();
		}
	}

	public Date get(final String key) {
		return map.get(key);
	}
	
	public boolean contains(final String key) {
		return map.containsKey(key);
	}
	
	public Set<String> keySet() {
		return map.keySet();
	}
	
	public void put(final String key, final Date value) {
		map.put(key, value);
	}
	
	/**
	 * Updates the value to the newest date (supplied or existing).
	 * @param key
	 * @param value
	 */
	public void updateNewest(final String key, final Date value) {
		if (!map.containsKey(key)) {
			put(key, value);
		}
		Date existing = map.get(key);
		if (existing.before(value)) {
			put(key, value);
		}
	}
	
	public void toXLSX(final File file) throws Exception {
		Matrix<String> matrix = new Matrix<String>();
		
		for (String string : map.keySet()) {
			List<String> values = new ArrayList<String>();
			values.add(string);
			values.add(DateUtils.getYYYYMMDD(map.get(string)));
			matrix.addRow(values);
		}
		matrix.toXLSX(file);
	}
	
}
