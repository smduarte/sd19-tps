package microgram.impl.srv.rest.posts;

import static utils.Log.Log;

import java.net.URI;
import java.util.logging.Level;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import discovery.Discovery;
import microgram.api.rest.RestPosts;
import microgram.impl.srv.rest.Partitioner;
import microgram.impl.srv.rest.utils.GenericExceptionMapper;
import microgram.impl.srv.rest.utils.PrematchingRequestFilter;
import utils.Args;
import utils.IP;

public class PostsRestServer {
	public static final int PORT = 17777;
	public static final String SERVICE = "Microgram-Posts";
	public static String SERVER_BASE_URI = "http://%s:%s/rest";

	public static void main(String[] args) throws Exception {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");

		System.setProperty("java.net.preferIPv4Stack", "true");

		Log.setLevel(Level.FINER);

		int instances = Args.valueOf(args, "-posts", 1);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		String serviceURI = serverURI + RestPosts.PATH;

		Discovery.announce(SERVICE, serviceURI);

		ResourceConfig config = new ResourceConfig();

		config.register(new PartitionedPostsResources(new Partitioner(SERVICE, serviceURI, instances)));
		config.register(new GenericExceptionMapper());
		config.register(new PrematchingRequestFilter());

		JdkHttpServerFactory.createHttpServer(URI.create(serverURI.replace(ip, "0.0.0.0")), config);

		Log.fine(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));
	}
}
