package tests.servers;

import java.net.URI;

import docker.ContainerFactory;
import tests._2_props.PropKeys;
import utils.Props;

public class MediaServer extends MicrogramServer<MediaServer> {

	
	public static final String SERVERNAME = "mediastorage-";
	
	public MediaServer() {
		super(SERVERNAME);
	}
	
	public MediaServer start(ContainerFactory factory ) throws Exception {
				
		int port = Props.intValue(PropKeys.MEDIASTORAGE_REST_SERVICE_PORT.toString(), 8080);

		super.start(factory, mainClass(), newName());
		super.uri = URI.create(String.format("http://%s:%d/rest", container.ip(), port));
		
		return this;
	}
	
	static public String mainClass() {
		return Props.get( PropKeys.MEDIASTORAGE_REST_SERVICE_MAINCLASS);
	}
}
