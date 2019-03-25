package tests._4_microgram_min.posts;

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

public class _04_LikePosts extends MicrogramTest {

	Faker faker;
	
	public _04_LikePosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ like Post... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {


		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);

		Profile owner = genNewProfile();	
		jprofiles.createProfile( owner );
		doOrThrow( () -> anyProfilesClient().createProfile(owner), OK,  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");

		Profile other = genNewProfile();	
		jprofiles.createProfile( other );
		doOrThrow( () -> anyProfilesClient().createProfile(other), OK,  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");

		
		List<String> ids = new CopyOnWriteArrayList<>();
		JavaPostsV2 jposts = new JavaPostsV2(jprofiles);
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( () -> {
			
			Post p = genNewPost( owner.getUserId(), true );
			
			Result<String> expected = jposts.createPost(p);						
			String id = doOrThrow( () -> anyPostsClient().createPost(p), expected.error(),  "Posts.createPost() failed test... Expected [%s] got: [%s]");
			if ( expected.isOK() )
				ids.add( id );
		});

		
		for(int i = 0; i < NUM_OPS/10; i++ )
			ids.add( genNewPost( owner.getUserId(), true ).getPostId() );
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( (op) -> {
			
			String id = ids.get( random().nextInt( ids.size() ));
			boolean isLiked = random().nextBoolean();
			
			Result<Void> expected = jposts.like(id, other.getUserId(), isLiked );
			doOrThrow( () -> anyPostsClient().like(id, other.getUserId(), isLiked), expected.error(),  "Posts.like() failed test... Expected [%s] got: [%s]");
		});
	}
		
}
