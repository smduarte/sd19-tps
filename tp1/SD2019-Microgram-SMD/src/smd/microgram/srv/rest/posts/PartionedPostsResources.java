package smd.microgram.srv.rest.posts;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.Response.Status;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import smd.discovery.Discovery;
import smd.microgram.clt.Clients;
import smd.microgram.srv.shared.JavaPosts;
import utils.Hash;

public class PartionedPostsResources extends RestPostsResources {

	private static final String FROM = "from/";
	private static final String LIKES = "/likes/";

	final int ownPartition;
	final List<String> partitions;

	public PartionedPostsResources(String serviceName, String ownURI, int instances) {

		URI[] uris;
		while ((uris = Discovery.findUrisOf(serviceName, instances)).length != instances)
			;

		this.partitions = new ArrayList<>(uris.length);

		for (URI uri : uris)
			partitions.add(uri + "/");

		Collections.sort(partitions);
		this.ownPartition = partitions.indexOf(ownURI + "/");

		System.err.println(partitions + "|" + ownURI + "|" + ownPartition);
	}

	@Override
	public Post getPost(String postId) {
		resolvePartitionFor(key(postId), postId);
		return super.getPost(postId);
	}

	@Override
	public void deletePost(String postId) {
		resolvePartitionFor(key(postId), postId);
		super.deletePost(postId);
	}

	@Override
	public String createPost(Post post) {
		resolvePartitionFor(keyFrom(post), "");
		return super.createPost(post);
	}

	@Override
	public void like(String postId, String userId, boolean isLiked) {
		resolvePartitionFor(key(postId), postId + LIKES + userId);
		super.like(postId, userId, isLiked);
	}

	@Override
	public boolean isLiked(String postId, String userId) {
		resolvePartitionFor(key(postId), postId + LIKES + userId);
		return super.isLiked(postId, userId);
	}

	@Override
	public List<String> getPosts(String userId) {
		resolvePartitionFor(keyFrom(userId), FROM + userId);
		return super.getPosts(userId);
	}

	@Override
	public List<String> getFeed(String userId) {
		Result<List<String>> following = Clients.getProfiles().following(userId);

		List<String> results = new CopyOnWriteArrayList<>();
		if (following.isOK()) {
			following.value().parallelStream().forEach(userFollowed -> {
				Result<List<String>> posts = clientForPartition(userFollowed).getPosts(userFollowed);
				if (posts.isOK())
					results.addAll(posts.value());
			});
		}
		return results;
	}

	private Posts clientForPartition(String userId) {
		String key = keyFrom(userId);
		int partition = (key.hashCode() >>> 1) % partitions.size();
		if (partition == ownPartition)
			return this.impl();
		else {
			String location = partitions.get(partition);
			return Clients.getPosts(location);
		}
	}

	private void resolvePartitionFor(String key, String resourcePath) {
		int partition = (key.hashCode() >>> 1) % partitions.size();
		if (partition != ownPartition) {
			URI location = URI.create(partitions.get(partition).concat(resourcePath));
			System.err.println("Redirect to: " + location);
			throw new RedirectionException(Status.TEMPORARY_REDIRECT, location);
		}
	}

	static private String key(String postId) {
		return postId.substring(0, JavaPosts.PROFILE_HASH_SIZE);
	}

	static private String keyFrom(Post post) {
		return keyFrom(post.getOwnerId());
	}

	static private String keyFrom(String userId) {
		return Hash.of(userId).substring(0, JavaPosts.PROFILE_HASH_SIZE);
	}
}