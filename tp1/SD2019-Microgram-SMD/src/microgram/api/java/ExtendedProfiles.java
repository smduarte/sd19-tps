package microgram.api.java;

import java.util.List;

import microgram.api.Profile;

/**
 * 
 * Interface for the service that manages Users/Profiles.
 * 
 */
public interface ExtendedProfiles extends Profiles {

	/**
	 * Decentralized version of search
	 * 
	 * @param partitioned - non-null indicates the search is local to server (ie.
	 *                    partition)
	 */
	default Result<List<Profile>> search(String prefix, boolean partitioned) {
		return search(prefix);
	}

	/**
	 * Obtains the list of profiles followed by the given user
	 * 
	 * @param userId - the profile
	 * @return - list of profiles
	 */
	@Override
	Result<List<String>> following(String userId);
}
