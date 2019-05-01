package tests.microgram.min.profiles;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import tests.FailedTestException;
import tests.microgram.MicrogramTestOperations;

public class h_DeleteProfiles extends MicrogramTestOperations {

	public h_DeleteProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
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

		Loop.items(jprofiles.userIds(), parallel).forEach((userId) -> {
			Result<Profile> expected = jprofiles.getProfile(userId);
			Profile result = doOrThrow(() -> anyProfilesClient().getProfile(userId), expected.error(), "Profiles.getProfile() failed test... Expected %s got: [%s]");
			if (expected.isOK() && !super.equals(expected.value(), result))
				throw new FailedTestException("Profiles.getProfile() failed test... <Retrieved Profile data does not match...");
		});

	}
}
