package kaflib.utils;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import kaflib.types.Directory;
import kaflib.types.Matrix;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import com.opencsv.CSVReader;

/**
 * A set of utilities file handling.
 */
public class FileUtils {
	
	/**
	 * Reads the specified xlsx file to a matrix of strings.
	 * @param file
	 * @param columnTitles
	 * @return
	 * @throws Exception
	 */
	public static Matrix<String> readXLSXSheet(final File file, final boolean columnTitles) throws Exception {
		Map<String, Matrix<String>> matrices = readXLSX(file, columnTitles);
		if (matrices.size() <= 0) {
			throw new Exception("No sheets.");
		}
		return matrices.get(matrices.keySet().iterator().next());
	}
	
	/**
	 * Reads a spreadsheet to a set of matrices (one per worksheet).
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Matrix<String>> readXLSX(final File file, final boolean columnTitles) throws Exception {
		Map<String, Matrix<String>> matrices = new HashMap<String, Matrix<String>>();
		Workbook workbook = null;
		workbook = new XSSFWorkbook(new FileInputStream(file));

		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String name = workbook.getSheetName(i);
			Sheet sheet = workbook.getSheetAt(i);
			if (sheet == null) {
				continue;
			}
			Matrix<String> matrix = new Matrix<String>();
			
			int start = 0;
			if (columnTitles) {
				Row row = sheet.getRow(0);
				if (row != null) {
					List<String> labels = new ArrayList<String>();
					for (int k = 0; k < row.getLastCellNum(); k++) {
						labels.add(row.getCell(k).toString());
					}
					matrix.setColumnLabels(labels);
				}
				start = 1;
			}
			for (int j = start; j <= sheet.getLastRowNum(); j++) {
				Row row = sheet.getRow(j);
				if (row == null) {
					continue;
				}
				for (int k = 0; k <= row.getLastCellNum(); k++) {
					Cell cell = row.getCell(k);
					if (cell != null) {
						matrix.set(j - start, k, cell.toString());
					}
				}
			}
			matrices.put(name, matrix);
		}
		workbook.close();
		return matrices;
	}

	public static File changeExtension(final File file, 
										 final String newExtension) throws Exception {
		String name = getFilenameWithoutExtension(file);
		if (newExtension.startsWith(".")) {
			return new File(file.getParentFile(), name + newExtension);
		}
		else {
			return new File(file.getParentFile(), name + "." + newExtension);
		}
	}
	
	public static String changeType(final String path, final String newExtension) throws Exception {
		int index = path.lastIndexOf('.');
		if (newExtension.contains(".")) {
			throw new Exception("No '.' in extension.");
		}
		
		if (index < 0) {
			return path + "." + newExtension;
		}
		else {
			return path.substring(0, index) + "." + newExtension;
		}
		
	}
	
	/**
	 * Reads the given file into a matrix of strings.  The matrix may be 
	 * jagged.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Matrix<String> readCSV(final File file) throws Exception {
		Matrix<String> matrix = new Matrix<String>();
		CSVReader reader = null;

		try {
			reader = new CSVReader(new FileReader(file));
			String row[] = reader.readNext();
			
			while (row != null) {
				matrix.addRow(Arrays.asList(row));
				row = reader.readNext();
			}
			reader.close();
			reader = null;
			
			return matrix;
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			throw e;
		}
	}

	/**
	 * Returns the file contents as a string.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String readString(final File file) throws Exception {
		return readString(file, null);
	}
	
	/**
	 * Returns the file contents as a string or null if the file exceeds max.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String readString(final File file, final Long maxLength) throws Exception {
		BufferedReader reader = getReader(file);
		StringBuffer buffer = new StringBuffer();
		
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
				if (maxLength != null && 
					maxLength > 0 && 
					buffer.length() > maxLength) {
					reader.close();
					return null;
				}
			}
			
			reader.close();
			return new String(buffer);
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			throw e;
		}
	}

	/**
	 * Returns the file contents as a string or null if the file exceeds max.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static List<String> readLines(final File file) throws Exception {
		BufferedReader reader = getReader(file);
		List<String> list = new ArrayList<String>();
		
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
			
			reader.close();
			return list;
		}
		catch (Exception e) {
			if (reader != null) {
				reader.close();
			}
			throw e;
		}
	}

	
	/**
	 * Creates a BufferedReader for the specified file.  Do not forget to close
	 * the reader.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static  BufferedReader getReader(final File file) throws Exception {
		CheckUtils.checkReadable(file);
		return new BufferedReader(new FileReader(file));
	}
	
	/**
	 * Creates the file if it does not already exist.
	 * @param file
	 * @throws Exception
	 */
	public static void createIf(final File file) throws Exception {
		CheckUtils.check(file, "file");
		if (!file.exists()) {
			file.createNewFile();
		}
	}
	
