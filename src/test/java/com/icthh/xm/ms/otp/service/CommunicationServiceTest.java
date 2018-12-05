package com.icthh.xm.ms.otp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.CommunicationConfig;
import com.icthh.xm.ms.otp.domain.TenantConfig;
import com.icthh.xm.ms.otp.domain.UaaConfig;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class CommunicationServiceTest {

    @Autowired
    private CommunicationService communicationService;

    @MockBean
    private OtpSpecService otpSpecService;

    @MockBean
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate restTemplate;

    @Test
    public void testSendOneTimePassword() {

        UaaConfig uaa = new UaaConfig();
        uaa.setSystemUsername("name");
        uaa.setSystemPassword("pass");
        uaa.setSystemAuthUrl("url");

        CommunicationConfig communicationConfig = new CommunicationConfig();
        communicationConfig.setUrl("http://test.url");

        TenantConfig config = new TenantConfig();

        config.setCommunication(communicationConfig);
        config.setUaa(uaa);

        Map<String, String> map = new HashMap<>();
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(map, HttpStatus.ACCEPTED);

        when(otpSpecService.getTenantConfig()).thenReturn(config);
        doReturn(responseEntity).when(restTemplate).postForEntity(anyString(), any(), any());
        communicationService.sendOneTimePassword("message", "receiver", "sender");
        verify(otpSpecService, times(2)).getTenantConfig();
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
        verify(restTemplate, times(1)).exchange(any(), eq(Object.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendOneTimePasswordEmptyConfig() {
        when(otpSpecService.getTenantConfig()).thenReturn(null);
        communicationService.sendOneTimePassword("message", "receiver", "sender");
    }

    @Test(expected = IllegalStateException.class)
    public void testSendOneTimePasswordEmptyCommunication() {
        TenantConfig config = new TenantConfig();
        config.setCommunication(null);
        when(otpSpecService.getTenantConfig()).thenReturn(config);
        communicationService.sendOneTimePassword("message", "receiver", "sender");
    }
}
