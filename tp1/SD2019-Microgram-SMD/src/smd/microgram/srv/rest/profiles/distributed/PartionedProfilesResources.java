package smd.microgram.srv.rest.profiles.distributed;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.Response.Status;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import smd.discovery.Discovery;
import smd.microgram.clt.Clients;
import smd.microgram.srv.rest.profiles.RestProfilesResources;

public class PartionedProfilesResources extends RestProfilesResources {
	private static final String FOLLOWING = "/following/";

	final int ownPartition;
	final List<String> partitions;
	final List<String> otherPartitions;

	public PartionedProfilesResources(String serviceName, String ownURI, int instances) {

		URI[] uris;
		while ((uris = Discovery.findUrisOf(serviceName, instances)).length != instances)
			;

		this.partitions = new ArrayList<>(uris.length);

		for (URI uri : uris)
			partitions.add(uri + "/");

		Collections.sort(partitions);
		this.ownPartition = partitions.indexOf(ownURI + "/");

		this.otherPartitions = new ArrayList<>(partitions);
		this.otherPartitions.remove(ownPartition);

		System.err.println(ownPartition + "|" + otherPartitions);
	}

	private void resolvePartitionFor(String userId, String resourcePath) {
		int partition = (userId.hashCode() >>> 1) % partitions.size();
		if (partition != ownPartition) {
			URI location = URI.create(partitions.get(partition).concat(resourcePath));
			System.err.println("Redirect to: " + location);
			throw new RedirectionException(Status.TEMPORARY_REDIRECT, location);
		}
	}

	@Override
	public Profile getProfile(String userId) {
		resolvePartitionFor(userId, userId);
		return super.getProfile(userId);
	}

	@Override
	public void createProfile(Profile profile) {
		resolvePartitionFor(profile.getUserId(), "");
		super.createProfile(profile);
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) {
		resolvePartitionFor(userId1, userId1 + FOLLOWING + userId2);
		super.follow(userId1, userId2, isFollowing);
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) {
		resolvePartitionFor(userId1, userId1 + FOLLOWING + userId2);
		return super.isFollowing(userId1, userId2);
	}

	@Override
	public List<String> following(String userId) {
		resolvePartitionFor(userId, userId + FOLLOWING);
		return super.following(userId);
	}

	@Override
	public void deleteProfile(String userId) {
		resolvePartitionFor(userId, userId);
		super.deleteProfile(userId);
	}

	@Override
	public List<Profile> search(String prefix, boolean partitioned) {
		System.err.println(prefix + "/" + partitioned);
		List<Profile> results = new CopyOnWriteArrayList<>();

		results.addAll(impl.search(prefix).value());

		if (!partitioned)
			otherPartitions.parallelStream().forEach(partitionURI -> {
				Result<List<Profile>> r = Clients.getProfiles(partitionURI).search(prefix, true);
				if (r.isOK())
					results.addAll(r.value());
			});
		return results;
	}

	@Override
	synchronized protected Profiles impl() {
		if (impl == null) {
			impl = new DistributedExtendedProfiles(Clients.getPosts(), Clients.getProfiles());
		}
		return impl;
	}

}