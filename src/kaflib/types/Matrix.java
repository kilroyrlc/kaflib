package kaflib.types;

/*
 * Copyright (c) 2015 Christopher Ritchie
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import kaflib.types.Direction.Cardinal;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.MathUtils;
import kaflib.utils.StringUtils;
import kaflib.utils.TypeUtils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVWriter;

/**
 * Defines a 2d array template type.
 */
public class Matrix<T> implements MatrixNavigator<T> {
	
	private ArrayList<String> columnLabels;
	private ArrayList<ArrayList<T>> matrix;
	
	/**
	 * Creates a matrix.
	 */
	public Matrix() {
		matrix = new ArrayList<ArrayList<T>>();
		columnLabels = null;
	}
	
	/**
	 * Creates a matrix with the specified column labels.
	 * @param columnLabels
	 * @throws Exception
	 */
	public Matrix(final String... columnLabels) throws Exception {
		matrix = new ArrayList<ArrayList<T>>();
		setColumnLabels(columnLabels);
	}
	
	/**
	 * Creates a matrix with the specified column labels.
	 * @param columnLabels
	 * @throws Exception
	 */
	public Matrix(final List<String> columnLabels) throws Exception {
		matrix = new ArrayList<ArrayList<T>>();
		setColumnLabels(columnLabels);
	}

	/**
	 * Sets the column labels for the matrix.
	 * @param labels
	 * @throws Exception
	 */
	public void setColumnLabels(final List<String> labels) throws Exception {
		columnLabels = new ArrayList<String>(labels);
	}
	
	/**
	 * Sets the column labels for the matrix.
	 * @param labels
	 * @throws Exception
	 */
	public void setColumnLabels(final String... labels) throws Exception {
		columnLabels = new ArrayList<String>();
		for (String label : labels) {
			columnLabels.add(label);
		}
	}
	
