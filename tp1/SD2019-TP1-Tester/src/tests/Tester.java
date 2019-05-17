package tests;

import static java.lang.System.out;

import java.util.logging.Level;

import docker.Docker;
import tests.deployment.DockerConnection;
import tests.deployment.DockerParallelism;
import tests.deployment.ImageProperties;
import tests.deployment.KafkaConnection;
import tests.microgram.base.discovery.a_Discovery;
import tests.microgram.base.posts_profiles.a_PostsStatistics;
import tests.microgram.base.posts_profiles.b_PostsStatistics;
import tests.microgram.base.posts_profiles.c_UserFeed;
import tests.microgram.base.posts_profiles.d_SearchProfiles;
import tests.microgram.base.posts_profiles_with_failures.a_PostsStatistics_withFailures;
import tests.microgram.base.posts_profiles_with_failures.b_PostsStatistics_withFailures;
import tests.microgram.base.posts_profiles_with_failures.c_UserFeed_withFailures;
import tests.microgram.base.posts_profiles_with_failures.d_SearchProfiles_withFailures;
import tests.microgram.min.media.a_UploadMedia;
import tests.microgram.min.media.b_DownloadMedia;
import tests.microgram.min.media.c_DeleteMedia;
import tests.microgram.min.posts.a_CreatePosts;
import tests.microgram.min.posts.b_GetPosts;
import tests.microgram.min.posts.c_DeletePosts;
import tests.microgram.min.posts.d_LikePosts;
import tests.microgram.min.posts.e_IsLikedPosts;
import tests.microgram.min.posts.f_GetUserPosts;
import tests.microgram.min.posts.g_GetUserFeed;
import tests.microgram.min.profiles.a_CreateProfiles;
import tests.microgram.min.profiles.b_GetProfiles;
import tests.microgram.min.profiles.c_SearchProfiles;
import tests.microgram.min.profiles.d_FollowProfiles;
import tests.microgram.min.profiles.e_IsFollowingProfiles;
import tests.microgram.min.profiles.f_FollowProfilesStats;
import tests.microgram.min.profiles.g_FollowProfilesStats;
import tests.microgram.min.profiles.h_DeleteProfiles;
import tests.microgram.workload.a_Workload;
import utils.Args;

