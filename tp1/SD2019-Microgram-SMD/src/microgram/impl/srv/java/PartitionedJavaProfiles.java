package microgram.impl.srv.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.FollowProfile;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramEvent.UnFollowProfile;
import static microgram.impl.srv.kafka.KafkaClient.MicrogramTopic.ProfilesEvents;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import microgram.api.java.Result;
import microgram.impl.clt.Clients;
import microgram.impl.srv.rest.Partitioner;
import utils.JSON;

public class PartitionedJavaProfiles extends JavaProfiles {
	private static final int FOLLOWER = 0, FOLLOWEE = 1;

	final Partitioner partitioner;

	public PartitionedJavaProfiles(Partitioner partitioner) {
		this.partitioner = partitioner;
		kafka.subscribe((topic, event, value) -> {
			switch (event) {
			case FollowProfile: {
				this.handleFollowProfile(JSON.decode(value, String[].class));
				break;
			}
			case UnFollowProfile: {
				this.handleUnFollowProfile(JSON.decode(value, String[].class));
				break;
			}
			default:
			}
		}, ProfilesEvents);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Set<String> s1 = following.get(userId1);

		if (s1 == null)
			return error(NOT_FOUND);

		if (!Clients.getProfiles().getProfile(userId2).isOK()) {
			return error(NOT_FOUND);
		}

		if (isFollowing) {
			if (!s1.add(userId2)) {
				return error(CONFLICT);
			} else
				kafka.publish(ProfilesEvents, FollowProfile, new String[] { userId1, userId2 });
		} else {
			if (!s1.remove(userId2)) {
				return error(NOT_FOUND);
			} else
				kafka.publish(ProfilesEvents, UnFollowProfile, new String[] { userId1, userId2 });
		}
		return ok();
	}

	protected void handleFollowProfile(String[] idOf) {
		if (partitioner.isOwnProfilePartition(idOf[FOLLOWEE]))
			followers.computeIfAbsent(idOf[FOLLOWEE], (__) -> ConcurrentHashMap.newKeySet()).add(idOf[FOLLOWER]);
	}

	protected void handleUnFollowProfile(String[] idOf) {
		if (partitioner.isOwnProfilePartition(idOf[FOLLOWEE]))
			followers.computeIfAbsent(idOf[FOLLOWEE], (__) -> ConcurrentHashMap.newKeySet()).remove(idOf[FOLLOWER]);
	}
}
