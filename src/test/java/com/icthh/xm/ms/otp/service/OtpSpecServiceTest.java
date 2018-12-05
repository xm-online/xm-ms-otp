package com.icthh.xm.ms.otp.service;

import static org.mockito.Mockito.when;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.ApplicationProperties;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.domain.OtpSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class OtpSpecServiceTest {

    @MockBean
    private ApplicationProperties appProps;

    @Autowired
    private OtpSpecService otpSpecService;

    @Test
    public void testGetOtpTypeSpec() {
        List<OtpSpec.OtpTypeSpec> types = new ArrayList<>();

        OtpSpec.OtpTypeSpec type1 = new OtpSpec.OtpTypeSpec();
        type1.setKey("TYPE1");

        OtpSpec.OtpTypeSpec type2 = new OtpSpec.OtpTypeSpec();
        type2.setKey("TYPE2");

        types.add(type1);
        types.add(type2);

        OtpSpec otpSpec = new OtpSpec();
        otpSpec.setTypes(types);
        otpSpecService.setOtpSpec(otpSpec);
        OtpSpec.OtpTypeSpec type = otpSpecService.getOtpTypeSpec("TYPE1");
        Assert.assertEquals(type, type1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetOtpTypeSpecProfileNotFound() {
        OtpSpec otpSpec = new OtpSpec();
        otpSpec.setTypes(Collections.emptyList());
        otpSpecService.setOtpSpec(otpSpec);
        otpSpecService.getOtpTypeSpec("TYPE1");
    }

    @Test(expected = IllegalStateException.class)
    public void testOnInit() {
        when(appProps.getSpecPath()).thenReturn("wrongKey");
        otpSpecService.onInit("wrongKey", "configValue");
    }

    @Test
    public void testIsListeningConfigurationSpec() {
        when(appProps.getSpecPath()).thenReturn("Key");
        Assert.assertTrue(otpSpecService.isListeningConfiguration("Key"));
    }

    @Test
    public void testIsListeningConfigurationTenant() {
        when(appProps.getSpecPath()).thenReturn("OtherKey");
        when(appProps.getTenantPath()).thenReturn("Key");
        Assert.assertTrue(otpSpecService.isListeningConfiguration("Key"));
    }

    @Test
    public void testIsListeningConfigurationOtherKey() {
        when(appProps.getSpecPath()).thenReturn("OtherKey");
        when(appProps.getTenantPath()).thenReturn("OtherKey");
        Assert.assertFalse(otpSpecService.isListeningConfiguration("Key"));
    }

}
