package io.dani.cloudkv.api.resources;

import io.dani.cloudkv.core.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/tx")
@Produces(MediaType.APPLICATION_JSON)
public final class TxResource {

    @Inject
    private TxManager tx; // injected by HK2

    @POST @Path("/begin")
    public Response begin() {
        UUID id = tx.begin();
        return Response.ok(Map.of("txId", id.toString())).build();
    }

    @POST @Path("/commit")
    public Response commit(@HeaderParam("X-Tx-Id") String txId) {
        try {
            tx.commit(UUID.fromString(txId));
            return Response.ok(Map.of("status", "COMMITTED")).build();
        } catch (Exception e) {
            return Response.status(400).entity(Map.of("error","INVALID_TX")).build();
        }
    }

    @POST @Path("/rollback")
    public Response rollback(@HeaderParam("X-Tx-Id") String txId) {
        try {
            tx.rollback(UUID.fromString(txId));
            return Response.ok(Map.of("status", "ROLLEDBACK")).build();
        } catch (Exception e) {
            return Response.status(400).entity(Map.of("error","INVALID_TX")).build();
        }
    }

    @POST @Path("/put")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response put(@HeaderParam("X-Tx-Id") String txId,
                        @QueryParam("key") String key,
                        @QueryParam("ttl") Long ttlMs,
                        String body) {
        if (key == null || key.isBlank()) return Response.status(400).entity(Map.of("error","INVALID_KEY")).build();
        long now = System.currentTimeMillis();
        Long expires = (ttlMs==null)?null: now + Math.max(1, ttlMs);
        try {
            tx.put(UUID.fromString(txId), key, new ValueEntry(body==null?"":body, now, expires));
            return Response.ok(Map.of("status","OK")).build();
        } catch (Exception e) {
            return Response.status(400).entity(Map.of("error","INVALID_TX")).build();
        }
    }
}
