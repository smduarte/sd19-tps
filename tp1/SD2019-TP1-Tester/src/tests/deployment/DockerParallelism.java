package tests.deployment;


import static utils.Log.Log;

import docker.Container;
import tests.BaseTest;
import tests.FailedTestException;


public class DockerParallelism extends BaseTest {

	private static final String DOCKER_CMD = "java -version";
	
	@Override
	protected void prepare() throws Exception {		
		printf("Testing: Running container with supplied image [executing: %s]\n", DOCKER_CMD);		
	}
	
	@Override
	protected void execute() throws Exception {		
		Container java = super.createContainer("", false).start( image(), DOCKER_CMD);
		sleep(true);
		Log.fine( java.logs() );
		if( ! java.logs().contains("openjdk") )
			throw new FailedTestException("java (openjdk) failed to execute...");
	}
}
