package kaflib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import kaflib.types.Pair;


public class AESUtils {
	public static final String DEFAULT_FILE_EXTENSION = "oo2";
	
	public static final int KEY_SIZE = 128;
	public static final int IV_LENGTH = 16;
	public static final int SALT_LENGTH = 8;
	public static final int MAX_FILENAME_LENGTH = 72;
	
	
	/**
	 * Reads the file to a string.
	 * @param in
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	public static String read(final File in,
							  final KeyPair keys) throws Exception {
		CheckUtils.checkReadable(in, "input file");
		CheckUtils.check(keys, "keys");
		byte bytes[] = AESUtils.decrypt(in, keys);
		return new String(bytes);
	}
	
	/**
	 * Writes the file to a string.
	 * @param out
	 * @param keys
	 * @param text
	 * @throws Exception
	 */
	public static void write(final File out,
							 final KeyPair keys,
							 final String text) throws Exception {
		CheckUtils.checkWritable(out, "output file");
		CheckUtils.check(keys, "keys");
		AESUtils.encrypt(out, text.getBytes("UTF-8"), keys);
	}
	
	/**
	 * Encrypts the file and its name to a new file with the specified extension.
	 * @param in
	 * @param outputExtension
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static File encrypt(final File in,
							   final String outputExtension,
							   final KeyPair keys) throws Exception {
		if (outputExtension.contains(".")) {
			throw new Exception("Chars only in extension.");
		}
		
		if (in.getName().endsWith(outputExtension)) {
			throw new Exception("Cannot encrypt encrypted file: " + in + ".");
		}
		
		String outname = encryptAESECBToBase64(in.getName().getBytes("UTF-8"), keys.getOuter());
		if (outname.length() > MAX_FILENAME_LENGTH) {
			throw new Exception("B64 name for " + in + " is: " + outname + ".");
		}
		outname += "." + outputExtension;
		File out = new File(in.getParentFile(), outname);
		encrypt(out, in, true, keys);
		return out;
	}
	

	public static void encrypt(final File out,
							   final File in,
							   final KeyPair keys) throws Exception {
		encrypt(out, in, false, keys);
	}
	
	public static void encrypt(final File out,
							   final File in,
							   final boolean deleteInput,
							   final KeyPair keys) throws Exception {
		CheckUtils.check(in, "input file");
		byte buffer[] = FileUtils.read(in, null);
		
		encrypt(out, buffer, keys);
		
		if (deleteInput) {
			in.delete();
		}
		
	}

	public static void encrypt(final File out,
							   final byte[] buffer,
							   final KeyPair keys) throws Exception {
		// Encrypt source file.
		Pair<byte[], byte[]> ciphertext = encryptAESCBC(buffer, keys.getOuter());
		byte temp[] = TypeUtils.concatenate(ciphertext.getFirst(), ciphertext.getSecond());
		ciphertext = null;
		ciphertext = encryptAESCBC(temp, keys.getInner());

		FileOutputStream ostream = new FileOutputStream(out);
		ostream.write(ciphertext.getFirst());
		ostream.write(ciphertext.getSecond());
		ostream.close();
	}

	public static String decryptName(final File in,
									 final KeyPair keys) throws Exception {
		return decryptName(in, null, keys);
	}
		
	public static String decryptName(final File in,
									final String outputExtension,
									final KeyPair keys) throws Exception {
		if (outputExtension != null && !in.getName().endsWith(outputExtension)) {
			throw new Exception("File not encrypted: " + in + ".");
		}

		String name = FileUtils.getFilenameWithoutExtension(in);
		return new String(decryptBase64AESECB(name, keys.getOuter()));
	}
	
	
	public static File decrypt(final File in,
							   final String outputExtension,
							   final KeyPair keys) throws Exception {
		if (!in.getName().endsWith(outputExtension)) {
			throw new Exception("File not encrypted: " + in + ".");
		}
		
		String name = FileUtils.getFilenameWithoutExtension(in);
		CheckUtils.checkNonEmpty(name, "name");
		File out;
		try {
			out = new File(in.getParentFile(), new String(decryptBase64AESECB(name, keys.getOuter())));
		}
		catch (Exception e) {
			System.err.println("Unable to generate decrypted filename for: " + name + ".");
			throw e;
		}
		decrypt(out, in, true, keys);
		return out;
	}
	

	public static void decrypt(final File out,
							   final File in, 
							   final KeyPair keys) throws Exception {
		decrypt(out, in, false, keys);
	}
	
	public static void decrypt(final File out,
							   final File in, 
							   final boolean deleteInput,
							   final KeyPair keys) throws Exception {
		CheckUtils.checkReadable(in, "input file");
		
		byte buffer[] = decrypt(in, keys);
		
		FileOutputStream ostream = new FileOutputStream(out);		
		ostream.write(buffer);	
		ostream.close();
		
		if (deleteInput) {
			in.delete();
		}
	}
	
	public static byte[] decrypt(final File in, 
								 final KeyPair keys) throws Exception {
		CheckUtils.checkReadable(in, "input file");
		
		int data_length = (int)in.length() - IV_LENGTH;
		FileInputStream instream = new FileInputStream(in);
		byte iv[] = FileUtils.read(instream, IV_LENGTH);
		byte ciphertext[] = FileUtils.read(instream, data_length);

		instream.close();

		byte buffer[];
		buffer = decryptAESCBC(iv, ciphertext, keys.getInner());
		ciphertext = null;

		Pair<byte[], byte[]> pair = TypeUtils.split(buffer, IV_LENGTH); 
		buffer = decryptAESCBC(pair.getFirst(), pair.getSecond(), keys.getOuter());
	
		return buffer;
	}
	
	
	/**
	 * Generates a key using the specified password and salt.
	 * @param password
	 * @param salt
	 * @return
	 * @throws Exception
	 */
	public static SecretKey generateKey(final String password, final byte salt[]) throws Exception {
		if (salt.length < SALT_LENGTH) {
			throw new Exception("Need a salt at least length " + SALT_LENGTH + ".");
		}
		return generateKey(password.toCharArray(), salt);
	}
	
