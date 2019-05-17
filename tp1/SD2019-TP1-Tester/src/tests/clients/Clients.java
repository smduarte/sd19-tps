package tests.clients;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import tests.clients.rest.RestPostsClient;
import tests.clients.rest.RestProfilesClient;
import tests.clients.soap.SoapPostsClient;
import tests.clients.soap.SoapProfilesClient;

public class Clients {

	private static final CharSequence REST = "/rest/";

	static Map<String, Posts> posts = new HashMap<>();
	static Map<String, Profiles> profiles = new HashMap<>();

	synchronized public static Profiles getProfiles(String server) {
		Profiles res = profiles.get(server);
		if (res == null) {
			if (server.contains(REST))
				res = new RestProfilesClient(URI.create(server));
			else
				res = new SoapProfilesClient(URI.create(server));
			profiles.put(server, res);
		}
		return res;
	}

	synchronized public static Posts getPosts(String server) {
		Posts res = posts.get(server);
		if (res == null) {
			if (server.contains(REST))
				res = new RestPostsClient(URI.create(server));
			else
				res = new SoapPostsClient(URI.create(server));

			posts.put(server, res);
		}
		return res;
	}

}
