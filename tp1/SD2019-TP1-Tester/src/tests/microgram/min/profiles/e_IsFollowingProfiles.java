package tests.microgram.min.profiles;

import loops.Loop;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class e_IsFollowingProfiles extends MicrogramTestOperations {

	public e_IsFollowingProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ isFollowing Profiles... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, false, false);

		super.followProfiles(225, true, () -> true);

		super.followProfiles(75, true, () -> false);

		super.sleep(true);

		Loop.times(2 * NUM_OPS, parallel).forEach((i) -> {
			String userId1 = lProfiles.randomId(), userId2 = lProfiles.randomId();

			Result<Boolean> expected = lProfiles.isFollowing(userId1, userId2);
			Boolean isFollowing = doOrThrow(() -> anyProfilesClient().isFollowing(userId1, userId2), expected.error(), "Profiles.isFollowing() failed test... Expected %s got: [%s]");
			if (expected.isOK() && expected.value() != isFollowing)
				throw new TestFailedException("Profiles.isFollowing() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + isFollowing);
		});
	}
}
