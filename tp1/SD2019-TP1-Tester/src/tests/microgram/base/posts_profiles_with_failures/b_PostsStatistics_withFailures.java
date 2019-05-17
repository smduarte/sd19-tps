package tests.microgram.base.posts_profiles_with_failures;

import java.util.List;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperationsWithFailures;

public class b_PostsStatistics_withFailures extends MicrogramTestOperationsWithFailures {

	public b_PostsStatistics_withFailures(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, true, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles+Posts Service [ Profile's posts statistics (2) ... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50);

		super.generateOnePostPerUser();

		super.generatePosts(200);

		super.deletePosts(50);

		super.sleep(true);

		Loop.items(jprofiles.userIds(), parallel).forEach(user -> {

			Result<List<String>> expected = posts.getPosts(user);

			Profile obtained = doOrThrow(() -> anyProfilesClient().getProfile(user), expected.error(), "Profiles.getProfile() failed test... Expected %s got: [%s]");

			if (expected.isOK() && expected.value().size() != obtained.getPosts())
				throw new TestFailedException("Profiles.getProfile() failed test...<returned wrong result, wanted:" + expected.value().size() + " posts, got " + obtained.getPosts() + " >");
		});

	}

}
