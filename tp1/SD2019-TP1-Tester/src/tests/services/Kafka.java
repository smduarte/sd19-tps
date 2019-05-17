package tests.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicListing;

import tests.TestFailedException;
import tests.deployment.PropKeys;
import utils.Props;

public class Kafka {

	public static void clean() {

		boolean usesKafka = Props.boolValue(PropKeys.USES_KAFKA_BOOL.toString(), false);

		if (usesKafka)
			try (AdminClient client = AdminClient.create(config())) {

				List<String> topics = client.listTopics().listings().get().stream().map(TopicListing::name).collect(Collectors.toList());

				client.deleteTopics(topics).all().get();

			} catch (Exception x) {
				throw new TestFailedException("Failed to connect to Kafka...[ Make sure Kafka is already running, execute: start-kafka.sh kafka]");
			}
	}

	static private Map<String, Object> config() {
		Map<String, Object> conf = new HashMap<>();
		conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
		conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
		return conf;
	}
}
