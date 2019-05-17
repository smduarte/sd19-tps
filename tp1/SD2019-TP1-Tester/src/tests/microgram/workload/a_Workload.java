package tests.microgram.workload;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import tests.BaseTest.OptionalTest;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;
import utils.Lock;
import utils.Sleep;

@OptionalTest
public class a_Workload extends MicrogramTestOperations {

	enum MicrogramOps {
		GET_PROFILE, FOLLOW, IS_FOLLOWING, CREATE_POST, GET_POST, LIKE_POST, IS_LIKED, GET_POSTS, GET_FEED
	}

	private static final int NUM_USERS = 10;
	private static final int TIMEOUT = 5000;

	public a_Workload(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, false, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("<optional> Testing Microgram App Backend [ simulate multiple users concurrently...] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50, false, false);

		super.generateOnePostPerUser();

		Set<String> userIds = new HashSet<>();
		while (userIds.size() < NUM_USERS)
			userIds.add(lProfiles.randomId());

		int Bound = NUM_USERS * 50;
		AtomicInteger counter = new AtomicInteger(0);

		Loop.items(userIds, parallel).forEach((userId) -> {
			Loop.times(Bound / NUM_USERS, false, counter, Bound).forEach(() -> user(userId));
		});
	}

	private void user(String userId) throws Exception {

		MicrogramOps[] ops = MicrogramOps.values();

		Posts posts = anyPostsClient();
		Profiles profiles = anyProfilesClient();

		switch (ops[random().nextInt(ops.length)]) {
		case GET_PROFILE:
			retry(() -> getProfile(profiles, posts));
			break;
		case FOLLOW:
			retry(() -> follow(userId, profiles, posts));
			break;
		case IS_FOLLOWING:
			retry(() -> isFollowing(userId, profiles, posts));
			break;
		case CREATE_POST:
			retry(() -> createPost(userId, profiles, posts));
			break;
		case GET_POST:
			retry(() -> getPost(profiles, posts));
			break;
		case LIKE_POST:
			retry(() -> like(userId, profiles, posts));
			break;
		case IS_LIKED:
			retry(() -> isLiked(userId, profiles, posts));
			break;
		case GET_POSTS:
			retry(() -> getPosts(userId, profiles, posts));
			break;
		case GET_FEED:
			retry(() -> getFeed(userId, profiles, posts));
			break;
		}
	}

	protected void getProfile(Profiles profiles, Posts posts) throws Exception {
		String userId = lProfiles.randomId();
		Result<Profile> expected = lProfiles.getProfile(userId);
		Profile result = doOrThrow(() -> profiles.getProfile(userId), expected.error(), "Profiles.getProfile() failed test... Expected %s got: [%s]");
		if (expected.isOK() && !super.equals(expected.value(), result)) {
			throw new TestFailedException("Profiles.getProfile() failed test... <Retrieved Profile data does not match...>");
		}
	}

	protected void follow(String userId1, Profiles profiles, Posts posts) throws Exception {
		String userId2 = lProfiles.randomId();
		try (Lock l = new Lock(userId1, userId2)) {
			boolean isFollowing = random().nextBoolean();
			Result<Void> expected = lProfiles.follow(userId1, userId2, isFollowing);
			doOrThrow(() -> profiles.follow(userId1, userId2, isFollowing), expected.error(), "Profiles.follow() failed test... Expected %s got: [%s]");
		}
	}

	protected void isFollowing(String userId1, Profiles profiles, Posts posts) throws Exception {
		String userId2 = lProfiles.randomId();
		Result<Boolean> expected = lProfiles.isFollowing(userId1, userId2);
		Boolean obtained = doOrThrow(() -> profiles.isFollowing(userId1, userId2), expected.error(), "Profiles.isFollowing() failed test... Expected %s got: [%s]");
		if (expected.isOK() && expected.value() != obtained)
			throw new TestFailedException("Profiles.isFollowing() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + obtained);
	}

	protected void createPost(String userId, Profiles profiles, Posts posts) throws Exception {
		Post post = genNewPost(userId);
		try (Lock l = new Lock(userId)) {
			Result<String> expected = tmp.createPost(post);
			String postId = doOrThrow(() -> posts.createPost(post), expected.error(), "Posts.createPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && postId != null)
				lPosts.createPost(postId, post);

		}
	}

	protected void getPost(Profiles profiles, Posts posts) throws Exception {
		String postId = lPosts.randomId();
		Result<Post> expected = lPosts.getPost(postId);
		Post obtained = doOrThrow(() -> posts.getPost(postId), expected.error(), "Posts.getPost() failed test... Expected %s got: [%s]");
		if (expected.isOK() && !super.equals(expected.value(), obtained))
			throw new TestFailedException("Posts.getPost() failed test... <Retrieved Post data does not match...>");
	}

	protected void like(String userId, Profiles profiles, Posts posts) throws Exception {
		String postId = lPosts.randomId();
		try (Lock l = new Lock(postId, userId)) {
			boolean isLiked = random().nextBoolean();
			Result<Void> expected = lPosts.like(postId, userId, isLiked);
			doOrThrow(() -> posts.like(postId, userId, isLiked), expected.error(), "Posts.like() failed test... Expected %s got: [%s]");
		}
	}

	protected void isLiked(String userId, Profiles profiles, Posts posts) throws Exception {
		String post = lPosts.randomId();

		Result<Boolean> expected = lPosts.isLiked(post, userId);
		Boolean obtained = doOrThrow(() -> posts.isLiked(post, userId), expected.error(), "Posts.isLiked() failed test... Expected %s got: [%s]");
		if (expected.isOK() && expected.value() != obtained)
			throw new TestFailedException("Posts.isLiked() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + obtained);
	}

	protected void getPosts(String userId, Profiles profiles, Posts posts) throws Exception {
		Result<List<String>> expected = lPosts.getPosts(userId);
		List<String> obtained = doOrThrow(() -> posts.getPosts(userId), expected.error(), "Posts.getPosts() failed test... Expected %s got: [%s]");
		if (expected.isOK() && !super.equals(expected.value(), obtained))
			throw new TestFailedException("Posts.getPosts() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + obtained);
	}

	protected void getFeed(String userId, Profiles profiles, Posts posts) throws Exception {
		Result<List<String>> expected = lPosts.getFeed(userId);
		List<String> obtained = doOrThrow(() -> posts.getFeed(userId), expected.error(), "Posts.getFeed() failed test... Expected %s got: [%s]");
		if (expected.isOK() && !super.equals(expected.value(), obtained)) {
			throw new TestFailedException("Posts.getFeed() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + obtained);
		}
	}

	@Override
	protected void onFailure() throws Exception {
		println("Note: This test is optional...");
		println("The test can fail due to timing issues, for instance because services exchange data asynchronously - via Kafka for example...");

	}

	void retry(NoisyRunnable r) throws Exception {
		Exception failed = null;
		long deadline = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < deadline) {
			try {
				r.run();
				return;
			} catch (Exception x) {
				failed = x;
				Sleep.ms(1000);
			}
		}
		if (failed != null)
			throw failed;
	}

	static interface NoisyRunnable {
		void run() throws Exception;
	}
}
