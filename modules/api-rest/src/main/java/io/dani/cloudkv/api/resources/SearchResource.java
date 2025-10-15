package io.dani.cloudkv.api.resources;

import io.dani.cloudkv.api.llm.LlmClient;
import io.dani.cloudkv.core.KVStore;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public final class SearchResource {

    @Inject
    private KVStore store; // not used directly here but could be

    @Inject
    private LlmClient llm;

    @POST
    @Path("/nlq")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nlq(Map<String, String> body) {
        String q = body.getOrDefault("query", "").toLowerCase();
        String xq = q.contains("starts with")
                ? "for $e in /store/entry[starts-with(@key,'k')] return $e"
                : "for $e in /store/entry return $e";
        return Response.ok(Map.of("xquery", xq)).build();
    }
}
