package tests._4_microgram_min.posts;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaPostsV2;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests._4_microgram_min.MicrogramTest;

public class _03_DeletePosts extends MicrogramTest {

	Faker faker;
	
	public _03_DeletePosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ delete Post... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {
		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);

		Profile owner = genNewProfile();	
		jprofiles.createProfile( owner );
		doOrThrow( () -> anyProfilesClient().createProfile(owner), OK,  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");

		
		List<String> ids = new CopyOnWriteArrayList<>();
		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			Post p = genNewPost( owner.getUserId(), true );
			if_NotFailed(() -> anyPostsClient().deletePost(p.getPostId()), NOT_FOUND, "Posts.deletePost() failed test... Expected [%s] got: [%s]");
		});
		
		JavaPostsV2 jposts = new JavaPostsV2(jprofiles);
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( () -> {			
			Post p = genNewPost( owner.getUserId(), true );
			Result<String> expected = jposts.createPost(p);						
			String id = doOrThrow( () -> anyPostsClient().createPost(p), expected.error(),  "Posts.createPost() failed test... Expected [%s] got: [%s]");
			if ( expected.isOK() )
				ids.add( id );
		});
		
		Loop.items(ids, parallel ).forEach( (id) -> {
			doOrThrow( () -> anyPostsClient().deletePost( id ), OK, "Posts.deletePost() failed test... Expected [%s] got: [%s]");
		});
	}
}
