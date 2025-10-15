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
    private KVStore store; // reserved for future use

    @Inject
    private LlmClient llm;

    public static class SearchRequest {
        public String query;
        public SearchRequest() {}
        public String getQuery() { return query; }
        public void setQuery(String q) { this.query = q; }
    }

    public static class SearchResponse {
        public String xquery;
        public SearchResponse() {}
        public SearchResponse(String xq) { this.xquery = xq; }
        public String getXquery() { return xquery; }
        public void setXquery(String xq) { this.xquery = xq; }
    }

    @POST
    @Path("/nlq")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response nlq(SearchRequest body) {
        String q = (body == null || body.getQuery() == null) ? "" : body.getQuery().toLowerCase();

        // If LLM is enabled, allow it to suggest an xquery (NoOp returns empty)
        String llmSuggestion = null;
        try {
            llmSuggestion = llm.complete(q);
        } catch (Exception e) {
            llmSuggestion = null;
        }
        if (llmSuggestion != null && !llmSuggestion.isBlank()) {
            return Response.ok(new SearchResponse(llmSuggestion)).build();
        }

        // Deterministic heuristics mapping
        String xq;
        if (q.contains("starts with")) {
            // try to extract following token as prefix
            String prefix = "k"; // default fallback
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("starts with\\s+(['\"]?)(\\w+)\\1").matcher(q);
            if (m.find()) prefix = m.group(2);
            xq = String.format("for $e in /store/entry[starts-with(@key,'%s')] return $e", prefix);
        } else if (q.contains("contains")) {
            String token = "";
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("contains\\s+(['\"]?)(\\w+)\\1").matcher(q);
            if (m.find()) token = m.group(2);
            if (token.isBlank()) xq = "for $e in /store/entry return $e";
            else xq = String.format("for $e in /store/entry[contains(@key,'%s') or contains(value,'%s')] return $e", token, token);
        } else if (q.contains("all keys") || q.contains("all entries") || q.contains("all")) {
            xq = "for $e in /store/entry return $e";
        } else {
            xq = "for $e in /store/entry return $e";
        }

        return Response.ok(new SearchResponse(xq)).build();
    }

}
