package tests._4_microgram_min.media;

import static microgram.api.java.Result.ErrorCode.*;

import static microgram.api.java.Result.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javafaker.Faker;

import loops.Loop;
import tests.FailedTestException;
import tests._4_microgram_min.MicrogramTest;

public class _03_DeleteMedia extends MicrogramTest {

	Faker faker;
	
	public _03_DeleteMedia(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super( parallel, 1, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description );
	}
	
	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Media Service [ <optional> delete media... ] %s ", description));
		super.prepare();
		
		faker = new Faker( new Locale("pt"));
	}

	@Override
	protected void execute() throws Exception {

		Map<String, byte[]> uris = new ConcurrentHashMap<>();
		
		Loop.times( NUM_OPS, parallel ).forEach( () -> {
			byte[] bytes = new byte[ 128 + random().nextInt(1024)];
			random().nextBytes( bytes );
			
			String uri = doOrThrow( () -> anyMediaClient().upload(bytes), OK,  "Media.upload() failed test... Expected [%s] got: [%s]");
			uris.put( uri, bytes);
		});

		List<String> list = new ArrayList<>( uris.keySet() );
		
		Loop.times( 11*NUM_OPS/10, parallel ).forEach( () -> {
			String uri = list.get( random().nextInt( list.size() ));
			
			ErrorCode expected = uris.remove( uri ) != null ? OK : NOT_FOUND;
			doOrThrow( () -> anyMediaClient().delete(uri), expected,  "Media.delete() failed test... Expected [%s] got: [%s]");			
		});
	}
}
