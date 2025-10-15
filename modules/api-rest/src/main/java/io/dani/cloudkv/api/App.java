package io.dani.cloudkv.api;

import io.dani.cloudkv.api.resources.*;
import io.dani.cloudkv.core.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.internal.inject.AbstractBinder;

import java.util.concurrent.ConcurrentHashMap;

public final class App {
    public static void main(String[] args) throws Exception {
        // Core singletons
        ConcurrentKVStore store = new ConcurrentKVStore(new ConcurrentHashMap<>());
        TTLService ttl = new TTLService(store.raw(), 1000);
        TxManager tx = new TxManager(store);

        ResourceConfig rc = new ResourceConfig()
                .packages("io.dani.cloudkv.api.resources") // scan @Path classes
                .register(new AbstractBinder() {
                    @Override protected void configure() {
                        bind(store).to(KVStore.class);
                        bind(tx).to(TxManager.class);
                           bind(new io.dani.cloudkv.api.llm.NoOpLlmClient()).to(io.dani.cloudkv.api.llm.LlmClient.class);
                    }
                });

        ServletHolder jersey = new ServletHolder(new ServletContainer(rc));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.addServlet(jersey, "/*");

        Server server = new Server(8080);
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
