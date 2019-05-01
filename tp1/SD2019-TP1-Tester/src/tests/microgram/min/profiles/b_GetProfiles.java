package tests._4_microgram_min.profiles;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;

import loops.Loop;
import microgram.api.Profile;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _02_GetProfiles extends MicrogramTest {

	Faker faker;
	
	public _02_GetProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ get Profile... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {

		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			Profile p = genNewProfile();
			if_NotFailed(() -> anyProfilesClient().getProfile(p.getUserId()), NOT_FOUND, "Profiles.getProfile() failed test... Expected [%s] got: [%s]");
		});
		
		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);
		
		Loop.times( 5 * NUM_OPS, parallel ).forEach( () -> {
			Profile p = genNewProfile();
			jprofiles.createProfile(p);	
		});
		
		List<Profile> profiles = jprofiles.search("").value();
		
		Loop.items( profiles, parallel ).forEach( (p) -> {
			doOrThrow( () -> anyProfilesClient().createProfile(p), OK,  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
		});

		List<String> ids = jprofiles.search("").value().stream().map(p -> p.getUserId()).collect( Collectors.toList());
		
		Loop.items(ids, parallel ).forEach( (id) -> {
			Profile q = doOrThrow( () -> anyProfilesClient().getProfile( id ), OK, "Profiles.getProfile() failed test... Expected [%s] got: [%s]");
			if( ! q.getFullName().equals( jprofiles.getProfile(id).value().getFullName()))
				throw  new FailedTestException("Profiles.getProfile() failed test... <Retrieved Profile data does not match...>");
		});
	}
}
