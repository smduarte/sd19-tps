package tests.microgram.base.posts_profiles;

import java.util.List;

import loops.Loop;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class c_UserFeed extends MicrogramTestOperations {

	public c_UserFeed(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, false, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles+Posts Service [ getFeed ... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50, false, false);

		super.followProfiles(150, true, () -> true);

		super.followProfiles(50, true, () -> false);

		super.generateOnePostPerUser();

		super.generatePosts(200);

		super.sleep(true);

		Loop.items(lProfiles.userIds(), parallel).forEach(user -> {

			Result<List<String>> expected = lPosts.getFeed(user);
			List<String> obtained = doOrThrow(() -> anyPostsClient().getFeed(user), expected.error(), "Posts.getFeed() failed test... Expected %s got: [%s]");
			if (expected.isOK() && !super.equals(expected.value(), obtained))
				throw new TestFailedException("Posts.getFeed() failed test...<returned wrong result, wanted: " + expected.value() + " got: " + obtained + " >");
		});
	}

}
