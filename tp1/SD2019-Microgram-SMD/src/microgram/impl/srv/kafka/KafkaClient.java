package microgram.impl.srv.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import utils.JSON;

public class KafkaClient {

	public static enum MicrogramEvent {
		CreatePost, DeletePost, FollowProfile, UnFollowProfile, CreateProfile, DeleteProfile
	}

	public static enum MicrogramTopic {
		PostsEvents, ProfilesEvents;
	}

	public void createTopic(MicrogramTopic topic) {
		KafkaUtils.createTopic(topic.name());
	}

	public <T> void subscribe(MicrogramEventHandler handler, MicrogramTopic... topics) {
		List<String> topicNames = Arrays.asList(topics).stream().map(MicrogramTopic::name).collect(Collectors.toList());
		new Subscriber(topicNames).consume((topic, key, value) -> {
			handler.onMicrogramEvent(MicrogramTopic.valueOf(topic), MicrogramEvent.valueOf(key), value);
		});
	}

	public void publish(MicrogramTopic topic, MicrogramEvent event, String data) {
		publisher().send(topic.name(), event.name(), data);
	}

	public void publish(MicrogramTopic topic, MicrogramEvent event, Object data) {
		this.publish(topic, event, JSON.encode(data));
	}

	private synchronized Publisher publisher() {
		if (publisher == null)
			publisher = new Publisher().init();
		return publisher;
	}

	private Publisher publisher;

	public static interface MicrogramEventHandler {
		void onMicrogramEvent(MicrogramTopic topic, MicrogramEvent key, String value);
	}
}
