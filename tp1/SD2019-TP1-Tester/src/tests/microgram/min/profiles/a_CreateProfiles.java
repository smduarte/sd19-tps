package tests.microgram.min.profiles;

import tests.microgram.MicrogramTestOperations;

public class a_CreateProfiles extends MicrogramTestOperations {

	public a_CreateProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ create Profile... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, true, true);

	}

}
