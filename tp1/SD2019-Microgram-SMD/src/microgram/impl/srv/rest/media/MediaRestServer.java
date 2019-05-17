package microgram.impl.srv.rest.media;

import static utils.Log.Log;

import java.net.URI;
import java.util.logging.Level;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import discovery.Discovery;
import microgram.api.rest.RestMedia;
import microgram.impl.srv.rest.utils.GenericExceptionMapper;
import microgram.impl.srv.rest.utils.PrematchingRequestFilter;
import utils.IP;

public class MediaRestServer {
	public static final int PORT = 12222;
	public static final String SERVICE = "Microgram-MediaStorage";
	public static String SERVER_BASE_URI = "http://%s:%s/rest";

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");

		Log.setLevel(Level.FINER);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		String serviceURI = serverURI + RestMedia.PATH;

		Discovery.announce(SERVICE, serviceURI);

		ResourceConfig config = new ResourceConfig();

		config.register(new RestMediaResources(serviceURI));
		config.register(new GenericExceptionMapper());
		config.register(new PrematchingRequestFilter());

		JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config);

		Log.fine(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));

	}
}
