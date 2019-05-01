package smd.microgram.clt.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;

public class RestPostsClient extends RestClient implements Posts {

	private static final String FROM = "/from/";
	private static final String FEED = "/feed/";
	private static final String LIKES = "/likes/";

	public RestPostsClient(URI serverUri) {
		super(serverUri);
	}

	@Override
	public Result<Post> getPost(String postId) {
		return super.reTry(() -> _getPost(postId));
	}

	@Override
	public Result<String> createPost(Post post) {
		return super.reTry(() -> _createPost(post));
	}

	@Override
	public Result<Void> deletePost(String postId) {
		return super.reTry(() -> _deletePost(postId));
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		return super.reTry(() -> _like(postId, userId, isLiked));
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		return super.reTry(() -> _isLiked(postId, userId));
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		return super.reTry(() -> _getPosts(userId));
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		return super.reTry(() -> _getFeed(userId));

	}

	private Result<String> _createPost(Post post) {
		Response r = target.request().post(Entity.entity(post, MediaType.APPLICATION_JSON));
		return responseContents(r, Status.OK, new GenericType<String>() {
		});
	}

	private Result<Post> _getPost(String postId) {
		Response r = target.path(postId).request().accept(MediaType.APPLICATION_JSON).get();
		return responseContents(r, Status.OK, new GenericType<Post>() {
		});
	}

	private Result<Void> _deletePost(String postId) {
		Response r = target.path(postId).request().delete();
		return verifyResponse(r, Status.NO_CONTENT);
	}

	private Result<Void> _like(String postId, String userId, boolean isLiked) {
		Response r = target.path(postId).path(LIKES).path(userId).request().put(Entity.entity(isLiked, MediaType.APPLICATION_JSON));
		return verifyResponse(r, Status.NO_CONTENT);
	}

	private Result<Boolean> _isLiked(String postId, String userId) {
		Response r = target.path(postId).path(LIKES).path(userId).request().accept(MediaType.APPLICATION_JSON).get();
		return responseContents(r, Status.OK, new GenericType<Boolean>() {
		});
	}

	private Result<List<String>> _getPosts(String userId) {
		Response r = target.path(FROM).path(userId).request().accept(MediaType.APPLICATION_JSON).get();
		return responseContents(r, Status.OK, new GenericType<List<String>>() {
		});
	}

	private Result<List<String>> _getFeed(String userId) {
		Response r = target.path(FEED).path(userId).request().accept(MediaType.APPLICATION_JSON).get();
		return responseContents(r, Status.OK, new GenericType<List<String>>() {
		});
	}
}
