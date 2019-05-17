package tests.deployment;

import tests.BaseTest;
import tests.BaseTest.MandatoryTest;
import utils.Props;

@MandatoryTest
public class KafkaConnection extends BaseTest {

	@Override
	protected void init() throws Exception {
		println("Testing Kafka requirement & connection...");
		boolean usesKafka = Props.boolValue(PropKeys.USES_KAFKA_BOOL.toString(), false);
		if (!usesKafka) {
			System.out.printf("%s=%s\n", PropKeys.USES_KAFKA_BOOL.toString(), usesKafka);
			System.out.println("Does not require Kafka...");
		}
	}

}
