package utils;

import java.security.MessageDigest;

public class Digest {

	final MessageDigest sha256;
	
	public Digest() throws Exception {
		sha256 = MessageDigest.getInstance("SHA-256");
	}
	
	public void update( byte[] data ) {
		sha256.update( data );
	}
	
	public void update( String data ) {
		update( data.getBytes() );
	}
	
	public byte[] digest() {
		return sha256.digest();
	}
}
