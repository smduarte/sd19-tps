package microgram.impl.clt;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import discovery.Discovery;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.impl.clt.rest.RestPostsClient;
import microgram.impl.clt.rest.RestProfilesClient;
import microgram.impl.clt.soap.SoapPostsClient;
import microgram.impl.clt.soap.SoapProfilesClient;
import microgram.impl.srv.rest.posts.PostsRestServer;
import microgram.impl.srv.rest.profiles.ProfilesRestServer;

/**
 * A factory to create service clients based on the contents of the discovery
 * URI.
 */
public class Clients {

	private static final CharSequence REST = "/rest/";

	static Map<String, Posts> posts = new HashMap<>();
	static Map<String, Profiles> profiles = new HashMap<>();

	synchronized public static Profiles getProfiles() {

		Optional<Profiles> res = profiles.values().stream().findAny();
		if (res.isPresent())
			return res.get();

		URI[] uris;
		while ((uris = Discovery.findUrisOf(ProfilesRestServer.SERVICE, 1)).length == 0)
			;

		return getProfiles(uris[0].toString());
	}

	synchronized public static Posts getPosts() {
		Optional<Posts> res = posts.values().stream().findAny();
		if (res.isPresent())
			return res.get();

		URI[] uris;
		while ((uris = Discovery.findUrisOf(PostsRestServer.SERVICE, 1)).length == 0)
			;

		return getPosts(uris[0].toString());
	}

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
