package smd.microgram.clt.java;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;

public class ProfilesClient extends RetryClient implements Profiles {

	final Profiles impl;

	ProfilesClient(Profiles impl) {
		this.impl = impl;
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return reTry(() -> impl.getProfile(userId));
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return reTry(() -> impl.createProfile(profile));
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return reTry(() -> impl.deleteProfile(userId));
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return reTry(() -> impl.search(prefix, false));
	}

	@Override
	public Result<List<Profile>> search(String prefix, boolean partitioned) {
		return reTry(() -> impl.search(prefix, partitioned));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		return reTry(() -> impl.follow(userId1, userId2, isFollowing));
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		return reTry(() -> impl.isFollowing(userId1, userId2));
	}

	@Override
	public Result<List<String>> following(String userId) {
		return reTry(() -> impl.following(userId));
	}
}
