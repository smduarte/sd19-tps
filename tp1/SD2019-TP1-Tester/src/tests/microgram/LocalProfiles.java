package tests.microgram;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Profile;
import microgram.api.java.Result;
import utils.RandomList;

public class LocalProfiles {

	private static final Set<String> DUMMY_SET = ConcurrentHashMap.newKeySet();
	protected Map<String, Profile> users = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> following = new ConcurrentHashMap<>();

	final Random random;

	public LocalProfiles() {
		this(new Random(1L));
	}

	public LocalProfiles(Random random) {
		this.random = random;
	}

	public RandomList<String> userIds() {
		return new RandomList<>(random, users.keySet());
	}

	String[] ids = null;

	synchronized public String randomId() {
		if (ids == null)
			ids = users.keySet().toArray(new String[users.size()]);

		return ids[random.nextInt(ids.length)];
	}

	public String randomId(double fakeIds) {
		return random.nextDouble() < fakeIds ? utils.Random.key64() : randomId();
	}

	public Result<Profile> getProfile(String userId) {
		Profile res = users.get(userId);
		if (res == null)
			return error(NOT_FOUND);

		res.setFollowers(followers.get(userId).size());
		res.setFollowing(following.get(userId).size());

		return ok(res);
	}

	public Result<Void> createProfile(Profile profile) {
		Profile res = users.putIfAbsent(profile.getUserId(), profile);
		if (res != null)
			return error(CONFLICT);

		followers.put(profile.getUserId(), ConcurrentHashMap.newKeySet());
		following.put(profile.getUserId(), ConcurrentHashMap.newKeySet());

		return ok();
	}

	synchronized public Result<Void> deleteProfile(String userId) {
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
		return ok();
	}

	public Result<List<Profile>> search(String prefix) {
		return ok(users.values().stream().filter(p -> p.getUserId().startsWith(prefix)).collect(Collectors.toList()));
	}

	public Result<List<String>> following(String userId) {
		Set<String> res = following.get(userId);
		if (res == null)
			return error(NOT_FOUND);
		else
			return ok(new ArrayList<>(res));
	}

	public Result<Boolean> isFollowing(String userId1, String userId2) {

		Set<String> s1 = following.get(userId1);
		Set<String> s2 = followers.get(userId2);

		if (s1 == null || s2 == null)
			return error(NOT_FOUND);
		else
			return ok(s1.contains(userId2) && s2.contains(userId1));
	}

	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Set<String> s1 = following.get(userId1);
		Set<String> s2 = followers.get(userId2);

		if (s1 == null || s2 == null)
			return error(NOT_FOUND);

		if (isFollowing) {
			if (!s1.add(userId2) || !s2.add(userId1))
				return error(CONFLICT);
		} else {
			if (!s1.remove(userId2) || !s2.remove(userId1))
				return error(NOT_FOUND);
		}
		return ok();
	}
}
