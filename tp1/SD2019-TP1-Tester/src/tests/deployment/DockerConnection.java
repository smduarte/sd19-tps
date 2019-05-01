package tests._1_deployment;

import docker.Docker;
import tests.BaseTest;
import tests.BaseTest.MandatoryTest;
import tests.FailedTestException;

@MandatoryTest
public class DockerConnection extends BaseTest {
	
	@Override
	protected void prepare() throws Exception {
		println("Testing Docker Environment [executing: docker info]");
	}
	
	@Override
	protected void execute() throws Exception {
		String id = Docker.get().id();
		
		if( id.split(":").length != 12)
			throw new FailedTestException("docker info failed...");
		else
			System.out.println("Docker ID: " + id );
	}

}
