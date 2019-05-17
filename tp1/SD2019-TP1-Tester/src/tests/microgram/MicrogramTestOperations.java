package tests.microgram;

import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static utils.Log.Log;

import java.util.function.Supplier;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import utils.Lock;
import utils.RandomList;

public class MicrogramTestOperations extends MicrogramTest {

	final protected LocalProfiles lProfiles = new LocalProfiles(random());
	final protected LocalPosts lPosts = new LocalPosts(lProfiles, random());

	final protected LocalPosts tmp = new LocalPosts(lProfiles);

	public MicrogramTestOperations(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		this(parallel, false, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	public MicrogramTestOperations(boolean parallel, boolean failures, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, failures, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	protected void generateProfiles(int numProfiles, boolean getBefore, boolean createTwice) {
		Log.fine("Generate profiles...");

		Loop.times(numProfiles, parallel).forEach(() -> {
			Profile p = genNewProfile();
			try (Lock l = new Lock(p.getUserId())) {
				if (getBefore) {
					Result<Profile> expected = lProfiles.getProfile(p.getUserId());
					doOrThrow(() -> anyProfilesClient().getProfile(p.getUserId()), expected.error(), "Profiles.getProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
				}

				Result<Void> expected = lProfiles.createProfile(p);
				doOrThrow(() -> anyProfilesClient().createProfile(p), expected.error(), "Profiles.createProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
				if (createTwice)
					doOrThrow(() -> anyProfilesClient().createProfile(p), CONFLICT, "Profiles.createProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void deleteProfiles(int maxProfiles) {
		Log.fine("Delete profiles...");

		Loop.times(maxProfiles, parallel).forEach(() -> {
			String userId = lProfiles.randomId();
			try (Lock l = new Lock(userId)) {
				Result<Void> expected = lProfiles.deleteProfile(userId);
				doOrThrow(() -> anyProfilesClient().deleteProfile(userId), expected.error(), "Profiles.deleteProfile() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void followProfiles(int operations, boolean addRandomUsers, Supplier<Boolean> isFollowingFunc) {
		Log.fine("Follow profiles...");

		RandomList<String> userIds = lProfiles.userIds();

		if (addRandomUsers)
			userIds.add(super.randomUserId());

		Loop.times(operations, parallel).forEach(() -> {
			String userId1 = userIds.randomElement(), userId2 = userIds.randomElement();
			try (Lock l = new Lock(userId1, userId2)) {
				boolean isFollowing = isFollowingFunc.get();

				Result<Void> expected = lProfiles.follow(userId1, userId2, isFollowing);
				doOrThrow(() -> anyProfilesClient().follow(userId1, userId2, isFollowing), expected.error(), "Profiles.follow() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void generateOnePostPerUser() {
		Log.fine("Generating one post per user...");

		Loop.items(lProfiles.userIds(), parallel).forEach((userId) -> {
			Post post = genNewPost(userId);
			try (Lock l = new Lock(userId)) {
				Result<String> expected = tmp.createPost(post);

				String postId = doOrThrow(() -> anyPostsClient().createPost(post), expected.error(), "Posts.createPost() failed test... Expected %s got: [%s]");
				if (expected.isOK() && postId != null)
					lPosts.createPost(postId, post);
			}
		});

	}

	protected void generatePosts(int numPosts) {
		Log.fine("Generate posts...");

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			Post post = genNewPost(lProfiles.randomId());

			Result<String> expected = tmp.createPost(post);
			String postId = doOrThrow(() -> anyPostsClient().createPost(post), expected.error(), "Posts.createPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && postId != null)
				lPosts.createPost(postId, post);
		});

	}

	protected void deletePosts(int maxPosts) {
		Log.fine("Delete posts...");

		Loop.times(maxPosts, parallel).forEach(() -> {
			String postId = lPosts.randomId();
			try (Lock l = new Lock(postId)) {
				Result<Void> expected = lPosts.deletePost(postId);
				doOrThrow(() -> anyPostsClient().deletePost(postId), expected.error(), "Posts.deletePost() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void likePosts(int operations, boolean addRandomPosts, Supplier<Boolean> isLikedFunc) {
		Log.fine("Like posts...");

		RandomList<String> postIds = lPosts.postIds();

		if (addRandomPosts)
			postIds.add(super.randomPostId());

		Loop.times(operations, parallel).forEach(() -> {
			String postId = postIds.randomElement(), userId = lProfiles.randomId();
			try (Lock l = new Lock(postId, userId)) {
				boolean isLiked = isLikedFunc.get();
				Result<Void> expected = lPosts.like(postId, userId, isLiked);
				doOrThrow(() -> anyPostsClient().like(postId, userId, isLiked), expected.error(), "Posts.like() failed test... Expected %s got: [%s]");
			}
		});
	}
}
