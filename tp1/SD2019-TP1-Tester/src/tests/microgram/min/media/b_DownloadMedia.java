package tests.microgram.min.media;

import static microgram.api.java.Result.ErrorCode.OK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import loops.Loop;
import tests.TestFailedException;
import tests.microgram.MicrogramTest;

public class b_DownloadMedia extends MicrogramTest {

	public b_DownloadMedia(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Media Service [ download media... ] %s ", description));
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

			byte[] bytes = doOrThrow(() -> anyMediaClient().download(uri), OK, "Media.download() failed test... Expected %s got: [%s]");

			if (!Arrays.equals(bytes, uris.get(uri)))
				throw new TestFailedException("Media.download() failed test... <Retrieved data does not match what was uploaded...>");
		});
	}
}
