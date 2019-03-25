package utils;

public class Loops {

	public static void forever( Runnable r, int ms ) {
		for(;;) {
			try {
				r.run();
			} catch(Exception x ) {
				x.printStackTrace();				
			}
			Sleep.ms( ms );
		}
	}
}
