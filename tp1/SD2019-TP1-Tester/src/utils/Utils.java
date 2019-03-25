package utils;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {

	public static void sleep( long ms) {
		try {
			Thread.sleep( ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static URL toURL( String url ) {
		try {
			return new URL( url );
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
