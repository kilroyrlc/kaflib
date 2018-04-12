package kaflib.utils;

import javax.crypto.SecretKey;

public class KeyPair {
	private final SecretKey outer;
	private final SecretKey inner;

	public KeyPair(final SecretKey outer, final SecretKey inner) {
		this.outer = outer;
		this.inner = inner;
	}
	
	public KeyPair(final String outer, final String inner) throws Exception {
		byte outer_salt[] = inner.substring(0, AESUtils.SALT_LENGTH).getBytes("UTF-8");
		byte inner_salt[] = outer.substring(0, AESUtils.SALT_LENGTH).getBytes("UTF-8");
		this.outer = AESUtils.generateKey(outer, outer_salt);
		this.inner = AESUtils.generateKey(inner, inner_salt);
	}
	
	public SecretKey getOuter() {
		return outer;
	}
	
	public SecretKey getInner() {
		return inner;
	}
}
