package by.andd3dfx.model

import groovyx.net.http.RESTClient

class Role {
    String name
    String serviceUrl;
    RESTClient restClient;
    String basicAuthClient;
    String basicAuthSecret;

    Role(String name, String serviceUrl, String basicAuthClient, String basicAuthSecret) {
        this.name = name;
        this.serviceUrl = serviceUrl;
        this.restClient = new RESTClient(serviceUrl);
        this.basicAuthClient = basicAuthClient;
        this.basicAuthSecret = basicAuthSecret;
    }

    @Override
    String toString() {
        return "Role{name=$name}";
    }
}
