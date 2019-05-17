package tests.microgram.base.discovery;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import discovery.Discovery;
import tests.TestFailedException;
import tests.microgram.MicrogramTest;
import utils.Sleep;

public class a_Discovery extends MicrogramTest {

	public a_Discovery(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Discovery Service [ listening for announcements... ] %s ", description));
		super.prepare();
	}

	static String[] ServiceNames = { "Microgram-Profiles", "Microgram-Posts", "Microgram-MediaStorage" };

	@Override
	protected void execute() throws Exception {

		Map<String, URI[]> uris = new ConcurrentHashMap<>();

		int timeout = 30;

		long deadline = System.currentTimeMillis() + 1000 * timeout;

		Stream.of(ServiceNames).parallel().forEach(name -> {
			URI[] reply = Discovery.findUrisOf(name, 1);
			if (reply.length > 0) {
				uris.put(name, reply);
				System.out.printf("Found: %s \n", name);
			}
		});

		while (uris.size() != ServiceNames.length && System.currentTimeMillis() < deadline)
			Sleep.ms(1000);

		if (uris.size() != ServiceNames.length)
			throw new TestFailedException(String.format("Discovery.findUris failed test... <got: %s, expected:%s>", uris.keySet(), Arrays.asList(ServiceNames)));
	}

}
