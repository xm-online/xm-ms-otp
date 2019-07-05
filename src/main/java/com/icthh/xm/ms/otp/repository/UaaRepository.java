package com.icthh.xm.ms.otp.repository;

import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.commons.tenant.TenantKey;
import com.icthh.xm.commons.tenant.internal.DefaultTenantContextHolder;
import io.github.jhipster.config.JHipsterProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.stream.Collectors;

import static com.icthh.xm.ms.otp.config.Constants.ACCESS_TOKEN;
import static com.icthh.xm.ms.otp.config.Constants.X_TENANT;

@Component
@RequiredArgsConstructor
public class UaaRepository {

    private final JHipsterProperties jHipsterProperties;
    @Qualifier("internalRestTemplate")
    private final RestTemplate restTemplate;

    private MultiValueMap<String, String> oauthTokenRequestMap;

    @PostConstruct
    private void init(){

        oauthTokenRequestMap = new LinkedMultiValueMap<String, String>() {
            {
                add("grant_type", "client_credentials");
                add("username", jHipsterProperties.getSecurity().getClientAuthorization().getClientId());
                add("password", jHipsterProperties.getSecurity().getClientAuthorization().getClientSecret());
            }
        };
    }

    public String getOAuth2Token(Map<String, Object> additionalDetails) {
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
        };

        JHipsterProperties.Security security = jHipsterProperties.getSecurity();
        String xTenant = TenantContextUtils.getTenantKey(new DefaultTenantContextHolder())
            .map(TenantKey::getValue)
            .orElse(null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(X_TENANT, xTenant);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(oauthTokenRequestMap, headers);
        String details = additionalDetails
            .entrySet()
            .stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
        String accessTokenUri = security.getClientAuthorization().getAccessTokenUri();
        String url = StringUtils.isEmpty(details) ? accessTokenUri : accessTokenUri + "?" + details;
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.POST, entity, typeRef).getBody();
        return (String) body.get(ACCESS_TOKEN);
    }

}
