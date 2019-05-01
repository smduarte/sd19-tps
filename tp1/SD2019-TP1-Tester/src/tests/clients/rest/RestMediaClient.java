package tests.clients.rest;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.java.Media;
import microgram.api.java.Result;

public class RestMediaClient extends RestClient implements Media {

	public RestMediaClient(URI mediaStorage) {
		super(mediaStorage);
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		return super.reTry(() -> _upload(bytes));
	}

	@Override
	public Result<byte[]> download(String id) {
		return super.reTry(() -> _download(id));
	}

	@Override
	public Result<Void> delete(String id) {
		return super.reTry(() -> _delete(id));
	}

	private Result<String> _upload(byte[] bytes) {
		Response r = target.request().accept(MediaType.APPLICATION_JSON).post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));

		return responseContents(r, Status.OK, new GenericType<String>() {
		});
	}

	private Result<byte[]> _download(String url) {
		Response r = client.target(url).request().accept(MediaType.APPLICATION_OCTET_STREAM).get();

		return responseContents(r, Status.OK, new GenericType<byte[]>() {
		});
	}

	private Result<Void> _delete(String url) {
		Response r = client.target(url).request().delete();

		return super.verifyResponse(r, Status.NO_CONTENT);
	}

}