package io.dani.cloudkv.api.soap;

import io.dani.cloudkv.core.KVStore;
import jakarta.xml.ws.Endpoint;

public final class SoapBootstrap {
    public static Endpoint start(KVStore store, String url) {
        KVSoapImpl impl = new KVSoapImpl(store);
        Endpoint ep = Endpoint.publish(url, impl);
        return ep;
    }
}
