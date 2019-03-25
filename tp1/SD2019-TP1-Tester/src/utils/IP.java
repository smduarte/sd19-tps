package utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class IP {

	public static InetAddress localHostAddress() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String localHostAddressString() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String localHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String addressString(String hostname) {
		try {
			return InetAddress.getByName(hostname).getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static boolean isLocalAddress(String locator) {
		InetAddress addr;
		try {
			locator = locator.replace("*", "0.0.0.0");
			addr = InetAddress.getByName(locator);
			return addr.isAnyLocalAddress() || addr.isLoopbackAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static InetSocketAddress toInetAddress(String sockAddr ) {
		String[] tokens = sockAddr.split(":");
		return new InetSocketAddress( tokens[0].trim(), Integer.valueOf( tokens[1].trim()));
	}
	
	public static String hostAndPort(URI uri ) {
		try {
			return InetAddress.getByName( uri.getHost() ) + ":" + uri.getPort();			
		} catch( Exception x ) {
			throw new RuntimeException( x.getMessage() );
		}
	}
}
