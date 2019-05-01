package tests.microgram;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Profile;
import microgram.api.java.Result;
import smd.microgram.api.java.ExtendedProfiles;

public class LocalProfiles implements ExtendedProfiles {

	private static final Set<String> DUMMY_SET = new HashSet<>();

	protected Map<String, Profile> users = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> following = new ConcurrentHashMap<>();

	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	public LocalProfiles() {
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
	synchronized public Result<Void> deleteProfile(String userId) {
		Profile res = users.get(userId);
		if (res == null)
			return error(NOT_FOUND);

		for (String follower : followers.remove(userId)) {
			this.follow(follower, userId, false);
		}
		for (String followee : following.remove(userId)) {
			this.follow(userId, followee, false);
		}
		users.remove(userId);
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

		if (s1 == null)
			return error(NOT_FOUND);

		if (isFollowing) {
			if (!s1.add(userId2)) {
				return error(CONFLICT);
			} else
				followers.getOrDefault(userId2, DUMMY_SET).add(userId1);
		} else {
			if (!s1.remove(userId2)) {
				return error(NOT_FOUND);
			} else
				followers.getOrDefault(userId2, DUMMY_SET).remove(userId1);
		}
		return ok();
	}

}
