package microgram.impl.srv.rest.media;

import microgram.api.rest.RestMedia;
import microgram.impl.srv.java.JavaMedia;
import microgram.impl.srv.rest.RestResource;

public class RestMediaResources extends RestResource implements RestMedia {

	final String baseUri;
	final JavaMedia impl;

	public RestMediaResources(String baseUri) {
		this.baseUri = baseUri;
		this.impl = new JavaMedia();
	}

	@Override
	public String upload(byte[] bytes) {
		return baseUri + "/" + super.resultOrThrow(impl.upload(bytes));
	}

	@Override
	public byte[] download(String id) {
		return super.resultOrThrow(impl.download(id));
	}

	@Override
	public void delete(String id) {
		super.resultOrThrow(impl.delete(id));
	}
}
