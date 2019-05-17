package tests.clients.soap;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;

import java.net.URI;

import javax.xml.ws.WebServiceException;

import microgram.api.java.Result;
import microgram.api.java.Result.ErrorCode;
import microgram.api.soap.MicrogramException;
import tests.clients.RetryClient;
import tests.clients.ServerCrashedException;

abstract class SoapClient extends RetryClient {
	protected static final String WSDL = "?wsdl";
	static final int SOAP_CONN_TIMEOUT = 2000;
	static final int SOAP_RECV_TIMEOUT = 5000;

	protected final URI uri;

	public SoapClient(URI uri) {
		this.uri = uri;
	}

	static interface MicrogramResutSupplier<T> {
		T get() throws MicrogramException;
	}

	static interface MicroagramVoidSupplier {
		void run() throws MicrogramException;
	}

	protected <T> Result<T> tryCatchResult(MicrogramResutSupplier<T> sup) {
		try {
			T result = sup.get();
			return ok(result);
		} catch (MicrogramException e) {
			return error(errorCode(e));
		} catch (WebServiceException e) {
			if (e.getCause() instanceof java.net.NoRouteToHostException)
				throw new ServerCrashedException(uri);
			else
				throw new RuntimeException(e.getMessage());
		}
	}

	protected <T> Result<T> tryCatchVoid(MicroagramVoidSupplier r) {
		try {
			r.run();
			return ok();
		} catch (MicrogramException e) {
			return error(errorCode(e));
		} catch (WebServiceException e) {
			if (e.getCause() instanceof java.net.NoRouteToHostException)
				throw new ServerCrashedException(uri);
			else
				throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	static private ErrorCode errorCode(MicrogramException me) {
		switch (me.getMessage()) {
		case "OK":
			return ErrorCode.OK;
		case "CONFLICT":
			return ErrorCode.CONFLICT;
		case "NOT_FOUND":
			return ErrorCode.NOT_FOUND;
		case "INTERNAL_ERROR":
			return ErrorCode.INTERNAL_ERROR;
		case "NOT_IMPLEMENTED":
			return ErrorCode.NOT_IMPLEMENTED;
		default:
			return ErrorCode.INTERNAL_ERROR;
		}
	}
}
