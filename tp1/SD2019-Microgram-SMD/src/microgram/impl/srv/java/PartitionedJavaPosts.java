package microgram.impl.srv.java;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import microgram.impl.srv.rest.Partitioner;

public class PartitionedJavaPosts extends JavaPosts {

	final Partitioner partitioner;

	public PartitionedJavaPosts(Partitioner partitioner) {
		this.partitioner = partitioner;
	}

	@Override
	protected void handleFollowProfile(String[] idOf) {
		if (partitioner.isOwnProfilePartition(idOf[FOLLOWER]))
			following.computeIfAbsent(idOf[FOLLOWER], (__) -> ConcurrentHashMap.newKeySet()).add(idOf[FOLLOWEE]);
	}

	@Override
	protected void handleUnFollowProfile(String[] idOf) {
		if (partitioner.isOwnProfilePartition(idOf[FOLLOWER]))
			following.computeIfAbsent(idOf[FOLLOWER], (__) -> ConcurrentHashMap.newKeySet()).remove(idOf[FOLLOWEE]);
	}

	@Override
	protected void handleCreateProfile(String userId) {
		if (partitioner.isOwnProfilePartition(userId))
			userPosts.computeIfAbsent(userId, (__) -> ConcurrentHashMap.newKeySet());
		System.err.println(userPosts);
	}

	@Override
	protected void handleDeleteProfile(String userId) {
		if (partitioner.isOwnProfilePartition(userId)) {
			following.remove(userId);
			Set<String> pset = userPosts.remove(userId);
			if (pset != null) {
				posts.keySet().removeAll(pset);
			}
		}
	}
}
