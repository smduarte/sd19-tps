package smd.microgram.srv.rest.posts;

import java.util.List;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.rest.RestPosts;
import smd.microgram.clt.Clients;
import smd.microgram.srv.rest.RestResource;
import smd.microgram.srv.shared.JavaPosts;

public class RestPostsResources extends RestResource implements RestPosts {

	Posts impl;

	@Override
	public Post getPost(String postId) {
		return super.resultOrThrow(impl().getPost(postId));
	}

	@Override
	public void deletePost(String postId) {
		super.resultOrThrow(impl().deletePost(postId));
	}

	@Override
	public String createPost(Post post) {
		return super.resultOrThrow(impl().createPost(post));
	}

	@Override
	public void like(String postId, String userId, boolean isLiked) {
		super.resultOrThrow(impl().like(postId, userId, isLiked));
	}

	@Override
	public boolean isLiked(String postId, String userId) {
		return super.resultOrThrow(impl().isLiked(postId, userId));
	}

	@Override
	public List<String> getPosts(String userId) {
		return super.resultOrThrow(impl().getPosts(userId));
	}

	@Override
	public List<String> getFeed(String userId) {
		return null;
	}

	synchronized protected Posts impl() {
		if (impl == null) {
			impl = new JavaPosts(Clients.getProfiles());
		}
		return impl;
	}
}
