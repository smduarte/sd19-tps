package smd.microgram.clt.rest;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;

import java.net.URI;
import java.util.function.Supplier;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import microgram.api.java.Result;
import microgram.api.java.Result.ErrorCode;
import smd.microgram.clt.java.RetryClient;
import utils.Sleep;

abstract class RestClient extends RetryClient {

	private static final int READ_TIMEOUT = 15000;
	private static final int CONNECT_TIMEOUT = 10000;

	protected final URI uri;
	protected final Client client;
	protected final WebTarget target;
	protected final ClientConfig config;

	public RestClient(URI uri, String path) {
		this.uri = uri;
		this.config = new ClientConfig();
		this.config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		this.config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		this.client = ClientBuilder.newClient(config);
		this.target = this.client.target(uri).path(path);
	}

	// Get the actual response, when the status matches what was expected, otherwise
		// return a default value
		protected <T> Result<T> verifyResponse(Response r, Status expected) {
			try {
				StatusType status = r.getStatusInfo();
				if( status.equals( expected ) )
					return ok();
				else
					return error( errorCode( status.getStatusCode() ));
			} finally {
				r.close();
			}
		}
		
	// Get the actual response, when the status matches what was expected, otherwise
	// return a default value
	protected <T> Result<T> responseContents(Response r, Status expected, GenericType<T> gtype) {
		try {
			StatusType status = r.getStatusInfo();
			if( status.equals( expected ) )
				return ok(r.readEntity(gtype));
			else
				return error( errorCode( status.getStatusCode() ));
		} finally {
			r.close();
		}
	}
	
	// higher order function to retry forever a call of a void return type,
	// until it succeeds and returns to break the loop
	protected void reTry(Runnable func) {
		for (;;)
			try {
				func.run();
				return;
			} catch (ProcessingException x) {
				x.printStackTrace();
				Sleep.ms(RETRY_SLEEP);
			}
	}

	// higher order function to retry forever a call until it succeeds
	// and return an object of some type T to break the loop
	protected <T> T reTry(Supplier<T> func) {
		for (;;)
			try {
				return func.get();
			} catch (ProcessingException x) {
				x.printStackTrace();
				Sleep.ms(RETRY_SLEEP);
			}
	}

	@Override
	public String toString() {
		return uri.toString();
	}
	
	
	static private ErrorCode errorCode(int status) {
		switch (status) {
		case 200:
		case 209:
			return ErrorCode.OK;
		case 404:
			return ErrorCode.NOT_FOUND;
		case 409:
			return ErrorCode.CONFLICT;
		case 500:
			return ErrorCode.INTERNAL_ERROR;
		case 501:
			return ErrorCode.NOT_IMPLEMENTED;
		default:
			return ErrorCode.INTERNAL_ERROR;
		}
	}
}
