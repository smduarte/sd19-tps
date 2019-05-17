package tests.microgram.min.profiles;

import tests.microgram.MicrogramTestOperations;

public class d_FollowProfiles extends MicrogramTestOperations {

	public d_FollowProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ follow Profiles... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, false, false);

		if (!parallel)
			super.followProfiles(300, true, () -> random().nextBoolean());
		else {
			super.followProfiles(175, true, () -> true);
			super.followProfiles(125, true, () -> false);
		}

	}
}
