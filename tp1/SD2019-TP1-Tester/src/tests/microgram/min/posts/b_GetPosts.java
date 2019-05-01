package tests._4_microgram_min.posts;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaPostsV2;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _02_GetPosts extends MicrogramTest {

	Faker faker;
	
	public _02_GetPosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ get Post... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {
		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);

		Profile owner = genNewProfile();	
		jprofiles.createProfile( owner );
		doOrThrow( () -> anyProfilesClient().createProfile(owner), OK,  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			Post p = genNewPost( owner.getUserId(), true );
			if_NotFailed(() -> anyPostsClient().getPost(p.getPostId()), NOT_FOUND, "Posts.getPost() failed test... Expected [%s] got: [%s]");
		});
		
		JavaPostsV2 jposts = new JavaPostsV2(jprofiles);
		Map<String, Post> posts =  new ConcurrentHashMap<>();
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( () -> {			
			Post p = genNewPost( owner.getUserId(), true );
			Result<String> expected = jposts.createPost(p);						
			String id = doOrThrow( () -> anyPostsClient().createPost(p), expected.error(),  "Posts.createPost() failed test... Expected [%s] got: [%s]");
			if ( expected.isOK() )
				posts.put( id, p );
		});
		
		Loop.items(posts.keySet(), parallel ).forEach( (id) -> {
			Post q = doOrThrow( () -> anyPostsClient().getPost( id ), OK, "Posts.getPost() failed test... Expected [%s] got: [%s]");
			if( ! super.equals( posts.get(id), q))
				throw  new FailedTestException("Posts.getPost() failed test... <Retrieved Post data does not match...>");
		});
	}
}
