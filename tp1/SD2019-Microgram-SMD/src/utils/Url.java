package utils;

import java.net.MalformedURLException;
import java.net.URL;

public class Url {

	public static URL from(String s) {
		try {
			return new URL(s);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
