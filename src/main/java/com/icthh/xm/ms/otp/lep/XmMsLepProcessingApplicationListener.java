package com.icthh.xm.ms.otp.lep;

import com.icthh.xm.commons.config.client.service.TenantConfigService;
import com.icthh.xm.commons.lep.commons.CommonsExecutor;
import com.icthh.xm.commons.lep.commons.CommonsService;
import com.icthh.xm.commons.lep.spring.SpringLepProcessingApplicationListener;
import com.icthh.xm.commons.permission.service.PermissionCheckService;
import com.icthh.xm.lep.api.ScopedContext;
import com.icthh.xm.ms.otp.repository.OneTimePasswordRepository;
import com.icthh.xm.ms.otp.service.OtpSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_COMMONS;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_OTP_REPOSITORY;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_SERVICES;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_KEY_TEMPLATES;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_PERMISSION_SERVICE;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_SPEC_SERVICE;
import static com.icthh.xm.ms.otp.lep.LepMsConstants.BINDING_SUB_KEY_TEMPLATE_REST;

/**
 * The {@link XmMsLepProcessingApplicationListener} class.
 */
@RequiredArgsConstructor
@Component
public class XmMsLepProcessingApplicationListener extends SpringLepProcessingApplicationListener {

    private final TenantConfigService tenantConfigService;
    @Qualifier("loadBalancedRestTemplate")
    private final RestTemplate restTemplate;
    private final CommonsService commonsService;
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final PermissionCheckService permissionCheckService;
    private final OtpSpecService otpSpecService;


    @Override
    protected void bindExecutionContext(ScopedContext executionContext) {
        // services
        Map<String, Object> services = new HashMap<>();
        services.put(BINDING_SUB_KEY_SERVICE_TENANT_CONFIG_SERICE, tenantConfigService);
        services.put(BINDING_SUB_KEY_PERMISSION_SERVICE, permissionCheckService);
        services.put(BINDING_SUB_KEY_SPEC_SERVICE, otpSpecService);

        executionContext.setValue(BINDING_KEY_COMMONS, new CommonsExecutor(commonsService));
        executionContext.setValue(BINDING_KEY_SERVICES, services);
        executionContext.setValue(BINDING_KEY_OTP_REPOSITORY, oneTimePasswordRepository);

        // templates
        Map<String, Object> templates = new HashMap<>();
        templates.put(BINDING_SUB_KEY_TEMPLATE_REST, restTemplate);

        executionContext.setValue(BINDING_KEY_TEMPLATES, templates);
    }

}

