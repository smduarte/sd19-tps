package tests;

import static utils.Log.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import docker.Container;
import docker.ContainerFactory;
import docker.Docker;
import microgram.api.java.Result;
import tests.services.Kafka;
import utils.Lock;
import utils.Sleep;

abstract public class BaseTest implements ContainerFactory {

	protected static final int SERVERS_TIMEOUT = 45000;

	protected static final String NETWORK = "sd-net";

	static final String CHARS = " abcdefghijklmnopqrstuvwxyz";

	private static final String LOGDIR = "./logs";

	private static final String[] SERVER_NAMES = { "posts", "profiles", "mediastorage" };

	static String image;
	static int majorTest = 0;
	static int minorTest = 0;

	static int sleep = 10;
	static String secret;
	static int minMajorTest = 0, minMinorTest = 0;

	static Random rg = new Random(1L);
	static boolean showTesterStackTrace;

	List<Container> containers = new ArrayList<>();

	public String secret() {
		return secret;
	}

	@Override
	public String image() {
		return image;
	}

	@Override
	public Container createContainer(String name, boolean network) {
		return createContainer(name, network ? NETWORK : null);
	}

	@Override
	public Container createContainer(String name, String network, String... aliases) {
		Container res = new Container(name, network);
		containers.add(res);
		return res;
	}

	public BaseTest major() {
		majorTest++;
		minorTest = 0;
		return this;
	}

	protected Random random() {
		return rg;
	}

	private static void beginTest() {
		minorTest++;
	}

	protected static void beginMajorTest() {
		minorTest++;
	}

	boolean isMandatory() {
		return this.getClass().isAnnotationPresent(MandatoryTest.class);
	}

	boolean isOptional() {
		return this.getClass().isAnnotationPresent(OptionalTest.class);
	}

	protected static RuntimeException wrapException(Exception x) {
		RuntimeException res = new RuntimeException(x.getMessage());
		res.setStackTrace(x.getStackTrace());
		return res;
	}

	protected static void println(String arg) {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.printf("%s )\t %s\n", testString(), arg);
		System.out.println("---------------------------------------------------------------");
	}

	protected static void printf(String format, Object... args) {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.printf("%s )\t", testString());
		System.out.printf(format, args);
		System.out.println("---------------------------------------------------------------");
	}

	static private String testString() {
		return new StringBuilder().append(majorTest).append(CHARS.charAt(minorTest)).toString();
	}

	protected void init() throws Exception {
	}

	protected void prepare() throws Exception {
	}

	protected void execute() throws Exception {
	}

	protected void cleanup() throws Exception {
	}

	protected void sleep(boolean show) {
		Sleep.seconds(sleep, show);
	}

	boolean notSkipped() {
		return majorTest > minMajorTest || (majorTest == minMajorTest && minorTest >= minMinorTest);
	}

	public void test() throws Exception {
		try {
			Lock.disposeAll();
			beginTest();
			init();
			if (notSkipped() || isMandatory()) {
				Docker.get().killByImage(image());
				Docker.get().killNamed(SERVER_NAMES);
				Kafka.clean();
				prepare();
				execute();
				cleanup();
				System.out.println("\rOK                                                                                                                   ");
			} else {
				println(" Skipped...");
			}
		} catch (Exception x) {
			if (showTesterStackTrace)
				x.getCause().printStackTrace();

			System.out.println("FAILED: " + x.getCause().getMessage() + "\n\n");

			if (!isOptional() || Log.getLevel().equals(Level.ALL)) {
				this.onFailure();
				dumpLogs(System.out);
				System.out.println("\n\nContinue (Y/N) ?");
				try (Scanner sc = new Scanner(System.in)) {
					if (sc.nextLine().toLowerCase().equals("y")) {
						System.out.println("Continuing to next test...");
						return;
					} else {
						System.out.println("Exiting...");
						System.exit(0);
					}
				}
			}
		}
	}

	protected void onFailure() throws Exception {
	}

	protected void dumpLogs(OutputStream out) throws Exception {
		PrintWriter pw = new PrintWriter(out);
		containers.forEach(c -> c.dumpLogs(pw));
		pw.flush();
	}

	protected void saveLogs() throws Exception {
		File filename = new File(String.format("%s/%02d_%02d%s.log", LOGDIR, majorTest, minorTest, getClass().getSimpleName()));
		filename.getParentFile().mkdirs();
		System.err.println(filename);
		try (FileOutputStream fos = new FileOutputStream(filename)) {
			dumpLogs(fos);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MandatoryTest {
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface OptionalTest {
	}

	protected <T> T doOrThrow(Supplier<Result<T>> func, Result.ErrorCode expected, String errorFormat) {
		return doOrThrow(func, Arrays.asList(expected), errorFormat);
	}

	protected <T> T doOrThrow(Supplier<Result<T>> func, List<Result.ErrorCode> expected, String errorFormat) {
		Result<T> r = func.get();
		if (expected.contains(r.error())) {
			return r.isOK() ? r.value() : null;
		} else {
			throw new TestFailedException(String.format(errorFormat, expected, r.error()));
		}
	}

	protected <T> void if_NotFailed(Supplier<Result<T>> func, Result.ErrorCode expected, String errorFormat) {
		try {
			Result<T> r = func.get();
			if (!r.error().equals(expected))
				throw new TestFailedException(String.format(errorFormat, expected, r.error()));
		} catch (Exception x) {
			throw new TestFailedException(x.getMessage());
		}
	}

	protected void if_Failed(Runnable closure, Status expected, String errorFormat) {
		try {
			closure.run();
		} catch (WebApplicationException wae) {
			throw new TestFailedException(String.format(errorFormat, expected, wae.getResponse().getStatus()));
		}
	}

}
