package tests.microgram;

import static utils.Log.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import tests.BaseTest;
import tests.clients.Clients;
import tests.clients.rest.RestMediaClient;
import tests.servers.MediaServer;
import tests.servers.MicrogramServer;
import tests.servers.PostsServer;
import tests.servers.ProfilesServer;
import utils.Random;
import utils.Sleep;
import utils.Threading;

public class MicrogramTest extends BaseTest {

	protected static final int NUM_OPS = 100;

	final int restMediaServers;

	final int restPostsServers, soapPostsServers;
	final int restProfilesServers, soapProfilesServers;
	protected final String description;
	protected final boolean parallel;
	protected final boolean failures;

	final Faker faker = new Faker(new Locale("en"), random());

	final List<PostsServer> postsInstances = new ArrayList<>();
	final List<MediaServer> mediaInstances = new ArrayList<>();
	final List<ProfilesServer> profilesInstances = new ArrayList<>();

	public MicrogramTest(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		this(parallel, false, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	public MicrogramTest(boolean parallel, boolean failures, int restMediaServers, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		this.restMediaServers = restMediaServers;
		this.restPostsServers = restPostsServers;
		this.soapPostsServers = soapPostsServers;
		this.restProfilesServers = restProfilesServers;
		this.soapProfilesServers = soapProfilesServers;
		this.parallel = parallel;
		this.failures = failures;
		this.description = description;
	}

	@Override
	protected void prepare() throws Exception {

		String extraArgs = extraArgs();

		if (restMediaServers > 0)
			Log.info(String.format("Starting %d REST Media service instances <%s>", restMediaServers, MediaServer.mainClass()));
		for (int i = 0; i < restMediaServers; i++)
			mediaInstances.add(new MediaServer().start(this));

		if (restPostsServers > 0)
			Log.info(String.format("Starting %d REST Posts service instances <%s>", restPostsServers, PostsServer.mainClass(true)));

		for (int i = 0; i < restPostsServers; i++)
			postsInstances.add(new PostsServer().start(true, this, extraArgs));

		if (soapPostsServers > 0)
			Log.info(String.format("Starting %d SOAP Posts service instances <%s>", soapPostsServers, PostsServer.mainClass(false)));

		for (int i = 0; i < soapPostsServers; i++)
			postsInstances.add(new PostsServer().start(false, this, extraArgs));

		if (restProfilesServers > 0)
			Log.info(String.format("Starting %d REST Profiles service instances <%s>", restProfilesServers, ProfilesServer.mainClass(true)));

		for (int i = 0; i < restProfilesServers; i++)
			profilesInstances.add(new ProfilesServer().start(true, this, extraArgs));

		if (soapProfilesServers > 0)
			Log.info(String.format("Starting %d SOAP Profiles service instances <%s>", soapProfilesServers, ProfilesServer.mainClass(false)));

		for (int i = 0; i < soapProfilesServers; i++)
			profilesInstances.add(new ProfilesServer().start(false, this, extraArgs));

		sleep(true);

		if (failures)
			startNetworkFailures();
	}

	synchronized protected Posts anyPostsClient() {
		PostsServer srv = postsInstances.get(random().nextInt(postsInstances.size()));
		return toPostsClient(srv.uri());
	}

	synchronized protected Media anyMediaClient() {
		MediaServer srv = mediaInstances.get(random().nextInt(mediaInstances.size()));
		return toMediaClient(srv.uri());
	}

	synchronized protected Profiles anyProfilesClient() {
		ProfilesServer srv = profilesInstances.get(random().nextInt(profilesInstances.size()));
		return toProfilesClient(srv.uri());
	}

	static Profiles toProfilesClient(URI uri) {
		return Clients.getProfiles(uri.toString());
	}

	static Posts toPostsClient(URI uri) {
		return Clients.getPosts(uri.toString());
	}

	static Media toMediaClient(URI uri) {
		return new RestMediaClient(uri);
	}

	synchronized protected Profile genNewProfile() {
		Profile res = new Profile();
		Name name = faker.name();
		res.setFollowers(0);
		res.setFollowing(0);
		res.setFullName(name.fullName());
		res.setPosts(0);
		res.setPhotoUrl(faker.internet().url());
		res.setUserId((name.lastName().toLowerCase() + (random().nextInt(100))).replace("'", ""));
		return res;
	}

	synchronized protected Post genNewPost(String owner) {
		Post res = new Post();
		Address addr = faker.address();
		res.setOwnerId(owner);
		res.setLocation(addr.cityName() + "," + addr.country());
		res.setTimestamp(System.currentTimeMillis());
		res.setLikes(random().nextInt() >>> 1);
		res.setMediaUrl(faker.internet().url());
		res.setPostId(null);
		return res;
	}

	protected String randomUserId() {
		return Random.key128();
	}

	protected String randomPostId() {
		return Random.key128();
	}

	protected boolean equals(Post a, Post b) {
		boolean res = a.getLocation().equals(b.getLocation());
		res &= a.getMediaUrl().equals(b.getMediaUrl());
		res &= a.getOwnerId().equals(b.getOwnerId());
		res &= a.getTimestamp() == b.getTimestamp();
		return res;
	}

	protected boolean equals(Profile a, Profile b) {
		boolean res = a.getFullName().equals(b.getFullName());
		res &= a.getPhotoUrl().equals(b.getPhotoUrl());
		res &= a.getPosts() == b.getPosts();
		res &= a.getFollowers() == b.getFollowers();
		res &= a.getFollowing() == b.getFollowing();
		return res;
	}

	private String extraArgs() {
		return String.format("-profiles %s -posts %s", (restProfilesServers + soapProfilesServers), (restPostsServers + soapPostsServers));
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> either(T... args) {
		return Arrays.asList(args);
	}

	static protected <T> boolean equals(List<T> a, List<T> b) {
		return a.size() == b.size() && new HashSet<>(a).equals(new HashSet<>(b));
	}

	protected void startNetworkFailures() {
		List<MicrogramServer<?>> servers = new ArrayList<>();
		servers.addAll(postsInstances);
		servers.addAll(profilesInstances);

		for (MicrogramServer<?> server : servers)
			Threading.newThread(true, () -> {
				try {
					for (;;) {
						int duration = 300 + random().nextInt(TIME_BETWEEN_FAILURES);
						server.container().networkingFailure(duration, server.uri().getPort());
						Sleep.ms(random().nextInt(3 * TIME_BETWEEN_FAILURES));
					}
				} catch (Exception x) {
				}
			}).start();
	}

	private static final int TIME_BETWEEN_FAILURES = 1000;
}
