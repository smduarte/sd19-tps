package tests.microgram.min.posts;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;
import static utils.Log.Log;

import loops.Loop;
import tests.microgram.MicrogramTestOperations;

public class g_GetUserFeed extends MicrogramTestOperations {

	public g_GetUserFeed(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ getFeed... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50, false, false);

		super.followProfiles(150, true, () -> true);

		super.followProfiles(50, true, () -> false);

		super.generateOnePostPerUser();

		super.generatePosts(200);

		Log.fine("Call getFeed...");
		Loop.times(250, parallel).forEach((i) -> {
			String user = lProfiles.randomId();
			doOrThrow(() -> anyPostsClient().getFeed(user), either(OK, NOT_FOUND), "Posts.getFeed() failed test... Expected %s got: [%s]");
		});
	}
}
