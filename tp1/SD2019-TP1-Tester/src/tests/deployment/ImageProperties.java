package tests.deployment;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import docker.Docker;
import jersey.repackaged.com.google.common.collect.Sets;
import tests.BaseTest;
import tests.BaseTest.MandatoryTest;
import tests.TestFailedException;
import utils.Props;;

@MandatoryTest
public class ImageProperties extends BaseTest {
	public static final String PROPS = "sd2019-tp1.props";
	private static final String FULL_PATH_PROPS = "/props/sd2019-tp1.props";
	private static final String OPENJDK_IMAGE = "smduarte/sd19-tp1-openjdk8";

	@Override
	protected void prepare() throws Exception {
		printf("Checking %s [executing: docker run %s /bin/cat %s]\n", PROPS, image(), PROPS + " and " + OPENJDK_IMAGE);
	}

	@Override
	protected void execute() throws Exception {

		Docker.get().copyFile(image(), FULL_PATH_PROPS, FULL_PATH_PROPS);

		if (!new File(FULL_PATH_PROPS).exists())
			throw new TestFailedException(PROPS + " not found in image or something...");

		Props.parseFile(FULL_PATH_PROPS);
		Props.set("image", image());

		Set<String> allKeys = Arrays.asList(PropKeys.values()).stream().map(Object::toString).collect(Collectors.toSet());

		if (!Props.keySet().containsAll(allKeys))
			throw new TestFailedException(String.format("%s must define: %s", PROPS, Sets.difference(allKeys, Props.keySet())));
	}
}
