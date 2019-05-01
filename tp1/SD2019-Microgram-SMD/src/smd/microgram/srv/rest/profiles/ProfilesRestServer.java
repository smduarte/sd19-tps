package smd.microgram.srv.rest.profiles;

import static utils.Log.Log;

import java.net.URI;
import java.util.logging.Level;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import microgram.api.rest.RestProfiles;
import smd.discovery.Discovery;
import smd.microgram.srv.rest.profiles.distributed.PartionedProfilesResources;
import smd.microgram.srv.rest.utils.GenericExceptionMapper;
import smd.microgram.srv.rest.utils.PrematchingRequestFilter;
import utils.Args;
import utils.IP;

public class ProfilesRestServer {
	public static final int PORT = 18888;
	public static final String SERVICE = "Microgram-Profiles";
	public static String SERVER_BASE_URI = "http://%s:%s/rest";

	public static void main(String[] args) throws Exception {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");

		int instances = Args.valueOf(args, "-profiles", 1);
		Log.setLevel(Level.OFF);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		String serviceURI = serverURI + RestProfiles.PATH;

		Discovery.announce(SERVICE, serviceURI);

		ResourceConfig config = new ResourceConfig();
		config.register(new PartionedProfilesResources(SERVICE, serviceURI, instances));
		config.register(new PrematchingRequestFilter());
		config.register(new GenericExceptionMapper());

		JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config);

		Log.fine(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));

	}
}
