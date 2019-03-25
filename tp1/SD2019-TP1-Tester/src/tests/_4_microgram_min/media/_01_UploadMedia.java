package tests._4_microgram_min.media;

import loops.Loop;
import microgram.api.java.Result;
import microgram.impl.srv.java.JavaMedia;
import tests._4_microgram_min.MicrogramTest;

public class _01_UploadMedia extends MicrogramTest {

	public _01_UploadMedia(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Media Service [ upload media... ] %s ", description));
		super.prepare();		
	}

	@Override
	protected void execute() throws Exception {
		

		JavaMedia media = new JavaMedia("");
		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			byte[] bytes = new byte[ 128 + random().nextInt(1024)];
			random().nextBytes( bytes );
			
			Result<String> expected = media.upload( bytes );			
			String uri = doOrThrow( () -> anyMediaClient().upload(bytes), expected.error(),  "Media.upload() failed test... Expected [%s] got: [%s]");
		});
	}
	
}
