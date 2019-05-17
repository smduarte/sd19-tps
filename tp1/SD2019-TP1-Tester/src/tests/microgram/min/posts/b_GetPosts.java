package tests.microgram.min.posts;

import static utils.Log.Log;

import loops.Loop;
import microgram.api.Post;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class b_GetPosts extends MicrogramTestOperations {

	public b_GetPosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ get Post... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(1, false, false);

		super.generatePosts(200);

		Loop.items(lPosts.postIds(), parallel).forEach((id) -> {
			Result<Post> expected = lPosts.getPost(id);
			Post obtained = doOrThrow(() -> anyPostsClient().getPost(id), expected.error(), "Posts.getPost() failed test... Expected %s got: [%s]");
			if (expected.isOK() && !super.equals(expected.value(), obtained)) {
				Log.fine(String.format("Expected: {%s} Got: {%s}", expected.value(), obtained));
				throw new TestFailedException("Posts.getPost() failed test... <Retrieved Post data does not match...>");
			}
		});
	}
}
