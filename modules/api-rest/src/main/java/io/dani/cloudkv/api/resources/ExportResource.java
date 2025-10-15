package io.dani.cloudkv.api.resources;

import io.dani.cloudkv.api.ExportService;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/export")
public class ExportResource {
    private final ExportService svc;
    public ExportResource(ExportService svc) { this.svc = svc; }

    private boolean enabled() {
        String v = System.getenv("XML_EXPORT_ENABLED");
        if (v == null) v = System.getProperty("XML_EXPORT_ENABLED");
        if (v == null) return true; // default on for dev
        return !(v.equalsIgnoreCase("0") || v.equalsIgnoreCase("false") || v.equalsIgnoreCase("off"));
    }

    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    public Response xml() throws Exception {
        if (!enabled()) {
            return Response.status(404).entity("XML export disabled").build();
        }
        String xml = svc.snapshotXmlString();
        return Response.ok(xml).build();
    }

    @GET
    @Path("/html")
    @Produces(MediaType.TEXT_HTML)
    public Response html() throws Exception {
        if (!enabled()) {
            return Response.status(404).entity("HTML export disabled").build();
        }
        String html = svc.snapshotHtml();
        return Response.ok(html).build();
    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_XML)
    public Response xquery(@QueryParam("xq") String xq) throws Exception {
        if (!enabled()) {
            return Response.status(404).entity("XML export disabled").build();
        }
        String result = svc.runXQuery(xq);
        return Response.ok(result).build();
    }
}
