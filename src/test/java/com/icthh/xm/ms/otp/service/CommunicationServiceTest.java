package com.icthh.xm.ms.otp.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey.EMAIL;
import static com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey.PHONE_NUMBER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.icthh.xm.commons.security.XmAuthenticationContext;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.commons.tenant.TenantContextHolder;
import com.icthh.xm.commons.tenant.TenantContextUtils;
import com.icthh.xm.commons.lep.api.LepManagementService;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.LepConfiguration;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.CommunicationConfig;
import com.icthh.xm.ms.otp.domain.OtpSpec;
import com.icthh.xm.ms.otp.domain.TenantConfig;
import com.icthh.xm.ms.otp.domain.UaaConfig;

import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.client.RestTemplate;

@Slf4j
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class,
    LepConfiguration.class
})
public class CommunicationServiceTest {

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private LepManagementService lepManager;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @MockitoBean
    private OtpSpecService otpSpecService;

    @Mock
    private XmAuthenticationContext context;

    @Mock
    private XmAuthenticationContextHolder authContextHolder;

    @BeforeEach
    public void before() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
        MockitoAnnotations.openMocks(this);
        when(authContextHolder.getContext()).thenReturn(context);
        when(context.getRequiredUserKey()).thenReturn("userKey");

        // no config server in tests: mark the LEP engines as initialized (no tenant scripts), as xm-config would
        lepManager.refreshEngines(Map.of(), null);
        lepManager.beginThreadContext();
    }

    @AfterEach
    public void afterTest() {
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
        lepManager.endThreadContext();
    }

    @MockitoBean
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

        OtpSpec.OtpTypeSpec otpTypeSpec = getOtpTypeSpec("sender", PHONE_NUMBER, "message");
        OneTimePasswordDto oneTimePasswordDto = getOneTimePasswordDto("receiver");

        communicationService.sendOneTimePassword("OTP", otpTypeSpec, oneTimePasswordDto);
        verify(otpSpecService, times(2)).getTenantConfig();
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
        verify(restTemplate, times(1)).exchange(any(), eq(Object.class));
    }

    private OneTimePasswordDto getOneTimePasswordDto(String receiver) {
        OneTimePasswordDto oneTimePasswordDto = new OneTimePasswordDto();
        oneTimePasswordDto.setReceiver(receiver);
        return oneTimePasswordDto;
    }

    private OtpSpec.OtpTypeSpec getOtpTypeSpec(String sender, ReceiverTypeKey receiverTypeKey, String message) {
        OtpSpec.OtpTypeSpec otpTypeSpec = new OtpSpec.OtpTypeSpec();
        otpTypeSpec.setOtpSenderId(sender);
        otpTypeSpec.setReceiverTypeKey(receiverTypeKey);

        TreeMap<String, String> messageMap = new TreeMap<>();
        messageMap.put("en", message);
        otpTypeSpec.setMessage(messageMap);
        return otpTypeSpec;
    }

    @Test
    public void testSendOneTimePassword_shouldSendEmailRequest() {

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

        OtpSpec.OtpTypeSpec otpTypeSpec = getOtpTypeSpec("sender", EMAIL, "message");
        OneTimePasswordDto oneTimePasswordDto = getOneTimePasswordDto("receiver");

        communicationService.sendOneTimePassword("OTP", otpTypeSpec, oneTimePasswordDto);
        verify(otpSpecService, times(2)).getTenantConfig();
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
        verify(restTemplate, times(1)).exchange(any(), eq(Object.class));
    }

    @Test
    public void testSendOneTimePasswordEmptyConfig() {
        assertThrows(IllegalStateException.class, () -> {
            when(otpSpecService.getTenantConfig()).thenReturn(null);
            OtpSpec.OtpTypeSpec otpTypeSpec = getOtpTypeSpec("sender", PHONE_NUMBER, null);
            OneTimePasswordDto oneTimePasswordDto = getOneTimePasswordDto("receiver");
    
            communicationService.sendOneTimePassword("OTP", otpTypeSpec, oneTimePasswordDto);
        });
    }

    @Test
    public void testSendOneTimePasswordEmptyCommunication() {
        assertThrows(IllegalStateException.class, () -> {
            TenantConfig config = new TenantConfig();
            config.setCommunication(null);
            when(otpSpecService.getTenantConfig()).thenReturn(config);
            OtpSpec.OtpTypeSpec otpTypeSpec = getOtpTypeSpec("sender", PHONE_NUMBER, "message");
            OneTimePasswordDto oneTimePasswordDto = getOneTimePasswordDto("receiver");
            communicationService.sendOneTimePassword("OTP", otpTypeSpec, oneTimePasswordDto);
        });
    }
}
