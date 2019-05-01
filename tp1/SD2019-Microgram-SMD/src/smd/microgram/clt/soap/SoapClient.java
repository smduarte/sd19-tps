package smd.microgram.clt.soap;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;

import java.net.URI;

import microgram.api.java.Result;
import microgram.api.java.Result.ErrorCode;
import microgram.api.soap.MicrogramException;
import smd.microgram.clt.java.RetryClient;

abstract class SoapClient extends RetryClient {
	protected static final String WSDL = "?wsdl";
	static final String SOAP_CONN_TIMEOUT = "1000";
	static final String SOAP_RECV_TIMEOUT = "1000";

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
		}
	}

	protected <T> Result<T> tryCatchVoid(MicroagramVoidSupplier r) {
		try {
			r.run();
			return ok();
		} catch (MicrogramException e) {
			return error(errorCode(e));
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

	static {
		// timeouts in ms
		System.setProperty("javax.xml.ws.client.receiveTimeout", SOAP_RECV_TIMEOUT);
		System.setProperty("javax.xml.ws.client.connectionTimeout", SOAP_CONN_TIMEOUT);
	}

}
