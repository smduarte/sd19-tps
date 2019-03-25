package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;


final public class IO {


	public static void write( File out, byte[] data ) {
		try {
			Files.write( out.toPath(), data);
		} catch( Exception x ) {
		}
	}

	public static byte[] read( File from) {
		try {
			return Files.readAllBytes( from.toPath() );
		} catch( Exception x ) {
			x.printStackTrace();
			return null;
		}
	}
	
	public static void delete( File file) {
		try {
			Files.delete( file.toPath() );
		} catch( Exception x ) {
			x.printStackTrace();
		}
	}
	
	public static void write( OutputStream out, char data ) {
		try {
			out.write( data );			
		} catch( IOException x ) {
		}
	}
	
	public static void write( OutputStream out, byte[] data ) {
		try {
			out.write( data );			
		} catch( IOException x ) {
		}
	}
	
	public static void write( OutputStream out, byte[] data, int off, int len ) {
		try {
			out.write( data, off, len );			
		} catch( IOException x ) {
		}
	}
	
	public static String readLine( BufferedReader reader ) {
		try {
			return reader.readLine();
		} catch (IOException e) {
			return null;
		}
	}
}
