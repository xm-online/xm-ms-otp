package com.icthh.xm.ms.otp.security;

import java.net.URI;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class HttpComponentsClientHttpRequestFactoryBasicAuth extends HttpComponentsClientHttpRequestFactory {

    public HttpComponentsClientHttpRequestFactoryBasicAuth() {
        super();
    }

    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        return createHttpContext();
    }

    private HttpContext createHttpContext() {
        HttpClientContext localcontext = HttpClientContext.create();
        localcontext.setAuthCache(new BasicAuthCache());
        return localcontext;
    }
}
