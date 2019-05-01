package tests._5_microgram_base.failures;

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

public class c_UserFeed extends MicrogramTest {

	public c_UserFeed(boolean parallel, boolean failures, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, failures, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles+Posts Service [ getFeed ... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		JavaExtendedProfiles jprofiles = new JavaExtendedProfiles(null, false);

		Loop.times(NUM_OPS / 2, parallel).forEach(() -> {
			Profile p = genNewProfile();
			try (Lock l = new Lock(p.getUserId())) {
				Result<Void> res = jprofiles.createProfile(p);
				if (!parallel || res.isOK()) {
					doOrThrow(() -> anyProfilesClient().createProfile(p), res.error(), "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
				}
			}
		});

		List<String> users = new ArrayList<>(jprofiles.search("").value().stream().map(p -> p.getUserId()).collect(Collectors.toList()));

		Loop.times(3 * NUM_OPS, parallel).forEach((prefix) -> {
			int i1 = random().nextInt(users.size()), i2 = random().nextInt(users.size());

			String id1 = users.get(i1);
			String id2 = users.get(i2);

			try (Lock l = new Lock(i1, i2)) {
				boolean isFollowing = random().nextBoolean();
				Result<Void> expected = jprofiles.follow(id1, id2, isFollowing);
				doOrThrow(() -> anyProfilesClient().follow(id1, id2, isFollowing), expected.error(), "Profiles.follow() failed test... Expected [%s] got: [%s]");
			}
		});

		JavaExtendedPosts jposts = new JavaExtendedPosts(jprofiles, false);

		Loop.times(3 * NUM_OPS, parallel).forEach(() -> {
			String owner = users.get(random().nextInt(users.size()));
			Post p = genNewPost(owner);
			try (Lock l = new Lock(p.getOwnerId())) {
				Result<String> expected = jposts.createPost(p);
				doOrThrow(() -> anyPostsClient().createPost(p), expected.error(), "Posts.createPost() failed test... Expected [%s] got: [%s]");
			}
		});

		super.sleep(true);

		Loop.items(users, parallel).forEach(user -> {
			Result<List<String>> expected = jposts.getFeed(user);
			List<String> result = doOrThrow(() -> anyPostsClient().getFeed(user), expected.error(), "Posts.getFeed() failed test... Expected [%s] got: [%s]");
			if (expected.isOK() && !super.equals(expected.value(), result))
				throw new FailedTestException("Posts.getFeed() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + result);
		});
	}

}
