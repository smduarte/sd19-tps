package tests.microgram.min.posts;

import tests.microgram.MicrogramTestOperations;

public class a_CreatePosts extends MicrogramTestOperations {

	public a_CreatePosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ create Post... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(1, false, false);

		super.generatePosts(200);

	}

}
