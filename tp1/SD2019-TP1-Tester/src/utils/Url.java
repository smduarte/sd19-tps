package utils;

import java.net.MalformedURLException;
import java.net.URL;

public class Url {

	public static URL decode(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static URL from(String urlString) {
		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