	/**
	 * Generates a key using the specified password and salt.
	 * @param password
	 * @param salt
	 * @return
	 * @throws Exception
	 */
	public static SecretKey generateKey(final char password[], final byte salt[]) throws Exception {
		if (salt.length != 8) {
			throw new Exception("Invalid salt length.");
		}
		
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE);
		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}
	
	/**
	 * Encrypts the specified bytes using aes ecb.
	 * @param message
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptAESECB(final byte message[], final SecretKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(message);
	}
	
	/**
	 * Encrypts the specified bytes to base64 using aes ecb.
	 * @param message
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encryptAESECBToBase64(final byte message[], final SecretKey key) throws Exception {
		return new String(MathUtils.encodeBase64(encryptAESECB(message, key), true));
	}
	
	/**
	 * Encrypts the specified bytes using aes ecb.
	 * @param message
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptAESECB(final byte message[], final SecretKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(message);
	}
	
	/**
	 * Encrypts the specified bytes to base64 using aes ecb.
	 * @param message
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptBase64AESECB(final String message, final SecretKey key) throws Exception {
		return decryptAESECB(MathUtils.decodeBase64(message.getBytes("UTF-8"), true), key);
	}
	
	/**
	 * Encrypt using AES/CBC/PKCS5, returns initialization vector and 
	 * ciphertext, respectively.
	 * @param packet
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static Pair<byte[], byte[]> encryptAESCBC(byte message[], SecretKey key) throws Exception {
	
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		AlgorithmParameters params = cipher.getParameters();

		Pair<byte[], byte[]> pair = new Pair<byte[], byte[]>();
		pair.setKey(params.getParameterSpec(IvParameterSpec.class).getIV());
		pair.setValue(cipher.doFinal(message));
		return pair;
	}
	
	/**
	 * Decrypt using AES/CBC/PKCS5 and the supplied data and iv.
	 * @param packet
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptAESCBC(final byte iv[], final byte ciphertext[], SecretKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		return cipher.doFinal(ciphertext);
	}
	
	public static void main(String args[]) throws Exception {
		try {
			File plaintext = new File("plain.txt");
			FileUtils.write(plaintext, "this message does this even crypto");
			KeyPair keys = new KeyPair("tacotaco", "baconbacon");
			File enc = new File("aestest.oo2");
			AESUtils.encrypt(enc, plaintext, keys);
			System.out.println("Wrote: " + enc);
			
			String text = new String(AESUtils.decrypt(enc, keys));
			System.out.println("Message: " + text);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}		
}


