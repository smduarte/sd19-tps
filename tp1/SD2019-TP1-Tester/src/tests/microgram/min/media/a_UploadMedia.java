package tests.microgram.min.media;

import loops.Loop;
import microgram.api.java.Result;
import tests.microgram.LocalMedia;
import tests.microgram.MicrogramTest;

public class a_UploadMedia extends MicrogramTest {

	public a_UploadMedia(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Media Service [ upload media... ] %s ", description));
		super.prepare();
	}

	@Override
	protected void execute() throws Exception {

		LocalMedia media = new LocalMedia();

		Loop.times(NUM_OPS, parallel).forEach(() -> {
			byte[] bytes = new byte[128 + random().nextInt(1024)];
			random().nextBytes(bytes);

			Result<String> expected = media.upload(bytes);
			doOrThrow(() -> anyMediaClient().upload(bytes), expected.error(), "Media.upload() failed test... Expected %s got: [%s]");
		});
	}

}
