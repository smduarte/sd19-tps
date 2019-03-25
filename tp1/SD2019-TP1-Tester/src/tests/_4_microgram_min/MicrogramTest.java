package tests._4_microgram_min;

import static utils.Log.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.impl.clt.rest.RestMediaClient;
import smd.microgram.clt.rest.RestPostsClient;
import smd.microgram.clt.rest.RestProfilesClient;
import smd.microgram.clt.soap.SoapPostsClient;
import smd.microgram.clt.soap.SoapProfilesClient;
import tests.BaseTest;
import tests.servers.MediaServer;
import tests.servers.PostsServer;
import tests.servers.ProfilesServer;

public class MicrogramTest extends BaseTest {

	protected static final int NUM_OPS = 100;

	final int restMediaServers;
	
	final int restPostsServers, soapPostsServers;
	final int restProfilesServers, soapProfilesServers;
	protected final String description;
	protected final boolean parallel;
	
	final Faker faker = new Faker( new Locale("pt"));
	
	
	final List<PostsServer> postsInstances = new ArrayList<>();
	final List<MediaServer> mediaInstances = new ArrayList<>();
	final List<ProfilesServer> profilesInstances = new ArrayList<>();

	public MicrogramTest(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		this(parallel, 0, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	public MicrogramTest(boolean parallel, int restMediaServers, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		this.restMediaServers = restMediaServers;
		this.restPostsServers = restPostsServers;
		this.soapPostsServers = soapPostsServers;
		this.restProfilesServers = restProfilesServers;
		this.soapProfilesServers = soapProfilesServers;
		this.parallel = parallel;
		this.description = description;
	}
	
	@Override
	protected void prepare() throws Exception {	

		if( restMediaServers > 0 )
			Log.info(String.format("Starting %d REST Media service instances <%s>", restMediaServers, MediaServer.mainClass()));
		for( int i = 0; i < restMediaServers ; i++ )
			mediaInstances.add( new MediaServer().start(this) ) ;

		
		if( restPostsServers > 0 )
			Log.info(String.format("Starting %d REST Posts service instances <%s>", restPostsServers, PostsServer.mainClass(true)));
		
		for( int i = 0; i < restPostsServers ; i++ )
			postsInstances.add( new PostsServer().start(true, this) ) ;

		if( soapPostsServers > 0 )
			Log.info(String.format("Starting %d SOAP Posts service instances <%s>", soapPostsServers, PostsServer.mainClass(false)));

		for( int i = 0; i < soapPostsServers ; i++ )
			postsInstances.add( new PostsServer().start(false, this) ) ;
		
		if( restProfilesServers > 0 )
			Log.info(String.format("Starting %d REST Profiles service instances <%s>", restProfilesServers, ProfilesServer.mainClass(true)));

		for( int i = 0; i < restProfilesServers ; i++ )
			profilesInstances.add( new ProfilesServer().start(true, this) ) ;

		if( soapProfilesServers > 0 )
			Log.info(String.format("Starting %d REST Profiles service instances <%s>", soapProfilesServers, ProfilesServer.mainClass(true)));
		
		for( int i = 0; i < soapProfilesServers ; i++ )
			profilesInstances.add( new ProfilesServer().start(false, this) ) ;

		sleep(true);
	}

	synchronized protected Posts anyPostsClient() {
		PostsServer srv = postsInstances.get(  random().nextInt( postsInstances.size() ));
		return toPostsClient( srv.uri());
	}
	
	synchronized protected Media anyMediaClient() {
		MediaServer srv = mediaInstances.get(  random().nextInt( mediaInstances.size() ));
		return toMediaClient( srv.uri());
	}
	
	synchronized protected Profiles anyProfilesClient() {
		ProfilesServer srv = profilesInstances.get( random().nextInt( profilesInstances.size() ) );
		return toProfilesClient( srv.uri() );
	}

	
	@Override
	protected void onFailure() {
		Thread.dumpStack();
	}
	
	static Profiles toProfilesClient( URI uri ) {
		if( uri.getPath().endsWith("rest"))
			return new RestProfilesClient(uri);
		
		if( uri.getPath().endsWith("soap"))
			return new SoapProfilesClient(uri);
			
		return null;	
	}
	
	static Posts toPostsClient( URI uri ) {
		if( uri.getPath().endsWith("rest"))
			return new RestPostsClient(uri);
		
		if( uri.getPath().endsWith("soap"))
			return new SoapPostsClient(uri);
			
		return null;	
	}
	
	static Media toMediaClient( URI uri ) {
		return new RestMediaClient(uri);	
	}
	
	synchronized protected Profile genNewProfile() {
		Profile res = new Profile();
		Name name = faker.name();
		res.setFollowers( 0 );
		res.setFollowing( 0 );
		res.setFullName( name.fullName() );
		res.setPosts(0);
		res.setPhotoUrl( faker.internet().url() );
		res.setUserId( name.lastName().toLowerCase() + (random().nextInt(100)));
		return res;
	}
	
	protected Post genNewPost( String owner) {
		return genNewPost(owner, false);
	}
	
	synchronized protected Post genNewPost( String owner, boolean withId) {
		Post res = new Post();
		Address addr = faker.address();
		res.setOwnerId( owner );
		res.setLocation( addr.cityName() + "," + addr.country() );
		res.setTimestamp( System.currentTimeMillis());
		res.setLikes( random().nextInt() >>> 1 );
		res.setMediaUrl( faker.internet().url());
		if( withId )
			res.setPostId( Long.toString( random().nextLong() >>> 1, 32));
		return res;
	}
	
	protected boolean equals( Post a, Post b) {
//		System.err.printf("%s      %s\n", new Gson().toJson(a), new Gson().toJson(b));
		
		boolean res = a.getLocation().equals( b.getLocation());
		res &= a.getMediaUrl().equals( b.getMediaUrl() );
		res &= a.getOwnerId().equals( b.getOwnerId() );
		res &= a.getTimestamp() == b.getTimestamp();
		return res;
	}
	
	protected boolean equals( Profile a, Profile b ) {
		boolean res = a.getFullName().equals( b.getFullName() );
		res &= a.getPhotoUrl().equals( b.getPhotoUrl());
		res &= a.getPosts() == b.getPosts();
		res &= a.getFollowers() == b.getFollowers();
		res &= a.getFollowing() == b.getFollowing();
		return res;
	}
}
