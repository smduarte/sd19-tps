package smd.microgram.srv.rest.profiles.distributed;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static smd.microgram.srv.shared.KafkaStrings.FOLLOW_PROFILE;
import static smd.microgram.srv.shared.KafkaStrings.UNFOLLOW_PROFILE;

import java.util.Arrays;
import java.util.Set;

import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import smd.microgram.srv.kafka.Publisher;
import smd.microgram.srv.kafka.Subscriber;
import smd.microgram.srv.shared.JavaProfiles;
import smd.microgram.srv.shared.KafkaStrings;
import utils.JSON;

public class DistributedExtendedProfiles extends JavaProfiles {

	final Profiles profiles;
	final Publisher kafkaPublisher;
	final Subscriber kafkaSubscriber;

	public DistributedExtendedProfiles(Posts posts, Profiles profiles) {
		super(posts);
		this.profiles = profiles;
		this.kafkaPublisher = new Publisher().init();
		this.kafkaSubscriber = new Subscriber(Arrays.asList(KafkaStrings.MICROGRAM_PROFILES));

		this.kafkaSubscriber.consume((topic, key, value) -> {
			switch (key) {
			case FOLLOW_PROFILE: {
				String[] ids = JSON.decode(value, String[].class);
				System.err.println("Kafka Follow" + Arrays.asList(ids));
				Set<String> fset = followers.get(ids[1]);
				if (fset != null)
					fset.add(ids[0]);
				break;
			}
			case UNFOLLOW_PROFILE: {
				String[] ids = JSON.decode(value, String[].class);
				System.err.println("Kafka UnFollow" + Arrays.asList(ids));
				Set<String> fset = followers.get(ids[1]);
				if (fset != null)
					fset.remove(ids[0]);
				break;
			}
			}
		});
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Set<String> s1 = following.get(userId1);

		if (s1 == null)
			return error(NOT_FOUND);

		if (!profiles.getProfile(userId2).isOK()) {
			return error(NOT_FOUND);
		}

		if (isFollowing) {
			if (!s1.add(userId2)) {
				return error(CONFLICT);
			} else
				this.publish(KafkaStrings.FOLLOW_PROFILE, userId1, userId2);
		} else {
			if (!s1.remove(userId2)) {
				return error(NOT_FOUND);
			} else
				this.publish(KafkaStrings.UNFOLLOW_PROFILE, userId1, userId2);
		}
		return ok();
	}

	public void publish(String key, String user1, String user2) {
		String[] value = { user1, user2 };
		kafkaPublisher.send(KafkaStrings.MICROGRAM_PROFILES, key, JSON.encode(value));
	}
}
