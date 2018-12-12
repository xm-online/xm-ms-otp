package com.icthh.xm.ms.otp.service;

import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantKey;
import com.icthh.xm.ms.otp.client.domain.CommunicationMessage;
import com.icthh.xm.ms.otp.client.domain.Receiver;
import com.icthh.xm.ms.otp.client.domain.Sender;
import com.icthh.xm.ms.otp.domain.TenantConfig;
import com.icthh.xm.ms.otp.domain.UaaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpMethod.POST;

@Component
@Slf4j
public class CommunicationService {

    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String SMS = "SMS";
    private final TenantContextHolder tenantContext;
    private final OtpSpecService otpSpecService;


    private final RestTemplate restTemplate;

    public CommunicationService(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                                TenantContextHolder tenantContext,
                                OtpSpecService otpSpecService) {
        this.restTemplate = restTemplate;
        this.tenantContext = tenantContext;
        this.otpSpecService = otpSpecService;
    }

    protected Map post(String url,
                    Map<String, String> args,
                    Map<String, String> additionalHeaders,
                    MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> arg : args.entrySet()) {
            map.add(arg.getKey(), arg.getValue());
        }

        for (Map.Entry<String, String> addHeader : additionalHeaders.entrySet()) {
            headers.set(addHeader.getKey(), addHeader.getValue());
        }

        Optional<TenantKey> tenantKey = tenantContext.getContext().getTenantKey();
        tenantKey.ifPresent(tenantKey1 -> headers.set("x-tenant", tenantKey1.getValue()));

        HttpEntity<MultiValueMap> request = new HttpEntity<>(map, headers);
        log.info("Post to {} with args {}", url, args);
        return restTemplate.postForEntity(url, request, Map.class).getBody();
    }

    protected String getSystemToken() {

        Map<String, String> body = new HashMap<>();
        body.put(GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS);
        UaaConfig uaa = otpSpecService.getTenantConfig().getUaa();

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, uaa.getSystemClientToken());
        Map response = this.post(
            uaa.getSystemAuthUrl(),
            body,
            headers,
            MediaType.APPLICATION_FORM_URLENCODED
        );

        String token = response.get(TOKEN_TYPE) + " " + response.get(ACCESS_TOKEN);
        log.info(token);
        return token;
    }

    public void sendOneTimePassword(String message, String receiver, String senderId) {
        TenantConfig tenantConfig = otpSpecService.getTenantConfig();
        if (tenantConfig == null) {
            throw new IllegalStateException("Can't send message, because tenant config is null");
        }
        if (tenantConfig.getCommunication() == null) {
            throw new IllegalStateException("Cant send message, because communication is null");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, this.getSystemToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = tenantConfig.getCommunication().getUrl() + "/communicationMessage/send";

        CommunicationMessage body = new CommunicationMessage();
        body.setContent(message)
            .setType(SMS)
            .setSender(new Sender(senderId))
            .setReceiver(new ArrayList<>())
            .getReceiver().add(new Receiver(receiver, receiver));
        RequestEntity<Object> request = new RequestEntity<>(body, headers, POST, URI.create(url));
        restTemplate.exchange(request, Object.class);
    }

}
