package tests.microgram.min.profiles;

import java.util.List;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class h_DeleteProfiles extends MicrogramTestOperations {

	public h_DeleteProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ delete Profile... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, false, false);

		Loop.items(lProfiles.userIds(), parallel).forEach((userId) -> {

			Result<Void> expected = lProfiles.deleteProfile(userId);

			doOrThrow(() -> anyProfilesClient().deleteProfile(userId), expected.error(), "Profiles.deleteProfile() failed test... Expected %s got: [%s]");
		});

		Result<List<Profile>> expected = lProfiles.search("");

		List<Profile> obtained = doOrThrow(() -> anyProfilesClient().search(""), expected.error(), "Profiles.search() failed test... Expected %s got: [%s]");

		if (expected.isOK() && expected.value().size() != obtained.size())
			throw new TestFailedException("Profiles.search() failed test...<search did not produce the expected results>, wanted:" + expected.value() + " got: " + obtained);

	}
}
