package com.icthh.xm.ms.otp.security;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
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

import static sun.security.krb5.SCDynamicStoreConfig.getConfig;

@Component
@Slf4j
public class CommunicationService {

    private final static AtomicReference atomicReference = new AtomicReference();

    static class AuthValue {
        Long createTokenTime;
        Integer expires_in;
        String token_type;
        String access_token;
    }


    private final ApplicationProperties applicationProperties;


    private final RestTemplate restTemplate;

    public CommunicationService(ApplicationProperties applicationProperties, @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.applicationProperties = applicationProperties;
        this.restTemplate = restTemplate;
    }

    public Map post(String url, Map <String, String> args, Map<String, String> additionalHeaders, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        MultiValueMap map = new LinkedMultiValueMap();
        for (Map.Entry<String, String> arg : args.entrySet()) {
            map.add(arg.getKey(), arg.getValue());
        }

        for (Map.Entry<String, String> addHeader : additionalHeaders.entrySet()) {
            headers.set(addHeader.getKey(), addHeader.getValue());
        }

        headers.set("x-tenant", "XM"); //TODO

        HttpEntity<MultiValueMap> request = new HttpEntity<MultiValueMap>(map, headers);
        log.info("Post to {} with args {}", url, args);
        return restTemplate.postForEntity( url, request , Map.class ).getBody();
    }

    public String getSystemToken() {

        AuthValue authValue = (AuthValue) atomicReference.get();

        if (authValue != null && ((System.currentTimeMillis() / 1000) - authValue.createTokenTime) < (authValue.expires_in - 60)) {
            return authValue.token_type + " " + authValue.access_token;
        }

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        ApplicationProperties.Uaa uaa = applicationProperties.getUaa();
        body.put("username", uaa.getSystemUsername());
        body.put("password", uaa.getSystemPassword());


        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", uaa.getSystemClientToken());
        Map response = this.post(uaa.getSystemAuthUrl(), body, headers, MediaType.APPLICATION_FORM_URLENCODED);
        AuthValue auth = new AuthValue();
        auth.access_token = (String) response.get("access_token");
        auth.token_type  = (String) response.get("token_type");
        auth.expires_in  = (Integer) response.get("expires_in");
        auth.createTokenTime = System.currentTimeMillis() / 1000;
        atomicReference.set(auth);

        String token = auth.token_type + " " + auth.access_token;
        log.info(token);
        return token;
    }

}
