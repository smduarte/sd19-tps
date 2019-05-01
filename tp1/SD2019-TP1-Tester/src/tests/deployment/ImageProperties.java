package tests._2_props;


import static utils.Log.Log;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import docker.Docker;
import jersey.repackaged.com.google.common.collect.Sets;
import tests.BaseTest;
import tests.BaseTest.MandatoryTest;
import tests.FailedTestException;
import utils.Props;;

@MandatoryTest
public class CheckImageProperties extends BaseTest {
	private static final String VERSION_FILE = "/version";
	private static final String PROPS = "/props/sd2019-tp1.props";
	private static final String OPENJDK_IMAGE = "smduarte/sd19-tp1-openjdk8"; 

	protected static final String OPENJDK_IMAGE_VERSION = "1104-1";

	@Override
	protected void prepare() throws Exception {
		printf("Checking %s [executing: docker run %s /bin/cat %s]\n", PROPS, image(), PROPS + " and " + OPENJDK_IMAGE);
	}

	@Override
	protected void execute() throws Exception {
		
		Docker.get().copyFile(image(), VERSION_FILE, VERSION_FILE);
		
		Docker.get().copyFile(image(), PROPS, PROPS);

		File versionFile = new File( VERSION_FILE );
		if( versionFile.exists() ) {
			List<String> versionLines = Files.readAllLines(new File(VERSION_FILE).toPath());
			if( versionLines.size() == 1 )
				assertOpenjdkImageVersion( versionLines.get(0));
		} else {
			
		}
		
		if( ! new File( PROPS).exists() )
			throw new FailedTestException( PROPS + " not found in image or something...");
		
		Props.parseFile(PROPS);
		Props.set("image", image());

		Set<String> allKeys = Arrays.asList(PropKeys.values())
				.stream()
				.map(Object::toString)
				.collect(Collectors.toSet());

		if (!Props.keySet().containsAll(allKeys))
			throw new FailedTestException(
					String.format("%s must define: %s", PROPS, Sets.difference(allKeys, Props.keySet())));
	}
	
	public void assertOpenjdkImageVersion(String version ) {
		if( ! version.equals( OPENJDK_IMAGE_VERSION ) ) {
			Log.severe(String.format("Expected version: %s, got %s", OPENJDK_IMAGE_VERSION, version ));
			throw new RuntimeException("rebuild the project image/base-image with latest smduarte/sd19-tp1-openjdk8 docker image...");
		}
	}
	
	@Override
	protected void onFailure() throws Exception {		
		super.println("Tip: Update the base image:\n(1) - docker pull smduarte/sd19-tp1-openjdk8\n(2) - touch sd2018-tp1.props\n(3) - mvn install\n");		
	}
}
