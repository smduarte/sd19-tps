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
import microgram.api.rest.RestPosts;
import smd.discovery.Discovery;
import smd.microgram.srv.rest.PostsRestServer;

public class RestPostsClient extends RestClient implements Posts {

	private static final String FROM = "/from/";
	private static final String FEED = "/feed/";
	private static final String LIKES = "/likes/";

	public RestPostsClient() {
		this( Discovery.findUrisOf( PostsRestServer.SERVICE, 1)[0]);
	}

	public RestPostsClient(URI serverUri) {
		super(serverUri, RestPosts.PATH);
	}

	
	public Result<String> createPost(Post post) {
		Response r = target
				.request()
				.post( Entity.entity( post, MediaType.APPLICATION_JSON));
		return responseContents(r, Status.OK, new GenericType<String>(){});	
	}
	
	public Result<Post> getPost(String postId) {
		Response r = target.path(postId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return responseContents(r, Status.OK, new GenericType<Post>() {});
	}
	
	public Result<Void> deletePost(String postId) {
		Response r = target.path(postId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		
		return verifyResponse(r, Status.NO_CONTENT);
	}
	
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		Response r = target.path(postId).path(LIKES).path(userId)
				.request()
				.put(Entity.entity(isLiked, MediaType.APPLICATION_JSON));
		
		return verifyResponse( r, Status.NO_CONTENT);
	}

	public Result<Boolean> isLiked(String postId, String userId) {
		Response r = target.path(postId).path(LIKES).path(userId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return responseContents( r, Status.OK, new GenericType<Boolean>() {});
	}
	
	public Result<List<String>> getPosts(String userId) {
		Response r = target.path(FROM).path(userId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return responseContents( r, Status.OK, new GenericType<List<String>>() {});
	}
	
	
	public Result<List<String>> getFeed(String userId) {
		Response r = target.path(FEED).path(userId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return responseContents( r, Status.OK, new GenericType<List<String>>() {});
	}
}
