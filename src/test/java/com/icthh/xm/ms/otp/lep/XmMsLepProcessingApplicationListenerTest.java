package com.icthh.xm.ms.otp.lep;

import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_COMMONS;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_SERVICES;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_TEMPLATES;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_PERMISSION_SERVICE;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_TEMPLATE_REST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.icthh.xm.commons.lep.commons.CommonsExecutor;
import com.icthh.xm.lep.api.ScopedContext;
import com.icthh.xm.lep.core.DefaultScopedContext;
import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.config.SecurityBeanOverrideConfiguration;
import com.icthh.xm.ms.otp.config.tenant.WebappTenantOverrideConfiguration;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class XmMsLepProcessingApplicationListenerTest {

    @Autowired
    private XmMsLepProcessingApplicationListener listener;

    @Test
    @SuppressWarnings("unchecked")
    public void testBindExecutionContext() {

        ScopedContext context = new DefaultScopedContext("scope");
        listener.bindExecutionContext(context);

        assertEquals(context.getValues().size(), 3);
        assertNotNull(context.getValue(BINDING_KEY_COMMONS, CommonsExecutor.class));
        assertNotNull(context.getValue(BINDING_KEY_SERVICES, Map.class));
        assertNotNull(context.getValue(BINDING_KEY_TEMPLATES, Map.class));

        Map<String, Object> services = (HashMap<String, Object>)context.getValue(BINDING_KEY_SERVICES, HashMap.class);
        assertEquals(services.values().size(), 2);
        assertNotNull(services.get(BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE));
        assertNotNull(services.get(BINDING_SUB_KEY_PERMISSION_SERVICE));

        Map<String, Object> templates = (HashMap<String, Object>)context.getValue(BINDING_KEY_TEMPLATES, HashMap.class);
        assertEquals(templates.values().size(), 1);
        assertNotNull(templates.get(BINDING_SUB_KEY_TEMPLATE_REST));
    }
}
