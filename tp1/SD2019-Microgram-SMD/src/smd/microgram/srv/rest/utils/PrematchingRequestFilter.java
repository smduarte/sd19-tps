package smd.microgram.srv.rest.utils;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class PrematchingRequestFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		System.err.println("method: " + ctx.getMethod() + " uri: " + ctx.getUriInfo().getRequestUri());
	}
}