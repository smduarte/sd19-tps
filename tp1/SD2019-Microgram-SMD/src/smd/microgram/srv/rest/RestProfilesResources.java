package smd.microgram.srv.rest;


import java.util.List;

import microgram.api.Profile;
import microgram.impl.srv.rest.RestResource;
import smd.microgram.api.java.ProfilesV2;
import smd.microgram.api.rest.RestProfilesV2;
import smd.microgram.clt.MicrogramService;
import smd.microgram.srv.shared.JavaProfilesV2;

public class RestProfilesResources extends RestResource implements RestProfilesV2 {

	ProfilesV2 impl;
	
	public RestProfilesResources() {
	}
	
	@Override
	public Profile getProfile(String userId) {
		return super.resultOrThrow( impl().getProfile(userId));
	}

	@Override
	public void createProfile(Profile profile) {
		super.resultOrThrow( impl().createProfile(profile));		
	}

	@Override
	public void updateProfile(Profile profile) {
		super.resultOrThrow( impl().updateProfile(profile));
	}

	@Override
	public List<Profile> search(String name) {
		return super.resultOrThrow( impl().search(name));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) {
		super.resultOrThrow( impl().follow(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) {
		return super.resultOrThrow( impl().isFollowing(userId1, userId2));
	}

	@Override
	public List<String> following(String userId) {
		return super.resultOrThrow( impl().following(userId));
	}
	
	synchronized private ProfilesV2 impl() {
		if( impl == null) {
			impl = new JavaProfilesV2( MicrogramService.getPosts());
		}
		return impl;
	}
}
