package tests.deployment;

import static java.util.stream.IntStream.range;

import java.util.Set;
import java.util.stream.Collectors;

import tests.BaseTest;
import tests.BaseTest.MandatoryTest;
import tests.BaseTest.OptionalTest;
import tests.TestWarningException;

@OptionalTest
@MandatoryTest
public class DockerParallelism extends BaseTest {

	@Override
	protected void prepare() throws Exception {
		printf("Testing: Docker engine CPU cores...\n");
	}

	@Override
	protected void execute() throws Exception {

		Set<String> names = range(0, 1000).parallel().mapToObj(i -> Thread.currentThread().getName()).collect(Collectors.toSet());
		if (names.size() == 1 || Runtime.getRuntime().availableProcessors() == 1)
			throw new TestWarningException("Runtime environment only has 1 CPU - concurrent tests may not provide reliable results!!!!");

	}
}
