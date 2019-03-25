package tests;

public class InSecurity {

	static public void setKeyStore(String name ) {
		System.setProperty("javax.net.ssl.keyStore", String.format("/tls/%s.jks", name));
		System.setProperty("javax.net.ssl.keyStorePassword", name);
	}
	
	static public void setTrustStore(String name, String password ) {
		System.setProperty("javax.net.ssl.trustStore", name);
		System.setProperty("javax.net.ssl.trustStorePassword", password);		
	}

}
