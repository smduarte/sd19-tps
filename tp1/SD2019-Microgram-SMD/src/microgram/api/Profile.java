package microgram.api;

/**
 * Represents a user Profile
 * 
 * A user Profile has an unique userId; a comprises: the user's full name; and, a photo, stored at some photourl. This information is immutable.
 * The profile also gathers the user's statistics: ie., the number of posts made, the number of profiles the user is following, the number of profiles following this user. 
 * All these are mutable.
 * 
 * @author smd
 *
 */
public class Profile {
	
	String userId;
	String fullName;
	String photoUrl;
	
	int posts;
	int following;
	int followers;
	
	public Profile() {}
	
	public Profile(String userId, String fullName, String photoUrl, int posts, int following, int followers) {
		this.userId = userId;
		this.fullName = fullName;
		this.photoUrl = photoUrl;
		this.posts = posts;
		this.following = following;
		this.followers = followers;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getFollowing() {
		return following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}
}
