package smd.microgram.clt;

import java.net.URI;

import microgram.api.java.Posts;
import microgram.impl.srv.rest.PostsRestServer;
import microgram.impl.srv.rest.ProfilesRestServer;
import smd.discovery.Discovery;
import smd.microgram.api.java.ProfilesV2;
import smd.microgram.clt.rest.RestPostsClient;
import smd.microgram.clt.rest.RestProfilesClient;
import smd.microgram.clt.soap.SoapPostsClient;
import smd.microgram.clt.soap.SoapProfilesClient;

public class MicrogramService {

	public static ProfilesV2 getProfiles() {
		URI[] uris;
		while((uris = Discovery.findUrisOf( ProfilesRestServer.SERVICE, 1)).length == 0);
		
		if( uris[0].toString().endsWith("/rest"))
			return new RestProfilesClient( uris[0] );
		else
			return new SoapProfilesClient( uris[0]) ;
	}
	
	public static Posts getPosts() {
		URI[] uris;
		while((uris = Discovery.findUrisOf( PostsRestServer.SERVICE, 1)).length == 0);
		
		if( uris[0].toString().endsWith("/rest"))
			return new RestPostsClient( uris[0] );
		else
			return new SoapPostsClient( uris[0]) ;
	}
}
