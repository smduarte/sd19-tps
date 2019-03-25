package smd.microgram.clt.rest;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.java.Media;
import microgram.api.java.Result;
import microgram.api.rest.RestMediaStorage;

public class MediaClient extends RestClient implements Media {

	public MediaClient(URI mediaStorage) {
		super(mediaStorage, RestMediaStorage.PATH );
	}

	public Result<String> upload(byte[] bytes) {
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));

		return responseContents(r, Status.OK, new GenericType<String>(){});
	}

	public Result<byte[]> download(String url) {
		System.err.println( url );
		
		Response r = client.target(url)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();
		return responseContents(r, Status.OK, new GenericType<byte[]>(){});
	}
}