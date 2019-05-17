package microgram.impl.srv.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.CreateProfile;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.DeleteProfile;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.FollowProfile;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.UnFollowProfile;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramTopic.PostsEvents;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramTopic.ProfilesEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.srv.kafka.KafkaClient;
import utils.JSON;

public class JavaProfiles implements Profiles {

	protected static final Set<String> DUMMY_SET = ConcurrentHashMap.newKeySet();

	protected Map<String, Profile> users = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> following = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	protected final KafkaClient kafka;

	public JavaProfiles() {
		this.kafka = new KafkaClient();

		kafka.createTopic(PostsEvents);
		kafka.subscribe((topic, event, value) -> {
			switch (event) {
			case CreatePost: {
				this.handleCreatePost(JSON.decode(value, Post.class));
				break;
			}
			case DeletePost: {
				this.handleDeletePost(JSON.decode(value, Post.class));
				break;
			}
			default:
			}
		}, PostsEvents);
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		Profile res = users.get(userId);
		if (res == null)
			return error(NOT_FOUND);

		res.setFollowers(followers.get(userId).size());
		res.setFollowing(following.get(userId).size());
		res.setPosts(userPosts.get(userId).size());
		return ok(res);
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		Profile res = users.putIfAbsent(profile.getUserId(), profile);
		if (res != null)
			return error(CONFLICT);

		followers.put(profile.getUserId(), ConcurrentHashMap.newKeySet());
		following.put(profile.getUserId(), ConcurrentHashMap.newKeySet());
		userPosts.put(profile.getUserId(), ConcurrentHashMap.newKeySet());

		kafka.publish(ProfilesEvents, CreateProfile, profile.getUserId());

		return ok();
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Profile res = users.get(userId);
		if (res == null)
			return error(NOT_FOUND);

		for (String follower : followers.remove(userId)) {
			following.computeIfAbsent(follower, (__) -> DUMMY_SET).remove(userId);
		}
		for (String followee : following.remove(userId)) {
			followers.computeIfAbsent(followee, (__) -> DUMMY_SET).remove(userId);
		}

		users.remove(userId);

		kafka.publish(ProfilesEvents, DeleteProfile, userId);

		return ok();
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return ok(users.values().stream().filter(p -> p.getUserId().startsWith(prefix)).collect(Collectors.toList()));
	}

	@Override
	public Result<List<String>> following(String userId) {
		Set<String> res = following.get(userId);
		if (res == null)
			return error(NOT_FOUND);
		else
			return ok(new ArrayList<>(res));
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {

		Set<String> s1 = following.get(userId1);

		if (s1 == null)
			return error(NOT_FOUND);
		else
			return ok(s1.contains(userId2));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Set<String> s1 = following.get(userId1);
		Set<String> s2 = followers.get(userId2);

		if (s1 == null || s2 == null)
			return error(NOT_FOUND);

		if (isFollowing) {
			if (!s1.add(userId2) || !s2.add(userId1))
				return error(CONFLICT);

			kafka.publish(ProfilesEvents, FollowProfile, new String[] { userId1, userId2 });

		} else {
			if (!s1.remove(userId2) || !s2.remove(userId1))
				return error(NOT_FOUND);

			kafka.publish(ProfilesEvents, UnFollowProfile, new String[] { userId1, userId2 });
		}
		return ok();
	}

	private void handleCreatePost(Post post) {
		userPosts.computeIfAbsent(post.getOwnerId(), (__) -> ConcurrentHashMap.newKeySet()).add(post.getPostId());
	}

	private void handleDeletePost(Post post) {
		userPosts.computeIfAbsent(post.getOwnerId(), (__) -> ConcurrentHashMap.newKeySet()).remove(post.getPostId());
	}
}
