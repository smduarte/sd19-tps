package microgram.impl.srv.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.Response.Status;

import discovery.Discovery;
import microgram.api.Post;

public class Partitioner {

	public final int ownPartition;
	public final List<String> partitions;
	public final List<String> otherPartitions;

	public Partitioner(String serviceName, String ownURI, int instances) {
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

	}

	public void resolveProfilePartition(String userId, String resourcePath) {
		resolvePartitionFor(userId, resourcePath);
	}

	public void resolvePostPartition(String postId, String resourcePath) {
		resolvePartitionFor(Post.ownerIdFromPostId(postId), resourcePath);
	}

	public void resolvePostPartition(Post post, String resourcePath) {
		resolvePartitionFor(post.getOwnerId(), resourcePath);
	}

	private void resolvePartitionFor(String userId, String resourcePath) {
		int partition = profile2partition(userId);
		if (partition != ownPartition) {
			URI location = URI.create(partitions.get(partition).concat(resourcePath));
			System.err.println("redirecting to:" + location);
			throw new RedirectionException(Status.TEMPORARY_REDIRECT, location);
		}
	}

	public <T> T clientProfilePartition(String userId, T self, Function<String, T> func) {
		int partition = profile2partition(userId);
		if (partition == ownPartition)
			return self;
		else
			return func.apply(partitions.get(partition));
	}

	public boolean isOwnProfilePartition(String userId) {
		return profile2partition(userId) == ownPartition;
	}

	private int profile2partition(String userId) {
		return (userId.hashCode() >>> 1) % partitions.size();
	}
}
