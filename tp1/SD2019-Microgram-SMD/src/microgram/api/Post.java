package microgram.api;

/**
 * Represents a Post.
 * 
 * A post has an unique postId and a single ownerId profile; comprises of an image, taken at some location and stored at some mediaurl; it is timestamped.
 * A post also has a number of likes, which can increase or decrease over time. It is the only piece of information that is mutable.
 *  
 * @author smd
 *
 */
public class Post {
	
	String postId;
	String ownerId;
	String mediaUrl;
	String location;
	long timestamp;
	
	int likes;

	public Post() {}


	public Post(String postId, String ownerId, String mediaUrl, String location, long timestamp, int likes) {
		this.postId = postId;
		this.ownerId = ownerId;
		this.mediaUrl = mediaUrl;
		this.location = location;
		this.timestamp = timestamp;
		this.likes = likes;
	}
	
	public String getMediaUrl() {
		return mediaUrl;
	}
	public String getLocation() {
		return location;
	}

	public int getLikes() {
		return likes;
	}
	
	public String getPostId() {
		return postId;
	}
	
	public String getOwnerId() {
		return ownerId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setPostId(String postId) {
		this.postId = postId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}
}
