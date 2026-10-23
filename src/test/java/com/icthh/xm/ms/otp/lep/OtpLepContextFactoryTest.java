package com.icthh.xm.ms.otp.lep;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * The LEP context keeps the bindings of the xm-commons 2 {@code XmMsLepProcessingApplicationListener}:
 * services (tenantConfigService, permissionService, specService), otpRepository and templates (rest).
 */
@WithMockUser(authorities = {"SUPER-ADMIN"})
@SpringBootTest(classes = {
    SecurityBeanOverrideConfiguration.class,
    OtpApp.class,
    WebappTenantOverrideConfiguration.class
})
public class OtpLepContextFactoryTest {

    @Autowired
    private OtpLepContextFactory factory;

    @Test
    public void testBuildLepContext() {
        LepContext context = (LepContext) factory.buildLepContext(null);

        assertNotNull(context.services);
        assertNotNull(context.services.tenantConfigService);
        assertNotNull(context.services.permissionService);
        assertNotNull(context.services.specService);
        assertInstanceOf(OneTimePasswordRepository.class, context.otpRepository);
        assertNotNull(context.templates);
        assertNotNull(context.templates.rest);
    }
}
