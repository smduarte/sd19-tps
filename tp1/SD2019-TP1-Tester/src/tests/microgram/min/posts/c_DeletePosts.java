package tests.microgram.min.posts;

import loops.Loop;
import microgram.api.java.Result;
import tests.microgram.MicrogramTestOperations;

public class c_DeletePosts extends MicrogramTestOperations {

	public c_DeletePosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ delete Post... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(1, false, false);

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			String postId = super.randomPostId();
			Result<Void> expected = lPosts.deletePost(postId);
			doOrThrow(() -> anyPostsClient().deletePost(postId), expected.error(), "Posts.deletePost() failed test... Expected %s got: [%s]");
		});

		super.generatePosts(200);

		super.sleep(true);

		Loop.items(lPosts.postIds(), parallel).forEach((postId) -> {
			Result<Void> expected = lPosts.deletePost(postId);
			doOrThrow(() -> anyPostsClient().deletePost(postId), expected.error(), "Posts.deletePost() failed test... Expected %s got: [%s]");
		});
	}
}
