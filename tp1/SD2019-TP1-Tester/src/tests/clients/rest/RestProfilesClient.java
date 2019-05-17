package tests.clients.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;

public class RestProfilesClient extends RestClient implements Profiles {

	private static final String FOLLOWING = "/following/";
	private static final String FOLLOWERS = "/followers/";

	public RestProfilesClient(URI serverUri) {
		super(serverUri);
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return super.reTry(() -> _getProfile(userId));
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return super.reTry(() -> _createProfile(profile));
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return super.reTry(() -> _deleteProfile(userId));
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return super.reTry(() -> _search(prefix));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		return super.reTry(() -> _follow(userId1, userId2, isFollowing));
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		return super.reTry(() -> _isFollowing(userId1, userId2));
	}

	private Result<Profile> _getProfile(String userId) {
		Response r = target.path(userId).request().accept(MediaType.APPLICATION_JSON).get();

		return responseContents(r, Status.OK, new GenericType<Profile>() {
		});
	}

	private Result<Void> _createProfile(Profile profile) {
		Response r = target.request().post(Entity.entity(profile, MediaType.APPLICATION_JSON));

		return verifyResponse(r, Status.NO_CONTENT);
	}

	private Result<Void> _deleteProfile(String userId) {
		Response r = target.path(userId).request().delete();

		return verifyResponse(r, Status.NO_CONTENT);
	}

	private Result<List<Profile>> _search(String prefix) {
		Response r = target.queryParam("query", prefix).request().accept(MediaType.APPLICATION_JSON).get();

		return responseContents(r, Status.OK, new GenericType<List<Profile>>() {
		});
	}

	private Result<Void> _follow(String userId1, String userId2, boolean isFollowing) {
		Response r = target.path(userId1).path(FOLLOWING).path(userId2).request().put(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));

		return verifyResponse(r, Status.NO_CONTENT);
	}

	private Result<Boolean> _isFollowing(String userId1, String userId2) {
		Response r = target.path(userId1).path(FOLLOWING).path(userId2).request().accept(MediaType.APPLICATION_JSON).get();

		return responseContents(r, Status.OK, new GenericType<Boolean>() {
		});
	}
}
