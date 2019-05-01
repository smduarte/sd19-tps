package tests._5_microgram_base.posts_profiles;

import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaPostsV2;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _01_PostsStatistics extends MicrogramTest {

	public _01_PostsStatistics(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ create profiles... ] %s ", description));
		super.prepare();		
	}

	@Override
	protected void execute() throws Exception {

		JavaProfilesV2 jprofiles = new JavaProfilesV2(null, false);
		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			Profile p = genNewProfile();
			
			Result<Void> res = jprofiles.createProfile(p);	
			if( ! parallel || res.isOK() ) {
				doOrThrow( () -> anyProfilesClient().createProfile(p), res.error(),  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
					
				if_NotFailed(() -> anyProfilesClient().createProfile(p), CONFLICT, "Profiles.createProfile() failed test... Expected [%s] got: [%s]");		
			}
		});
		
		List<String> users = new ArrayList<>(jprofiles.search("").value().stream().map( p -> p.getUserId()).collect( Collectors.toList()));

		JavaPostsV2 jposts = new JavaPostsV2(jprofiles, false);
		
		Loop.times( 5 * NUM_OPS, parallel ).forEach( () -> {
			
			String owner = users.get( random().nextInt( users.size()) );
			Post p = genNewPost( owner );
			
			Result<String> expected = jposts.createPost(p);			
			doOrThrow( () -> anyPostsClient().createPost(p), expected.error(),  "Posts.createPost() failed test... Expected [%s] got: [%s]");
			if_NotFailed(() -> anyPostsClient().createPost(p), CONFLICT, "Posts.createPost() failed test... Expected [%s] got: [%s]");
		});
		
		Loop.items( users, parallel).forEach( user -> {
			
			int userPosts = jposts.getPosts(user).value().size();
			Profile p = doOrThrow( () -> anyProfilesClient().getProfile(user), OK,  "Profiles.getProfile() failed test... Expected [%s] got: [%s]");
			if( p.getPosts() != userPosts )
				throw new FailedTestException("Profiles.getProfile() failed test...<returned wrong result>, wanted:" + userPosts + " got: " + p.getPosts() );

		});
	}
	
}
