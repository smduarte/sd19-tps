package smd.microgram.clt.java;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Result;
import microgram.impl.clt.java.RetryClient;
import smd.microgram.api.java.ProfilesV2;

public class ProfilesClient extends RetryClient implements ProfilesV2 {

	final ProfilesV2 impl;

	ProfilesClient( ProfilesV2 impl ) {
		this.impl = impl;
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return reTry( () -> impl.getProfile(userId));
	}

	@Override
	public Result<Void> updateProfile(Profile profile) {
		return reTry( () -> impl.updateProfile(profile));
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return reTry( () -> impl.createProfile(profile));		
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return reTry( () -> impl.deleteProfile(userId));		
	}
	
	@Override
	public Result<List<Profile>> search(String name) {
		return reTry( () -> impl.search(name));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		return reTry( () -> impl.follow(userId1, userId2, isFollowing));		
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		return reTry( () -> isFollowing(userId1, userId2));		
	}

	@Override
	public Result<List<String>> following(String userId) {
		return reTry( () -> impl.following(userId));
	}
}
