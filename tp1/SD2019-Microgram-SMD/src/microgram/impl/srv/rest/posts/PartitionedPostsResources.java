package microgram.impl.srv.rest.posts;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.impl.clt.Clients;
import microgram.impl.srv.java.PartitionedJavaPosts;
import microgram.impl.srv.rest.Partitioner;

public class PartitionedPostsResources extends RestPostsResources {

	private static final String FROM = "from/";
	private static final String FEED = "feed/";
	private static final String LIKES = "/likes/";

	final Partitioner partitioner;

	public PartitionedPostsResources(Partitioner partitioner) {
		super(new PartitionedJavaPosts(partitioner));
		this.partitioner = partitioner;
	}

	@Override
	public Post getPost(String postId) {
		partitioner.resolvePostPartition(postId, postId);
		return super.resultOrThrow(impl.getPost(postId));
	}

	@Override
	public void deletePost(String postId) {
		partitioner.resolvePostPartition(postId, postId);
		super.resultOrThrow(impl.deletePost(postId));
	}

	@Override
	public String createPost(Post post) {
		partitioner.resolvePostPartition(post, "");
		return super.resultOrThrow(impl.createPost(post));
	}

	@Override
	public void like(String postId, String userId, boolean isLiked) {
		partitioner.resolvePostPartition(postId, postId + LIKES + userId);
		super.resultOrThrow(impl.like(postId, userId, isLiked));
	}

	@Override
	public boolean isLiked(String postId, String userId) {
		partitioner.resolvePostPartition(postId, postId + LIKES + userId);
		return super.resultOrThrow(impl.isLiked(postId, userId));
	}

	@Override
	public List<String> getPosts(String userId) {
		partitioner.resolveProfilePartition(userId, FROM + userId);
		return super.resultOrThrow(impl.getPosts(userId));
	}

	@Override
	public List<String> getFeed(String userId) {
		partitioner.resolveProfilePartition(userId, FEED + userId);
		return super.resultOrThrow(getPartitionedFeed(userId));

	}

	private Result<List<String>> getPartitionedFeed(String userId) {
		Result<List<String>> followingResult = impl.following(userId);
		if (followingResult.isOK()) {
			List<String> results = new CopyOnWriteArrayList<>();
			followingResult.value().parallelStream().forEach(userFollowed -> {
				Posts client = partitioner.clientProfilePartition(userFollowed, impl, Clients::getPosts);
				Result<List<String>> usrPosts = client.getPosts(userFollowed);
				if (usrPosts.isOK())
					results.addAll(usrPosts.value());
			});
			return Result.ok(results);
		} else
			return followingResult;
	}

}