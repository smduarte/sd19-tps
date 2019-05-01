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
import tests._4_microgram_min.MicrogramTest;

public class _07_GetUserFeed extends MicrogramTest {

	Faker faker;
	
	public _07_GetUserFeed(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Posts Service [ getFeed... ] %s ", description));
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

		Loop.times( 2*NUM_OPS, parallel ).forEach( (i) -> {
			
			String user = users.get( random().nextInt( users.size() ));
			
			Result<List<String>> expected = jposts.getFeed(user);
			doOrThrow( () -> anyPostsClient().getFeed(user), expected.error(),  "Posts.getFeed() failed test... Expected [%s] got: [%s]");

		});
	}	
}
