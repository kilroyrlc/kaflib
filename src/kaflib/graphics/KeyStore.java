package kaflib.graphics;

import java.awt.Component;
import java.io.File;

import kaflib.gui.TwoPasswordConfirmPanel;
import kaflib.utils.AESUtils;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.KeyPair;

/**
 * Maintains a static set of keys for the duration of a run.
 * @author 0
 *
 */
public class KeyStore {
	private static KeyPair keys = null;
	
	public static void promptForPasswords(final Component parent) throws Exception {
		if (keys != null) {
			throw new Exception("Can only set keys once.");
		}
		keys = TwoPasswordConfirmPanel.promptForPasswords(parent);
	}
	
	public File encrypt(final File input) throws Exception {
		if (keys == null) {
			throw new Exception("Must prompt for passwords.");
		}
		CheckUtils.checkReadable(input, "input file");
		if (FileUtils.getExtension(input).equals(AESUtils.DEFAULT_FILE_EXTENSION)) {
			throw new Exception("Cannot encrypt " + input + ".");
		}
		return AESUtils.doubleEncrypt(input, keys);
	}
	
	public File decrypt(final File input) throws Exception {
		if (keys == null) {
			throw new Exception("Must prompt for passwords.");
		}
		CheckUtils.checkReadable(input, "input file");
		if (!FileUtils.getExtension(input).equals(AESUtils.DEFAULT_FILE_EXTENSION)) {
			throw new Exception("Cannot decrypt " + input + ".");
		}
		return AESUtils.doubleDecrypt(input, keys);
	}
}
