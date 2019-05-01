package tests.deployment;

import docker.Docker;
import tests.BaseTest;
import tests.BaseTest.MandatoryTest;
import tests.TestFailedException;

@MandatoryTest
public class KafkaConnection extends BaseTest {

	@Override
	protected void prepare() throws Exception {
		println("Testing Docker environment [executing: docker info]");
	}

	@Override
	protected void execute() throws Exception {
		String id = Docker.get().id();

		if (id.split(":").length != 12)
			throw new TestFailedException("docker info failed...");
		else
			System.out.println("Docker ID: " + id);
	}

}
