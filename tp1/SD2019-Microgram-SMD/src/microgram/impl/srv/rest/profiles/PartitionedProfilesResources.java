package microgram.impl.srv.rest.profiles;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import microgram.api.Profile;
import microgram.api.java.Result;
import microgram.impl.clt.Clients;
import microgram.impl.srv.java.PartitionedJavaProfiles;
import microgram.impl.srv.rest.Partitioner;

public class PartitionedProfilesResources extends RestProfilesResources {
	private static final String FOLLOWING = "/following/";

	final Partitioner partitioner;

	public PartitionedProfilesResources(Partitioner partitioner) {
		super(new PartitionedJavaProfiles(partitioner));
		this.partitioner = partitioner;
	}

	@Override
	public Profile getProfile(String userId) {
		partitioner.resolveProfilePartition(userId, userId);
		return super.getProfile(userId);
	}

	@Override
	public void createProfile(Profile profile) {
		partitioner.resolveProfilePartition(profile.getUserId(), "");
		super.createProfile(profile);
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) {
		partitioner.resolveProfilePartition(userId1, userId1 + FOLLOWING + userId2);
		super.follow(userId1, userId2, isFollowing);
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) {
		partitioner.resolveProfilePartition(userId1, userId1 + FOLLOWING + userId2);
		return super.isFollowing(userId1, userId2);
	}

	@Override
	public List<String> following(String userId) {
		partitioner.resolveProfilePartition(userId, userId + FOLLOWING);
		return super.following(userId);
	}

	@Override
	public void deleteProfile(String userId) {
		partitioner.resolveProfilePartition(userId, userId);
		super.deleteProfile(userId);
	}

	@Override
	public List<Profile> search(String prefix, boolean partitioned) {
		System.err.println(prefix + "/" + partitioned);
		List<Profile> results = new CopyOnWriteArrayList<>();

		results.addAll(impl.search(prefix).value());

		if (!partitioned)
			partitioner.otherPartitions.parallelStream().forEach(partitionURI -> {
				Result<List<Profile>> r = Clients.getProfiles(partitionURI).search(prefix, true);
				if (r.isOK())
					results.addAll(r.value());
			});
		return results;
	}
}