package tests;

import static java.lang.System.out;

import java.util.logging.Level;

import docker.Docker;
import tests._1_deployment.DockerConnection;
import tests._1_deployment.DockerRunContainer;
import tests._2_props.CheckImageProperties;
import tests._4_microgram_min.media._01_UploadMedia;
import tests._4_microgram_min.media._02_DownloadMedia;
import tests._4_microgram_min.posts._01_CreatePosts;
import tests._4_microgram_min.posts._02_GetPosts;
import tests._4_microgram_min.posts._03_DeletePosts;
import tests._4_microgram_min.posts._04_LikePosts;
import tests._4_microgram_min.posts._05_IsLikedPosts;
import tests._4_microgram_min.posts._06_GetUserPosts;
import tests._4_microgram_min.posts._07_GetUserFeed;
import tests._4_microgram_min.profiles._01_CreateProfiles;
import tests._4_microgram_min.profiles._02_GetProfiles;
import tests._4_microgram_min.profiles._03_SearchProfiles;
import tests._4_microgram_min.profiles._04_FollowProfiles;
import tests._4_microgram_min.profiles._05_IsFollowingProfiles;
import utils.Args;

public class Tester {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	
	private static final String IMAGE_SUBSTRING = "sd19-tp1";

	public static void main(String[] args) throws Exception {
		
		out.printf("");
		
		String image = Args.valueOf(args, "-image", "sd19-tp1-smd");
		String test = Args.valueOf(args, "-test", "1a");
		String log = Args.valueOf(args, "-log", "INFO");
		int sleep = Args.valueOf(args, "-sleep", 15);
		String secret = Args.valueOf(args, "-secret", Long.toString(System.nanoTime(), 32));

		utils.Log.Log.setLevel(Level.parse(log));
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");

		System.out.println("SD2019 +++ TP1 +++ Testing image: " + image);
		System.out.println("version: 24/Mar/2019");

		if( ! Docker.get().validImage( image )) {
			System.err.println("Image name provided was not found...");
			System.err.println("Use the name of image shown at the end of the output of the mvn install command..." );
			System.exit(0);
		}
			
		if( test.length() > 3 ) {
			System.err.println("eg. -test 2c");
			System.exit(0);
		}
		
		
		try {
			BaseTest.minMajorTest = Integer.valueOf( test );	
		} catch( Exception x  ) {
			BaseTest.minMajorTest = Integer.valueOf( test.substring(0, test.length() - 1) );	
			BaseTest.minMinorTest = BaseTest.CHARS.indexOf( test.charAt( test.length() - 1));
		}
		
		BaseTest.image = image;
		BaseTest.sleep = sleep;
		BaseTest.secret = secret;
		try {

			new DockerConnection().major().test();

			// Remove all containers based on this image...
			Docker.get().killByImage(image);
			Docker.get().killNamed("posts", "profiles");
			
			new DockerRunContainer().test();

			new CheckImageProperties().test();

			Docker.get().killByImage(image);

			new _01_UploadMedia(false, 0, 0, 0, 0, "REST <one instance per service>").major().test();

			new _02_DownloadMedia(false, 0, 0, 0, 0, "REST <one instance per service>").test();
			
			
			new _01_CreateProfiles(false, 1, 0, 1, 0, "REST <one instance per service>").major().test();
			
			new _02_GetProfiles(false, 1, 0, 1, 0, "REST <one instance per service>").test();

			new _03_SearchProfiles(false, 1, 0, 1, 0, "REST <one instance per service>").test();

			new _04_FollowProfiles(false, 1, 0, 1, 0, "REST <one instance per service>").test();

			new _05_IsFollowingProfiles(false, 1, 0, 1, 0, "REST <one instance per service>").test();
			
			
			new _01_CreateProfiles(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").major().test();
			
			new _02_GetProfiles(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			new _03_SearchProfiles(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			new _04_FollowProfiles(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			new _05_IsFollowingProfiles(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();
			

			new _01_CreatePosts(false, 1, 0, 1, 0, "REST <one instance per service>").major().test();
			
			new _02_GetPosts(false, 1, 0, 1, 0, "REST <one instance per service>").test();
			
			new _03_DeletePosts(false, 1, 0, 1, 0, "REST <one instance per service>").test();
			
			new _04_LikePosts(false, 1, 0, 1, 0, "REST <one instance per service>").test();

			new _05_IsLikedPosts(false, 1, 0, 1, 0, "REST <one instance per service>").test();

			new _06_GetUserPosts(false, 1, 0, 1, 0, "REST <one instance per service>").test();

			new _07_GetUserFeed(false, 1, 0, 1, 0, "REST <one instance per service>").test();


			new _01_CreatePosts(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").major().test();
			
			new _02_GetPosts(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();
			
			new _03_DeletePosts(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();
			
			new _04_LikePosts(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			new _05_IsLikedPosts(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			new _06_GetUserPosts(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			new _07_GetUserFeed(true, 1, 0, 1, 0, "REST <one instance per service, concurrent requests>").test();

			
		} catch (Exception x) {
			System.err.println( x.getMessage() );
		}

		Docker.get().killByImage(image);
		System.out.println("Test complete...");
		System.exit(0);
	}
}
