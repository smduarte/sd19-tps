package smd.microgram.clt;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import smd.discovery.Discovery;
import smd.microgram.clt.rest.RestPostsClient;
import smd.microgram.clt.rest.RestProfilesClient;
import smd.microgram.srv.rest.posts.PostsRestServer;
import smd.microgram.srv.rest.profiles.ProfilesRestServer;

public class Clients {

	private static final CharSequence REST = "/rest/";

	static Map<String, Posts> posts = new HashMap<>();
	static Map<String, Profiles> profiles = new HashMap<>();

	public static Profiles getProfiles() {

		Optional<Profiles> res = profiles.values().stream().findAny();
		if (res.isPresent())
			return res.get();

		URI[] uris;
		while ((uris = Discovery.findUrisOf(ProfilesRestServer.SERVICE, 1)).length == 0)
			;

		return getProfiles(uris[0].toString());
	}

	public static Posts getPosts() {
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
				res = null;
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
				res = null;

			posts.put(server, res);
		}
		return res;
	}

}
