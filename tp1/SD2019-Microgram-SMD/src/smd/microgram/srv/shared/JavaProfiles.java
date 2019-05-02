package smd.microgram.srv.shared;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static smd.microgram.srv.shared.KafkaStrings.CREATE_POST;
import static smd.microgram.srv.shared.KafkaStrings.DELETE_POST;

import java.util.Arrays;
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
import utils.JSON;

public class JavaProfiles implements Profiles {
	private static final Set<String> DUMMY_SET = ConcurrentHashMap.newKeySet();

	protected Map<String, Profile> users = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> following = new ConcurrentHashMap<>();

	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	final Posts posts;
	final protected Publisher kafkaPublisher;
	final private Subscriber kafkaSubscriber;

	public JavaProfiles(Posts posts) {
		this.posts = posts;
		this.kafkaPublisher = new Publisher().init();

		KafkaUtils.createTopic(KafkaStrings.MICROGRAM_POSTS);
		this.kafkaSubscriber = new Subscriber(Arrays.asList(KafkaStrings.MICROGRAM_POSTS));

		kafkaSubscriber.consume((topic, key, value) -> {
			switch (key) {
			case CREATE_POST: {
				Post p = JSON.decode(value, Post.class);

				Set<String> pset = userPosts.get(p.getOwnerId());
				if (pset != null)
					pset.add(p.getPostId());

				break;
			}
			case DELETE_POST: {
				Post p = JSON.decode(value, Post.class);
				Set<String> pset = userPosts.get(p.getOwnerId());
				if (pset != null)
					pset.remove(p.getPostId());
				break;
			}
			}
		});
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

		kafkaPublisher.send(KafkaStrings.MICROGRAM_PROFILES, KafkaStrings.DELETE_PROFILE, userId);

		return ok();
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return ok(users.values().stream().filter(p -> p.getUserId().startsWith(prefix)).collect(Collectors.toList()));
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
			if (!s1.add(userId2) || !s2.add(userId1)) {
				return error(CONFLICT);
			}
		} else {
			if (!s1.remove(userId2) || !s2.remove(userId1)) {
				return error(NOT_FOUND);
			}
		}
		return ok();
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		Set<String> followees = following.get(userId);
		if (followees != null)
			return ok(followees.stream().map(userPosts::get).flatMap(s -> s.stream()).collect(Collectors.toList()));
		else
			return error(NOT_FOUND);
	}

}
