package kaflib.types;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import kaflib.utils.FileUtils;

/**
 * Provides a convenience type for using Apache's FTP client.
 */
public class FTP {
	private boolean connected;
	private final FTPClient ftp;
	private final FTPClientConfig config;
	
	public class DirList {
		public Map<String, Long> list;
	}
	
	public FTP() throws Exception {
		ftp = new FTPClient();
		config = new FTPClientConfig();
		ftp.configure(config);
		connected = false;
	}
	
	public synchronized boolean connect(final String server,
										final String user,
										final String password) throws Exception {
		if (connected) {
			throw new Exception("Already connected.");
		}
		
		ftp.connect(server);
		if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
			System.out.println("Connection refused.");
			ftp.disconnect();
			return false;
		}
		if (!ftp.login(user, password)) {
			System.out.println("Login failed.");
			ftp.disconnect();
			return false;
		}		
		
		connected = true;
		
		return true;
	}

	public void checkFile(final String path) throws Exception {
		if (!isFile(path)) {
			throw new Exception("Not a file: " + path + ".");
		}			
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isFile(final String path) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		FTPFile file = ftp.mlistFile(path);
		if (file == null || !file.isFile()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean exists(final String path) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		FTPFile file = ftp.mlistFile(path);
		if (file == null || file.isUnknown() || !file.isValid()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean isDirectory(final String path) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		FTPFile file = ftp.mlistFile(path);
		if (file == null || !file.isDirectory()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void checkDirectory(final String path) throws Exception {
		if (!isDirectory(path)) {
			throw new Exception("Not a directory: " + path + ".");
		}
	}
	

	private int getFileTypeFromExtension(final String fileExtension) throws Exception {
		String extension = fileExtension.toLowerCase();
		
		if (extension.equals("txt") || extension.equals("html")) {
			return FTPClient.ASCII_FILE_TYPE;
		}
		else if (extension.equals("jpg") || extension.equals("png") ||
				 extension.equals("gif") || extension.equals("bmp") ||
				 extension.equals("tif") || extension.equals("gz")) {
			return FTPClient.BINARY_FILE_TYPE;
		}
		else {
			throw new Exception("Unsupported format: " + extension + ".");
		}
		
	}
	
	public int getFileType(final String path) throws Exception {
		int index = path.lastIndexOf(".");
		if (index < 0 || index == path.length() - 1) {
			throw new Exception("No file extension.");
		}
		return getFileTypeFromExtension(path.substring(index + 1));
	}
	
	public int getFileType(final File file) throws Exception {
		return getFileTypeFromExtension(FileUtils.getExtension(file));
	}

	public DirList createDirList(final String path) throws Exception {
		return createDirList(listFTPFiles(path));
	}
	
	protected DirList createDirList(final Collection<FTPFile> dirlist) {
		if (dirlist == null) {
			return null;
		}
		Map<String, Long> map = new HashMap<String, Long>();
		for (FTPFile file : dirlist) {
			map.put(file.getName(), file.getSize());
		}
		DirList list = new DirList();
		list.list = map;
		
		return list;
	}

	public boolean put(final String path, 
			   			final File file) throws Exception {
		return put(path, file, false, null);
	}
	
	public boolean put(final String path, 
			   final File file, 
			   final DirList dirlist) throws Exception {
		return put(path, file, false, dirlist);
	}
	
	/**
	 * Writes the specified file to the given path (fully qualified, 
	 * including filename).  Checks against an existing file of the same
	 * size, does not re-upload unless force specified.  Takes an optional
	 * directory list to check for the file, otherwise re-checks.
	 * @param path
	 * @param file
	 * @param dirlist
	 * @return
	 * @throws Exception
	 */
	public boolean put(final String path, 
					   final File file, 
					   boolean force, 
					   final DirList dirlist) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}

		if (isDirectory(path)) {
			throw new Exception("Path must be destination file name.");
		}
		
		// If force isn't specified, check for a match.
		if (!force) {
			// Dirlist provided, check it for a match.
			if (dirlist != null) {
				if (dirlist.list.containsKey(file.getName()) &&
					dirlist.list.get(file.getName()) == file.length()) {
					return true;
				}
			}
			// Dirlist not provided, query.
			else {
				FTPFile remote = ftp.mlistFile(path);
				if (remote != null && 
					remote.isFile() && 
					remote.getSize() == file.length()) {
					return true;
				}
			}
		}
		
		// Upload the file.
		FileInputStream stream = new FileInputStream(file);
		ftp.setFileType(getFileType(file));
		boolean success = ftp.storeFile(path, stream);
		stream.close();
		return success;
	}

	public boolean sync(final String path,
						final Collection<File> files) throws Exception {
		
		if (!connected) {
			throw new Exception("Not connected.");
		}
		checkDirectory(path);

		Set<File> local = new HashSet<File>();
		local.addAll(files);
		DirList remote = createDirList(path);

		// Iterate over all remove files.
		for (String name : remote.list.keySet()) {
			File match = null;
			
			// Try to find a local.
			for (File file : local) {
				if (file.getName().equals(name)) {
					match = file;
					break;
				}
			}
			
			// No local version, delete.
			if (match == null || match.length() != remote.list.get(name)) {
				delete(FileUtils.append(path, name));
			}
			else {
				local.remove(match);
			}
		}

		boolean success = true;
		for (File file : local) {
			success &= put(FileUtils.append(path, file.getName()), file, true, null);
		}
		return success;
		
	}

	public boolean mput(final String path, 
						final Collection<File> files) throws Exception {
		return mput(path, files, false);
	}
	
	public boolean mput(final String path, 
						final Collection<File> files,
						final boolean force) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		checkDirectory(path);

		DirList dirlist = null;
		if (!force) {
			dirlist = createDirList(path);
		}
		
		boolean success = true;
		for (File file : files) {
			success &= put(FileUtils.append(path, file.getName()), file, force, dirlist);
		}
		return success;
	}
	

	protected Set<FTPFile> listFTPFiles(final String path) throws Exception {
		return listFTP(path, true, false, false);
	}

	protected Set<FTPFile> listFTPDirectories(final String path) throws Exception {
		return listFTP(path, false, true, false);
	}
	
	protected Set<FTPFile> listFTPSymlinks(final String path) throws Exception {
		return listFTP(path, false, false, true);
	}
	
	/**
	 * Returns a list of all files and directories, not symbolic links, 
	 * in path.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	protected Set<FTPFile> listFTP(final String path,
								    final boolean files,
								    final boolean directories,
								    final boolean symlinks) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		
		Set<FTPFile> matches = new HashSet<FTPFile>();
		FTPFile[] array;
		if (path == null) {
			array = ftp.mlistDir();
		}
		else {
			array = ftp.mlistDir(path);
		}
		for (FTPFile file : array) {
			if (file.isFile() && files) {
				matches.add(file);
			}
			else if (file.isDirectory() && directories) {
				matches.add(file);
			}
			else if (file.isSymbolicLink() && symlinks) {
				matches.add(file);
			}
			else {
			}
		}
		return matches;
	}
	
	public boolean getFiles(final String path, final Directory directory) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		checkDirectory(path);
		
		List<String> files = listFiles(path);
		boolean success = true;
		for (String file : files) {
			success &= getFile(FileUtils.append(path, file), new File(directory, file));
		}
		return success;
	}
	
	public boolean getFile(final String path, final File file) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		checkFile(path);
		FileOutputStream stream = new FileOutputStream(file);

		ftp.setFileType(getFileType(path));
		boolean success = ftp.retrieveFile(path, stream);
		stream.close();
		return success;
	}
	
	public boolean makeDir(final String path) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		return ftp.makeDirectory(path);
	}
	
	public void delete(final String path) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		checkFile(path);
		ftp.deleteFile(path);
	}
	
	public boolean deleteAllFiles(final String path) throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		checkDirectory(path);
		List<String> files = listFiles(path);
		boolean success = true;
		for (String file : files) {
			success &= ftp.deleteFile(FileUtils.append(path, file));
		}
		return success;
	}
	
	public List<String> list() throws Exception {
		return list(null);
	}

	/**
	 * Lists all files for the specified path.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public List<String> listFiles(final String path) throws Exception {
		Set<FTPFile> files = listFTPFiles(path);
		List<String> names = new ArrayList<String>();
		for (FTPFile file : files) {
			names.add(file.getName());
		}
		return names;
	}
	
	/**
	 * Lists all file/directory paths for the given path.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public List<String> list(final String path) throws Exception {
		Set<FTPFile> files = listFTP(path, true, true, true);
		List<String> names = new ArrayList<String>();
		for (FTPFile file : files) {
			names.add(file.getName());
		}
		return names;
	}
	
	/**
	 * Disconnects from ftp.
	 * @throws Exception
	 */
	public synchronized void disconnect() throws Exception {
		if (!connected) {
			throw new Exception("Not connected.");
		}
		ftp.logout();
	}

	/**
	 * Test driver.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			//FTP ftp = new FTP();
			//if (!ftp.connect()) {
			//	System.out.println("Connect failed.");
			//	return;
			//}
			//System.out.println(StringUtils.concatenate(ftp.list(), " "));
			//ftp.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
