package smd.microgram.srv.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import utils.Random;

public class Subscriber {

	private final AtomicBoolean ready = new AtomicBoolean(false);
	private final List<String> topics;

	public Subscriber(List<String> topics) {
		this.topics = topics;
	}

	public void consume(Listener listener) {

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, Random.key128());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Random.key128());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		new Thread(() -> {
			try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
				consumer.subscribe(topics);

				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
					ready.set(true);
					records.forEach(r -> {
//						System.err.println( r.key() + "/" + r.partition() + "/" + r.offset());
						listener.onReceive(r.topic(), r.key(), r.value());
					});
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}).start();
	}

	static public interface Listener {

		void onReceive(String topic, String key, String value);
	}

}
