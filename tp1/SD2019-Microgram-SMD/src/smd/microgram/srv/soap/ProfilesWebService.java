package smd.microgram.srv.soap;

import java.util.List;

import javax.jws.WebService;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapProfiles;
import smd.microgram.clt.Clients;
import smd.microgram.srv.shared.JavaProfiles;

@WebService(serviceName = SoapProfiles.NAME, targetNamespace = SoapProfiles.NAMESPACE, endpointInterface = SoapProfiles.INTERFACE)
public class ProfilesWebService extends SoapService implements SoapProfiles {

	Profiles impl;

	protected ProfilesWebService() {
	}

	@Override
	public Profile getProfile(String userId) throws MicrogramException {
		return super.resultOrThrow(impl().getProfile(userId));
	}

	@Override
	public void createProfile(Profile profile) throws MicrogramException {
		super.resultOrThrow(impl().createProfile(profile));
	}

	@Override
	public void deleteProfile(String userId) throws MicrogramException {
		super.resultOrThrow(impl().deleteProfile(userId));
	}

	@Override
	public List<Profile> search(String prefix) throws MicrogramException {
		return super.resultOrThrow(impl().search(prefix));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) throws MicrogramException {
		super.resultOrThrow(impl().follow(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) throws MicrogramException {
		return super.resultOrThrow(impl().isFollowing(userId1, userId2));
	}

	@Override
	public List<String> following(String userId) throws MicrogramException {
		return super.resultOrThrow(impl().following(userId));
	}

	synchronized private Profiles impl() {
		if (impl == null) {
			impl = new JavaProfiles(Clients.getPosts());
		}
		return impl;
	}
}