	/**
	 * Deletes the file if it exists.
	 * @param file
	 * @throws Exception
	 */
	public static void deleteIf(final File file) throws Exception {
		CheckUtils.check(file, "file");
		if (!file.exists()) {
			return;
		}
		if (!file.delete()) {
			throw new Exception("Unable to delete file: " + file + ".");
		}
	}
	
	/**
	 * Opens a print writer for the file.  Be sure to close it.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static PrintWriter getWriter(final File file) throws Exception {
		createIf(file);
		CheckUtils.checkWritable(file, "output file");
		
		PrintWriter writer = new PrintWriter(file);
		return writer;
	}
	
	/**
	 * Writes the given string the the specified file.
	 * @param file
	 * @param text
	 * @throws Exception
	 */
	public static void write(final File file, final String text) throws Exception {
		PrintWriter writer = getWriter(file);
		writer.write(text);
		writer.close();
	}
	
	/**
	 * Writes the specified lines to the file, putting the given separator
	 * after each line (including the last).
	 * @param file
	 * @param lines
	 * @param separator
	 * @throws Exception
	 */
	public static void write(final File file, final Collection<String> lines, final String separator) throws Exception {
		PrintWriter writer = getWriter(file);
		
		for (String line : lines) {
			if (line.contains(separator)) {
				throw new Exception("Line contains separator:\n" + line);
			}
			writer.print(line + separator);
		}
		writer.close();
	}
	
