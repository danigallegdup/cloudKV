package io.dani.cloudkv.api.resources;

import io.dani.cloudkv.core.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Optional;

@Path("/kv")
@Produces(MediaType.APPLICATION_JSON)
public final class KvResource {

    @Inject
    private KVStore store; // injected by HK2

    @GET
    public Response get(@QueryParam("key") String key) {
        if (key == null || key.isBlank()) {
            return Response.status(400).entity(Map.of("error","INVALID_KEY")).build();
        }
        Optional<ValueEntry> v = store.get(key);
        if (v.isEmpty() || v.get().isExpired(System.currentTimeMillis())) {
            return Response.status(404).entity(Map.of("error","NOT_FOUND")).build();
        }
        ValueEntry e = v.get();
        Long ttlRem = (e.expiresAtMs() == null)
                ? null
                : Math.max(0, e.expiresAtMs() - System.currentTimeMillis());

        // Use a mutable map to avoid NPE on null
        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("key", key);
        resp.put("value", e.value());
        resp.put("createdAtMs", e.createdAtMs());
        if (ttlRem != null) {
            resp.put("ttlMillisRemaining", ttlRem);
        }
        return Response.ok(resp).build();
    }


    @POST
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response put(@QueryParam("key") String key,
                        @QueryParam("ttl") Long ttlMs,
                        String body) {
        if (key == null || key.isBlank()) return Response.status(400).entity(Map.of("error","INVALID_KEY")).build();
        String value = body == null ? "" : body;
        long now = System.currentTimeMillis();
        Long expires = (ttlMs == null) ? null : now + Math.max(1, ttlMs);
        store.put(key, new ValueEntry(value, now, expires));
        return Response.ok(Map.of("status","OK")).build();
    }

    @DELETE
    public Response delete(@QueryParam("key") String key) {
        if (key == null || key.isBlank()) return Response.status(400).entity(Map.of("error","INVALID_KEY")).build();
        store.delete(key);
        return Response.noContent().build();
    }
}
