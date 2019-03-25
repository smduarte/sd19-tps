package smd.microgram.srv.soap;

import java.util.List;

import javax.jws.WebService;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapPosts;
import microgram.impl.srv.soap.SoapService;
import smd.microgram.clt.MicrogramService;
import smd.microgram.srv.shared.JavaPostsV2;

@WebService(serviceName=SoapPosts.NAME, targetNamespace=SoapPosts.NAMESPACE, endpointInterface=SoapPosts.INTERFACE)
public class PostsWebService extends SoapService implements SoapPosts {

	Posts impl;
	
	protected PostsWebService() {
	}

	@Override
	public Post getPost( String postId ) throws MicrogramException {
		return super.resultOrThrow( impl().getPost(postId));
	}
	
	@Override
	public String createPost(Post post) throws MicrogramException {
		return super.resultOrThrow( impl().createPost(post));
	}

	@Override
	public boolean isLiked(String postId, String userId) throws MicrogramException {
		return super.resultOrThrow( impl().isLiked(postId, userId));
	}

	@Override
	public void like(String postId, String userId, boolean isLiked) throws MicrogramException {
		super.resultOrThrow( impl().like(postId, userId, isLiked));
	}

	@Override
	public List<String> getPosts(String userId) throws MicrogramException {
		return super.resultOrThrow(impl().getPosts(userId));
	}

	@Override
	public List<String> getFeed(String userId) throws MicrogramException {
		return super.resultOrThrow(impl().getFeed(userId));
	}

	@Override
	public void deletePost(String postId) throws MicrogramException {
		throw new MicrogramException("not implemented...");
	}
	
	private Posts impl() {
		if( impl == null) {
			impl = new JavaPostsV2(MicrogramService.getProfiles());
		}
		return impl;
	}
}
