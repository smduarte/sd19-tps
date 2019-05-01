package tests._3_services;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicListing;

public class Kafka {
	static final String MICROGRAM = "Microgram";
	
	public static void clean() {
		
		try( AdminClient client = AdminClient.create(config()) ) {
			
			client.listTopics().listings().get().stream().map( TopicListing::name ).filter( name -> name.startsWith(MICROGRAM))
			
		} catch( Exception x ) {
			x.printStackTrace();
		}
	}
	
	static private Map<String, Object> config() {
		Map<String, Object> conf = new HashMap<>();
		conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
		return conf;
	}
}
