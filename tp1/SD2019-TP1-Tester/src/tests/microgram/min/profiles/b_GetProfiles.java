package tests.microgram.min.profiles;

import static utils.Log.Log;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class b_GetProfiles extends MicrogramTestOperations {

	public b_GetProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ get Profile... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, false, false);

		Loop.items(lProfiles.userIds(), parallel).forEach((userId) -> {
			Result<Profile> expected = lProfiles.getProfile(userId);
			Profile result = doOrThrow(() -> anyProfilesClient().getProfile(userId), expected.error(), "Profiles.getProfile() failed test... Expected %s got: [%s]");
			if (expected.isOK() && !super.equals(expected.value(), result)) {
				Log.fine(String.format("Expected: {%s} Got: {%s}", expected.value(), result));
				throw new TestFailedException("Profiles.getProfile() failed test... <Retrieved Profile data does not match...>");
			}
		});

	}
}
