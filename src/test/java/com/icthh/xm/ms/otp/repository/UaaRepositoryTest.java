package com.icthh.xm.ms.otp.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.Constants;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.util.Map;
import java.util.UUID;

import static com.icthh.xm.ms.otp.config.Constants.ACCESS_TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class UaaRepositoryTest {

    @Autowired
    UaaRepository uaaRepository;

    @Qualifier("internalRestTemplate")
    @MockBean
    private RestTemplate restTemplate;


    @Test
    public void oauthTokenShouldBeRetrievedFromUaaTest() {

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity(Maps.newHashMap(), HttpStatus.OK));

        Map<String, Object> params = ImmutableMap.of("key1", "value1", "key2", "value2");
        uaaRepository.getOAuth2Token(params);

        ArgumentCaptor<String> urlArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityArgument = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(urlArgument.capture(), eq(HttpMethod.POST), entityArgument.capture(), any(ParameterizedTypeReference.class));
        String url = urlArgument.getValue();
        HttpEntity entity = entityArgument.getValue();
        assertTrue(StringUtils.endsWith(url, "?key1=value1&key2=value2"));
        assertNotNull(entity.getHeaders().get(Constants.X_TENANT));
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertNotNull(body.get("grant_type"));
        assertNotNull(body.get("username"));
        assertNotNull(body.get("password"));
    }


    @Test
    public void requestShouldBeSendWithEmptyDetailsMap() {

        String token = UUID.randomUUID().toString();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(new ResponseEntity(ImmutableMap.of(ACCESS_TOKEN, token), HttpStatus.OK));

        String accessToken = uaaRepository.getOAuth2Token(Maps.newHashMap());
        assertEquals(accessToken, token);

    }

}
