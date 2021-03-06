package tests.servers;

import java.net.URI;

import docker.ContainerFactory;
import tests.deployment.PropKeys;
import utils.Props;

public class ProfilesServer extends MicrogramServer<ProfilesServer> {

	public static final String SERVERNAME = "profiles-";

	public ProfilesServer() {
		super(SERVERNAME);
	}

	public ProfilesServer start(boolean isRest, ContainerFactory factory, String extraArgs) throws Exception {

		int port = Props.intValue((isRest ? PropKeys.PROFILES_REST_SERVICE_PORT : PropKeys.PROFILES_SOAP_SERVICE_PORT).toString(), 8080);

		super.start(factory, mainClass(isRest), newName(), extraArgs);
		super.uri = URI.create(String.format("http://%s:%d/%s/profiles", container.ip(), port, isRest ? "rest" : "soap"));

		return this;
	}

	static public String mainClass(boolean isRest) {
		return Props.get(isRest ? PropKeys.PROFILES_REST_SERVICE_MAINCLASS : PropKeys.PROFILES_SOAP_SERVICE_MAINCLASS);
	}
}
