package io.dani.cloudkv.api.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(serviceName = "KVService")
public interface KVSoap {
    @WebMethod String get(String key);
    @WebMethod String put(String key, String value);
    @WebMethod boolean delete(String key);
}
