package smd.microgram.srv.soap;

import java.util.List;

import javax.jws.WebService;

import microgram.api.Profile;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapProfiles;
import microgram.impl.srv.soap.SoapService;
import smd.microgram.api.java.ProfilesV2;
import smd.microgram.api.soap.SoapProfilesV2;
import smd.microgram.clt.MicrogramService;
import smd.microgram.srv.shared.JavaProfilesV2;

@WebService(serviceName=SoapProfiles.NAME, targetNamespace=SoapProfiles.NAMESPACE, endpointInterface=SoapProfilesV2.INTERFACE)
public class ProfilesWebService extends SoapService implements SoapProfilesV2 {

	ProfilesV2 impl;
	
	protected ProfilesWebService() {
	}

	@Override
	public Profile getProfile(String userId) throws MicrogramException {
		return super.resultOrThrow( impl().getProfile(userId));
	}

	@Override
	public void createProfile(Profile profile) throws MicrogramException {
		super.resultOrThrow( impl().createProfile(profile));
	}

	@Override
	public void updateProfile(Profile profile) throws MicrogramException {
		super.resultOrThrow( impl().updateProfile(profile));
	}
	
	@Override
	public void deleteProfile(String userId) throws MicrogramException {
		super.resultOrThrow( impl().deleteProfile(userId));
	}

	@Override
	public List<Profile> search(String prefix) throws MicrogramException {
		return super.resultOrThrow( impl().search(prefix));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) throws MicrogramException {
		super.resultOrThrow( impl().follow(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) throws MicrogramException {
		return super.resultOrThrow( impl().isFollowing(userId1, userId2));
	}

	@Override
	public List<String> following(String userId) throws MicrogramException {
		return super.resultOrThrow( impl().following(userId));
	}
	
	private ProfilesV2 impl() {
		if( impl == null) {
			impl = new JavaProfilesV2( MicrogramService.getPosts());
		}
		return impl;
	}
}
