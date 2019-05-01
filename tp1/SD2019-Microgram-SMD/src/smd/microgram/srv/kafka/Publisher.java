package smd.microgram.srv.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class Publisher {

	private Producer<String, String> producer;

	public Publisher() {
	}

	public Publisher init() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producer = new KafkaProducer<>(config);
		return this;
	}

	public void send(String topic, String key, String value) {
		try {
			producer.send(new ProducerRecord<>(topic, key, value)).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
