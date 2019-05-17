package tests.microgram.base.posts_profiles_with_failures;

import java.util.List;

import loops.Loop;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperationsWithFailures;

public class c_UserFeed_withFailures extends MicrogramTestOperationsWithFailures {

	public c_UserFeed_withFailures(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, true, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles+Posts Service [ getFeed ... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50);

		super.followProfiles(150, () -> true);

		super.followProfiles(50, () -> false);

		super.generateOnePostPerUser();

		super.generatePosts(200);

		super.sleep(true);

		Loop.items(jprofiles.userIds(), parallel).forEach(user -> {

			Result<List<String>> expected = posts.getFeed(user);

			List<String> obtained = doOrThrow(() -> anyPostsClient().getFeed(user), expected.error(), "Posts.getFeed() failed test... Expected %s got: [%s]");

			if (expected.isOK() && !super.equals(expected.value(), obtained))
				throw new TestFailedException("Posts.getFeed() failed test...<returned wrong result, wanted: " + expected.value() + " got: " + obtained + " >");
		});
	}

}
