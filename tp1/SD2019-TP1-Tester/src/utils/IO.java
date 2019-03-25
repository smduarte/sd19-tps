package utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.file.Files;
import java.util.Collection;

public class IO {

	protected IO() {
	};

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
	
	public static void write( byte[] data, OutputStream out ) {
		try {
			out.write( data );
		} catch( IOException x) {
			x.printStackTrace();
		} 
	}
	
	public static void dumpString(String data, OutputStream os, boolean close) {
		try {
			os.write(data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (close)
			close(os);
	}

	public static void close(Socket s) {
		try {
			if (s != null)
				s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(ServerSocket s) {
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(OutputStream out) {
		try {
			if (out != null)
				out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close(Channel ch) {
		try {
			if (ch != null)
				ch.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String readString(InputStream is ) throws IOException {
		byte[] tmp = new byte[256];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int n;
		while( (n = is.read(tmp)) > 0 )
			baos.write( tmp, 0, n );
		return new String( baos.toByteArray() );
	}
	
	public static void redirect(String stdout, String stderr) {
		try {
			System.setOut(new PrintStream(new FileOutputStream(stdout)));
			System.setErr(new PrintStream(new FileOutputStream(stderr)));
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	public static void dumpTo(InputStream in, OutputStream out) {
		try {
			int n;
			byte[] tmp = new byte[1024];
			while ((n = in.read( tmp )) > 0)
				out.write( tmp, 0, n);
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
	
	public static void dumpTo(Collection<?> data, String dstFile) {
		try {
			PrintStream ps = new PrintStream(dstFile);
			for (Object i : data)
				ps.println(i);
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static class Gobbler extends ByteArrayOutputStream {
		public Gobbler(InputStream is) {
			new Thread(() -> {
				try {
					int n;
					byte[] buffer = new byte[1024];
					while ((n = is.read(buffer)) > 0) {
						super.write(buffer, 0, n);
					}
				} catch (Exception e) {
				}
			}).start();
			;
		}

		@Override
		public void close() {
			try {
				super.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return new String(super.toByteArray());
		}
	}
	
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
	
	public static void receive( DatagramSocket s, DatagramPacket pkt ) {
		try {
			s.receive( pkt );
		} catch (IOException e) {
		}
	}
}
