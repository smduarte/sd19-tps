package tests.clients;

import java.net.URI;

public class ServerCrashedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ServerCrashedException(URI uri) {
		super(String.format("Server <%s> not running...[ Maybe it crashed ????]", uri));
	}
}