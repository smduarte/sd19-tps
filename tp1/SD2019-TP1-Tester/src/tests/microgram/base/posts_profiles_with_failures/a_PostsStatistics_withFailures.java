package tests._5_microgram_base.failures;

import static microgram.api.java.Result.ErrorCode.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaExtendedPosts;
import smd.microgram.srv.shared.JavaExtendedProfiles;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;
import utils.Lock;
import utils.Sleep;

public class a_PostsStatistics extends MicrogramTest {

	public a_PostsStatistics(boolean parallel, boolean failures, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, failures, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles+Posts Service [ Profile's posts statistics (1)... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		JavaExtendedProfiles jprofiles = new JavaExtendedProfiles(null, false);

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			Profile p = genNewProfile();
			try (Lock l = new Lock(p.getUserId())) {
				Result<Void> expected = jprofiles.createProfile(p);
				doOrThrow(() -> anyProfilesClient().createProfile(p), expected.error(), "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
			}
		});

		List<String> users = new ArrayList<>(jprofiles.search("").value().stream().map(p -> p.getUserId()).collect(Collectors.toList()));

		JavaExtendedPosts jposts = new JavaExtendedPosts(jprofiles, false);

		Loop.times(3 * NUM_OPS, parallel).forEach(() -> {
			String owner = users.get(random().nextInt(users.size()));
			Post p = genNewPost(owner);
			try (Lock l = new Lock(owner)) {
				Result<String> expected = jposts.createPost(p);
				doOrThrow(() -> anyPostsClient().createPost(p), expected.error(), "Posts.createPost() failed test... Expected [%s] got: [%s]");
			}
		});

		Sleep.seconds(5, true);
		Loop.items(users, parallel).forEach(user -> {
			Result<List<String>> posts = jposts.getPosts(user);
			int userPosts = posts.isOK() ? posts.value().size() : 0;
			Profile p = doOrThrow(() -> anyProfilesClient().getProfile(user), OK, "Profiles.getProfile() failed test... Expected [%s] got: [%s]");
			if (p == null || p.getPosts() != userPosts)
				throw new FailedTestException("Profiles.getProfile() failed test...<returned wrong result>, wanted:" + userPosts + " got: " + p.getPosts());

		});
	}

}