	/**
	 * Gets all values in the specified column.
	 * @param column
	 * @return
	 * @throws Exception
	 */
	public List<T> getColumn(final int column) throws Exception {
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < getRowCount(); i++) {
			if (hasValue(i, column)) {
				list.add(get(i, column));
			}
			else {
				list.add(null);
			}
		}
		return list;
	}
	
	/**
	 * Sets the specified matrix cell to the given value.  Pads empty values
	 * until the specified row and column is reached.
	 * @param row
	 * @param column
	 * @param value
	 * @throws Exception
	 */
	public void set(final int row, final int column, T value) throws Exception {
		CheckUtils.checkNonNegative(row);
		CheckUtils.checkNonNegative(column);

		while (row >= matrix.size()) {
			matrix.add(new ArrayList<T>());
		}
		List<T> target_row = matrix.get(row);
		
		while (column >= target_row.size()) {
			target_row.add(null);
		}
		target_row.set(column, value);
	}
	
	/**
	 * Returns whether or not the given index is within range.
	 * @param row
	 * @param column
	 * @return
	 */
	public boolean hasValue(final int row, final int column) {
		if (row < 0 || row >= matrix.size()) {
			return false;
		}
		if (column < 0 || column >= matrix.get(row).size()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the column labels, may be null.
	 * @return
	 */
	public List<String> getColumnLabels() {
		return columnLabels;
	}
	
	/**
	 * Returns the value at the given row and column.  Can be a null value.
	 * Note the matrix is jagged unless square() is called.
	 * @param row
	 * @param column
	 * @return
	 * @throws Exception
	 */
	public T get(final int row, final int column) throws Exception {
		// If the indices are out of range, throw with some helpful 
		// information.
		if (!hasValue(row, column)) {
			String r = "";
			
			if (row < getRowCount()) {
				r = StringUtils.concatenate(matrix.get(row), " | ");
			}
			
			throw new Exception("Row: " + row + ", column: " + column + 
								" out of range for matrix size: " + 
								getSizeString() + ".\n" + r);
		}
		
		return matrix.get(row).get(column);
	}

	/**
	 * Adds an empty row of data to the end of matrix.
	 * @param row
	 */
	public void addRow() {
		matrix.add(new ArrayList<T>());
	}

	/**
	 * Adds an empty row of data to the end of matrix.
	 * @param row
	 */
	public void addRow(final int size) {
		ArrayList<T> row = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			row.add(null);
		}
		addRow(row);
	}
	
	/**
	 * Adds the specified row of data to the end of matrix.
	 * @param row
	 */
	public void addRow(final List<T> row) {
		matrix.add(new ArrayList<T>(row));
	}
	
	/**
	 * Appends a matrix to this one.
	 * @param matrix
	 */
	public void add(final Matrix<T> matrix) throws Exception {
		for (int i = 0; i < matrix.getRowCount(); i++) {
			addRow(matrix.getRow(i));
		}
	}
	
	/**
	 * Pads all columns with null such that each row is the same length.
	 */
	public void square() { 
		int columns = getColumnCount();
		
		for (List<T> row : matrix) {
			while (row.size() < columns) {
				row.add(null);
			}
		}
	}
	
	/**
	 * Returns a string representation of the matrix size.
	 * @return
	 */
	public String getSizeString() {
		return getRowCount() + " rows, " + getColumnCount() + " columns";
	}

	/**
	 * Returns the number of rows in the matrix.
	 * @return
	 */
	public int getRowCount() {
		return matrix.size();
	}
	
	/**
	 * Returns the largest number of columns in any row.
	 * @return
	 */
	public int getColumnCount() {
		int max = 0;
		for (List<T> row : matrix) {
			max = Math.max(max, row.size());
		}
		return max;
	}

	/**
	 * Returns the index of the specified label, null if it does not exist.
	 * @param label
	 * @return
	 */
	public Integer getColumnIndex(final String label) {
		if (columnLabels == null) {
			return null;
		}
		for (int i = 0; i < columnLabels.size(); i++) {
			if (columnLabels.get(i).equals(label)) {
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Returns the index-th row of the matrix.
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public List<T> getRow(final int index) throws Exception {
		CheckUtils.checkWithin(index, matrix);
		return matrix.get(index);
	}

	
	/**
	 * Writes the matrix to a single worksheet Excel file.  Each object is
	 * represented by its toString() value.
	 * @param file
	 * @param worksheet
	 * @throws Exception
	 */
	public static <T> void toXLSX(final File file, 
								  final Matrix<T> worksheet) throws Exception {
		toXLSX(file, new Pair<String, Matrix<T>>("main", worksheet));
	}

	public static void toEmptyXLSX(final File file) throws Exception {
		Matrix<String> matrix = new Matrix<String>();
		toXLSX(file, matrix);
	}
	
	/**
	 * Writes the specified matrices to file as worksheets.  Each object is
	 * represented by its toString() value.
	 * @param file
	 * @param worksheets
	 * @throws Exception
	 */
	@SafeVarargs
	public static <T> void toXLSX(final File file, 
								  final Pair<String, Matrix<T>>... worksheets) throws Exception {
		
		Workbook workbook = new XSSFWorkbook();
		
		for (Pair<String, Matrix<T>> worksheet : worksheets) {
			Sheet sheet = workbook.createSheet(worksheet.getFirst());
			Matrix<T> matrix = worksheet.getSecond();
			
			int i = 0;
			if (matrix.getColumnLabels() != null && matrix.getColumnLabels().size() > 0) {
				Row row = sheet.createRow(i);
				int j = 0;
				for (String string : matrix.getColumnLabels()) {
					row.createCell(j).setCellValue(string);
					j++;
				}
				i++;
			}
		
			for (int mrow = 0; mrow < matrix.getRowCount(); mrow++) {
				Row row = sheet.createRow(i);
				int j = 0;
				for (T t : matrix.getRow(mrow)) {
					if (t != null) {
						row.createCell(j).setCellValue(t.toString());
					}
					else {
						row.createCell(j).setCellValue("");
					}
					j++;
				}
				i++;
			}
			
		}
		
		FileOutputStream stream = new FileOutputStream(file);
		workbook.write(stream);
		stream.close();
		
		workbook.close();
		
	}
	
	/**
	 * Writes this matrix to an Excel file.
	 * @param file
	 * @throws Exception
	 */
	public void toXLSX(final File file) throws Exception {
		Matrix.toXLSX(file, this);
	}
	
	/**
	 * Outputs the matrix to a csv.
	 * @throws Exception
	 */
	public void toCSV(final File file) throws Exception {
		FileUtils.deleteIf(file);
		CSVWriter writer = null;
		
		try {
			writer = new CSVWriter(new FileWriter(file));
			
			if (columnLabels != null) {
				writer.writeNext(columnLabels.toArray(new String[columnLabels.size()]));
			}
			
			for (List<T> row : matrix) {
				writer.writeNext(row.toArray(new String[row.size()]));
			}
			writer.close();
		}
		catch (Exception e) {
			if (writer != null) {
				writer.close();
			}
			throw e;
		}
	}
	
	/**
	 * Returns a string representation of the matrix - up to maxRows
	 * rows of the toString() values of the elements.
	 * @param maxRows
	 * @return
	 */
	public String toString(int maxRows) throws Exception {
		// Get the column widths.
		List<Integer> widths = new ArrayList<Integer>();
		
		if (getRowCount() > 0) {
			for (int column = 0; column < getColumnCount(); column++) {
				int max_length = 5;
				
				for (int row = 0; row < getRowCount(); row++) {
					if (hasValue(row, column) && get(row, column) != null) {
						max_length = Math.max(max_length, get(row, column).toString().length());
					}
				}
				widths.add(max_length);
			}
		}
		else if (columnLabels != null) {
			for (String string : columnLabels) {
				widths.add(string.length());
			}
		}
		else {
			return "[No results or labels]";
		}
		
		return toString(maxRows, widths);
	}
		
	/**
	 * Returns a string representation of the matrix - up to maxRows
	 * rows of the toString() values of the elements.  Each column is padded/
	 * truncated to the specified width.
	 * @param maxRows
	 * @param columnWidth
	 * @return
	 * @throws Exception
	 */
	public String toString(int maxRows, int columnWidth) throws Exception {
		return toString(maxRows, TypeUtils.getList(getColumnCount(), columnWidth));
	}
	
	/**
	 * Returns a formatted representation of the column labels.
	 * @param columnWidths
	 * @return
	 * @throws Exception
	 */
	private String getColumnLabels(List<Integer> columnWidths) throws Exception {
		if (columnLabels == null) {
			return "";
		}
		if (columnWidths.size() != columnLabels.size()) {
			throw new Exception("Column widths: " + columnWidths.size() + " != " +
							    "column labels: " + columnLabels.size() + ".");
		}
		
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < columnLabels.size(); i++) {
			buffer.append(" | " + StringUtils.resize(columnLabels.get(i).toString(), columnWidths.get(i)));
		}
		buffer.append(" |\n");
		
		buffer.append(" " + StringUtils.toString('=', 
				   MathUtils.sum(columnWidths) + columnWidths.size() * 3 + 1) + 
				   "\n");
		return new String(buffer);
	}
	
	/**
	 * Returns the row as a vertical list of key-value.
	 * @param row
	 * @return
	 * @throws Exception
	 */
	public String toRowString(final int row) throws Exception {
		CheckUtils.checkWithin(row, matrix);
		StringBuffer buffer = new StringBuffer();
		buffer.append("Row " + row + ":\n");
		for (int i = 0; i < getColumnCount(); i++) {
			buffer.append("   ");
			if (columnLabels != null && columnLabels.size() == getColumnCount()) {
				buffer.append(columnLabels.get(i) + ": ");
			}
			else {
				buffer.append("Column " + i + ": ");
			}
			if (hasValue(row, i) && get(row, i) != null) {
				buffer.append(get(row, i).toString().replace('\n', ' ').replace('\r', ' '));
			}
			else {
				buffer.append("[no value]");
			}
			buffer.append("\n");

		}
		return new String(buffer);
	}
	
	/**
	 * Returns a string representation of the matrix - up to maxRows
	 * rows of the toString() values of the elements.
	 * @param maxRows
	 * @param columnWidth
	 * @return
	 */
	public String toString(final int maxRows, 
						   final List<Integer> columnWidths) throws Exception {
		if (getRowCount() > 0 && columnWidths.size() != getColumnCount()) {
			throw new Exception("Column counts do not match: " + columnWidths.size() + " != " + getColumnCount() + ".");
		}
		for (Integer i : columnWidths) {
			if (i < 0) {
				throw new Exception("Invalid column width: " + columnWidths.get(i) + ".");
			}
		}
		
		String hr = " " + StringUtils.toString('-', 
											   MathUtils.sum(columnWidths) + columnWidths.size() * 3 + 1) + 
					"\n";
		
		StringBuffer buffer = new StringBuffer();			
		buffer.append(hr);
		int count = 0;
		
		buffer.append(getColumnLabels(columnWidths));
		
		for (List<T> row : matrix) {
			for (int i = 0; i < getColumnCount(); i++) {
				if (i < row.size() && row.get(i) != null) {
					buffer.append(" | " + StringUtils.resize(row.get(i).toString(), columnWidths.get(i)));
				}
				else {
					buffer.append(" | " + StringUtils.toString(' ', columnWidths.get(i)));
				}
			}
			buffer.append(" |\n");
			buffer.append(hr);
			
			count++;
			if (maxRows > 0 && count > maxRows) {
				buffer.append(matrix.size() - count + " more rows.");
				break;
			}
		}
		return new String(buffer);
	}

	/**
	 * Creates a two-column String matrix from the given map.  Null values
	 * default to "[null]".
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static <K, V> Matrix<String> createMatrix(final Map<K, V> map) throws Exception {
		return createMatrix(map, "[null]");
	}
	
	/**
	 * Creates a two-column String matrix from the given map.
	 * @param map
	 * @param nullValue
	 * @return
	 * @throws Exception
	 */
	public static <K, V> Matrix<String> createMatrix(final Map<K, V> map,
													 final String nullValue) throws Exception {
		Matrix<String> matrix = new Matrix<String>();
		int i = 0;
		for (K k : map.keySet()) {
			matrix.set(i, 0, k.toString());
			if (map.get(k) == null) {
				matrix.set(i, 1, nullValue);
			}
			else {
				matrix.set(i, 1, map.get(k).toString());
			}
			i++;
		}
		return matrix;
	}
	
	/**
	 * Creates a single-column matrix.
	 * @param values
	 * @return
	 */
	public static <T> Matrix<T> createMatrix(final Collection<T> values) {
		Matrix<T> matrix = new Matrix<T>();
		for (T value : values) {
			matrix.addRow(TypeUtils.getList(value));
		}
		return matrix;
	}

	@Override
	public T getNeighbor(Coordinate me, Cardinal direction) throws Exception {
		int column = me.getX() + Direction.getOffset(direction).getX();
		int row = me.getY() + Direction.getOffset(direction).getY();
		if (!hasValue(row, column)) {
			return null;
		}
		else {
			return get(row, column);
		}
	}

	@Override
	public List<T> getNeighbors(Coordinate me, boolean nsewOnly) throws Exception {
		List<T> neighbors = new ArrayList<T>();
		if (getNeighbor(me, Cardinal.NORTH) != null) {
			neighbors.add(getNeighbor(me, Cardinal.NORTH));
		}
		if (getNeighbor(me, Cardinal.SOUTH) != null) {
			neighbors.add(getNeighbor(me, Cardinal.SOUTH));
		}
		if (getNeighbor(me, Cardinal.EAST) != null) {
			neighbors.add(getNeighbor(me, Cardinal.EAST));
		}
		if (getNeighbor(me, Cardinal.WEST) != null) {
			neighbors.add(getNeighbor(me, Cardinal.WEST));
		}
		if (!nsewOnly) {
			if (getNeighbor(me, Cardinal.NORTHEAST) != null) {
				neighbors.add(getNeighbor(me, Cardinal.NORTHEAST));
			}
			if (getNeighbor(me, Cardinal.SOUTHEAST) != null) {
				neighbors.add(getNeighbor(me, Cardinal.SOUTHEAST));
			}
			if (getNeighbor(me, Cardinal.SOUTHWEST) != null) {
				neighbors.add(getNeighbor(me, Cardinal.SOUTHWEST));
			}
			if (getNeighbor(me, Cardinal.NORTHWEST) != null) {
				neighbors.add(getNeighbor(me, Cardinal.NORTHWEST));
			}
		}
		return neighbors;
	}
}
