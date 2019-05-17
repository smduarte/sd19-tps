package tests.microgram.min.media;

import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.ErrorCode.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import loops.Loop;
import microgram.api.java.Result.ErrorCode;
import tests.BaseTest.OptionalTest;
import tests.microgram.MicrogramTest;

@OptionalTest
public class c_DeleteMedia extends MicrogramTest {

	public c_DeleteMedia(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Media Service [ <optional> delete media... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		Map<String, byte[]> uris = new ConcurrentHashMap<>();

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			byte[] bytes = new byte[128 + random().nextInt(1024)];
			random().nextBytes(bytes);

			String uri = doOrThrow(() -> anyMediaClient().upload(bytes), OK, "Media.upload() failed test... Expected %s got: [%s]");
			uris.put(uri, bytes);
		});

		List<String> list = new ArrayList<>(uris.keySet());

		Loop.times(2 * NUM_OPS, parallel).forEach(() -> {
			String uri = list.get(random().nextInt(list.size()));

			ErrorCode expected = uris.remove(uri) != null ? OK : NOT_FOUND;
			doOrThrow(() -> anyMediaClient().delete(uri), expected, "Media.delete() failed test... Expected %s got: [%s]");
		});
	}
}
