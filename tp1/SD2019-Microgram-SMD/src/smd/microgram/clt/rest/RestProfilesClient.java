package smd.microgram.clt.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.Profile;
import microgram.api.java.Result;
import microgram.api.rest.RestProfiles;
import smd.discovery.Discovery;
import smd.microgram.api.java.ProfilesV2;
import smd.microgram.srv.rest.ProfilesRestServer;


public class RestProfilesClient extends RestClient implements ProfilesV2 {

	private static final String FOLLOWING = "/following/";

	public RestProfilesClient() {
		this( Discovery.findUrisOf(ProfilesRestServer.SERVICE, 1)[0]);
	}
	
	public RestProfilesClient(URI serverUri) {
		super(serverUri, RestProfiles.PATH);
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		Response r = target.path(userId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return responseContents(r, Status.OK, new GenericType<Profile>() {});
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		Response r = target
				.request()
				.post(Entity.entity(profile, MediaType.APPLICATION_JSON));

		return verifyResponse(r, Status.NO_CONTENT);
	}

	@Override
	public Result<Void> updateProfile(Profile profile) {
		Response r = target
				.request()
				.put(Entity.entity(profile, MediaType.APPLICATION_JSON));
		return verifyResponse(r, Status.NO_CONTENT);
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Response r = target.path(userId)
				.request()
				.delete();

		return verifyResponse(r, Status.NO_CONTENT);
	}
	
	@Override
	public Result<List<Profile>> search(String name) {
		Response r = target.queryParam("query", name)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		return responseContents(r, Status.OK, new GenericType<List<Profile>>() {});
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Response r = target.path(userId1).path(FOLLOWING).path(userId2)
				.request()
				.put(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));
		
		return verifyResponse(r, Status.NO_CONTENT);
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		Response r = target.path(userId1).path(FOLLOWING).path(userId2)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return responseContents(r, Status.OK, new GenericType<Boolean>() {});
	}

	@Override
	public Result<List<String>> following(String userId) {
		Response r = target.path(userId).path(FOLLOWING)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		System.err.println( r.getStatus() );
		return responseContents(r, Status.OK, new GenericType<List<String>>() {});
	}
}
