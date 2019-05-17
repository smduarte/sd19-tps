package tests.clients.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import com.sun.xml.ws.client.BindingProviderProperties;

import discovery.Discovery;
import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.api.soap.SoapPosts;
import utils.Url;

public class SoapPostsClient extends SoapClient implements Posts {

	private static final String SERVICE = "Microgram-Posts";

	SoapPosts impl;

	public SoapPostsClient() {
		this(Discovery.findUrisOf(SERVICE, 1)[0]);
	}

	public SoapPostsClient(URI serverUri) {
		super(serverUri);
	}

	@Override
	public Result<Post> getPost(String postId) {
		return super.tryCatchResult(() -> impl().getPost(postId));
	}

	@Override
	public Result<String> createPost(Post post) {
		return super.tryCatchResult(() -> impl().createPost(post));
	}

	@Override
	public Result<Void> deletePost(String postId) {
		return super.tryCatchVoid(() -> impl().deletePost(postId));
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		return super.tryCatchVoid(() -> impl().like(postId, userId, isLiked));
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		return super.tryCatchResult(() -> impl().isLiked(postId, userId));
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		return super.tryCatchResult(() -> impl().getPosts(userId));
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		return super.tryCatchResult(() -> impl().getFeed(userId));
	}

	synchronized private SoapPosts impl() {
		if (impl == null) {
			QName QNAME = new QName(SoapPosts.NAMESPACE, SoapPosts.NAME);
			Service service = Service.create(Url.from(super.uri + WSDL), QNAME);
			this.impl = service.getPort(microgram.api.soap.SoapPosts.class);
			((BindingProvider) this.impl).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, SOAP_RECV_TIMEOUT);
			((BindingProvider) this.impl).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, SOAP_CONN_TIMEOUT);
		}
		return impl;
	}
}
