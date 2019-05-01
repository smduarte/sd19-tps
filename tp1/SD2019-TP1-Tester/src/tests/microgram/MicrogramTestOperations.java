package tests.microgram;

import static microgram.api.java.Result.ErrorCode.CONFLICT;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import utils.Lock;

public class MicrogramTestOperations extends MicrogramTest {

	final LocalProfiles jprofiles = new LocalProfiles(random());
	final LocalPosts tmp = new LocalPosts(jprofiles), posts = new LocalPosts(jprofiles, random());

	public MicrogramTestOperations(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, false, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	protected void generateProfiles(int numProfiles) {

		Loop.times(numProfiles, parallel).forEach(() -> {
			Profile p = genNewProfile();
			try (Lock l = new Lock(p.getUserId())) {
				Result<Void> expected = jprofiles.createProfile(p);
				doOrThrow(() -> anyProfilesClient().createProfile(p), expected.error(), "1 - Profiles.createProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
				if_NotFailed(() -> anyProfilesClient().createProfile(p), CONFLICT, "2 - Profiles.createProfile(" + p.getUserId() + ") failed test... Expected %s got: [%s]");
			}
		});

	}
}
