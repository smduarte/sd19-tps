package tests.servers;

import java.net.URI;

import docker.ContainerFactory;
import tests.deployment.PropKeys;
import utils.Props;

public class PostsServer extends MicrogramServer<PostsServer> {

	public static final String SERVERNAME = "posts-";

	public PostsServer() {
		super(SERVERNAME);
	}

	public PostsServer start(boolean isRest, ContainerFactory factory, String extraArgs) throws Exception {

		int port = Props.intValue((isRest ? PropKeys.POSTS_REST_SERVICE_PORT : PropKeys.POSTS_SOAP_SERVICE_PORT).toString(), 8080);

		super.start(factory, mainClass(isRest), newName(), extraArgs);
		super.uri = URI.create(String.format("http://%s:%d/%s/posts", container.ip(), port, isRest ? "rest" : "soap"));

		return this;
	}

	static public String mainClass(boolean isRest) {
		return Props.get(isRest ? PropKeys.POSTS_REST_SERVICE_MAINCLASS : PropKeys.POSTS_SOAP_SERVICE_MAINCLASS);
	}
}
