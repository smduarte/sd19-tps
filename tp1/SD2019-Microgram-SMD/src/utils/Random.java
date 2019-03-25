package utils;

public class Random {
	static java.util.Random rnd = new java.util.Random();
	
	public static String key64() {
		return Long.toString((System.nanoTime() ^ rnd.nextLong())>>> 1, 32) ;
	}

	public static String key128() {
		return Long.toString(System.nanoTime()>>> 1, 32) + Long.toString( rnd.nextLong() >>> 1, 32 );
	}

	
	public static boolean nextBoolean() {
		return rnd.nextBoolean();
	}

	public static int nextInt(int bound) {
		return rnd.nextInt(bound);
	}
}
