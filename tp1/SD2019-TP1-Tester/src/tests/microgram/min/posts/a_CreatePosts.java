package tests._4_microgram_min.posts;

import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.OK;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaPostsV2;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests._4_microgram_min.MicrogramTest;

public class _01_CreatePosts extends MicrogramTest {

	public _01_CreatePosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ create posts... ] %s ", description));
		super.prepare();		
	}

	@Override
	protected void execute() throws Exception {
		
		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);
		
		Profile owner = genNewProfile();
		jprofiles.createProfile( owner );
		doOrThrow( () -> anyProfilesClient().createProfile(owner), OK,  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
		
		JavaPostsV2 jposts = new JavaPostsV2(jprofiles);
		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			Post p = genNewPost( owner.getUserId() );
			
			Result<String> expected = jposts.createPost(p);			
			doOrThrow( () -> anyPostsClient().createPost(p), expected.error(),  "Posts.createPost() failed test... Expected [%s] got: [%s]");
			if_NotFailed(() -> anyPostsClient().createPost(p), CONFLICT, "Posts.createPost() failed test... Expected [%s] got: [%s]");
		});
	}
	
}
