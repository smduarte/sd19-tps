package tests.microgram.min.posts;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;
import static utils.Log.Log;

import java.util.List;

import loops.Loop;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class f_GetUserPosts extends MicrogramTestOperations {

	public f_GetUserPosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ getPosts... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(75, false, false);

		super.generateOnePostPerUser();

		super.generatePosts(250);

		Log.fine("Read back posts...");
		Loop.items(lProfiles.userIds(), parallel).forEach((i) -> {
			String user = lProfiles.randomId();
			Result<List<String>> expected = lPosts.getPosts(user);
			List<String> obtained = doOrThrow(() -> anyPostsClient().getPosts(user), either(OK, NOT_FOUND), "Posts.getPosts() failed test... Expected %s got: [%s]");
			if (expected.isOK() && !super.equals(expected.value(), obtained))
				throw new TestFailedException("Posts.getPosts() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + obtained);
		});
	}
}
