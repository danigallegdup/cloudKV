package io.dani.cloudkv.api;

import io.dani.cloudkv.core.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Map;

import net.sf.saxon.s9api.*;

public final class ExportService {
    private final KVStore store;

    public ExportService(KVStore store) {
        this.store = store;
    }

    public Document snapshotAsDocument() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document doc = f.newDocumentBuilder().newDocument();
        Element root = doc.createElement("store");
        doc.appendChild(root);

        if (store instanceof io.dani.cloudkv.core.ConcurrentKVStore) {
            for (Map.Entry<String, ValueEntry> en : ((io.dani.cloudkv.core.ConcurrentKVStore) store).raw().entrySet()) {
                Element e = doc.createElement("entry");
                e.setAttribute("key", en.getKey());
                if (en.getValue().getTtlMs() != null) e.setAttribute("ttlMs", String.valueOf(en.getValue().getTtlMs()));
                Element val = doc.createElement("value");
                val.setTextContent(en.getValue().getValue());
                e.appendChild(val);
                root.appendChild(e);
            }
        }
        return doc;
    }

    public String snapshotXmlString() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(snapshotAsDocument()), new StreamResult(out));
        return out.toString();
    }

    public String snapshotHtml() throws Exception {
        TransformerFactory tf = new net.sf.saxon.TransformerFactoryImpl();
        Source xsl = new javax.xml.transform.stream.StreamSource(getClass().getResourceAsStream("/xsl/store-to-html.xsl"));
        Transformer t = tf.newTransformer(xsl);
        StringWriter out = new StringWriter();
        t.transform(new DOMSource(snapshotAsDocument()), new StreamResult(out));
        return out.toString();
    }

    public String runXQuery(String query) throws Exception {
        Processor proc = new Processor(false);
        XQueryCompiler comp = proc.newXQueryCompiler();
        XQueryExecutable exec = comp.compile(query);
        XQueryEvaluator eval = exec.load();
        Document doc = snapshotAsDocument();
        XdmNode ctx = proc.newDocumentBuilder().wrap(doc);
        eval.setContextItem(ctx);
        StringWriter sw = new StringWriter();
        Serializer ser = proc.newSerializer(sw);
        eval.run(ser);
        return sw.toString();
    }
}
