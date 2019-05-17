package tests.microgram.min.profiles;

import static utils.Log.Log;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class g_FollowProfilesStats extends MicrogramTestOperations {

	public g_FollowProfilesStats(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ follow Profiles statistics (2)... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, false, false);

		super.followProfiles(225, true, () -> true);

		super.followProfiles(75, true, () -> false);

		super.deleteProfiles(25);

		super.sleep(true);

		Loop.items(lProfiles.userIds(), parallel).forEach((user) -> {

			Result<Profile> expected = lProfiles.getProfile(user);

			Profile obtained = doOrThrow(() -> anyProfilesClient().getProfile(user), expected.error(), "Profiles.getProfile() failed test... Expected %s got: [%s]");

			if (expected.isOK() && !super.equals(expected.value(), obtained)) {
				Log.fine(String.format("Expected: {%s} Got: {%s}", expected.value(), obtained));
				throw new TestFailedException("Profiles.getProfile() failed test... <Retrieved Profile data does not match...>");
			}
		});
	}
}
