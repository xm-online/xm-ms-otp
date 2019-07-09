package com.icthh.xm.ms.otp.repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.Constants;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import java.util.Map;
import java.util.UUID;

import static com.icthh.xm.ms.otp.config.Constants.ACCESS_TOKEN;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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

    @Autowired
    @Qualifier("internalRestTemplate")
    private RestTemplate restTemplate;

    MockRestServiceServer mockServer;

    @Before
    public void beforeClass(){
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(restTemplate);
        mockServer = MockRestServiceServer.createServer(gateway);
    }

    @Test
    public void oauthTokenShouldBeRetrievedFromUaaTest() {

        mockServer.expect(method(HttpMethod.POST))
            .andExpect(queryParam("key1", "value1"))
            .andExpect(queryParam("key2", "value2"))
            .andExpect(header(Constants.X_TENANT, "xm"))
            .andExpect(content().string(containsString("grant_type")))
            .andExpect(content().string(containsString("username")))
            .andExpect(content().string(containsString("password")))
        .andRespond(withSuccess("{\"access_token\" : \"123\"}".getBytes(), MediaType.APPLICATION_JSON));

        Map<String, Object> params = ImmutableMap.of("key1", "value1", "key2", "value2");
        String oAuth2Token = uaaRepository.getOAuth2Token(params);
        assertEquals(oAuth2Token, "123");
        mockServer.reset();
    }


    @Test
    public void requestShouldBeSendWithEmptyDetailsMap() {

        mockServer.expect(method(HttpMethod.POST))
            .andExpect(header(Constants.X_TENANT, "xm"))
            .andExpect(content().string(containsString("grant_type")))
            .andExpect(content().string(containsString("username")))
            .andExpect(content().string(containsString("password")))
            .andRespond(withSuccess("{\"access_token\" : \"123\"}".getBytes(), MediaType.APPLICATION_JSON));

        String accessToken = uaaRepository.getOAuth2Token(Maps.newHashMap());
        assertEquals(accessToken, "123");

    }

}
