package kaflib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import kaflib.types.Pair;


public class AESUtils {
	public static final int KEY_SIZE = 128;
	public static final int IV_LENGTH = 16;
	public static final int SALT_LENGTH = 8;
	public static final int MAX_FILENAME_LENGTH = 72;
	
	/**
	 * Encrypts the specified file set using two separate passwords, that salt
	 * each other.  Output files get a specified extension.
	 * @param files
	 * @param inner
	 * @param outer
	 * @throws Exception
	 */
	public static void doubleEncrypt(final Set<File> files,
									 final String extension,
									 final String outerPassword, 
									 final String innerPassword) throws Exception {
		// Generate keys, statically salted by the opposite.
		SecretKey outer = generateKey(outerPassword, innerPassword.substring(0, SALT_LENGTH).getBytes("UTF-8"));
		SecretKey inner = generateKey(innerPassword, outerPassword.substring(0, SALT_LENGTH).getBytes("UTF-8"));
		
		for (File file : files) {
			doubleEncrypt(file, extension, outer, inner);
		}
	}

	public static void doubleEncrypt(final File file,
			 final String extension,
			 final KeyPair keys) throws Exception {
		doubleEncrypt(file, extension, keys.getOuter(), keys.getInner());
	}
	
	public static void doubleEncrypt(final File file,
									 final String extension,
									 final SecretKey outerKey, 
									 final SecretKey innerKey) throws Exception {
		if (file.getName().endsWith(extension)) {
			System.out.println("Skipping encrypted file: " + file + ".");
			return;
		}
		
		File temp = encrypt(file, true, null, outerKey, 1000000000);
		encrypt(temp, false, extension, innerKey, 1000000000);
		temp.delete();
		file.delete();
	}
				
	public static void doubleDecrypt(final Set<File> files,
			 						 final String extension,
									 final String outerPassword, 
									 final String innerPassword) throws Exception {
		// Generate keys, statically salted by the opposite.
		SecretKey outer = generateKey(outerPassword, innerPassword.substring(0, SALT_LENGTH).getBytes("UTF-8"));
		SecretKey inner = generateKey(innerPassword, outerPassword.substring(0, SALT_LENGTH).getBytes("UTF-8"));

		for (File file : files) {
			if (!file.getName().endsWith(extension)) {
				continue;
			}
			doubleDecrypt(file, extension, outer, inner);
		}
	}
	

	public static File doubleDecrypt(final File file,
			 						 final boolean keepOriginal,
									 final String extension,
									 final KeyPair keys) throws Exception {
		return doubleDecrypt(file, keepOriginal, extension, keys.getOuter(), keys.getInner());
	}


	public static File doubleDecrypt(final File file,
									 final String extension,
									 final SecretKey outerKey, 
									 final SecretKey innerKey) throws Exception {
		return doubleDecrypt(file, false, extension, outerKey, innerKey);
	}

	public static File doubleDecrypt(final File file,
									 final boolean keepOriginal,
									 final String extension,
									 final SecretKey outerKey, 
									 final SecretKey innerKey) throws Exception {
		if (!file.getName().endsWith(extension)) {
			System.out.println("Skipping non-encrypted file: " + file + ".");
			return null;
		}
		File temp = decrypt(file, false, extension, innerKey);
		File output = decrypt(temp, true, null, outerKey);
		
		temp.delete();
		if (!keepOriginal) {
			file.delete();
		}
		return output;
	}
	
	
	/**
	 * Encrypt the specified input file, using the given key.  The output file
	 * will be named the b64 encoding of the input, the IV will be written to
	 * the beginning of the output file.
	 * @param in
	 * @param key
	 * @param maxLength
	 * @throws Exception
	 */
	public static File encrypt(final File in, 
							   final boolean encryptName,
							   final String outputExtension,
							   final SecretKey key,
							   final int maxLength) throws Exception {
		String outname = in.getName();
		
		if (encryptName) {
			outname = encryptAESECBToBase64(in.getName().getBytes("UTF-8"), key);
			if (outname.length() > MAX_FILENAME_LENGTH) {
				throw new Exception("B64 name for " + in + " is: " + outname + ".");
			}
		}
		
		File out;
		if (outputExtension == null || outputExtension.length() == 0) {
			out = new File(in.getParentFile(), outname);
		}
		else {
			out = new File(in.getParentFile(), outname + "." + outputExtension);
		}
		byte plaintext[] = FileUtils.read(in, maxLength);
						
		// Encrypt source file.
		Pair<byte[], byte[]> ciphertext = encryptAESCBC(plaintext, key);
		plaintext = null;
		System.gc();
		
		FileOutputStream ostream = new FileOutputStream(out);
		ostream.write(ciphertext.getFirst());
		ostream.write(ciphertext.getSecond());
		ostream.close();
		return out;
	}
	
