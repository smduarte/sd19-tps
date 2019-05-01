package tests._4_microgram_min.profiles;

import static microgram.api.java.Result.ErrorCode.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _06_FollowProfilesStats extends MicrogramTest {

	Faker faker;

	public _06_FollowProfilesStats(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ follow Profiles statistics (1)... ] %s ", description));
		super.prepare();

		faker = new Faker(new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {

		JavaProfilesV2 jprofiles = new JavaProfilesV2(null, false);

		Loop.times(2 * NUM_OPS, parallel).forEach(() -> {

			Profile p = genNewProfile();
			Result<Void> expected = jprofiles.createProfile(p);
			doOrThrow(() -> anyProfilesClient().createProfile(p), expected.error(), "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
		});

		List<String> users = new ArrayList<>(jprofiles.search("").value().stream().map(p -> p.getUserId()).collect(Collectors.toList()));

		Loop.times(2 * NUM_OPS, parallel).forEach((prefix) -> {

			String id1 = users.get(random().nextInt(users.size()));
			String id2 = users.get(random().nextInt(users.size()));
			boolean isFollowing = random().nextBoolean();

			Result<Void> expected = jprofiles.follow(id1, id2, isFollowing);
			doOrThrow(() -> anyProfilesClient().follow(id1, id2, isFollowing), expected.error(), "Profiles.follow() failed test... Expected [%s] got: [%s]");
		});

		Loop.items(users, true).forEach((id) -> {
			Profile u = jprofiles.getProfile(id).value();
			Profile q = doOrThrow(() -> anyProfilesClient().getProfile(id), OK, "Profiles.getProfile() failed test... Expected [%s] got: [%s]");
			if (super.equals(u, q))
				throw new FailedTestException("Profiles.getProfile() failed test... <Retrieved Profile data does not match...>");
		});
	}
}
