package smd.microgram.srv.rest;

import static utils.Log.Log;

import java.net.URI;
import java.util.logging.Level;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import microgram.impl.srv.rest.RestMediaResources;
import smd.discovery.Discovery;
import smd.microgram.srv.rest.utils.PrematchingRequestFilter;
import utils.IP;


public class MediaRestServer {
	public static final int PORT = 12222;
	public static final String SERVICE = "Microgram-Media";
	public static String SERVER_BASE_URI = "http://%s:%s/rest";
	
	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");

		Log.setLevel( Level.FINER );

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);
		
		Discovery.announce(SERVICE, serverURI);

		ResourceConfig config = new ResourceConfig();

		config.register(new RestMediaResources(serverURI));
		config.register(new PrematchingRequestFilter());
		
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(ip, "0.0.0.0")), config);

		Log.fine(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));
	}
}
