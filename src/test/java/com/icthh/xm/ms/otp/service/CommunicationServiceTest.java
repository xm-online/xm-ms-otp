package com.icthh.xm.ms.otp.service;

import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_AUTH_CONTEXT;
import static com.icthh.xm.commons.lep.XmLepConstants.THREAD_CONTEXT_KEY_TENANT_CONTEXT;
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
import com.icthh.xm.lep.api.LepManager;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.LepConfiguration;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.CommunicationConfig;
import com.icthh.xm.ms.otp.domain.TenantConfig;
import com.icthh.xm.ms.otp.domain.UaaConfig;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    WebappTenantOverrideConfiguration.class,
    LepConfiguration.class
})
public class CommunicationServiceTest {

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private LepManager lepManager;

    @Autowired
    private TenantContextHolder tenantContextHolder;

    @MockBean
    private OtpSpecService otpSpecService;

    @Mock
    private XmAuthenticationContext context;

    @Mock
    private XmAuthenticationContextHolder authContextHolder;

    @Before
    public void before() {
        TenantContextUtils.setTenant(tenantContextHolder, "RESINTTEST");
        MockitoAnnotations.initMocks(this);
        when(authContextHolder.getContext()).thenReturn(context);
        when(context.getRequiredUserKey()).thenReturn("userKey");

        lepManager.beginThreadContext(ctx -> {
            ctx.setValue(THREAD_CONTEXT_KEY_TENANT_CONTEXT, tenantContextHolder.getContext());
            ctx.setValue(THREAD_CONTEXT_KEY_AUTH_CONTEXT, authContextHolder.getContext());
        });
    }

    @After
    public void afterTest() {
        tenantContextHolder.getPrivilegedContext().destroyCurrentContext();
        lepManager.endThreadContext();
    }

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
