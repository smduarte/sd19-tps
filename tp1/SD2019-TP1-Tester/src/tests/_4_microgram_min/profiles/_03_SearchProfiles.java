package tests._4_microgram_min.profiles;

import static microgram.api.java.Result.ErrorCode.OK;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _03_SearchProfiles extends MicrogramTest {

	Faker faker;
	
	public _03_SearchProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ search Profiles... ] %s ", description));
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
		
		Set<String> prefixes = jprofiles.search("").value().stream()
				.map( p -> p.getUserId().substring( 0, Math.max(1, random().nextInt(p.getUserId().length()))))
				.collect( Collectors.toSet() );
						
		Loop.items( prefixes, parallel ).forEach( (prefix) -> {
			
			List<Profile> obtained = doOrThrow( () -> anyProfilesClient().search(prefix), OK, "Profiles.search() failed test... Expected [%s] got: [%s]");

			Set<String> expected = jprofiles.search(prefix).value().stream().map( p -> p.getUserId() ).collect( Collectors.toSet() );
			Set<String> resulted = obtained.stream().map( p -> p.getUserId() ).collect( Collectors.toSet() );
			if( ! expected.equals( resulted ) )
				throw new FailedTestException("Profiles.search() failed test...<search did not produce the expected results>, wanted:" + expected + " got: " + resulted);
		});
	}
	
	
	
}
