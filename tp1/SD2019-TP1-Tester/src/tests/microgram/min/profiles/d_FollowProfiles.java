package tests._4_microgram_min.profiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests._4_microgram_min.MicrogramTest;

public class _04_FollowProfiles extends MicrogramTest {

	Faker faker;
	
	public _04_FollowProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ follow Profiles... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {


		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( () -> {
			
			Profile p = genNewProfile();
			Result<Void> expected = jprofiles.createProfile(p);
			doOrThrow( () -> anyProfilesClient().createProfile(p), expected.error(),  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
		});
		
		List<String> ids = new ArrayList<>(jprofiles.search("").value().stream().map( p -> p.getUserId()).collect( Collectors.toList()));
		for(int i = 0; i < NUM_OPS/10; i++ )
			ids.add( genNewProfile().getUserId());
		
		Loop.times( 2*NUM_OPS, parallel ).forEach( (prefix) -> {
			
			String id1 = ids.get( random().nextInt( ids.size() ));
			String id2 = ids.get( random().nextInt( ids.size() ));
			boolean isFollowing = random().nextBoolean();
			
			Result<Void> expected = jprofiles.follow( id1, id2, isFollowing);
			doOrThrow( () -> anyProfilesClient().follow(id1, id2, isFollowing), expected.error(),  "Profiles.follow() failed test... Expected [%s] got: [%s]");
		});
	}
}
