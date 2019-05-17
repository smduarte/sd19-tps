package tests.microgram;

import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;
import static utils.Log.Log;

import java.util.function.Supplier;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import utils.Lock;

public class MicrogramTestOperationsWithFailures extends MicrogramTest {

	final protected LocalProfiles jprofiles = new LocalProfiles(random());
	final protected LocalPosts posts = new LocalPosts(jprofiles, random());

	final private LocalPosts tmp = new LocalPosts(jprofiles);

	public MicrogramTestOperationsWithFailures(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		this(parallel, false, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	public MicrogramTestOperationsWithFailures(boolean parallel, boolean failures, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, failures, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	protected void generateProfiles(int numProfiles) {
		Log.fine("Generate profiles...");

		Loop.times(numProfiles, parallel).forEach(() -> {
			Profile p = genNewProfile();
			try (Lock l = new Lock(p.getUserId())) {
				jprofiles.createProfile(p);
				doOrThrow(() -> anyProfilesClient().createProfile(p), either(OK, CONFLICT), "Profiles.createProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void deleteProfiles(int maxProfiles) {
		Log.fine("Delete profiles...");

		Loop.times(maxProfiles, parallel).forEach(() -> {
			String userId = jprofiles.randomId();
			try (Lock l = new Lock(userId)) {
				jprofiles.deleteProfile(userId);
				doOrThrow(() -> anyProfilesClient().deleteProfile(userId), either(OK, NOT_FOUND), "Profiles.deleteProfile() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void followProfiles(int operations, Supplier<Boolean> isFollowingFunc) {
		Log.fine("Follow profiles...");

		Loop.times(operations, parallel).forEach(() -> {
			String userId1 = jprofiles.randomId(), userId2 = jprofiles.randomId();
			try (Lock l = new Lock(userId1, userId2)) {
				boolean isFollowing = isFollowingFunc.get();

				jprofiles.follow(userId1, userId2, isFollowing);
				doOrThrow(() -> anyProfilesClient().follow(userId1, userId2, isFollowing), either(OK, NOT_FOUND, CONFLICT), "Profiles.follow() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void generateOnePostPerUser() {
		Log.fine("Generating one post per user...");

		Loop.items(jprofiles.userIds(), parallel).forEach((userId) -> {
			Post post = genNewPost(userId);
			Result<String> expected = tmp.createPost(post);
			String postId = doOrThrow(() -> anyPostsClient().createPost(post), either(OK, CONFLICT), "Posts.createPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && postId != null)
				posts.createPost(postId, post);
		});

	}

	protected void generatePosts(int numPosts) {
		Log.fine("Generate posts...");

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			Post post = genNewPost(jprofiles.randomId());

			Result<String> expected = tmp.createPost(post);
			String postId = doOrThrow(() -> anyPostsClient().createPost(post), expected.error(), "Posts.createPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && postId != null)
				posts.createPost(postId, post);
		});

	}

	protected void deletePosts(int maxPosts) {
		Log.fine("Delete posts...");

		Loop.times(maxPosts, parallel).forEach(() -> {
			String postId = posts.randomId();
			try (Lock l = new Lock(postId)) {
				posts.deletePost(postId);
				doOrThrow(() -> anyPostsClient().deletePost(postId), either(OK, NOT_FOUND), "Posts.deletePost() failed test... Expected %s got: [%s]");
			}
		});
	}
}
