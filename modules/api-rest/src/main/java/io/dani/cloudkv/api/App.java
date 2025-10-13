package io.dani.cloudkv.api;

import io.dani.cloudkv.api.resources.KvResource;
import io.dani.cloudkv.api.resources.TxResource;
import io.dani.cloudkv.core.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.concurrent.ConcurrentHashMap;

public final class App {
    public static void main(String[] args) throws Exception {
        // Core singletons (simple wiring for MVP)
        ConcurrentKVStore store = new ConcurrentKVStore(new ConcurrentHashMap<>());
        TTLService ttl = new TTLService(store.raw(), 1000);
        TxManager tx = new TxManager(store);

        ResourceConfig rc = new ResourceConfig()
                .register(new KvResource(store))
                .register(new TxResource(tx));

        ServletHolder jersey = new ServletHolder(new ServletContainer(rc));

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.addServlet(jersey, "/*");

        Server server = new Server(8080); // HTTP for speed; HTTPS next commit
        server.setHandler(context);

        try {
            server.start();
            System.out.println("cloudKV running on http://localhost:8080");
            server.join();
        } finally {
            ttl.close();
            server.stop();
        }
    }
}
