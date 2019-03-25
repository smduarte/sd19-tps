package smd.microgram.api.java;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;

public interface ProfilesV2 extends Profiles {

	
	Result<Void> updateProfile(Profile profile);

	Result<List<String>> following( String userId);

}
