package com.icthh.xm.ms.otp.security;
import com.icthh.xm.commons.tenant.TenantContext;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class CommunicationService {


    private final ApplicationProperties applicationProperties;
    private final TenantContextHolder tenantContext;


    private final RestTemplate restTemplate;

    public CommunicationService(
        ApplicationProperties applicationProperties,
        @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
        TenantContextHolder tenantContext
    ) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = restTemplate;
        this.tenantContext = tenantContext;
    }

    public Map post(
        String url,
        Map <String, String> args,
        Map<String, String> additionalHeaders,
        MediaType mediaType
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> arg : args.entrySet()) {
            map.add(arg.getKey(), arg.getValue());
        }

        for (Map.Entry<String, String> addHeader : additionalHeaders.entrySet()) {
            headers.set(addHeader.getKey(), addHeader.getValue());
        }

        headers.set("x-tenant", tenantContext.getContext().getTenantKey().get().getValue());

        HttpEntity<MultiValueMap> request = new HttpEntity<MultiValueMap>(map, headers);
        log.info("Post to {} with args {}", url, args);
        return restTemplate.postForEntity(url, request , Map.class ).getBody();
    }

    public String getSystemToken() {

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        ApplicationProperties.Uaa uaa = applicationProperties.getUaa();
        body.put("username", uaa.getSystemUsername());
        body.put("password", uaa.getSystemPassword());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", uaa.getSystemClientToken());
        Map response = this.post(
            uaa.getSystemAuthUrl(),
            body,
            headers,
            MediaType.APPLICATION_FORM_URLENCODED
        );

        String token = response.get("token_type") + " " + response.get("access_token");
        log.info(token);
        return token;
    }

}
