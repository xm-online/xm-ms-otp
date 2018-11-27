package com.icthh.xm.ms.otp.security;
import com.icthh.xm.commons.tenant.TenantContext;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.domain.UaaConfig;
import com.icthh.xm.ms.otp.service.OtpSpecService;
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

    public Map post(String url,
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

        headers.set("x-tenant", tenantContext.getContext().getTenantKey().get().getValue());

        HttpEntity<MultiValueMap> request = new HttpEntity<MultiValueMap>(map, headers);
        log.info("Post to {} with args {}", url, args);
        return restTemplate.postForEntity(url, request, Map.class).getBody();
    }

    public String getSystemToken() {

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "password");
        UaaConfig uaa = otpSpecService.getUaaConfig();
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
