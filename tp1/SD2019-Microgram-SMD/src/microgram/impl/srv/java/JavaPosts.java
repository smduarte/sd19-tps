package microgram.impl.srv.java;

import static java.util.Collections.emptySet;
import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.CreatePost;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.DeletePost;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramTopic.PostsEvents;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramTopic.ProfilesEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.impl.srv.kafka.KafkaClient;
import utils.JSON;

public class JavaPosts implements Posts {
	protected static final int FOLLOWER = 0, FOLLOWEE = 1;

	private static final Set<String> DUMMY_SET = new HashSet<>();

	public static final int PROFILE_HASH_SIZE = 4;

	protected Map<String, Post> posts = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> following = new ConcurrentHashMap<>();

	final KafkaClient kafka;

	public JavaPosts() {

		this.kafka = new KafkaClient();

		kafka.createTopic(ProfilesEvents);

		kafka.subscribe((topic, event, value) -> {
			switch (event) {
			case CreateProfile: {
				this.handleCreateProfile(value);
				break;
			}
			case DeleteProfile: {
				this.handleDeleteProfile(value);
				break;
			}
			case FollowProfile: {
				this.handleFollowProfile(JSON.decode(value, String[].class));
				break;
			}
			case UnFollowProfile: {
				this.handleUnFollowProfile(JSON.decode(value, String[].class));
				break;
			}
			default:
				break;
			}
		}, ProfilesEvents);
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
			userPosts.getOrDefault(post.getOwnerId(), DUMMY_SET).remove(postId);

			post.setPostId(postId);

			kafka.publish(PostsEvents, DeletePost, post);

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

		Set<String> usrPosts = userPosts.get(post.getOwnerId());
		if (usrPosts == null)
			return error(NOT_FOUND);

		String postId = post.getPostId();

		usrPosts.add(postId);

		if (posts.putIfAbsent(postId, post) == null) {
			likes.put(postId, ConcurrentHashMap.newKeySet());

			kafka.publish(PostsEvents, CreatePost, post);
		}
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
		Set<String> res = userPosts.get(userId);
		if (res != null)
			return ok(new ArrayList<>(res));
		else
			return ok(Collections.emptyList());
	}

	@Override
	public Result<List<String>> following(String userId) {
		return ok(new ArrayList<>(following.getOrDefault(userId, Collections.emptySet())));
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		return ok(following.getOrDefault(userId, emptySet()).stream().flatMap(user -> userPosts.getOrDefault(user, emptySet()).stream()).collect(Collectors.toList()));
	}

	protected void handleFollowProfile(String[] idOf) {
		following.computeIfAbsent(idOf[FOLLOWER], (__) -> ConcurrentHashMap.newKeySet()).add(idOf[FOLLOWEE]);
	}

	protected void handleUnFollowProfile(String[] idOf) {
		following.computeIfAbsent(idOf[FOLLOWER], (__) -> ConcurrentHashMap.newKeySet()).remove(idOf[FOLLOWEE]);
	}

	protected void handleCreateProfile(String userId) {
		userPosts.computeIfAbsent(userId, (__) -> ConcurrentHashMap.newKeySet());
	}

	protected void handleDeleteProfile(String userId) {
		following.remove(userId);
		Set<String> pset = userPosts.remove(userId);
		if (pset != null) {
			posts.keySet().removeAll(pset);
		}
	}
}
