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
    @Path("/transform")
    public Response transform(@QueryParam("xsl") String xsl,
                              @QueryParam("format") String format,
                              @Context jakarta.ws.rs.core.UriInfo uriInfo) throws Exception {
        if (!enabled()) {
            return Response.status(404).entity("XML export disabled").build();
        }

        String stylesheet = (xsl == null || xsl.isBlank()) ? "store-to-html.xsl" : xsl;
        java.io.InputStream in = getClass().getResourceAsStream("/xsl/" + stylesheet);
        if (in == null) return Response.status(404).entity("xsl not found: " + stylesheet).build();

        javax.xml.transform.Source xslSrc = new javax.xml.transform.stream.StreamSource(in);
        javax.xml.transform.TransformerFactory tf = new net.sf.saxon.TransformerFactoryImpl();
        javax.xml.transform.Transformer t = tf.newTransformer(xslSrc);

        // Set all other query params as XSLT params
        for (java.util.Map.Entry<String, java.util.List<String>> e : uriInfo.getQueryParameters().entrySet()) {
            String key = e.getKey();
            if (key.equals("xsl") || key.equals("format")) continue;
            String val = e.getValue().isEmpty() ? "" : e.getValue().get(0);
            t.setParameter(key, val);
        }

        java.io.StringWriter out = new java.io.StringWriter();
        t.transform(new javax.xml.transform.dom.DOMSource(svc.snapshotAsDocument()), new javax.xml.transform.stream.StreamResult(out));

        String body = out.toString();
        String ct = null;
        if (format != null) {
            switch (format) {
                case "json": ct = "application/json"; break;
                case "text": ct = "text/plain"; break;
                case "csv": ct = "text/csv"; break;
                case "xml": ct = "application/xml"; break;
                case "html": ct = "text/html"; break;
            }
        }
        if (ct == null) ct = "application/xml";
        return Response.ok(body, ct).build();
    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_XML)
    public Response xquery(@QueryParam("xq") String xq) throws Exception {
        if (!enabled()) {
            return Response.status(404).entity("XML export disabled").build();
        }
        if (xq == null || xq.isBlank()) {
            return Response.status(400).entity("missing xq parameter").build();
        }
        String result = svc.runXQuery(xq);
        return Response.ok(result).build();
    }
}
