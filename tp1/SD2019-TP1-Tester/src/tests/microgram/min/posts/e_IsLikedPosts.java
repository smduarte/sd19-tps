package tests.microgram.min.posts;

import loops.Loop;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class e_IsLikedPosts extends MicrogramTestOperations {

	public e_IsLikedPosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ isLiked Post... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50, false, false);

		super.generatePosts(200);

		super.likePosts(250, false, () -> true);

		super.likePosts(50, false, () -> false);

		super.sleep(true);

		Loop.times(300, parallel).forEach(() -> {

			String post = lPosts.randomId(), user = lProfiles.randomId();

			Result<Boolean> expected = lPosts.isLiked(post, user);
			Boolean obtained = doOrThrow(() -> anyPostsClient().isLiked(post, user), expected.error(), "Posts.isLiked() failed test... Expected %s got: [%s]");
			if (expected.isOK() && expected.value() != obtained)
				throw new TestFailedException("Posts.isLiked() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + obtained);

		});
	}
}
