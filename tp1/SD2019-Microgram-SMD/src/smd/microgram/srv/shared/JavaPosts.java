package smd.microgram.srv.shared;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static smd.microgram.srv.shared.KafkaStrings.CREATE_POST;
import static smd.microgram.srv.shared.KafkaStrings.DELETE_POST;
import static smd.microgram.srv.shared.KafkaStrings.DELETE_PROFILE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import smd.microgram.srv.kafka.KafkaUtils;
import smd.microgram.srv.kafka.Publisher;
import smd.microgram.srv.kafka.Subscriber;
import utils.Hash;
import utils.JSON;

public class JavaPosts implements Posts {

	private static final Set<String> EMPTY_SET = new HashSet<>();

	public static final int PROFILE_HASH_SIZE = 4;

	protected Map<String, Post> posts = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	final Profiles profiles;
	final Publisher kafkaPublisher;
	final Subscriber kafkaSubscriber;

	public JavaPosts(Profiles profiles) {
		this.profiles = profiles;
		this.kafkaPublisher = new Publisher().init();

		KafkaUtils.createTopic(KafkaStrings.MICROGRAM_PROFILES);
		this.kafkaSubscriber = new Subscriber(Arrays.asList(KafkaStrings.MICROGRAM_PROFILES));

		this.kafkaSubscriber.consume((topic, key, value) -> {
			switch (key) {
			case DELETE_PROFILE: {
				String userId = value;
				Set<String> pset = userPosts.remove(userId);
				if (pset != null) {
					posts.keySet().removeAll(pset);
					likes.keySet().removeAll(pset);
				}
				likes.values().forEach(set -> {
					set.remove(userId);
				});
				break;
			}
			}
		});
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
			if (kafkaPublisher != null)
				this.kafkaPublisher.send(KafkaStrings.MICROGRAM_POSTS, DELETE_POST, JSON.encode(post));

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

	@Override
	public Result<String> createPost(Post post) {
		Result<Profile> owner = profiles.getProfile(post.getOwnerId());

		if (!owner.isOK())
			return error(NOT_FOUND);

		String postId = Hash.of(post.getOwnerId()).substring(0, PROFILE_HASH_SIZE) + Hash.of(post.getMediaUrl());

		if (posts.putIfAbsent(postId, post) != null)
			return ok(postId);

		likes.put(postId, ConcurrentHashMap.newKeySet());

		userPosts.computeIfAbsent(post.getOwnerId(), (__) -> ConcurrentHashMap.newKeySet()).add(postId);

		post.setPostId(postId);
		if (kafkaPublisher != null)
			this.kafkaPublisher.send(KafkaStrings.MICROGRAM_POSTS, CREATE_POST, JSON.encode(post));

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
	public Result<List<String>> getFeed(String userId) {
		Result<List<String>> following = profiles.following(userId);
		if (!following.isOK())
			return following;
		else
			return ok(following.value().stream().map(user -> getPosts(user)).filter(Result::isOK).flatMap(okRes -> okRes.value().stream()).collect(Collectors.toList()));
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		Set<String> res = userPosts.get(userId);
		if (res != null)
			return ok(new ArrayList<>(res));
		else
			return ok(Collections.emptyList());
	}
}
