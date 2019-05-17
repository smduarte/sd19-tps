package tests.microgram;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import utils.Hash;
import utils.RandomList;

public class LocalPosts implements Posts {

	private static final Set<String> EMPTY_SET = new HashSet<>();

	protected Map<String, Post> posts = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	final LocalProfiles profiles;

	final Random random;

	public LocalPosts(LocalProfiles profiles) {
		this(profiles, new Random(1L));
	}

	public LocalPosts(LocalProfiles profiles, Random random) {
		this.profiles = profiles;
		this.random = random;
	}

	public RandomList<String> postIds() {
		return new RandomList<>(random, posts.keySet());
	}

	String[] ids = null;

	synchronized public String randomId() {
		if (ids == null)
			ids = posts.keySet().toArray(new String[posts.size()]);

		return ids[random.nextInt(ids.length)];
	}

	@Override
	public Result<Post> getPost(String postId) {
		Post res = posts.get(postId);
		if (res != null)
			return ok(res);
		else
			return error(NOT_FOUND);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Post post = posts.remove(postId);
		if (post != null) {
			likes.remove(postId);
			userPosts.getOrDefault(post.getOwnerId(), EMPTY_SET).remove(postId);

			post.setPostId(postId);

			return ok();
		} else
			return error(NOT_FOUND);
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Set<String> res = likes.get(postId);

		if (res != null)
			return ok(res.contains(userId));
		else
			return error(NOT_FOUND);
	}

	public Result<String> createPost(String postId, Post post) {
		post.setPostId(postId);
		return this.createPost(post);
	}

	@Override
	public Result<String> createPost(Post post) {
		Result<Profile> owner = profiles.getProfile(post.getOwnerId());

		if (!owner.isOK())
			return error(NOT_FOUND);

		String postId = post.getPostId() != null ? post.getPostId() : Hash.of(post.getOwnerId(), post.getMediaUrl()) + "-Tester";
		if (posts.putIfAbsent(postId, post) != null)
			return ok(postId);

		likes.put(postId, ConcurrentHashMap.newKeySet());

		Set<String> posts = userPosts.computeIfAbsent(post.getOwnerId(), (__) -> ConcurrentHashMap.newKeySet());
		posts.add(postId);

		owner.value().setPosts(posts.size());

		return ok(postId);
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {

		Set<String> res = likes.get(postId);
		if (res == null)
			return error(NOT_FOUND);

		if (isLiked) {
			if (!res.add(userId))
				return error(CONFLICT);
		} else {
			if (!res.remove(userId))
				return error(NOT_FOUND);
		}

		getPost(postId).value().setLikes(res.size());
		return ok();
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		Result<Profile> p = profiles.getProfile(userId);
		if (!p.isOK())
			error(NOT_FOUND);

		Set<String> res = userPosts.get(userId);
		if (res != null)
			return ok(new ArrayList<>(res));
		else
			return ok(Collections.emptyList());
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		Result<List<String>> following = profiles.following(userId);

		if (!following.isOK())
			return following;
		else
			return ok(following.value().stream().map(user -> getPosts(user)).filter(Result::isOK).flatMap(okRes -> okRes.value().stream()).collect(Collectors.toList()));
	}
}