	/**
	 * Appends the given string the the specified file.
	 * @param file
	 * @param text
	 * @throws Exception
	 */
	public static void append(final File file, final String text) throws Exception {
		PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));
		writer.write(text);
		writer.close();
	}
	
	public static File appendToFilename(final File file, final String text) throws Exception {
		File directory = file.getParentFile();
		String extension = FileUtils.getExtension(file);
		String name = FileUtils.getFilenameWithoutExtension(file);
		return new File(directory, name + text + "." + extension);
	}

	/**
	 * Downloads the specified url to a file.
	 * @param file
	 * @param url
	 * @throws Exception
	 */
	public static void download(final File file, final URL url) throws Exception {
		if (!file.exists()) {
			file.createNewFile();
		}
		
		InputStream input = url.openStream();
		OutputStream output = new FileOutputStream(file);

		byte[] bytes = new byte[2048];
		int length;

		while ((length = input.read(bytes)) != -1) {
			output.write(bytes, 0, length);
		}
		input.close();
		output.close();
	}
	
	/**
	 * Read the url to a string.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String read(final URL url) throws Exception {
		InputStream input = url.openStream();

		String string = StringUtils.read(input);
		input.close();
		return string;
	}
	
	
	/**
	 * Reads the html from the specified url using tor.
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String torGet(final URL url) throws Exception {
		
		final Proxy proxy = new Proxy(Proxy.Type.SOCKS, 
									  new InetSocketAddress("127.0.0.1", 9150));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		//HttpURLConnection.setFollowRedirects(false);
		//connection.setConnectTimeout(60000);
		//connection.setReadTimeout(60000);
		for (int i = 0; i < 5; i++) {
			try {
				connection.connect();
				break;
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}		
		
		String string = StringUtils.read(connection.getInputStream());
		connection.disconnect();
		return string;
	}
	
	/**
	 * Downloads the specified url to file using tor.
	 * @param file
	 * @param url
	 * @throws Exception
	 */
	public static void torDownload(final File file, final URL url) throws Exception {
		if (!file.exists()) {
			file.createNewFile();
		}
		
		final Proxy proxy = new Proxy(Proxy.Type.SOCKS, 
									  new InetSocketAddress("127.0.0.1", 9150));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
		//HttpURLConnection.setFollowRedirects(false);
		//connection.setConnectTimeout(60000);
		//connection.setReadTimeout(60000);
		for (int i = 0; i < 5; i++) {
			try {
				connection.connect();
				break;
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}		
		
		InputStream input = connection.getInputStream();
		OutputStream output = new FileOutputStream(file);

		byte[] bytes = new byte[2048];
		int length;

		while ((length = input.read(bytes)) != -1) {
			output.write(bytes, 0, length);
		}
		input.close();
		output.close();
		connection.disconnect();
	}
	
	/**
     * Creates a file [prefix][rand%256][suffix] in the current directory.
	 * Thread safe and ensures the file does not already exist.
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	public static File createUniqueFile(final String prefix, final String suffix) throws Exception {
		return createUniqueFile(new File("."), prefix, suffix);
	}
	
	/**
	 * Writes the collection to a single-column Excel file.
	 * @param collection
	 * @param file
	 * @throws Exception
	 */
	public static <T> void toXLSX(final Collection<T> collection, final File file) throws Exception { 
		Matrix<T> matrix = Matrix.createMatrix(collection);
		matrix.toXLSX(file);
	}
	
	/**
	 * Creates a file [prefix][rand%256][suffix] in the specified directory.
	 * Thread safe and ensures the file does not already exist.
	 * @param directory
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	public static File createUniqueFile(final File directory, final String prefix, final String suffix) throws Exception {
		CheckUtils.check(directory, "directory");
		CheckUtils.check(prefix, "prefix");
		CheckUtils.check(suffix, "suffix");

		if (!directory.exists()) {
			directory.mkdir();
		}
		
		File file = null;
		for (int i = 0; i < 256; i++) {
			file = new File(directory, prefix + RandomUtils.randomInt(256) + suffix);
			if (file.exists()) {
				continue;
			}
			if (file.createNewFile()) {
				return file;
			}
		}
		throw new Exception("Failed to create file after many tries.");
	}
	
	/**
	 * Converts the xls file to xlsx using excelcnv.  Note Excel likes to
	 * display GUI with this conversion.
	 * @param xls
	 * @return
	 * @throws Exception
	 */
	public static File xlsToXLSX(final File xls) throws Exception {
		CheckUtils.checkReadable(xls, "xls file");
		if (!xls.getAbsolutePath().endsWith(".xls")) {
			throw new Exception("Not an xls file: " + xls.getAbsolutePath() + ".");
		}
		
		File xlsx = new File(xls.getAbsolutePath() + "x");
		CheckUtils.check(xlsx, "xlsx file");

		try {
			// excelcnv -nme -oice xls xlsx
			SystemUtils.excecuteCommandSerially("excelcnv",
												"-nme",
												"-oice",
												xls.getAbsolutePath(),
												xlsx.getAbsolutePath());
			return xlsx;
		}
		catch (Exception e) {
			System.out.println("Unable to execute, be sure 'excelcnv' is in your path.");
			throw e;
		}
	}

	public static void gUnzip(final File gzip, final File output) throws Exception {
		GZIPInputStream in = new GZIPInputStream(new FileInputStream(gzip));
		
		createIf(output);
		FileOutputStream out = new FileOutputStream(output);

		byte[] bytes = new byte[2048];
		int length;

		while ((length = in.read(bytes)) != -1) {
			out.write(bytes, 0, length);
		}
		in.close();
		out.close();
	}
	
	/**
	 * Extracts the zip file to the specified directory.
	 * @param zipFile
	 * @param destination
	 * @throws Exception
	 */
	public static void unzip(final File zipFile, final File destination) throws Exception {
		CheckUtils.checkReadable(zipFile, "zip file");
		if (!destination.exists()) {
			destination.mkdir();
		}
		else {
			if (!destination.isDirectory()) {
				throw new Exception("Not a directory: " + destination + ".");
			}
		}
		
		ZipFile file = new ZipFile(zipFile);
		file.extractAll(destination.getAbsolutePath());
	}

	/**
	 * Returns a zip file handle for the given directory to be zipped,
	 * specifically: [parent]/[dirname].zip.
	 * @param directory
	 * @return
	 * @throws Exception
	 */
	public static File getZipFile(final File directory) throws Exception {
		return new File(directory.getParent(), directory.getName() + ".zip");
	}
	
	/**
	 * Zips the directory to a file of the same name.
	 * @param directory
	 * @throws Exception
	 */
	public static void zip(final File directory) throws Exception {
		CheckUtils.checkReadable(directory, "directory");
		if (!directory.isDirectory()) {
			throw new Exception("Must specify directory.");
		}
		
		ZipFile file = new ZipFile(getZipFile(directory));
		
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        file.createZipFile(new ArrayList<File>(Arrays.asList(directory.listFiles())), parameters);
	}
	
	/**
	 * Returns the file extension - all text after the last '.'.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String getExtension(final File file) throws Exception {
		return getExtension(file.getName());
	}
	
	public static String getExtension(final String name) throws Exception {
		if (name == null || name.lastIndexOf('.') < 0) {
			return null;
		}
		return name.substring(name.lastIndexOf('.') + 1);
	}
	
	public static String append(final String directory, final String path) {
		if (directory.endsWith("/")) {
			return directory + path;
		}
		else {
			return directory + "/" + path;
		}
	}
	
	public static String getFilenameWithoutExtension(final File file) throws Exception {
		String name = file.getName();
		if (name == null || name.lastIndexOf('.') < 0) {
			return name;
		}
		return name.substring(0, name.lastIndexOf('.'));
	}
	
	/**
	 * An unsophisticated check to see if the file extension is
	 *  - bmp
	 *  - gif
	 *  - jpg
	 *  - png
	 *  - tif
	 *  - mp4
	 *  - jpeg
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static boolean isImageFile(final File file) throws Exception {
		if (getExtension(file) == null) {
			return false;
		}
		
		String extension = getExtension(file).toLowerCase();
		if (extension == null) {
			return false;
		}
		
		return isImageExtension(extension);
	}

	public static boolean isImageExtension(final String extension) {
		if (extension.equals("bmp") ||
			extension.equals("gif") ||
			extension.equals("jpg") ||
			extension.equals("png") ||
			extension.equals("tif") ||
			extension.equals("jpeg") ||
			extension.equals("mp4")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Reads bytes from a stream into an array.
	 * @param stream
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static byte[] read(final InputStream stream, final int length) throws Exception {
		
		byte bytes[] = new byte[length];
		int index = 0;
		while (index < length) {
			index += stream.read(bytes, index, length - index);
		}
		
		return bytes;
	}
	
	/**
	 * Returns the md5 value for the specified file.
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static byte[] getMD5(final File file) throws Exception {
		return MathUtils.getMD5(FileUtils.readString(file, null).getBytes());
	}
	
	/**
	 * Renames the specified file to a base 64 name based on a hash of its
	 * contents.  Truncates at a max length, or null to not truncate.
	 * @param file
	 * @throws Exception
	 */
	public static void renameToBase64Hash(final File file, 
										  final File outputDirectory,
										  final Integer length) throws Exception {
		if (file.isDirectory()) {
			throw new Exception("Cannot rename directory.");
		}
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		if (!outputDirectory.isDirectory() || !outputDirectory.exists()) {
			throw new Exception("Cannot access output directory: " + outputDirectory + ".");
		}

		String name = getMD5Base64Name(file, length);
		
		File ofile = new File(outputDirectory, name);
		if (ofile.exists()) {
			throw new Exception("Collision: " + file + "(" + 
								file.length()/1000 + "k)" + " -> " + name + 
								"(" + ofile.length()/1000 + "k).");
		}
		
		FileUtils.copy(ofile, file);
	}
	
	/**
	 * 
	 * @param file
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String getMD5Base64(final File file) throws Exception {
		final byte md5[] = getMD5(file);
		return new String(MathUtils.encodeBase64(md5, true));
	}
	
	/**
	 * Returns the md5 hash of the file as a url-safe base-64 encoded name.
	 * File extension is preserved.  If length nonnull, truncates at length.
	 * @param file
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static String getMD5Base64Name(final File file,
										  final Integer length) throws Exception {
		final String extension = getExtension(file);
		final byte md5[] = getMD5(file);
		
		String name = new String(MathUtils.encodeBase64(md5, true));
		if (length != null && name.length() > length) {
			name = name.substring(0, length);
		}
		if (extension != null && extension.length() > 0) {
			name = name + "." + extension;
		}
		return name;
	}
	
	/**
	 * Renames the specified file to an [a-z0-9] name based on a hash of its
	 * contents.  The file is md5 hashed, those bytes are then mapped to 
	 * [a-z0-9].
	 * @param file
	 * @throws Exception
	 */
	public static void renameToHexHash(final File file, final File outputDirectory) throws Exception {
		if (file.isDirectory()) {
			throw new Exception("Cannot rename directory.");
		}
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		if (!outputDirectory.isDirectory() || !outputDirectory.exists()) {
			throw new Exception("Cannot access output directory: " + outputDirectory + ".");
		}

		
		final String extension = getExtension(file);
		final byte md5[] = getMD5(file);

		String name = StringUtils.mapToWords(md5) + "." + extension;
		
		File ofile = new File(outputDirectory, name);
		if (ofile.exists()) {
			throw new Exception("Collision: " + file + " -> " + name + ".");
		}
		
		FileUtils.copy(ofile, file);
	}
	
	/**
	 * Copy the file from source to destination.
	 */	
	public static void copy(final File destination, final File source) throws Exception {
		FileUtils.createIf(destination);
		if (!destination.canWrite()) {
			throw new Exception("Cannot write: " + destination.getAbsolutePath() + ".");
		}

		FileInputStream instream = new FileInputStream(source);
		FileOutputStream outstream = new FileOutputStream(destination);
        FileChannel in = instream.getChannel();
        FileChannel out = outstream.getChannel();
        out.transferFrom(in, 0, in.size());
        
        in.close();
        out.close();
        instream.close();
        outstream.close();
	}
	
	public static void copyTo(final Directory destination, final File source) throws Exception {
		File destination_file = new File(destination, source.getName());
		copy(destination_file, source);
	}
	
	/**
	 * Reads the input file into a byte array.
	 * 
	 *  @param input the file to read.
	 *  @return A byte array of the file contents.
	 *  @throws Exception On null input.
	 */	
	public static byte[] read(final File input) throws Exception {
		return read(input, null);
	}
	
	/**
	 * Reads the input file into a byte array.
	 * 
	 *  @param input the file to read.
	 *  @param maxBytes the maximum number of bytes allowed, -1 for unlimited.
	 *  @return A byte array of the file contents or null if the file is too 
	 *  large.
	 *  @throws Exception On null input.
	 */	
	public static byte[] read(final File input, final Integer maxBytes) throws Exception {
		CheckUtils.checkReadable(input, "input file");

		if (input.length() > Integer.MAX_VALUE || 
			(maxBytes != null && input.length() > maxBytes)) {
			throw new Exception("File size (" + input.length() + 
								") longer than max (" + maxBytes + ".");
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(input);
			byte buffer[] = new byte[(int)input.length()];
			int read = 0;
			int index = 0;
			
			read = stream.read(buffer, 0, buffer.length);
			while (read > 0) {
				index += read;
				read = stream.read(buffer, index, buffer.length - read);
			}
			
			stream.close();
			
			if (index != buffer.length) {
				throw new Exception("File read error, file length: " + buffer.length +
									" but read: " + read + ".");
			}
			
			return buffer;
		}
		catch (Exception e) {
			if (stream != null) {
				stream.close();
			}
			throw e;
		}
	}
	
	
	/**
	 * Returns all files under the specified root directory/file.
	 * @param root
	 * @return
	 */
	public static Set<File> getRecursive(final File root) {
		return getRecursive(root, null);
	}
	
	
	public static Set<Directory> getLeaves(final Directory root) throws Exception {
		Set<Directory> leaves = new HashSet<Directory>();
		if (root.isLeaf()) {
			leaves.add(root);
			return leaves;
		}
		
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				leaves.addAll(getLeaves(new Directory(file)));
			}
		}
		return leaves;
	}
	
	/**
	 * Returns all files under the specified root directory/file ending
	 * with the specified extension.
	 * @param root
	 * @param extension
	 * @return
	 */
	public static Set<File> getRecursive(final File root, final String extension) {
		Set<File> files = new HashSet<File>();
		String dot_extension = extension;
		if (dot_extension != null) {
			if (!dot_extension.startsWith(".")) {
				dot_extension = "." + dot_extension;
			}
		}
		
		File list[] = root.listFiles();
		for (File file : list) {
			if (file.isDirectory()) {
				files.addAll(getRecursive(file, dot_extension));
			}
			else {
				if (extension == null || file.getName().endsWith(dot_extension)) {
					files.add(file);					
				}
			}
		}
		return files;
	}

	/**
	 * Returns the total number of files under the given root directory/file.
	 * @param root
	 * @return
	 */
	public static int countFiles(final File root) {
		int count = 0;
		if (root.isFile()) {
			return 1;
		}
		
		File list[] = root.listFiles();
		for (File file : list) {
			if (file.isDirectory()) {
				count += countFiles(root);
			}
			else {
				count++;
			}
		}
		return count;
	}
	

	public static String getRelativePath(final File from, final File to) throws Exception {
		return getRelativePath(from, to, File.separator);
	}
	
	public static String getRelativePath(final File from, final File to, final String outputSeparator) throws Exception {
		String from_tokens[] = from.getAbsolutePath().split("\\" + File.separator);
		String to_tokens[] = to.getAbsolutePath().split("\\" + File.separator);
		if (!from_tokens[0].equals(to_tokens[0])) {
			throw new Exception("Files share no path root:\n" + from + "\n" + to);
		}
		int i = 0;
		while(from_tokens[i].equals(to_tokens[i])) {
			i++;
		}
		StringBuffer path = new StringBuffer();
		for (int j = i; j < from_tokens.length; j++) {
			path.append("..");
			path.append(outputSeparator);
		}
		for (int j = i; j < to_tokens.length - 1; j++) {
			path.append(to_tokens[j]);
			path.append(outputSeparator);
		}
		path.append(to_tokens[to_tokens.length - 1]);
		return new String(path);
	}
	
	public static void main(String args[]) {
		try {
			//File from = new File("C:\\data\\code\\kilroy\\source\\tags");
			//File to = new File("C:\\data\\code\\kilroy\\source\\archive\\2009\\06\\2c349a_b_t130.jpg");
			//System.out.println(FileUtils.appendToFilename(to, "blah"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
