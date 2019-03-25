package smd.microgram.srv.shared;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import smd.microgram.api.java.ProfilesV2;
import utils.Hash;

public class JavaPostsV2 extends microgram.impl.srv.java.JavaPosts implements Posts {

	private static final Set<String> EMPTY_SET = new HashSet<>();
	
	protected Map<String, Post> posts = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	final ProfilesV2 profiles;
	
	public JavaPostsV2( ProfilesV2 profiles ) {
		this.profiles = profiles;
	}
	
	@Override
	public Result<Post> getPost(String postId) {
		Post res = posts.get(postId);
		if (res != null)
			return ok(res);
		else
			return error(NOT_FOUND);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Post post = posts.remove( postId );
		if( post != null ) {
			likes.remove( postId );
			userPosts.getOrDefault(post.getOwnerId(), EMPTY_SET).remove( postId );
			return ok();
		} else
			return error( NOT_FOUND);
 	}


	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Set<String> res = likes.get(postId);
		
		if (res != null)
			return ok(res.contains(userId));
		else
			return error( NOT_FOUND );
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		Set<String> res = userPosts.get(userId);
		if (res != null)
			return ok(new ArrayList<>(res));
		else
			return error( NOT_FOUND );
	}
	
	@Override
	public Result<String> createPost(Post post) {
		Result<Profile> owner = profiles.getProfile( post.getOwnerId() );
		
		if( ! owner.isOK() )
			return error(NOT_FOUND);
		
		String postId = Hash.of(post.getOwnerId(), post.getMediaUrl());
		if (posts.putIfAbsent(postId, post) != null) 
			return error(CONFLICT);
		
		likes.put(postId, ConcurrentHashMap.newKeySet());

		Set<String> posts = userPosts.get(post.getOwnerId());
		if (posts == null)
			userPosts.put(post.getOwnerId(), posts = ConcurrentHashMap.newKeySet());
		posts.add(postId);

		Profile profile = owner.value();
		profile.setPosts( profile.getPosts() + 1);
		profiles.updateProfile(profile);
			
		return ok(postId);
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		
		Set<String> res = likes.get(postId);
		if (res == null)
			return error( NOT_FOUND );

		if (isLiked) {
			if (!res.add(userId))
				return error( CONFLICT );
		} else {
			if (!res.remove(userId))
				return error( NOT_FOUND );
		}

		getPost(postId).value().setLikes(res.size());
		return ok();
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		Result<List<String>> following = profiles.following(userId);
		if( ! following.isOK() ) 
			return error( NOT_FOUND );	
		
		return ok(following.value().stream().flatMap( user -> getPosts(user).value().stream() ).collect( Collectors.toList() ));
	}
}
