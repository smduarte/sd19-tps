package smd.microgram.srv.rest.profiles;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.rest.RestProfiles;
import smd.microgram.clt.Clients;
import smd.microgram.srv.rest.RestResource;
import smd.microgram.srv.shared.JavaProfiles;

public class RestProfilesResources extends RestResource implements RestProfiles {

	protected Profiles impl;

	@Override
	public Profile getProfile(String userId) {
		return super.resultOrThrow(impl().getProfile(userId));
	}

	@Override
	public void createProfile(Profile profile) {
		super.resultOrThrow(impl().createProfile(profile));
	}

	@Override
	public List<Profile> search(String prefix, boolean partitioned) {
		return super.resultOrThrow(impl().search(prefix, partitioned));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) {
		super.resultOrThrow(impl().follow(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) {
		return super.resultOrThrow(impl().isFollowing(userId1, userId2));
	}

	@Override
	public void deleteProfile(String userId) {
		super.resultOrThrow(impl().deleteProfile(userId));
	}

	synchronized protected Profiles impl() {
		if (impl == null) {
			impl = new JavaProfiles(Clients.getPosts());
		}
		return impl;
	}

	@Override
	public List<String> getFeed(String userId) {
		return super.resultOrThrow(impl().getFeed(userId));
	}

}
