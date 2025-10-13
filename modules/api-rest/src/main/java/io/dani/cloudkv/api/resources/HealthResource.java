package io.dani.cloudkv.api.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public final class HealthResource {
    @GET
    public Response ping() {
        return Response.ok(Map.of("status", "UP")).build();
    }
}
