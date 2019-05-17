package tests.microgram.min.posts;

import tests.microgram.MicrogramTestOperations;

public class d_LikePosts extends MicrogramTestOperations {

	public d_LikePosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ like Post... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(50, false, false);

		super.generatePosts(200);

		if (!parallel)
			super.likePosts(300, true, () -> random().nextBoolean());
		else {
			super.likePosts(175, true, () -> true);
			super.likePosts(125, true, () -> false);
		}
	}

}
