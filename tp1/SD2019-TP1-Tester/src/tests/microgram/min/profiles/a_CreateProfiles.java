package tests._4_microgram_min.profiles;

import static microgram.api.java.Result.ErrorCode.CONFLICT;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.srv.shared.JavaProfilesV2;
import tests._4_microgram_min.MicrogramTest;

public class _01_CreateProfiles extends MicrogramTest {

	public _01_CreateProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ create profiles... ] %s ", description));
		super.prepare();		
	}

	@Override
	protected void execute() throws Exception {

		JavaProfilesV2 jprofiles = new JavaProfilesV2(null);
		
		Loop.times( 5 * NUM_OPS, parallel ).forEach( () -> {
			Profile p = genNewProfile();
			
			Result<Void> res = jprofiles.createProfile(p);	
			if( ! parallel || res.isOK() ) {
				doOrThrow( () -> anyProfilesClient().createProfile(p), res.error(),  "Profiles.createProfile() failed test... Expected [%s] got: [%s]");
					
				if_NotFailed(() -> anyProfilesClient().createProfile(p), CONFLICT, "Profiles.createProfile() failed test... Expected [%s] got: [%s]");		
			}
		});
		
	}
	
}
