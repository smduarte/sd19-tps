package tests._4_microgram_min.posts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaPostsV2;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _05_IsLikedPosts extends MicrogramTest {

	Faker faker;
	
	public _05_IsLikedPosts(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ isLiked Post... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {


		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);
		
		Loop.times( NUM_OPS/2, parallel ).forEach( () -> {
			Profile p = genNewProfile();
			Result<Void> expected = jprofiles.createProfile(p);
			doOrThrow( () -> anyProfilesClient().createProfile(p), expected.error(),  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
		});
		
		List<String> users = new ArrayList<>(jprofiles.search("").value().stream().map( p -> p.getUserId()).collect( Collectors.toList()));
		
		List<String> posts = new CopyOnWriteArrayList<>();
		JavaPostsV2 jposts = new JavaPostsV2(jprofiles);
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( () -> {
			
			String user1 = users.get( random().nextInt( users.size() ));
			
			Post p = genNewPost( user1, false );

			Result<String> expected = jposts.createPost(p);						
			String id = doOrThrow( () -> anyPostsClient().createPost(p), expected.error(),  "Posts.createPost() failed test... Expected [%s] got: [%s]");
			if ( expected.isOK() )
				posts.add( id );
		});

		
		
		for(int i = 0; i < NUM_OPS/10; i++ )
			users.add( genNewProfile().getUserId());
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( (op) -> {
			
			String user = users.get( random().nextInt( users.size() ));
			String post = posts.get( random().nextInt( users.size()));
			boolean isLiked = random().nextBoolean();
			
			Result<Void> expected = jposts.like(post, user, isLiked );
			doOrThrow( () -> anyPostsClient().like(post, user, isLiked), expected.error(),  "Posts.like() failed test... Expected [%s] got: [%s]");
		});

		Loop.times( 2*NUM_OPS, parallel ).forEach( (i) -> {
			
			String user = users.get( random().nextInt( users.size() ));
			String post = posts.get( random().nextInt( users.size()));
			
			Result<Boolean> expected = jposts.isLiked(post, user);
			Boolean isLiked = doOrThrow( () -> anyPostsClient().isLiked(post, user), expected.error(),  "Posts.isLiked() failed test... Expected [%s] got: [%s]");
			if( expected.isOK() && expected.value() != isLiked )
				throw new FailedTestException("Posts.isLiked() failed test...<returned wrong result>, wanted:" + expected.value() + " got: " + isLiked);

		});
	}	
}
