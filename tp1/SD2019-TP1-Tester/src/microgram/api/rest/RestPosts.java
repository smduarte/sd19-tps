package microgram.api.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import microgram.api.Post;

/**
 * REST API of the Posts service
 * 
 * @author smd
 *
 */
@Path(RestPosts.PATH)
public interface RestPosts {

	static final String PATH = "/posts";

	@GET
	@Path("/{postId}")
	@Produces(MediaType.APPLICATION_JSON)
	Post getPost(@PathParam("postId") String postId);

	@DELETE
	@Path("/{postId}")
	void deletePost(@PathParam("postId") String postId);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String createPost(Post post);

	@GET
	@Path("/{postId}/likes/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	boolean isLiked(@PathParam("postId") String postId, @PathParam("userId") String userId);

	@PUT
	@Path("/{postId}/likes/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	void like(@PathParam("postId") String postId, @PathParam("userId") String userId, boolean isLiked);

	@GET
	@Path("/from/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	List<String> getPosts(@PathParam("userId") String userId);

	@GET
	@Path("/feed/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	List<String> getFeed(@PathParam("userId") String userId);
}
