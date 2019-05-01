package tests.microgram;

import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;

import java.util.List;

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
		Loop.times(numProfiles, parallel).forEach(() -> {
			Profile p = genNewProfile();
			try (Lock l = new Lock(p.getUserId())) {
				jprofiles.createProfile(p);
				doOrThrow(() -> anyProfilesClient().createProfile(p), either(OK, CONFLICT), "Profiles.createProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void deleteProfiles(int maxProfiles) {
		Loop.times(maxProfiles, parallel).forEach(() -> {
			String userId = jprofiles.randomId();
			try (Lock l = new Lock(userId)) {
				jprofiles.deleteProfile(userId);
				doOrThrow(() -> anyProfilesClient().deleteProfile(userId), either(OK, NOT_FOUND), "Profiles.deleteProfile() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void followProfiles(int operations, boolean addRandomUsers) {

		List<String> userIds = jprofiles.userIds();

		if (addRandomUsers)
			userIds.add(super.randomUserId());

		Loop.times(operations, parallel).forEach(() -> {
			int i1 = random().nextInt(userIds.size()), i2 = random().nextInt(userIds.size());
			try (Lock l = new Lock(i1, i2)) {
				String id1 = userIds.get(i1);
				String id2 = userIds.get(i2);
				boolean isFollowing = random().nextBoolean();

				Result<Void> expected = jprofiles.follow(id1, id2, isFollowing);
				doOrThrow(() -> anyProfilesClient().follow(id1, id2, isFollowing), expected.error(), "Profiles.follow() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void generateOnePostPerUser() {
		Loop.items(jprofiles.userIds(), parallel).forEach((userId) -> {
			Post post = genNewPost(userId);
			Result<String> expected = tmp.createPost(post);
			String postId = doOrThrow(() -> anyPostsClient().createPost(post), expected.error(), "Posts.createPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && postId != null)
				posts.createPost(postId, post);
		});

	}

	protected void generatePosts(int numPosts, boolean createTwice) {

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			Post post = genNewPost(jprofiles.randomId());

			Result<String> expected = tmp.createPost(post);
			String postId = doOrThrow(() -> anyPostsClient().createPost(post), expected.error(), "Posts.createPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && postId != null)
				posts.createPost(postId, post);

			if (createTwice)
				doOrThrow(() -> anyPostsClient().createPost(post), CONFLICT, "Posts.createPost() failed test... Expected %s got: [%s]");
		});

	}

	protected void deletePosts(int maxPosts) {
		Loop.times(maxPosts, parallel).forEach(() -> {
			String postId = posts.randomId();
			try (Lock l = new Lock(postId)) {
				Result<Void> expected = posts.deletePost(postId);
				doOrThrow(() -> anyPostsClient().deletePost(postId), expected.error(), "Posts.deletePost() failed test... Expected %s got: [%s]");
			}
		});
	}

	protected void likePosts(int operations, boolean addRandomPosts) {

		List<String> postIds = posts.postIds();

		if (addRandomPosts)
			postIds.add(super.randomPostId());

		Loop.times(operations, parallel).forEach(() -> {
			int i1 = random().nextInt(postIds.size()), i2 = random().nextInt(postIds.size());
			try (Lock l = new Lock(i1, i2)) {
				String userId = jprofiles.randomId();
				String postId = postIds.get(random().nextInt(postIds.size()));
				boolean isLiked = random().nextBoolean();

				Result<Void> expected = posts.like(postId, userId, isLiked);
				doOrThrow(() -> anyPostsClient().like(postId, userId, isLiked), expected.error(), "Posts.like() failed test... Expected %s got: [%s]");
			}
		});
	}

}