public class Tester {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}

	public static void main(String[] args) throws Exception {

		out.printf("");

		String image = Args.valueOf(args, "-image", "sd19-tp1-smd");
		String test = Args.valueOf(args, "-test", "1a");
		String log = Args.valueOf(args, "-log", "INFO");
		int sleep = Args.valueOf(args, "-sleep", 15);
		String secret = Args.valueOf(args, "-secret", Long.toString(System.nanoTime(), 32));
		BaseTest.showTesterStackTrace = Args.valueOf(args, "-trace", false);

		String threads = Args.valueOf(args, "-threads", "4");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", threads);

		utils.Log.Log.setLevel(Level.parse(log));
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "OFF");

		System.out.println("SD2019 +++ TP1 +++ Testing image: " + image);
		System.out.println("version: 2/Maio/2019-000");

		if (!Docker.get().validImage(image)) {
			System.err.println("Image name provided was not found...");
			System.err.println("Use the name of image shown at the end of the output of the mvn install command...");
			System.exit(0);
		}

		if (test.length() > 3) {
			System.err.println("eg. -test 2c");
			System.exit(0);
		}

		try {
			BaseTest.minMajorTest = Integer.valueOf(test);
		} catch (Exception x) {
			BaseTest.minMajorTest = Integer.valueOf(test.substring(0, test.length() - 1));
			BaseTest.minMinorTest = BaseTest.CHARS.indexOf(test.charAt(test.length() - 1));
		}

		BaseTest.image = image;
		BaseTest.sleep = sleep;
		BaseTest.secret = secret;
		try {
				
			new DockerConnection().major().test();

			new DockerParallelism().test();

			// Remove all containers based on this image...
			Docker.get().killByImage(image);
			Docker.get().killNamed("posts", "profiles", "mediastorage");

			new ImageProperties().test();

			Docker.get().killByImage(image);

			new KafkaConnection().test();

			boolean sequential = false;
			boolean parallel = !sequential && Integer.valueOf(threads) > 1;

			// 2---------- REST MediaStorage
			_test_Media(sequential, "REST - one instance per service");

			// 3---------- REST Profiles
			_test_Profiles(sequential, 1, 0, 1, 0, "REST - one instance per service");

			// 4---------- REST Profiles concurrent
			_test_Profiles(parallel, 1, 0, 1, 0, "REST - one instance per service");

			// 5---------- REST Posts sequential
			_test_Posts(sequential, 1, 0, 1, 0, "REST - one instance per service");

			// 6--------- REST Posts concurrent
			_test_Posts(parallel, 1, 0, 1, 0, "REST - one instance per service");

			// 7--------- REST Discovery
			new a_Discovery(false, 1, 0, 1, 0, "REST - one instance per service").major().test();

			// 8--------- REST Posts + Profiles sequential
			_test_Profiles_Plus_Posts(sequential, 1, 0, 1, 0, "REST - one instance per service");

			// 9--------- REST Posts + Profiles concurrent
			_test_Profiles_Plus_Posts(parallel, 1, 0, 1, 0, "REST - one instance per service");

			// 10--------- REST Posts + Profiles concurrent
			_test_Profiles_Plus_Posts_withFailures(parallel, 1, 0, 1, 0, "REST - one instance per service, with network failures");

			// SOAP

			// 11---------- SOAP Profiles
			_test_Profiles(sequential, 0, 1, 0, 1, "SOAP - one instance per service");

			// 12---------- SOAP Profiles concurrent
			_test_Profiles(parallel, 0, 1, 0, 1, "SOAP - one instance per service");

			// 13---------- SOAP Posts sequential
			_test_Posts(sequential, 0, 1, 0, 1, "SOAP - one instance per service");

			// 14--------- SOAP Posts concurrent
			_test_Posts(parallel, 0, 1, 0, 1, "SOAP - one instance per service");

			// 15--------- SOAP Posts + Profiles concurrent
			_test_Profiles_Plus_Posts(parallel, 0, 1, 0, 1, "SOAP - one instance per service");

			// MIXED - SOAP + REST

			// 16---------- SOAP Profiles + REST Posts concurrent
			_test_Profiles(parallel, 0, 1, 1, 0, "SOAP + REST - one instance per service");

			// 17--------- SOAP Posts + REST Profiles concurrent
			_test_Posts(parallel, 1, 0, 0, 1, "SOAP + REST - one instance per service");

			// 18--------- Posts + Profiles MIXED
			_test_Profiles_Plus_Posts(parallel, 1, 0, 0, 1, "REST + SOAP - one instance per service");

			// 19--------- Posts + Profiles MIXED
			_test_Profiles_Plus_Posts(parallel, 0, 1, 1, 0, "REST + SOAP - one instance per service");

			// DISTRIBUTED

			// 20--------- Posts + Profiles distributed
			_test_Profiles_Plus_Posts(parallel, 3, 0, 1, 0, "REST - decentralized Profiles service");

			// 21--------- Posts + Profiles distributed
			_test_Profiles_Plus_Posts(parallel, 1, 0, 3, 0, "REST - decentralized Posts service");

			// 22--------- Posts + Profiles fully distributed
			_test_Profiles_Plus_Posts(parallel, 2, 0, 2, 0, "REST - decentralized Posts and Profiles services");

			// WITH FAILURES

			// 23--------- Posts + Profiles distributed, with failures
			_test_Profiles_Plus_Posts_withFailures(parallel, 3, 0, 1, 0, "REST - decentralized Profiles service, with network failures");

			// 24--------- Posts + Profiles distributed, with failures
			_test_Profiles_Plus_Posts_withFailures(parallel, 1, 0, 3, 0, "REST - decentralized Posts service, with network failures");

			// 25--------- Posts + Profiles fully distributed, with failures
			_test_Profiles_Plus_Posts_withFailures(parallel, 2, 0, 2, 0, "REST - decentralized Posts and Profiles services, with network failures");

			// 26--------- Posts + Profiles Microgram APP Workload
			_test_Workload(parallel, "");

		} catch (Exception x) {
			System.err.println("oops...");
			if (BaseTest.showTesterStackTrace)
				System.err.println(x.getMessage());
		}

		Docker.get().killByImage(image);
		System.out.println("Test complete...");
		System.exit(0);
	}

	static void _test_Media(boolean parallel, String msg) throws Exception {
		// 2---------- MediaStorage
		new a_UploadMedia(parallel, 0, 0, 0, 0, msg).major().test();

		new b_DownloadMedia(parallel, 0, 0, 0, 0, msg).test();

		new c_DeleteMedia(parallel, 0, 0, 0, 0, msg).test();
	}

	static void _test_Profiles(boolean parallel, int restProfiles, int soapProfiles, int restPosts, int soapPosts, String msg) throws Exception {

		msg += (parallel ? ", concurrent requests." : ".");

		new a_CreateProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).major().test();

		new b_GetProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new c_SearchProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new d_FollowProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new e_IsFollowingProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new f_FollowProfilesStats(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new g_FollowProfilesStats(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new h_DeleteProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();
	}

	static void _test_Posts(boolean parallel, int restProfiles, int soapProfiles, int restPosts, int soapPosts, String msg) throws Exception {

		msg += (parallel ? ", concurrent requests." : ".");

		new a_CreatePosts(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).major().test();

		new b_GetPosts(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new c_DeletePosts(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new d_LikePosts(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new e_IsLikedPosts(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new f_GetUserPosts(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new g_GetUserFeed(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

	}

	static void _test_Profiles_Plus_Posts(boolean parallel, int restProfiles, int soapProfiles, int restPosts, int soapPosts, String msg) throws Exception {

		msg += (parallel ? ", concurrent requests." : ".");

		new a_PostsStatistics(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).major().test();

		new b_PostsStatistics(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new c_UserFeed(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		if (restProfiles > 1)
			new d_SearchProfiles(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();
	}

	static void _test_Profiles_Plus_Posts_withFailures(boolean parallel, int restProfiles, int soapProfiles, int restPosts, int soapPosts, String msg) throws Exception {

		msg += (parallel ? ", concurrent requests." : ".");

		new a_PostsStatistics_withFailures(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).major().test();

		new b_PostsStatistics_withFailures(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		new c_UserFeed_withFailures(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();

		if (restProfiles > 1)
			new d_SearchProfiles_withFailures(parallel, restProfiles, soapProfiles, restPosts, soapPosts, msg).test();
	}

	static void _test_Workload(boolean parallel, String msg) throws Exception {
		new a_Workload(parallel, 1, 0, 1, 0, msg).major().test();
		new a_Workload(parallel, 2, 0, 1, 0, msg + " - Decentralized Profiles").test();
		new a_Workload(parallel, 1, 0, 2, 0, msg + " - Decentralized Posts").test();
	}

}
