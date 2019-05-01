package smd.microgram.api.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import microgram.api.Profile;
import microgram.api.rest.RestProfiles;

@Path(RestProfiles.PATH)
public interface RestProfilesV2 extends RestProfiles {

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	void updateProfile( Profile profile );

	@GET
	@Path("/{userId}/following/")
	List<String> following( @PathParam("userId") String userId);
}
