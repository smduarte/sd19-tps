package smd.microgram.srv.soap;

import static utils.Log.Log;

import java.util.logging.Level;

import javax.xml.ws.Endpoint;

import smd.discovery.Discovery;
import utils.IP;

public class ProfilesSoapServer {

	public static final int PORT = 28888;
	public static final String SERVICE = "Microgram-Profiles";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";

	public static void main(String[] args) throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");

		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		Log.setLevel(Level.FINER);

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		Discovery.announce(SERVICE, serverURI);

		Endpoint.publish(serverURI, new ProfilesWebService());

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE, serverURI));

	}
}