	public static void encryptObject(final Serializable object, 
	   		  final File file, 
	   		  final SecretKey key) throws Exception {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream ostream = new ObjectOutputStream(bytes);
		try {
			ostream.writeObject(object);
			AESUtils.encrypt(bytes.toByteArray(), file, key);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			bytes.close();
			ostream.close();
		}

	}
	
	public static void encryptObjects(final Collection<Serializable> objects, 
							   		  final File file, 
							   		  final SecretKey key) throws Exception {
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream ostream = new ObjectOutputStream(bytes);
		try {
			for (Serializable object : objects) {
				ostream.writeObject(object);
			}
			
			AESUtils.encrypt(bytes.toByteArray(), file, key);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			bytes.close();
			ostream.close();
		}

	}
	
	public static Object decryptObject(final File file, final SecretKey key) throws Exception {
		ByteArrayInputStream bytes = new ByteArrayInputStream(AESUtils.decrypt(file, key));
		ObjectInputStream ostream = new ObjectInputStream(bytes);
		Object object = null;
		try {
			object = ostream.readObject();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			bytes.close();
			ostream.close();
		}
		return object;

	}
		
	public static List<Object> decryptObjects(final File file, final SecretKey key) throws Exception {
		ByteArrayInputStream bytes = new ByteArrayInputStream(AESUtils.decrypt(file, key));
		ObjectInputStream ostream = new ObjectInputStream(bytes);
		List<Object> objects = new ArrayList<Object>();
		try {
			Object object = ostream.readObject();
			while (object != null) {
				objects.add(object);
				object = ostream.readObject();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			bytes.close();
			ostream.close();
		}
		return objects;

	}
	
	/**
	 * Encrypt the specified data, using the given key.  The IV will be written to
	 * the beginning of the output file.
	 * @param in
	 * @param key
	 * @param maxLength
	 * @throws Exception
	 */
	public static File encrypt(final byte in[],
							   final File out,
							   final SecretKey key) throws Exception {
		CheckUtils.check(out, "output file");
		
		// Encrypt source file.
		Pair<byte[], byte[]> ciphertext = encryptAESCBC(in, key);
		
		FileOutputStream ostream = new FileOutputStream(out);
		ostream.write(ciphertext.getFirst());
		ostream.write(ciphertext.getSecond());
		ostream.close();
		return out;
	}

	/**
	 * Decrypts the specified file, using the given key.
	 * @param in
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static File decrypt(final File in, 
							   final boolean decryptName,
							   final String extension, 
							   final SecretKey key) throws Exception {
		if (in.length() > Integer.MAX_VALUE) {
			throw new Exception("Input file longer than int max.");
		}	
		int data_length = (int)in.length() - IV_LENGTH;
		
		FileInputStream instream = new FileInputStream(in);

		byte iv[] = FileUtils.read(instream, IV_LENGTH);
		byte ciphertext[] = FileUtils.read(instream, data_length);

		instream.close();

		byte plaintext[];
		plaintext = decryptAESCBC(iv, ciphertext, key);
		ciphertext = null;
		System.gc();
		
		String name = StringUtils.truncateAt(in.getName(), "." + extension);
		File out;
		if (decryptName) {
			out = new File(in.getParentFile(), new String(decryptBase64AESECB(name, key)));
		}
		else {
			out = new File(in.getParentFile(), name);
		}
		FileOutputStream ostream = new FileOutputStream(out);		
		ostream.write(plaintext);	
		ostream.close();
		
		return out;
	}
	
	/**
	 * Decrypts the specified file, using the given key.
	 * @param in
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(final File in,
							     final SecretKey key) throws Exception {
		CheckUtils.checkReadable(in, "file: " + in);
		
		if (in.length() > Integer.MAX_VALUE) {
			throw new Exception("Input file longer than int max.");
		}	
		int data_length = (int)in.length() - IV_LENGTH;
		
		try {
			FileInputStream instream = new FileInputStream(in);
	
			byte iv[] = FileUtils.read(instream, IV_LENGTH);
			byte ciphertext[] = FileUtils.read(instream, data_length);
	
			instream.close();
	
			byte plaintext[];
			plaintext = decryptAESCBC(iv, ciphertext, key);
			ciphertext = null;
			return plaintext;
		}
		catch (Exception e) {
			System.out.println("Unable to read: " + in + ".\n");
			throw e;
		}
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
		
}

class Packet {
	public byte initialization_vector[];
	public byte message[];
}


