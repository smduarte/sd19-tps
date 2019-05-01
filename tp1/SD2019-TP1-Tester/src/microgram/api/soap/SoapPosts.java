package microgram.api.soap;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import microgram.api.Post;

@WebService(serviceName=SoapPosts.NAME, targetNamespace=SoapPosts.NAMESPACE, endpointInterface=SoapPosts.INTERFACE)
public interface SoapPosts {
	
	static final String NAME = "posts";
	static final String NAMESPACE = "http://sd2019";
	static final String INTERFACE = "microgram.api.soap.SoapPosts";
	
	@WebMethod
	Post getPost( String postId ) throws MicrogramException;
		
	@WebMethod
	void deletePost( String postId ) throws MicrogramException;
	
	@WebMethod
	String createPost( Post post ) throws MicrogramException;

	@WebMethod
	boolean isLiked(String postId, String userId) throws MicrogramException;

	@WebMethod
	void like( String postId, String userId, boolean isLiked) throws MicrogramException;
		
	@WebMethod
	List<String> getPosts( String userId) throws MicrogramException;
	
	@WebMethod
	List<String> getFeed( String userId) throws MicrogramException;
}
